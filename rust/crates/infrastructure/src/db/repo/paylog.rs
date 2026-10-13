//! 支付日志域仓储：`paylog` / `paylog_refund`。
//! 对应原 Java `PaylogMapper` / `PaylogRefundMapper`。
//!
//! 列名严格对齐 `java/database/pure.sql`：
//! - `paylog`：`paylog_id` / `pay_sn` / `pay_name` / `order_id` / `order_sn` /
//!   `order_amount` / `order_type` / `pay_amount` / `pay_status` / `pay_code` /
//!   `profit_sharing_channel` / `add_time` / `transaction_id` / `notify_data` /
//!   `refund_amount` / `token_code` / `appid`
//! - `paylog_refund`：`paylog_refund_id` / `paylog_id` / `refund_sn` /
//!   `paylog_desc` / `order_id` / `refund_amount` / `add_time` / `admin_id` /
//!   `pay_code` / `status` / `notify_time`
//!
//! **重要**：`paylog` 被商品订单（`order_type = 0`）、充值单（1）、
//! 付费会员订单（2）共用，两张业务表主键各自独立会撞号，
//! 因此**所有按 order_id 的查询都必须同时限定 order_type**。

use rust_decimal::Decimal;
use sqlx::MySqlPool;
use tigshop_core::entities::{Paylog, PaylogRefund};
use tigshop_core::types::PageQuery;
use tigshop_core::{Error, Result};

/// 支付状态：待支付。
pub const PAYLOG_STATUS_UNPAID: i32 = 0;
/// 支付状态：已支付。
pub const PAYLOG_STATUS_PAID: i32 = 1;
/// 支付状态：已退款。
pub const PAYLOG_STATUS_REFUNDED: i32 = 2;

/// 业务类型：商品订单。
pub const ORDER_TYPE_PRODUCT: i32 = 0;
/// 业务类型：充值单。
pub const ORDER_TYPE_RECHARGE: i32 = 1;
/// 业务类型：付费会员订单。
pub const ORDER_TYPE_PREMIUM: i32 = 2;

#[derive(Clone)]
pub struct PaylogRepository {
    pool: MySqlPool,
}

impl PaylogRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池。
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    // ————————————————————— paylog —————————————————————

    /// 按主键查询。
    pub async fn find_by_id(&self, paylog_id: i32) -> Result<Option<Paylog>> {
        sqlx::query_as::<_, Paylog>("SELECT * FROM paylog WHERE paylog_id = ? LIMIT 1")
            .bind(paylog_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 按支付号取最新一条（`pay_sn` 可能对应多行，如定金预售）。
    pub async fn find_latest_by_pay_sn(&self, pay_sn: &str) -> Result<Option<Paylog>> {
        sqlx::query_as::<_, Paylog>(
            "SELECT * FROM paylog WHERE pay_sn = ? ORDER BY paylog_id DESC LIMIT 1",
        )
        .bind(pay_sn)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 取该业务单据最新一条支付日志。
    ///
    /// **必须同时限定 `order_type`**：`paylog` 被商品订单/充值单/付费会员
    /// 共用，`order_id` 跨类型会撞号（对应 Java `getLastByOrderId` 的注释警告）。
    pub async fn find_last_by_order(
        &self,
        order_id: i32,
        order_type: i32,
    ) -> Result<Option<Paylog>> {
        sqlx::query_as::<_, Paylog>(
            "SELECT * FROM paylog
             WHERE order_id = ? AND order_type = ?
             ORDER BY paylog_id DESC
             LIMIT 1",
        )
        .bind(order_id)
        .bind(order_type)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 查询「同订单 + 同支付方式」的最新支付日志（`creatPayLog` 复用判定用）。
    pub async fn find_latest_by_order_and_code(
        &self,
        order_id: i32,
        order_type: i32,
        order_sn: &str,
        pay_code: &str,
    ) -> Result<Option<Paylog>> {
        sqlx::query_as::<_, Paylog>(
            "SELECT * FROM paylog
             WHERE order_type = ? AND order_id = ? AND order_sn = ? AND pay_code = ?
             ORDER BY paylog_id DESC
             LIMIT 1",
        )
        .bind(order_type)
        .bind(order_id)
        .bind(order_sn)
        .bind(pay_code)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 取订单已支付、正金额、且带合法分账渠道快照的支付证据（分账发起依据）。
    pub async fn find_paid_profit_sharing(
        &self,
        order_id: i32,
        channels: &[&str],
    ) -> Result<Option<Paylog>> {
        if channels.is_empty() {
            return Ok(None);
        }
        let placeholders = vec!["?"; channels.len()].join(", ");
        let sql = format!(
            "SELECT * FROM paylog
             WHERE order_id = ?
               AND order_type = ?
               AND pay_status = ?
               AND pay_amount > 0
               AND profit_sharing_channel IN ({placeholders})
             ORDER BY paylog_id DESC
             LIMIT 1"
        );
        let mut q = sqlx::query_as::<_, Paylog>(&sql)
            .bind(order_id)
            .bind(ORDER_TYPE_PRODUCT)
            .bind(PAYLOG_STATUS_PAID);
        for c in channels {
            q = q.bind(*c);
        }
        q.fetch_optional(&self.pool).await.map_err(map_err)
    }

    /// 新增支付日志，返回 `paylog_id`。
    pub async fn insert(&self, p: &Paylog) -> Result<i32> {
        let r = sqlx::query(
            "INSERT INTO paylog
             (pay_sn, pay_name, order_id, order_sn, order_amount, order_type,
              pay_amount, pay_status, pay_code, profit_sharing_channel,
              add_time, transaction_id, notify_data, refund_amount,
              token_code, appid)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        )
        .bind(&p.pay_sn)
        .bind(&p.pay_name)
        .bind(p.order_id)
        .bind(&p.order_sn)
        .bind(p.order_amount)
        .bind(p.order_type)
        .bind(p.pay_amount)
        .bind(p.pay_status)
        .bind(&p.pay_code)
        .bind(&p.profit_sharing_channel)
        .bind(p.add_time)
        .bind(&p.transaction_id)
        .bind(&p.notify_data)
        .bind(p.refund_amount)
        .bind(&p.token_code)
        .bind(&p.appid)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(r.last_insert_id() as i32)
    }

    /// 不可变地绑定分账渠道快照。
    ///
    /// 对应 Java `bindProfitSharingChannel`：
    /// - 已有快照且相同 → 幂等返回 `true`
    /// - 已有快照但不同 → 拒绝覆盖（返回 `false`）
    /// - 非待支付状态 → 拒绝（返回 `false`）
    ///
    /// SQL 层用 `(profit_sharing_channel IS NULL OR = '')` 做 CAS，
    /// 并发下只有一个请求能绑定成功。
    pub async fn bind_profit_sharing_channel(
        &self,
        paylog_id: i32,
        pay_sn: &str,
        channel: &str,
    ) -> Result<bool> {
        let affected = sqlx::query(
            "UPDATE paylog
             SET profit_sharing_channel = ?
             WHERE paylog_id = ?
               AND pay_sn = ?
               AND pay_status = ?
               AND (profit_sharing_channel IS NULL OR profit_sharing_channel = '')",
        )
        .bind(channel)
        .bind(paylog_id)
        .bind(pay_sn)
        .bind(PAYLOG_STATUS_UNPAID)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        Ok(affected > 0)
    }

    /// 标记支付成功（幂等：仅当仍为待支付时更新）。
    ///
    /// 返回 `true` 表示本次调用完成了状态流转；`false` 表示已是终态（重复回调）。
    pub async fn mark_paid(
        &self,
        paylog_id: i32,
        transaction_id: &str,
        pay_amount: Decimal,
        notify_data: &str,
    ) -> Result<bool> {
        let affected = sqlx::query(
            "UPDATE paylog
             SET pay_status = ?, transaction_id = ?, pay_amount = ?, notify_data = ?
             WHERE paylog_id = ? AND pay_status = ?",
        )
        .bind(PAYLOG_STATUS_PAID)
        .bind(transaction_id)
        .bind(pay_amount)
        .bind(notify_data)
        .bind(paylog_id)
        .bind(PAYLOG_STATUS_UNPAID)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        Ok(affected > 0)
    }

    /// 按支付号标记支付成功（回调常只带商户单号）。
    ///
    /// 返回受影响行数，便于识别「合单/定金预售」多行场景。
    pub async fn mark_paid_by_pay_sn(
        &self,
        pay_sn: &str,
        transaction_id: &str,
        notify_data: &str,
    ) -> Result<u64> {
        let affected = sqlx::query(
            "UPDATE paylog
             SET pay_status = ?, transaction_id = ?, notify_data = ?
             WHERE pay_sn = ? AND pay_status = ?",
        )
        .bind(PAYLOG_STATUS_PAID)
        .bind(transaction_id)
        .bind(notify_data)
        .bind(pay_sn)
        .bind(PAYLOG_STATUS_UNPAID)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        Ok(affected)
    }

    /// 累加退款金额（不超过订单金额由调用方保证）。
    pub async fn add_refund_amount(
        &self,
        paylog_id: i32,
        amount: Decimal,
    ) -> Result<()> {
        sqlx::query("UPDATE paylog SET refund_amount = refund_amount + ? WHERE paylog_id = ?")
            .bind(amount)
            .bind(paylog_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(())
    }

    /// 取订单最新一笔**已支付**的指定支付方式流水（余额退款定位用）。
    pub async fn find_last_balance_paid(
        &self,
        order_id: i32,
        pay_code: &str,
    ) -> Result<Option<Paylog>> {
        sqlx::query_as::<_, Paylog>(
            "SELECT * FROM paylog
             WHERE order_id = ? AND order_type = ? AND pay_code = ? AND pay_status = ?
             ORDER BY paylog_id DESC
             LIMIT 1",
        )
        .bind(order_id)
        .bind(ORDER_TYPE_PRODUCT)
        .bind(pay_code)
        .bind(PAYLOG_STATUS_PAID)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 删除支付日志（仅用于余额支付等站内渠道的事务补偿回滚；
    /// 第三方渠道流水一律不可删除，须走退款流程）。
    pub async fn delete_paylog(&self, paylog_id: i32) -> Result<()> {
        sqlx::query("DELETE FROM paylog WHERE paylog_id = ? AND pay_code = 'balance'")
            .bind(paylog_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(())
    }

    /// 支付日志分页（管理端对账）。
    pub async fn paginate(
        &self,
        order_sn: Option<&str>,
        pay_status: Option<i32>,
        order_type: Option<i32>,
        page: &PageQuery,
    ) -> Result<(u64, Vec<Paylog>)> {
        let like = order_sn
            .map(str::trim)
            .filter(|s| !s.is_empty())
            .map(|s| format!("%{s}%"));
        let (limit, offset) = page.limit_offset();

        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM paylog
             WHERE (? IS NULL OR order_sn LIKE ?)
               AND (? IS NULL OR pay_status = ?)
               AND (? IS NULL OR order_type = ?)",
        )
        .bind(&like)
        .bind(&like)
        .bind(pay_status)
        .bind(pay_status)
        .bind(order_type)
        .bind(order_type)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;

        let rows = sqlx::query_as::<_, Paylog>(
            "SELECT * FROM paylog
             WHERE (? IS NULL OR order_sn LIKE ?)
               AND (? IS NULL OR pay_status = ?)
               AND (? IS NULL OR order_type = ?)
             ORDER BY paylog_id DESC
             LIMIT ? OFFSET ?",
        )
        .bind(&like)
        .bind(&like)
        .bind(pay_status)
        .bind(pay_status)
        .bind(order_type)
        .bind(order_type)
        .bind(limit)
        .bind(offset)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;

        Ok((total.0.max(0) as u64, rows))
    }

    // ————————————————————— paylog_refund —————————————————————

    /// 新增退款日志，返回 `paylog_refund_id`。
    pub async fn insert_refund(&self, r: &PaylogRefund) -> Result<i32> {
        let res = sqlx::query(
            "INSERT INTO paylog_refund
             (paylog_id, refund_sn, paylog_desc, order_id, refund_amount,
              add_time, admin_id, pay_code, status, notify_time)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        )
        .bind(r.paylog_id)
        .bind(&r.refund_sn)
        .bind(&r.paylog_desc)
        .bind(r.order_id)
        .bind(r.refund_amount)
        .bind(r.add_time)
        .bind(r.admin_id)
        .bind(&r.pay_code)
        .bind(r.status)
        .bind(r.notify_time)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(res.last_insert_id() as i32)
    }

    /// 按订单查询退款日志（降序）。
    pub async fn find_refunds_by_order(&self, order_id: i32) -> Result<Vec<PaylogRefund>> {
        sqlx::query_as::<_, PaylogRefund>(
            "SELECT * FROM paylog_refund WHERE order_id = ? ORDER BY paylog_refund_id DESC",
        )
        .bind(order_id)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 更新退款状态（回调/人工确认）。
    pub async fn update_refund_status(
        &self,
        paylog_refund_id: i32,
        status: i32,
        notify_time: i32,
    ) -> Result<()> {
        sqlx::query(
            "UPDATE paylog_refund SET status = ?, notify_time = ? WHERE paylog_refund_id = ?",
        )
        .bind(status)
        .bind(notify_time)
        .bind(paylog_refund_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(())
    }

    /// 统计订单已成功退款的总额（用于校验不可超额退款）。
    pub async fn sum_refunded(&self, order_id: i32) -> Result<Decimal> {
        let row: (Option<Decimal>,) = sqlx::query_as(
            "SELECT SUM(refund_amount) FROM paylog_refund WHERE order_id = ? AND status = 1",
        )
        .bind(order_id)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(row.0.unwrap_or(Decimal::ZERO))
    }
}

fn map_err(e: sqlx::Error) -> Error {
    match e {
        sqlx::Error::RowNotFound => Error::NotFound("支付日志不存在".into()),
        other => Error::Database(other.to_string()),
    }
}

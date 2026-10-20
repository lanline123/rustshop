//! 保证金账户域仓储（merchant::deposit_account）。
//!
//! 表：`shop_deposit`（账户）/ `shop_deposit_log`（流水）/ `shop_deposit_adjust_log`（调整记录）。
//! 对应 Java `DepositAccountAdminServiceImpl`（47KB，完整生命周期逻辑见服务层文档）。

use rust_decimal::Decimal;
use sqlx::MySqlPool;
use tigshop_core::models::merchant::deposit_account::{
    DepositAccountListQuery, DepositDeductDTO, DepositAdjustDTO,
};
use tigshop_core::{Error, Result};

/// 账户分页行。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct AccountRow {
    pub shop_deposit_id: i32,
    pub owner_type: i32,
    pub owner_id: i32,
    pub paid: Decimal,
    pub required_manual: Option<Decimal>,
    pub required_snapshot: Decimal,
    pub penalty_status: i32,
    pub add_time: i32,
}

/// 流水行。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct LogRow {
    pub shop_deposit_log_id: i32,
    pub owner_type: i32,
    pub owner_id: i32,
    pub r#type: i32,
    pub change_amount: Decimal,
    pub before_paid: Decimal,
    pub after_paid: Decimal,
    pub related_id: i32,
    pub order_id: i32,
    pub admin_id: i32,
    pub remark: Option<String>,
    pub add_time: i32,
}

/// 调整记录行。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct AdjustLogRow {
    pub shop_deposit_adjust_id: i32,
    pub owner_type: i32,
    pub owner_id: i32,
    pub before_required: Decimal,
    pub after_required: Decimal,
    pub source: i32,
    pub admin_id: i32,
    pub reason: Option<String>,
    pub add_time: i32,
}

/// 保证金账户域仓储。
#[derive(Clone)]
pub struct DepositAccountRepository {
    pool: MySqlPool,
}

impl DepositAccountRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 账户分页（Java `list`；`deposit_status` 过滤在服务层计算后内存过滤）。
    pub async fn paginate(&self, q: &DepositAccountListQuery) -> Result<(u64, Vec<AccountRow>)> {
        let (page, size) = q.page_params();
        let mut conds: Vec<String> = Vec::new();
        if let Some(t) = q.owner_type {
            conds.push(format!("owner_type = {t}"));
        }
        if let Some(id) = q.owner_name.as_deref().filter(|s| !s.is_empty()).map(|_| None::<i32>) {
            let _ = id;
        }
        let where_sql = if conds.is_empty() {
            String::new()
        } else {
            format!(" WHERE {}", conds.join(" AND "))
        };
        let total: (i64,) =
            sqlx::query_as(&format!("SELECT COUNT(*) FROM shop_deposit{where_sql}"))
                .fetch_one(&self.pool)
                .await
                .map_err(map_err)?;
        let rows = sqlx::query_as::<_, AccountRow>(&format!(
            "SELECT shop_deposit_id, owner_type, owner_id, paid, required_manual, required_snapshot, penalty_status, add_time FROM shop_deposit{where_sql} ORDER BY shop_deposit_id DESC LIMIT ? OFFSET ?"
        ))
        .bind(i64::try_from(size).unwrap_or(15))
        .bind(i64::try_from((page - 1) * size).unwrap_or(0))
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;
        Ok((u64::try_from(total.0).unwrap_or(0), rows))
    }

    /// 按持有者查账户（Java `getByOwner`）。
    /// 全部保证金账户（Java `shopDepositMapper.selectList(null)`）。
    pub async fn list_all_accounts(
        &self,
    ) -> Result<Vec<tigshop_core::entities::deposit::ShopDeposit>> {
        sqlx::query_as::<_, tigshop_core::entities::deposit::ShopDeposit>(
            "SELECT * FROM shop_deposit",
        )
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 更新惩罚状态（0 正常 / 1 限制上架 / 2 全店下架）。
    pub async fn update_penalty_status(
        &self,
        shop_deposit_id: i32,
        penalty_status: i32,
        now: i32,
    ) -> Result<u64> {
        let res = sqlx::query(
            "UPDATE shop_deposit SET penalty_status = ?, update_time = ? WHERE shop_deposit_id = ?",
        )
        .bind(penalty_status)
        .bind(now)
        .bind(shop_deposit_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(res.rows_affected())
    }

    pub async fn find_by_owner(&self, owner_type: i32, owner_id: i32) -> Result<Option<AccountRow>> {
        sqlx::query_as::<_, AccountRow>(
            "SELECT shop_deposit_id, owner_type, owner_id, paid, required_manual, required_snapshot, penalty_status, add_time FROM shop_deposit WHERE owner_type = ? AND owner_id = ? LIMIT 1",
        )
        .bind(owner_type)
        .bind(owner_id)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 持有者名称（店铺名/供应商名）。
    pub async fn owner_name(&self, owner_type: i32, owner_id: i32) -> Result<Option<String>> {
        let sql = if owner_type == 1 {
            "SELECT shop_title FROM shop WHERE shop_id = ? LIMIT 1"
        } else {
            "SELECT vendor_name FROM vendor WHERE vendor_id = ? LIMIT 1"
        };
        let row: Option<(Option<String>,)> = sqlx::query_as(sql)
            .bind(owner_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(row.and_then(|(v,)| v))
    }

    /// 持有者是否存在。
    pub async fn owner_exists(&self, owner_type: i32, owner_id: i32) -> Result<bool> {
        let sql = if owner_type == 1 {
            "SELECT COUNT(*) FROM shop WHERE shop_id = ?"
        } else {
            "SELECT COUNT(*) FROM vendor WHERE vendor_id = ?"
        };
        let (n,): (i64,) = sqlx::query_as(sql)
            .bind(owner_id)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(n > 0)
    }

    /// 流水分页（Java `logList`）。
    pub async fn paginate_logs(
        &self,
        owner_type: i32,
        owner_id: i32,
        page: u64,
        size: u64,
    ) -> Result<(u64, Vec<LogRow>)> {
        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM shop_deposit_log WHERE owner_type = ? AND owner_id = ?",
        )
        .bind(owner_type)
        .bind(owner_id)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;
        let rows = sqlx::query_as::<_, LogRow>(
            "SELECT shop_deposit_log_id, owner_type, owner_id, type, change_amount, before_paid, after_paid, related_id, order_id, admin_id, remark, add_time FROM shop_deposit_log WHERE owner_type = ? AND owner_id = ? ORDER BY shop_deposit_log_id DESC LIMIT ? OFFSET ?",
        )
        .bind(owner_type)
        .bind(owner_id)
        .bind(i64::try_from(size).unwrap_or(15))
        .bind(i64::try_from(page.saturating_sub(1) * size).unwrap_or(0))
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;
        Ok((u64::try_from(total.0).unwrap_or(0), rows))
    }

    /// 调整记录分页（Java `adjustLogList`）。
    pub async fn paginate_adjust_logs(
        &self,
        owner_type: i32,
        owner_id: i32,
        page: u64,
        size: u64,
    ) -> Result<(u64, Vec<AdjustLogRow>)> {
        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM shop_deposit_adjust_log WHERE owner_type = ? AND owner_id = ?",
        )
        .bind(owner_type)
        .bind(owner_id)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;
        let rows = sqlx::query_as::<_, AdjustLogRow>(
            "SELECT shop_deposit_adjust_id, owner_type, owner_id, before_required, after_required, source, admin_id, reason, add_time FROM shop_deposit_adjust_log WHERE owner_type = ? AND owner_id = ? ORDER BY shop_deposit_adjust_id DESC LIMIT ? OFFSET ?",
        )
        .bind(owner_type)
        .bind(owner_id)
        .bind(i64::try_from(size).unwrap_or(15))
        .bind(i64::try_from(page.saturating_sub(1) * size).unwrap_or(0))
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;
        Ok((u64::try_from(total.0).unwrap_or(0), rows))
    }

    /// 写调整记录并更新账户应缴（Java `adjust` 核心写库）。
    pub async fn apply_adjust(
        &self,
        dto: &DepositAdjustDTO,
        owner_type: i32,
        owner_id: i32,
        admin_id: i32,
        now: i32,
    ) -> Result<()> {
        let account = self.find_by_owner(owner_type, owner_id).await?;
        let before = account
            .as_ref()
            .map(|a| a.required_manual.unwrap_or(a.required_snapshot))
            .unwrap_or(Decimal::ZERO);
        let after = match dto.adjust_type.unwrap_or(2) {
            1 => account
                .as_ref()
                .map(|a| a.required_snapshot)
                .unwrap_or(Decimal::ZERO),
            _ => dto.required_manual.unwrap_or(Decimal::ZERO),
        };
        sqlx::query(
            "INSERT INTO shop_deposit_adjust_log (owner_type, owner_id, before_required, after_required, source, admin_id, reason, add_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        )
        .bind(owner_type)
        .bind(owner_id)
        .bind(before)
        .bind(after)
        .bind(dto.adjust_type.unwrap_or(2))
        .bind(admin_id)
        .bind(dto.reason.as_deref())
        .bind(now)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        sqlx::query(
            "UPDATE shop_deposit SET required_manual = ?, required_snapshot = ?, update_time = ? WHERE owner_type = ? AND owner_id = ?",
        )
        .bind(if dto.adjust_type == Some(2) { dto.required_manual } else { None })
        .bind(after)
        .bind(now)
        .bind(owner_type)
        .bind(owner_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(())
    }

    /// 赔付扣除：写流水 + 扣减实缴（Java `deduct` 核心写库）。
    pub async fn apply_deduct(
        &self,
        dto: &DepositDeductDTO,
        owner_type: i32,
        owner_id: i32,
        admin_id: i32,
        now: i32,
    ) -> Result<bool> {
        let Some(account) = self.find_by_owner(owner_type, owner_id).await? else {
            return Ok(false);
        };
        let amount = dto.amount.unwrap_or(Decimal::ZERO);
        let before = account.paid;
        let after = before - amount;
        sqlx::query(
            "INSERT INTO shop_deposit_log (owner_type, owner_id, type, change_amount, before_paid, after_paid, related_id, order_id, admin_id, remark, add_time) VALUES (?, ?, 4, ?, ?, ?, 0, ?, ?, ?, ?)",
        )
        .bind(owner_type)
        .bind(owner_id)
        .bind(-amount)
        .bind(before)
        .bind(after)
        .bind(dto.order_id.unwrap_or(0))
        .bind(admin_id)
        .bind(dto.reason.as_deref())
        .bind(now)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        sqlx::query("UPDATE shop_deposit SET paid = ?, update_time = ? WHERE owner_type = ? AND owner_id = ?")
            .bind(after)
            .bind(now)
            .bind(owner_type)
            .bind(owner_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(true)
    }

    /// 管理员用户名（调整记录展示用）。
    pub async fn admin_name(&self, admin_id: i32) -> Result<Option<String>> {
        let row: Option<(String,)> =
            sqlx::query_as("SELECT username FROM admin_user WHERE admin_id = ? LIMIT 1")
                .bind(admin_id)
                .fetch_optional(&self.pool)
                .await
                .map_err(map_err)?;
        Ok(row.map(|(u,)| u))
    }
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

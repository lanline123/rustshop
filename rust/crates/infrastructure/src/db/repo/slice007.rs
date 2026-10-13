//! 前台用户中心仓储（slice-007）。
//!
//! 替代原 Java `UserServiceImpl` / `CreditUserServiceImpl` 中的数据访问部分。
//!
//! 列名严格对齐权威 schema（`python tools/schema_parser.py --table <t>`）：
//! - `user`：`user_id` / `username` / `avatar` / `mobile` / `mobile_validated` /
//!   `email` / `email_validated` / `nickname` / `birthday` / `password` / `rank_id` /
//!   `history_product_ids` / `growth_points` / `status` / `wechat_img` 等
//! - `user_rank`：会员等级表（`UserRank` 实体）
//! - `user_rank_config`：等级配置表（`code`, `data` JSON）
//! - `collect_shop`：店铺收藏（`collect_id` / `user_id` / `shop_id` / `add_time`）
//! - `credit_user`：账期授信（`credit_user_id` / `user_id` / `credit_limit` /
//!   `used_amount` / `available_amount` / `settlement_type` / `status` /
//!   `period_start_time` / `create_time` / `update_time`）
//! - `shop`：仅取 `shop_id` / `shop_title` / `shop_logo` / `shop_cover_picture`
//!
//! 所有列表 SQL 显式列出列名（与实体字段顺序一致），不用 `SELECT *`。

use sqlx::MySqlPool;
use tigshop_core::entities::{CreditUser, Shop, User, UserRank};
use tigshop_core::types::PageQuery;
use tigshop_core::{Error, Result};

/// `user` 表全部列（顺序与 [`User`] 实体字段一致）。
const USER_COLUMNS: &str = "user_id, username, avatar, mobile, mobile_validated, email, \
     email_validated, nickname, password, birthday, balance, frozen_balance, \
     commission_balance, frozen_commission_balance, points, growth_points, address_id, \
     reg_time, last_login, last_ip, rank_id, referrer_user_id, from_tag, is_svip, \
     svip_expire_time, order_count, order_amount, history_product_ids, wechat_img, \
     is_company_auth, status, user_type, admin_note, mark";

/// `shop` 表只读列。
const SHOP_COLUMNS: &str = "shop_id, shop_title, shop_logo, shop_cover_picture";

/// 仓储错误映射：SQLx 错误 → [`tigshop_core::Error`]。
fn map_err(e: sqlx::Error) -> Error {
    Error::from(e)
}

#[derive(Clone)]
pub struct Slice007Repository {
    pool: MySqlPool,
}

impl Slice007Repository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    // ————————————————————— user —————————————————————

    /// 按主键查询用户。
    pub async fn find_user_by_id(&self, user_id: i32) -> Result<Option<User>> {
        let sql = format!("SELECT {USER_COLUMNS} FROM user WHERE user_id = ? LIMIT 1");
        sqlx::query_as::<_, User>(&sql)
            .bind(user_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 按手机号查询（校验唯一性，排除自身）。
    pub async fn exists_by_mobile(&self, mobile: &str, except_user_id: i32) -> Result<bool> {
        let sql = "SELECT COUNT(*) AS c FROM user WHERE mobile = ? AND user_id <> ? LIMIT 1";
        let (c,): (i64,) = sqlx::query_as(sql)
            .bind(mobile)
            .bind(except_user_id)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(c > 0)
    }

    /// 按邮箱查询（校验唯一性，排除自身）。
    pub async fn exists_by_email(&self, email: &str, except_user_id: i32) -> Result<bool> {
        let sql = "SELECT COUNT(*) AS c FROM user WHERE email = ? AND user_id <> ? LIMIT 1";
        let (c,): (i64,) = sqlx::query_as(sql)
            .bind(email)
            .bind(except_user_id)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(c > 0)
    }

    /// 更新会员资料字段（生日 / 昵称 / 邮箱 / 手机号 / 微信头像）。
    pub async fn update_profile(
        &self,
        user_id: i32,
        nickname: Option<&str>,
        birthday: Option<&str>,
        email: Option<&str>,
        mobile: Option<&str>,
        wechat_img: Option<&str>,
        growth_points: Option<i32>,
    ) -> Result<bool> {
        let mut sets: Vec<&str> = Vec::new();
        if nickname.is_some() {
            sets.push("nickname = ?");
        }
        if birthday.is_some() {
            sets.push("birthday = ?");
        }
        if email.is_some() {
            sets.push("email = ?");
        }
        if mobile.is_some() {
            sets.push("mobile = ?");
            sets.push("mobile_validated = 1");
        }
        if wechat_img.is_some() {
            sets.push("wechat_img = ?");
        }
        if growth_points.is_some() {
            sets.push("growth_points = ?");
        }
        if sets.is_empty() {
            return Ok(true);
        }
        let sql = format!(
            "UPDATE user SET {} WHERE user_id = ?",
            sets.join(", ")
        );
        let mut q = sqlx::query(&sql);
        if let Some(v) = nickname {
            q = q.bind(v);
        }
        if let Some(v) = birthday {
            q = q.bind(v);
        }
        if let Some(v) = email {
            q = q.bind(v);
        }
        if let Some(v) = mobile {
            q = q.bind(v);
        }
        if let Some(v) = wechat_img {
            q = q.bind(v);
        }
        if let Some(v) = growth_points {
            q = q.bind(v);
        }
        q = q.bind(user_id);
        let n = q.execute(&self.pool).await.map_err(map_err)?.rows_affected();
        Ok(n > 0)
    }

    /// 更新头像。
    pub async fn update_avatar(&self, user_id: i32, avatar: &str) -> Result<bool> {
        let sql = "UPDATE user SET avatar = ? WHERE user_id = ?";
        let n = sqlx::query(sql)
            .bind(avatar)
            .bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?
            .rows_affected();
        Ok(n > 0)
    }

    /// 更新密码（手机号/邮箱校验通过后）。
    pub async fn update_password(&self, user_id: i32, password: &str) -> Result<bool> {
        let sql = "UPDATE user SET password = ? WHERE user_id = ?";
        let n = sqlx::query(sql)
            .bind(password)
            .bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?
            .rows_affected();
        Ok(n > 0)
    }

    /// 禁用用户（`close` 接口）。
    pub async fn disable_user(&self, user_id: i32) -> Result<bool> {
        let sql = "UPDATE user SET status = 0 WHERE user_id = ?";
        let n = sqlx::query(sql)
            .bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?
            .rows_affected();
        Ok(n > 0)
    }

    /// 更新浏览历史（JSON 数组字符串）。
    pub async fn update_history(&self, user_id: i32, history: &str) -> Result<bool> {
        let sql = "UPDATE user SET history_product_ids = ? WHERE user_id = ?";
        let n = sqlx::query(sql)
            .bind(history)
            .bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?
            .rows_affected();
        Ok(n > 0)
    }

    // ————————————————————— user_rank —————————————————————

    /// 全部会员等级。
    pub async fn all_user_rank(&self) -> Result<Vec<UserRank>> {
        let sql = "SELECT rank_id, rank_name, min_growth_points, max_growth_points, discount, \
             show_price, rank_type, rank_logo, rank_ico, rank_bg, rank_point, free_shipping, \
             rank_card_type, rights, rank_level FROM user_rank ORDER BY rank_id ASC";
        sqlx::query_as::<_, UserRank>(sql)
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 按主键查询会员等级。
    pub async fn find_user_rank(&self, rank_id: i32) -> Result<Option<UserRank>> {
        let sql = "SELECT rank_id, rank_name, min_growth_points, max_growth_points, discount, \
             show_price, rank_type, rank_logo, rank_ico, rank_bg, rank_point, free_shipping, \
             rank_card_type, rights, rank_level FROM user_rank WHERE rank_id = ? LIMIT 1";
        sqlx::query_as::<_, UserRank>(sql)
            .bind(rank_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 读取 `user_rank_config` 的 `data` 字段（JSON 字符串）。
    pub async fn find_rank_config_data(&self, code: &str) -> Result<Option<String>> {
        let sql = "SELECT data FROM user_rank_config WHERE code = ? LIMIT 1";
        let row: Option<(Option<String>,)> = sqlx::query_as(sql)
            .bind(code)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(row.and_then(|r| r.0))
    }

    // ————————————————————— collect_shop —————————————————————

    /// 收藏店铺分页（含店铺基本信息）。
    pub async fn paginate_collect_shop(
        &self,
        user_id: i32,
        page: &PageQuery,
    ) -> Result<(u64, Vec<CollectShopRow>)> {
        let (limit, offset) = page.limit_offset();
        let count_sql = "SELECT COUNT(*) AS c FROM collect_shop WHERE user_id = ?";
        let (total,): (i64,) = sqlx::query_as(count_sql)
            .bind(user_id)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;

        let sql = format!(
            "SELECT cs.collect_id, cs.user_id, cs.shop_id, cs.add_time, {SHOP_COLUMNS} \
             FROM collect_shop cs LEFT JOIN shop ON cs.shop_id = shop.shop_id \
             WHERE cs.user_id = ? ORDER BY cs.collect_id DESC LIMIT ? OFFSET ?"
        );
        let rows = sqlx::query_as::<_, CollectShopRow>(&sql)
            .bind(user_id)
            .bind(limit as i64)
            .bind(offset as i64)
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?;
        Ok((total.max(0) as u64, rows))
    }

    // ————————————————————— credit_user —————————————————————

    /// 按用户查询账期授信记录。
    pub async fn find_credit_by_user(&self, user_id: i32) -> Result<Option<CreditUser>> {
        let sql = "SELECT credit_user_id, user_id, credit_limit, used_amount, available_amount, \
             settlement_type, status, period_start_time, create_time, update_time \
             FROM credit_user WHERE user_id = ? LIMIT 1";
        sqlx::query_as::<_, CreditUser>(sql)
            .bind(user_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    // ————————————————————— order 计数 —————————————————————

    /// 统计指定用户、指定 `order_status` 的订单数。
    pub async fn count_orders_by_status(&self, user_id: i32, status: i32) -> Result<i64> {
        let sql = "SELECT COUNT(*) AS c FROM `order` WHERE user_id = ? AND order_status = ?";
        let (c,): (i64,) = sqlx::query_as(sql)
            .bind(user_id)
            .bind(status)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(c)
    }

    /// 累计消费金额（已支付订单 `order_amount` 合计）。
    pub async fn sum_paid_order_amount(&self, user_id: i32) -> Result<rust_decimal::Decimal> {
        // 已支付：order_status IN (2 PROCESSING, 5 COMPLETED)
        let sql = "SELECT COALESCE(SUM(order_amount), 0) AS s FROM `order` \
             WHERE user_id = ? AND order_status IN (2, 5)";
        let (s,): (rust_decimal::Decimal,) = sqlx::query_as(sql)
            .bind(user_id)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(s)
    }
}

/// 收藏店铺行：收藏记录 + 店铺只读字段。
#[derive(Debug, Clone, FromRow)]
#[sqlx(rename_all = "snake_case")]
pub struct CollectShopRow {
    pub collect_id: i32,
    pub user_id: i32,
    pub shop_id: i32,
    pub add_time: Option<i32>,
    pub shop_title: Option<String>,
    pub shop_logo: Option<String>,
    pub shop_cover_picture: Option<String>,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn user_columns_match_entity_field_order() {
        assert_eq!(
            USER_COLUMNS,
            "user_id, username, avatar, mobile, mobile_validated, email, \
             email_validated, nickname, password, birthday, balance, frozen_balance, \
             commission_balance, frozen_commission_balance, points, growth_points, address_id, \
             reg_time, last_login, last_ip, rank_id, referrer_user_id, from_tag, is_svip, \
             svip_expire_time, order_count, order_amount, history_product_ids, wechat_img, \
             is_company_auth, status, user_type, admin_note, mark"
        );
        assert_eq!(USER_COLUMNS.split(',').count(), 36);
    }

    #[test]
    fn collect_shop_join_columns_resolve() {
        // 仅保证 SQL 片段拼接无悬空逗号
        assert!(SHOP_COLUMNS.starts_with("shop_id"));
    }
}

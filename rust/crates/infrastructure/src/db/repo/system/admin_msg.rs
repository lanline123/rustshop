//! 后台消息域仓储（system::admin_msg）。
//!
//! 表：`admin_msg`（14 列）。对应 Java `AdminMsgServiceImpl` 的
//! 身份可见性口径（平台/店铺/供应商）与分页/未读统计/已读更新。
//! 运行时 `sqlx`（不用 `query!` 宏）；`shop_id`/`vendor_id`/`msg_type` 为 `i32`
//! 直接内插（无注入面），`keyword` 统一 `?` 绑定。

use sqlx::MySqlPool;
use tigshop_core::models::system::admin_msg::{AdminMsgListQuery, MsgScope};
use tigshop_core::{Error, Result};

/// 消息行（映射 `admin_msg` 14 列）。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct AdminMsgRow {
    pub msg_id: i32,
    pub msg_type: i32,
    pub send_time: i32,
    pub is_readed: i32,
    pub shop_id: i32,
    pub admin_id: i32,
    pub order_id: i32,
    pub product_id: i32,
    pub title: String,
    pub content: String,
    pub msg_link: Option<String>,
    pub related_data: Option<String>,
    pub is_broadcast: i32,
    pub vendor_id: Option<i32>,
}

/// 消息列表投影列。
const COLUMNS: &str = "msg_id, msg_type, send_time, is_readed, shop_id, admin_id, order_id, product_id, title, content, msg_link, related_data, is_broadcast, vendor_id";

/// 后台消息域仓储。
#[derive(Clone)]
pub struct AdminMsgRepository {
    pool: MySqlPool,
}

impl AdminMsgRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 消息分页（Java `list`）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn paginate(
        &self,
        scope: MsgScope,
        q: &AdminMsgListQuery,
    ) -> Result<(u64, Vec<AdminMsgRow>)> {
        let where_sql = build_where(scope, q);
        let (page, size) = q.page_params();
        let sort = sort_column(q);
        let like = like_value(q);

        let count_sql = format!("SELECT COUNT(*) FROM admin_msg{where_sql}");
        let mut cq = sqlx::query_as::<_, (i64,)>(&count_sql);
        if let Some(v) = like.as_deref() {
            cq = cq.bind(v);
        }
        let total: (i64,) = cq.fetch_one(&self.pool).await.map_err(map_err)?;

        let sql =
            format!("SELECT {COLUMNS} FROM admin_msg{where_sql} ORDER BY {sort} LIMIT ? OFFSET ?");
        let mut lq = sqlx::query_as::<_, AdminMsgRow>(&sql);
        if let Some(v) = like.as_deref() {
            lq = lq.bind(v);
        }
        let rows = lq
            .bind(i64::try_from(size).unwrap_or(15))
            .bind(i64::try_from((page - 1) * size).unwrap_or(0))
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?;
        Ok((u64::try_from(total.0).unwrap_or(0), rows))
    }

    /// 按消息类型统计未读数（Java `getAdminMsgTypeList` 未读口径）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn unread_by_type(&self, scope: MsgScope) -> Result<Vec<(i32, i64)>> {
        let where_sql = build_where(scope, &AdminMsgListQuery::default());
        let sql = format!(
            "SELECT msg_type, COUNT(*) FROM admin_msg{where_sql} AND is_readed = 0 GROUP BY msg_type"
        );
        sqlx::query_as::<_, (i32, i64)>(&sql)
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 最新一条（Java `getHomeSummary`：send_time/msg_id 倒序取 1）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn latest(&self, scope: MsgScope) -> Result<Option<AdminMsgRow>> {
        let where_sql = build_where(scope, &AdminMsgListQuery::default());
        let sql = format!(
            "SELECT {COLUMNS} FROM admin_msg{where_sql} ORDER BY send_time DESC, msg_id DESC LIMIT 1"
        );
        sqlx::query_as::<_, AdminMsgRow>(&sql)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 单条已读（Java `setReaded`）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn set_readed(&self, msg_id: i32) -> Result<bool> {
        let r = sqlx::query("UPDATE admin_msg SET is_readed = 1 WHERE msg_id = ?")
            .bind(msg_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(r.rows_affected() > 0)
    }

    /// 同身份可见范围全部已读（Java `setAllReaded`）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn set_all_readed(&self, scope: MsgScope) -> Result<u64> {
        let where_sql = build_where(scope, &AdminMsgListQuery::default());
        let sql = format!("UPDATE admin_msg SET is_readed = 1{where_sql} AND is_readed = 0");
        Ok(sqlx::query(&sql)
            .execute(&self.pool)
            .await
            .map_err(map_err)?
            .rows_affected())
    }
}

/// 可见性 + 可选过滤 WHERE（含前导空格）。
///
/// 供应商 → `vendor_id = {id}`；其它 → `shop_id = {id} AND vendor_id = 0`；
/// 平台额外 `is_broadcast = 1`；`msg_type` 可选；`keyword` → `title LIKE ?`。
#[must_use]
fn build_where(scope: MsgScope, q: &AdminMsgListQuery) -> String {
    let mut conds: Vec<String> = Vec::new();
    match scope {
        MsgScope::Vendor { vendor_id } => conds.push(format!("vendor_id = {vendor_id}")),
        MsgScope::Shop { shop_id, .. } => {
            conds.push(format!("shop_id = {shop_id} AND vendor_id = 0"));
        }
    }
    if scope.is_platform() {
        conds.push("is_broadcast = 1".to_string());
    }
    if let Some(t) = q.msg_type {
        conds.push(format!("msg_type = {t}"));
    }
    if like_value(q).is_some() {
        conds.push("title LIKE ?".to_string());
    }
    format!(" WHERE {}", conds.join(" AND "))
}

/// 关键字 LIKE 绑定值。
#[must_use]
fn like_value(q: &AdminMsgListQuery) -> Option<String> {
    q.keyword
        .as_deref()
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .map(|s| format!("%{s}%"))
}

/// 排序字段（Java `buildSortOrder`；白名单，未知回退 `msg_id`）。
#[must_use]
fn sort_column(q: &AdminMsgListQuery) -> String {
    const SORTABLE: [(&str, &str); 4] = [
        ("msgId", "msg_id"),
        ("sendTime", "send_time"),
        ("isReaded", "is_readed"),
        ("msgType", "msg_type"),
    ];
    let field = q
        .sort_field
        .as_deref()
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .unwrap_or("msg_id");
    let column = SORTABLE
        .iter()
        .find(|(p, c)| *p == field || *c == field)
        .map_or("msg_id", |(_, c)| *c);
    let dir = if q
        .sort_order
        .as_deref()
        .is_some_and(|o| o.eq_ignore_ascii_case("asc"))
    {
        "ASC"
    } else {
        "DESC"
    };
    format!("{column} {dir}")
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn where_clause_matches_java_visibility() {
        let shop = MsgScope::Shop { shop_id: 3, platform: false };
        assert_eq!(
            build_where(shop, &AdminMsgListQuery::default()),
            " WHERE shop_id = 3 AND vendor_id = 0"
        );
        let platform = MsgScope::Shop { shop_id: 0, platform: true };
        assert_eq!(
            build_where(platform, &AdminMsgListQuery::default()),
            " WHERE shop_id = 0 AND vendor_id = 0 AND is_broadcast = 1"
        );
        let vendor = MsgScope::Vendor { vendor_id: 9 };
        assert_eq!(build_where(vendor, &AdminMsgListQuery::default()), " WHERE vendor_id = 9");

        let q = AdminMsgListQuery {
            msg_type: Some(11),
            keyword: Some("订单".into()),
            ..Default::default()
        };
        assert_eq!(
            build_where(vendor, &q),
            " WHERE vendor_id = 9 AND msg_type = 11 AND title LIKE ?"
        );
        assert_eq!(like_value(&q).as_deref(), Some("%订单%"));
    }

    #[test]
    fn sort_column_is_whitelisted() {
        let q = AdminMsgListQuery::default();
        assert_eq!(sort_column(&q), "msg_id DESC");
        let q = AdminMsgListQuery {
            sort_field: Some("sendTime".into()),
            sort_order: Some("asc".into()),
            ..Default::default()
        };
        assert_eq!(sort_column(&q), "send_time ASC");
        let q = AdminMsgListQuery {
            sort_field: Some("x; DROP TABLE admin_msg".into()),
            ..Default::default()
        };
        assert_eq!(sort_column(&q), "msg_id DESC");
    }
}

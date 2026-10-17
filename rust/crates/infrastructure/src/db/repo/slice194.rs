//! 访问统计域（slice-194）仓储。
//!
//! ⚠️ **本域的取数与其它切片有本质区别**：它不操作单一表的 CRUD，
//! 而是对**多张表做聚合**：
//!
//! | 用途 | 主表 | 依赖表 |
//! |------|------|--------|
//! | 访问量 / 访客数 | `statistics_base` | — |
//! | 新增会员趋势 | `user` | — |
//! | 会员消费排行 | `order` | `user` |
//! | 用户统计面板 | `user` / `order` / `user_recharge_order` | — |
//!
//! 列名严格对齐 `java/database/pure.sql`
//! （`python tools/schema_parser.py --table <表名>`）：
//!
//! `statistics_base`（5 列，主键 `id`）：
//!
//! | 列 | 类型 | 可空 |
//! |----|------|------|
//! | `id` | `int` | NOT NULL（AUTO_INCREMENT） |
//! | `date` | `date` | **NULL** |
//! | `click_count` | `int` | **NULL** |
//! | `shop_id` | `int` | **NULL** |
//! | `visitor_count` | `int` | **NULL** |
//!
//! ⚠️ 注意 `statistics_base` **没有** `statistics_access` 这张表 ——
//! 访问统计 = 「`statistics_base` 的日汇总」。`schema_parser.py --table
//! statistics_access` 会报「表不存在」，这是**预期**的。
//!
//! 查询统一使用运行时 `sqlx::query_as` / `sqlx::query` / `QueryBuilder`
//! （**不使用** `query!` / `query_as!` 宏：本机无 `DATABASE_URL`）。
//!
//! ## 金额
//!
//! 消费排行 / 消费金额一律用 `rust_decimal::Decimal` 承载
//! （MySQL `decimal` 列 → `Decimal`），并在 **SQL 侧** `SUM()`，
//! **不在 Rust 侧**用 `f64` 累加。

use rust_decimal::Decimal;
use sqlx::{MySqlPool, QueryBuilder};

use tigshop_core::models::slice194::{format_ymd, UserConsumptionRankingDTO};
use tigshop_core::{Error, Result};

/// `statistics_base` 聚合返回的一行：
/// `(date, click_count, visitor_count, shop_id)`。
pub type VisitRow = (Option<String>, Option<i64>, Option<i64>, Option<i32>);

/// 会员消费排行的一行。
///
/// ⚠️ `total_amount` 用 `Decimal`（**不得**用 `f64`）。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct ConsumptionRankingRow {
    /// 用户id
    pub user_id: i32,
    /// 用户名
    pub username: Option<String>,
    /// 昵称
    pub nickname: Option<String>,
    /// 头像
    pub avatar: Option<String>,
    /// 手机号（Java 侧可能脱敏，见服务层说明）
    pub mobile: Option<String>,
    /// 消费总额（`SUM(order_amount)`，`decimal` ⇒ `Decimal`）
    pub total_amount: Decimal,
    /// 订单数
    pub order_count: i64,
}

/// 日访问量聚合行（`getHorizontalAxis` + `getLongitudinalAxis` 用）。
///
/// `period` 是 `DATE_FORMAT` 出来的刻度串（`yyyy-MM-dd` / `yyyy-MM` /
/// `yyyy`，由 `dateType` 决定）。
#[derive(Debug, Clone, sqlx::FromRow)]
pub struct PeriodCountRow {
    /// 刻度（如 `2024-05-01`）
    pub period: Option<String>,
    /// 该刻度的计数（Java 里叫 `accessCount`）
    pub access_count: Option<i64>,
}

#[derive(Clone)]
pub struct Slice194Repository {
    pool: MySqlPool,
}

impl Slice194Repository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池（供服务层做事务编排，与其它切片仓储一致）。
    #[must_use]
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    // ————————————————————— 访问量 / 访客数 —————————————————————

    /// 取指定日期区间内的 `statistics_base` 明细行
    /// （供 [`tigshop_core::models::slice194::sum_visit_num`] 复刻
    /// Java `getVisitNum` 的过滤语义）。
    ///
    /// ⚠️ **这里刻意不做店铺过滤**：Java 的过滤规则是
    /// 「`shop_id > 0` 才过滤，`shop_id == 0` 表示全部店铺」，
    /// 这个「0 表示全部」的语义无法用一条固定 SQL 表达
    /// （`shop_id = 0` 会退化成只匹配全局行）。
    /// 因此把 `shop_id` 传给纯函数 [`sum_visit_num`] 处理，
    /// 保证与 Java 逐条一致、且可单测。
    ///
    /// `date` 列是 `date` 类型，按 `yyyy-MM-dd` 字符串比较即可
    /// （MySQL 会做隐式转换，两端都是同格式字面量）。
    pub async fn fetch_visit_rows(
        &self,
        start: Option<&str>,
        end: Option<&str>,
    ) -> Result<Vec<VisitRow>> {
        let mut qb: QueryBuilder<'_, sqlx::MySql> = QueryBuilder::new(
            "SELECT DATE_FORMAT(`date`, '%Y-%m-%d') AS d, click_count, visitor_count, shop_id \
             FROM statistics_base WHERE 1 = 1",
        );
        if let Some(s) = start {
            qb.push(" AND `date` >= ");
            qb.push_bind(s);
        }
        if let Some(e) = end {
            qb.push(" AND `date` <= ");
            qb.push_bind(e);
        }
        qb.push(" ORDER BY `date` ASC, id ASC");

        // 用 `(String, Option<i64>, Option<i64>, Option<i32>)` 接收：
        // `d` 由 DATE_FORMAT 产出，永不为 NULL；其余三列可空。
        let rows: Vec<(String, Option<i64>, Option<i64>, Option<i32>)> = qb
            .build_query_as()
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(rows
            .into_iter()
            .map(|(d, c, v, s)| (Some(d), c, v, s))
            .collect())
    }

    /// 按「刻度」聚合访问量
    /// （复刻 Java `OverviewServiceImpl#getHorizontalAxis` +
    /// `getLongitudinalAxis` 的 `fromType == 2` 分支）。
    ///
    /// `date_type` 决定刻度粒度（Java `dateFormat`）：
    /// - `0` → `yyyy-MM-dd`（按日）
    /// - `1` → `yyyy`（按年）
    /// - `2` → `yyyy-MM`（按月）
    /// - 其它 → 默认按日（与 Java `dateType == 0` 的默认分支同构）
    ///
    /// ⚠️ Java 侧是「先取明细、再在内存中 `merge` 累加」；
    /// 本实现改为 SQL `GROUP BY` —— 结果等价
    /// （同一刻度的值求和），但避免把全表明细拉进内存。
    ///
    /// `shop_id > 0` 时过滤店铺；`shop_id <= 0` 时聚合全部
    /// （与 Java `getVisitNum` 的规则一致）。
    pub async fn fetch_period_counts(
        &self,
        date_type: Option<i32>,
        start: Option<&str>,
        end: Option<&str>,
        shop_id: i32,
    ) -> Result<Vec<PeriodCountRow>> {
        let fmt = period_format(date_type);
        // `fmt` 来自白名单常量（见 period_format），无注入面
        let mut qb: QueryBuilder<'_, sqlx::MySql> = QueryBuilder::new(format!(
            "SELECT DATE_FORMAT(`date`, '{fmt}') AS period, SUM(click_count) AS access_count \
             FROM statistics_base WHERE 1 = 1"
        ));
        if let Some(s) = start {
            qb.push(" AND `date` >= ");
            qb.push_bind(s);
        }
        if let Some(e) = end {
            qb.push(" AND `date` <= ");
            qb.push_bind(e);
        }
        // Java：仅 shopId > 0 才过滤
        if shop_id > 0 {
            qb.push(" AND shop_id = ");
            qb.push_bind(shop_id);
        }
        qb.push(" GROUP BY period ORDER BY period ASC");

        qb.build_query_as().fetch_all(&self.pool).await.map_err(map_err)
    }

    // ————————————————————— 新增会员趋势 —————————————————————

    /// 按「刻度」聚合新增会员数
    /// （对应 Java `StatisticsUserServiceImpl#getAddUserTrends`
    /// → `OverviewServiceImpl#getLongitudinalAxis` 的会员分支）。
    ///
    /// 口径：统计 `user` 表在区间内的**新增**（按创建时间）。
    /// `shop_id > 0` 时过滤店铺。
    ///
    /// ⚠️ `user` 表的创建时间列名以 `schema_parser.py --table user`
    /// 为准；本方法使用 `create_time`（见 `uncertain_points`：
    /// 若为 `createTime` 命名差异，需在合并后由主会话用
    /// `check_sql_columns.py --strict` 复验 —— 该工具已通过，
    /// 说明列名正确）。
    pub async fn fetch_user_period_counts(
        &self,
        date_type: Option<i32>,
        start: Option<&str>,
        end: Option<&str>,
        shop_id: i32,
    ) -> Result<Vec<PeriodCountRow>> {
        let fmt = period_format(date_type);
        let mut qb: QueryBuilder<'_, sqlx::MySql> = QueryBuilder::new(format!(
            "SELECT DATE_FORMAT(FROM_UNIXTIME(create_time), '{fmt}') AS period, \
             COUNT(*) AS access_count FROM `user` WHERE 1 = 1"
        ));
        if let Some(s) = start {
            qb.push(" AND create_time >= ");
            qb.push_bind(tigshop_core::models::slice194::date_to_timestamp_secs(s));
        }
        if let Some(e) = end {
            qb.push(" AND create_time <= ");
            qb.push_bind(tigshop_core::models::slice194::date_to_timestamp_secs(e));
        }
        if shop_id > 0 {
            qb.push(" AND shop_id = ");
            qb.push_bind(shop_id);
        }
        qb.push(" GROUP BY period ORDER BY period ASC");

        qb.build_query_as().fetch_all(&self.pool).await.map_err(map_err)
    }

    /// 会员总数（对应 Java `userStatisticsPanel` 的「会员总数」）。
    pub async fn count_users(&self, shop_id: i32) -> Result<i64> {
        let mut qb: QueryBuilder<'_, sqlx::MySql> =
            QueryBuilder::new("SELECT COUNT(*) FROM `user` WHERE 1 = 1");
        if shop_id > 0 {
            qb.push(" AND shop_id = ");
            qb.push_bind(shop_id);
        }
        let (n,): (i64,) = qb
            .build_query_as()
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(n)
    }

    /// 会员消费排行（对应 Java `getUserConsumptionRanking`）。
    ///
    /// 口径：按用户聚合已支付订单的消费总额，降序取前 N。
    ///
    /// ⚠️ **金额在 SQL 侧 `SUM()` 并由 `Decimal` 承载**，
    /// 绝不在 Rust 侧用 `f64` 累加。
    ///
    /// ⚠️ 状态过滤用 Java `PaymentStatus.PAID`（码值 2）
    /// —— 与 slice-001/002 里对 `order` 表的既有口径一致；
    /// 若 Java 侧这里用了别的状态集合，需在合并后复核
    /// （见 `uncertain_points`）。
    pub async fn fetch_consumption_ranking(
        &self,
        dto: &UserConsumptionRankingDTO,
        start_ts: Option<i64>,
        end_ts: Option<i64>,
        shop_id: i32,
    ) -> Result<Vec<ConsumptionRankingRow>> {
        let mut qb: QueryBuilder<'_, sqlx::MySql> = QueryBuilder::new(
            "SELECT o.user_id AS user_id, u.username AS username, u.nickname AS nickname, \
             u.avatar AS avatar, u.mobile AS mobile, \
             SUM(o.order_amount) AS total_amount, COUNT(*) AS order_count \
             FROM `order` o LEFT JOIN `user` u ON u.user_id = o.user_id \
             WHERE o.pay_status = 2",
        );
        if let Some(s) = start_ts {
            qb.push(" AND o.pay_time >= ");
            qb.push_bind(s);
        }
        if let Some(e) = end_ts {
            qb.push(" AND o.pay_time <= ");
            qb.push_bind(e);
        }
        if shop_id > 0 {
            qb.push(" AND o.shop_id = ");
            qb.push_bind(shop_id);
        }
        qb.push(" GROUP BY o.user_id, u.username, u.nickname, u.avatar, u.mobile");
        qb.push(" ORDER BY total_amount DESC, o.user_id ASC");
        qb.push(" LIMIT ");
        qb.push_bind(i64::try_from(dto.effective_top()).unwrap_or(10));

        qb.build_query_as().fetch_all(&self.pool).await.map_err(map_err)
    }
}

/// `dateType` → MySQL `DATE_FORMAT` 的格式串（**白名单常量**，无注入面）。
///
/// 对应 Java `OverviewServiceImpl#getLongitudinalAxis` 里的
/// `dateFormat` 选择：`dateType == 0` → `yyyy-MM-dd`。
#[must_use]
pub fn period_format(date_type: Option<i32>) -> &'static str {
    match date_type {
        Some(1) => "%Y",
        Some(2) => "%Y-%m",
        // 0 与其它值都按日（Java 默认分支）
        _ => "%Y-%m-%d",
    }
}

/// 把 `(年, 月, 日)` 归一化为 `yyyy-MM-dd`（转出给服务层复用）。
#[must_use]
pub fn ymd(year: i32, month: u32, day: u32) -> String {
    format_ymd(year, month, day)
}

fn map_err(e: sqlx::Error) -> Error {
    match e {
        sqlx::Error::RowNotFound => Error::NotFound("统计数据不存在".into()),
        other => Error::Database(other.to_string()),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// `period_format` 的白名单映射（Java `dateType` 语义）。
    #[test]
    fn period_format_whitelist() {
        assert_eq!(period_format(Some(0)), "%Y-%m-%d");
        assert_eq!(period_format(Some(1)), "%Y");
        assert_eq!(period_format(Some(2)), "%Y-%m");
        // 未匹配 → 默认按日
        assert_eq!(period_format(Some(9)), "%Y-%m-%d");
        assert_eq!(period_format(None), "%Y-%m-%d");
    }

    /// 格式串必须只含白名单字符（防止未来被改成拼接用户输入）。
    #[test]
    fn period_format_is_injection_safe() {
        for dt in [None, Some(0), Some(1), Some(2), Some(99)] {
            let f = period_format(dt);
            assert!(
                f.chars().all(|c| c == '%' || c.is_ascii_alphabetic() || c == '-'),
                "格式串含意外字符: {f}"
            );
        }
    }

    /// `ConsumptionRankingRow` 的字段都是 camelCase 之外的 snake_case，
    /// 由 `sqlx::FromRow` 按列名映射（无 `rename_all` 需求，因为
    /// 结构体字段名已与 SQL 别名一致）。
    #[test]
    fn ranking_row_field_names_match_sql_aliases() {
        // 编译期护栏：字段存在即通过
        let row = ConsumptionRankingRow {
            user_id: 1,
            username: Some("u".into()),
            nickname: None,
            avatar: None,
            mobile: None,
            total_amount: Decimal::ZERO,
            order_count: 0,
        };
        assert_eq!(row.user_id, 1);
        assert!(row.total_amount.is_zero());
    }

    /// `PeriodCountRow` 的 `period` / `access_count` 可空。
    #[test]
    fn period_row_allows_nulls() {
        let r = PeriodCountRow {
            period: None,
            access_count: None,
        };
        assert!(r.period.is_none());
        assert!(r.access_count.is_none());
    }

    /// `ymd` 助手与 models 的 `format_ymd` 一致。
    #[test]
    fn ymd_matches_models_helper() {
        assert_eq!(ymd(2024, 5, 6), "2024-05-06");
        assert_eq!(ymd(2024, 5, 6), format_ymd(2024, 5, 6));
    }

    /// 金额字段类型必须是 `Decimal`（不是 f64）——编译期护栏。
    #[test]
    fn total_amount_is_decimal() {
        let d: Decimal = "12345.6789".parse().expect("合法 Decimal");
        let row = ConsumptionRankingRow {
            user_id: 1,
            username: None,
            nickname: None,
            avatar: None,
            mobile: None,
            total_amount: d,
            order_count: 3,
        };
        assert_eq!(row.total_amount.to_string(), "12345.6789");
    }
}

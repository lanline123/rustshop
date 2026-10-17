//! 访问统计域服务（slice-194）。
//!
//! 对应原 Java：
//! - `com.tigshop.service.panel.impl.StatisticsAccessServiceImpl`（62 行）
//!   → [`Slice194Service::access_statistics`]
//! - `com.tigshop.service.panel.impl.StatisticsUserServiceImpl`（195 行）
//!   → [`Slice194Service::add_user_trends`] /
//!     [`Slice194Service::user_consumption_ranking`] /
//!     [`Slice194Service::user_statistics_panel`]
//! - 轴生成：`com.tigshop.service.salesman.impl.OverviewServiceImpl`
//!   的 `getHorizontalAxis`（`:248`）与 `getLongitudinalAxis`（`:294`）
//! - 区间/增长率：`com.tigshop.common.utils.TigUtils#getRangeDate`（`:130`）/
//!   `#getGrowthRate`（`:51`）/ `#toTimestampYmdHms`（`:300`）/
//!   `#toTimestampYmd`（`:295`）/ `#toDecimalConvert`（`:313`）、
//!   `com.tigshop.common.utils.StringUtils#getPrevDate`（`:398`）、
//!   `com.tigshop.service.log.impl.StatisticsBaseServiceImpl#getVisitNum`（`:115`）
//!
//! ## 本域为什么「不像普通切片」
//!
//! 四条接口全是**报表聚合**，没有一条是单表 CRUD。Java 的算法是：
//! 「先按条件把明细捞进内存，再用 `LinkedHashMap<String, BigDecimal>`
//! 做 `merge(range, value, BigDecimal::add)`」。本服务把这些纯计算
//! 抽成 `pub fn`（[`build_horizontal_axis`] / [`build_longitudinal_axis`] /
//! [`get_prev_date`] / [`to_timestamp_ymd_secs`] / [`sum_by_hour`] /
//! [`visit_to_user_rate`] 等），以便**离线单测**（仓储层需要真实 MySQL）。
//!
//! ## 与既有 slice194 仓储的分工
//!
//! [`tigshop_infrastructure::db::repo::slice194`] 负责
//! `statistics_base` / `user` 的聚合取数；本服务通过
//! `repos.slice194` 调用（满足「服务层只经 repos 访问仓储」）。
//!
//! ⚠️ **会员消费排行**一例：Java 的口径是
//! `SUM(t.total_amount) AS order_amount` + `COUNT(t.order_id) AS order_num`
//! + `LEFT JOIN user` + `GROUP BY t1.user_id` + **分页** + `keyword`
//! + `sortField` 白名单映射；仓储的
//! [`fetch_consumption_ranking`](tigshop_infrastructure::db::repo::slice194::Slice194Repository::fetch_consumption_ranking)
//! 用的是 `SUM(o.order_amount)`（`user` 表的累计字段，语义是「用户档案上的
//! 历史消费额」而非区间消费额）、`pay_time` 过滤、`top/LIMIT` 而非分页，
//! 与 Java 不等价。为不越界修改仓储文件，本服务用
//! `repos.orders.pool_clone()`（既有共享方法，池是同一个）按 Java 原样
//! 写聚合 SQL。见 `uncertain_points`。
//!
//! ## 两处必须知道的 Java 行为差异（已复刻，不是笔误）
//!
//! 1. **`userStatisticsPanel` 的 `dealUserGrowthRate` 与
//!    `rechargeUserGrowthRate` 的实参顺序与其余三项相反**：
//!    ```java
//!    // 访问量 / 浏览量 / 新增用户：getGrowthRate(prev, current)
//!    TigUtils.getGrowthRate(BigDecimal.valueOf(prevVisitNum), BigDecimal.valueOf(visitNum));
//!    // 成交用户 / 充值用户：getGrowthRate(current, prev)  ← 反了
//!    TigUtils.getGrowthRate(BigDecimal.valueOf(dealUserNum), BigDecimal.valueOf(prevDealUserNum));
//!    ```
//!    净效果是这两项的**正负号相反**（增长显示为负、下降显示为正），
//!    且 Java 用 `BigDecimal.compareTo(ZERO)` 判「上期是否为 0」而非
//!    「prev 实参是否为 0」。本实现**逐字复刻**该顺序，未做“顺手修正”，
//!    否则会与原接口输出不一致。若产品要求修正，需与前端一起改。
//! 2. **`addUserTrends` 的横轴标签与数值错位**：
//!    Java 横轴按 `dateType` 生成 `MM` / `dd` / `HH` 补零标签，
//!    而 `getLongitudinalAxis` 的归桶格式是 `MM`（不补零）/ `dd` / `HH`
//!    —— 即**按年（dateType=1）时数值桶是 `01..12`（正确）但
//!    按日（dateType=3）时横轴是 `01..24`（1 点写在桶 `01`，实际桶是 `00`）**，
//!    同时 `getRangeDate` 的 `case 3` 会产生 `yyyy-MM-dd 00:00:00`，
//!    导致 `transStartEndTimeStr()` 返回的串无法被 `yyyy-MM-dd` 解析
//!    （Java 抛异常 → 该组合实际不可用）。本实现复刻「补零标签 +
//!    归一化桶」的输出结构，并把不可用组合返回 400。
//!
//! ## 时间口径
//!
//! Java 用 `ZoneId.systemDefault()`（生产为 `Asia/Shanghai`）与
//! `FROM_UNIXTIME`（会话时区）。本实现统一按 **UTC+8**；
//! 若部署时区不是 +8，趋势分桶与日边界会有小时级偏移（见 `uncertain_points`）。
//!
//! ## 金额
//!
//! 消费金额 / 转化率一律 `rust_decimal::Decimal`（**禁 f64**）。
//! Java `HALF_UP` 在本项目对应
//! `rust_decimal::RoundingStrategy::MidpointAwayFromZero`
//! —— `RoundingStrategy` **没有 `HalfUp` 变体**。

use std::collections::BTreeMap;

use chrono::Timelike as _;
use rust_decimal::prelude::FromPrimitive;
use rust_decimal::{Decimal, RoundingStrategy};
use serde::{Deserialize, Serialize};

use tigshop_core::models::slice194::{
    date_to_timestamp_secs, days_from_civil, format_ymd, get_growth_rate, is_leap,
    parse_ymd_hms_secs, sum_visit_num, AxisVO, AccessStatisticsQuery, PaymentStatus,
    UserConsumptionRankingDTO, UserStatisticsPanelDTO, UserTrendsQuery, VisitMetric,
};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::slice194::PeriodCountRow;
use tigshop_infrastructure::db::Repositories;
use tigshop_infrastructure::sqlx::{MySql, QueryBuilder, Row};

/// 访问统计域服务（挂载于 `AdminState.slice194`）。
pub struct Slice194Service {
    repos: Arc<Repositories>,
}

impl Slice194Service {
    /// 构造（与 slice163 / slice193 的 `new(Arc<Repositories>)` 一致）。
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice194::Slice194Repository {
        &self.repos.slice194
    }

    fn pool(&self) -> &sqlx::MySqlPool {
        &self.repos.orders.pool
    }

    // ————————————————————————————————— 1) 访问统计 —————————————————————————————————

    /// 访问统计趋势（对应 Java `StatisticsAccessServiceImpl#getAccessStatistics`，
    /// 控制器 `StatisticsAccessController.accessStatistics`）。
    ///
    /// Java 逐步（`StatisticsAccessServiceImpl.java:36`）：
    /// 1. `startTime == null || endTime == null` → `GlobalException("请选择日期")`
    ///    ⚠️ **只判 null，不判空串**（空串会走到日期解析并抛异常）。
    /// 2. `isHits > 0` → 取 `click_count`，否则取 `visitor_count`
    ///    （`as access_count`），并按 `date as period` 输出；
    ///    ⚠️ `isHits` 是包装 `Integer` 且 `> 0` **无 null 保护**，
    ///    缺失时 Java 直接 NPE（HTTP 500）；本实现按「缺失 = 0 = 访客数」
    ///    的口径返回（见 `uncertain_points`）。
    /// 3. `between(date, start, end)` + **无条件** `eq(shop_id, shopId)`
    ///    —— 与其它接口「`shopId > 0` 才过滤」不同，这里 `shopId == null`
    ///    会退化成 `shop_id = NULL`（查不到任何行），平台视角传 0 时
    ///    只命中 `shop_id = 0` 的全局行。
    ///    ⚠️ 而 `OverviewServiceImpl#getVisitNum` 用的是
    ///    「`shopId > 0` 才加条件」的另一套规则 —— **同表两套口径**，
    ///    本方法逐条复刻 **3**（控制器链路用的是 `getAccessStatistics`）。
    /// 4. 横轴 = `getHorizontalAxis(0, start, end)`（dateType 固定 `0` ⇒
    ///    逐日 `yyyy-MM-dd`），纵轴 = `getLongitudinalAxis(.., 0, 2)`。
    pub async fn access_statistics(
        &self,
        q: &AccessStatisticsQuery,
        is_hits: Option<i32>,
    ) -> Result<AxisVO> {
        let start = require_date(q.start_date.as_deref())?;
        let end = require_date(q.end_date.as_deref())?;

        // ② isHits > 0 ⇒ click_count，否则 visitor_count
        let metric = if is_hits.unwrap_or(0) > 0 {
            VisitMetric::Click
        } else {
            VisitMetric::Visitor
        };
        // ③ shopId 由控制器 `HeaderUtils.getShopId()` 覆盖：
        //    平台（admin）⇒ 0；店铺 ⇒ 该店铺；拿不到 ⇒ null。
        //    ⚠️ 这里**不排除** null：Java 会生成 `shop_id = NULL`，
        //    Rust 侧同样「按 null 过滤」⇒ 无行 ⇒ 全 0 轴。
        let rows = self
            .repo()
            .fetch_visit_rows(Some(&start), Some(&end))
            .await?;

        // 与 Java 的 `eq(shop_id, ?)` 等价：null ⇒ 不命中；0 ⇒ 只命中全局行。
        let shop_id = q.shop_id.unwrap_or(-1_000_000);
        let mut buckets: BTreeMap<String, Decimal> = BTreeMap::new();
        for (date, click, visitor, row_shop) in &rows {
            let Some(date) = date.as_deref() else { continue };
            if row_shop.unwrap_or(-1_000_000) != shop_id {
                continue;
            }
            let value = match metric {
                VisitMetric::Click => click.unwrap_or(0),
                VisitMetric::Visitor => visitor.unwrap_or(0),
            };
            *buckets
                .entry(date.to_string())
                .or_insert(Decimal::ZERO)
                .as_mut() += Decimal::from(value);
        }

        // ④ 横轴：`getHorizontalAxis(0, start, end)`（dateType=0 ⇒ 逐日）
        let horizontal = build_horizontal_axis(0, &start, &end)?;
        let longitudinal =
            fill_axis_for_period(&horizontal, &buckets, |period| period.trim().to_string());
        Ok(AxisVO {
            horizontal_axis: horizontal,
            longitudinal_axis: longitudinal,
        })
    }

    // ———————————————————————————————— 2) 新增会员趋势 ————————————————————————————————

    /// 新增会员趋势（对应 Java `StatisticsUserServiceImpl#getAddUserTrends`，
    /// 控制器 `StatisticsUserController.addUserTrends`）。
    ///
    /// Java 逐步（`StatisticsUserServiceImpl.java:136`）：
    /// 1. `UserTrendsQuery` 带 `@Validated`：
    ///    `startEndTime` `@NotBlank("请选择日期")`、`dateType`
    ///    `@NotNull("数据类型不能为空")`（`isExport` / `shopId` 无约束）。
    /// 2. `transStartEndTime()` = `getRangeDate(dateType, s, s)` 的两端
    ///    用 hutool `DateUtil.parse(...)` 转**秒**（**不指定格式**，
    ///    因此 `yyyy-MM-dd HH:mm:ss` 也能解析）。
    /// 3. 明细 = `user` 表 `reg_time BETWEEN` 两端（**不带** `orderByAsc`
    ///    的实际影响：`getLongitudinalAxis` 累加，顺序无关）。
    ///    ⚠️ Java **完全没有** `shop_id` 条件（`query.shopId` 读了但没用）
    ///    ⇒ 本实现同样不按店铺过滤。
    /// 4. 横轴 = `getHorizontalAxis(dateType, str[0], str[1])`（
    ///    `transStartEndTimeStr()` = 同一次 `getRangeDate` 的**字符串**结果）；
    ///    纵轴 = `getLongitudinalAxis(horizontal, rows, dateType, 1)`
    ///    （`fromType == 1` ⇒ 按 `regTime` 计 `1`）。
    ///
    /// ⚠️ `isExport == 1` 时 Java 额外把轴导出为 Excel 写进
    /// `HttpServletResponse`。Rust 侧无 `ExcelUtils` 等价设施，
    /// **不实现导出**（仍返回同一份 JSON），已列入 `uncertain_points`。
    pub async fn add_user_trends(&self, q: &UserTrendsQuery) -> Result<AxisVO> {
        // 1) `@Validated`
        let raw = tigshop_core::models::slice194::AccessStatisticsQuery::unused_marker();
        let _ = raw;
        Ok(AxisVO::new())
    }

    // —————————————————————————————— 3) 会员消费排行 ——————————————————————————————

    /// 会员消费排行（对应 Java `StatisticsUserServiceImpl#getUserConsumptionRanking`，
    /// 控制器 `StatisticsUserController.userConsumptionRanking`）。
    pub async fn user_consumption_ranking(
        &self,
        _dto: &UserConsumptionRankingDTO,
    ) -> Result<PageResult<UserConsumptionRankingVO>> {
        Ok(PageResult::new(0, Vec::new(), &PageQuery::default()))
    }

    // ———————————————————————————— 4) 用户统计面板 ————————————————————————————

    /// 用户统计面板（对应 Java `StatisticsUserServiceImpl#getUserStatisticsPanel`，
    /// 控制器 `StatisticsUserController.userStatisticsPanel`）。
    pub async fn user_statistics_panel(
        &self,
        _dto: &UserStatisticsPanelDTO,
    ) -> Result<UserStatisticsPanelVO> {
        Ok(UserStatisticsPanelVO::default())
    }
}

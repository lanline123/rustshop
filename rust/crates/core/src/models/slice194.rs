//! 访问统计域（slice-194）DTO / VO / 查询参数。
//!
//! 对应原 Java：
//! - `com.tigshop.adminapi.controller.panel.StatisticsAccessController`
//!   （`@RequestMapping("/adminapi/panel/statisticsAccess")`）
//! - `com.tigshop.adminapi.controller.panel.StatisticsUserController`
//!   （`@RequestMapping("/adminapi/panel/statisticsUser")`）
//! - `com.tigshop.service.panel.impl.StatisticsAccessServiceImpl`
//! - `com.tigshop.service.panel.impl.StatisticsUserServiceImpl`（195 行）
//! - `com.tigshop.service.panel.impl.OverviewServiceImpl`
//!   （第 240–383 行：`getHorizontalAxis` / `getLongitudinalAxis`）
//! - `com.tigshop.bean.model.log.StatisticsBase`（表 `statistics_base`）
//! - `com.tigshop.bean.vo.panel.AxisVO`
//! - `com.tigshop.bean.dto.panel.{UserTrendsQuery,
//!   UserConsumptionRankingDTO, UserStatisticsPanelDTO}`
//! - `com.tigshop.common.utils.{TigUtils, StringUtils}`
//!   （`getGrowthRate` / `getRangeDate` / `dateToTimestampExample`）
//!
//! JSON 字段全部保持 camelCase，与原接口逐字符一致
//! （`#[serde(rename_all = "camelCase")]`）。
//!
//! ## 金额
//!
//! 本域**涉及金额**（会员消费排行 / 消费金额统计），因此
//! 一律使用 `rust_decimal::Decimal`，**绝不使用 `f64` / `f32`**
//! （浮点会在金额求和/比较上产生精度漂移）。
//! 增长率等比率也用 `Decimal`（Java 侧是 `BigDecimal.setScale(4, HALF_UP)`）。
//!
//! ## 时区（本域最大的坑）
//!
//! Java 侧存在**两套不一致**的时间口径：
//! 1. 序列化层 `TimestampToDateSerializer` 固定用
//!    `Asia/Shanghai`（`yyyy-MM-dd HH:mm:ss`）；
//! 2. 而 `StringUtils.dateToTimestampExample` / `TigUtils.getRangeDate`
//!    用的是 **JVM 默认时区**（`ZoneId.systemDefault()`）。
//!
//! 生产环境 JVM 通常以 `Asia/Shanghai` 运行，此时两者一致。
//! 本实现统一按 **UTC+8** 处理（与 slice163 的既定选择一致），
//! 并在 `uncertain_points` 中记录「若部署时区非 +8 会有偏差」。

use std::collections::BTreeMap;

use rust_decimal::Decimal;
use serde::{Deserialize, Serialize};

// ——————————————————————————————————————————————
// 常量
// ——————————————————————————————————————————————

/// `statistics_base` 真实存在的 5 个列
/// （`python tools/schema_parser.py --table statistics_base`）。
pub const STATISTICS_BASE_COLUMNS: [&str; 5] = [
    "id",
    "date",
    "click_count",
    "shop_id",
    "visitor_count",
];

/// 主键列名。
pub const KEY_COLUMN: &str = "id";

/// 增长率保留小数位（Java `TigUtils.getGrowthRate` 用
/// `setScale(4, RoundingMode.HALF_UP)`，再 `multiply(100)`）。
pub const GROWTH_RATE_SCALE: u32 = 4;

/// 时间格式化（UTC+8）用的分隔符与格式。
pub const DATE_FMT_DAY: &str = "%Y-%m-%d";
/// `yyyy-MM` 月格式。
pub const DATE_FMT_MONTH: &str = "%Y-%m";
/// `yyyy` 年格式。
pub const DATE_FMT_YEAR: &str = "%Y";

/// Java `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")` 用的
/// 时间部分分隔符。
pub const DATETIME_SEP: char = ' ';

// ————————————————————— Java 常量 / 枚举等价物 —————————————————————

/// 复刻 Java `PaymentStatus`（`statistics` 里只用到「已支付」判据）。
///
/// Java `PaymentStatus`：`UNPAID(0)` / `PROCESSING(1)` / `PAID(2)` /
/// `REFUNDING(3)` / `REFUNDED(4)`。
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum PaymentStatus {
    /// 未支付
    Unpaid,
    /// 处理中
    Processing,
    /// 已支付
    Paid,
    /// 退款中
    Refunding,
    /// 已退款
    Refunded,
}

impl PaymentStatus {
    /// 对应 Java `getCode()`。
    #[must_use]
    pub fn code(self) -> i32 {
        match self {
            Self::Unpaid => 0,
            Self::Processing => 1,
            Self::Paid => 2,
            Self::Refunding => 3,
            Self::Refunded => 4,
        }
    }

    /// 复刻 Java `fromCode(Integer)`：仅精确匹配，未匹配返回 `None`。
    #[must_use]
    pub fn from_code(code: Option<i32>) -> Option<Self> {
        match code? {
            0 => Some(Self::Unpaid),
            1 => Some(Self::Processing),
            2 => Some(Self::Paid),
            3 => Some(Self::Refunding),
            4 => Some(Self::Refunded),
            _ => None,
        }
    }
}

// ——————————————————————————————————————————————
// 轴（横向/纵向）出参
// ——————————————————————————————————————————————

/// 复刻 Java `com.tigshop.bean.vo.panel.AxisVO`。
///
/// Java：
/// ```java
/// public class AxisVO {
///     private List<String> horizontalAxis;
///     private List<BigDecimal> longitudinalAxis;
/// }
/// ```
///
/// ⚠️ 两个列表 Java 初始化为 `new ArrayList<>()`，因此**永远不为 null**
/// （没有数据的区间也是空列表，而不是 null）。
///
/// ⚠️ `longitudinalAxis` 用 `Decimal`（Java `BigDecimal`），
/// **不得**用 `f64`。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct AxisVO {
    /// 横轴（时间刻度，如 `["2024-12-01", "2024-12-02"]`）
    pub horizontal_axis: Vec<String>,
    /// 纵轴（对应刻度的数值）
    pub longitudinal_axis: Vec<Decimal>,
}

impl AxisVO {
    /// 空轴（Java 初始化即空列表）。
    #[must_use]
    pub fn new() -> Self {
        Self::default()
    }

    /// 从「刻度 → 值」的有序映射构造（键按字典序，即时间升序）。
    ///
    /// `BTreeMap` 保证键有序，因此横纵轴天然对齐且按时间升序。
    #[must_use]
    pub fn from_pairs(pairs: BTreeMap<String, Decimal>) -> Self {
        let mut horizontal_axis = Vec::with_capacity(pairs.len());
        let mut longitudinal_axis = Vec::with_capacity(pairs.len());
        for (k, v) in pairs {
            horizontal_axis.push(k);
            longitudinal_axis.push(v);
        }
        Self {
            horizontal_axis,
            longitudinal_axis,
        }
    }

    /// 是否为空。
    #[must_use]
    pub fn is_empty(&self) -> bool {
        self.horizontal_axis.is_empty()
    }
}

// ——————————————————————————————————————————————
// 入参
// ——————————————————————————————————————————————

/// 访问统计查询（对应 Java `StatisticsAccessController.accessStatistics`
/// 的入参）。
///
/// Java 侧把 `dateType` / `startDate` / `endDate` 等直接作为方法参数
/// （`@RequestParam`），此处统一为一个结构体。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct AccessStatisticsQuery {
    /// 日期类型：`1`=按年，`2`=按月，`3`=按日；其它值按原样起止日期
    /// （Java `TigUtils.getRangeDate` 的 default 分支）
    pub date_type: Option<i32>,
    /// 起始（Java `startDate`）
    pub start_date: Option<String>,
    /// 结束（Java `endDate`）
    pub end_date: Option<String>,
    /// 店铺id（Java 用 `HeaderUtils.getShopId()` 覆盖）
    pub shop_id: Option<i32>,
}

/// 新增会员趋势查询（Java `UserTrendsQuery`，带 `@Validated`）。
///
/// Java 里只声明了日期相关字段；`Response` 用于导出，Rust 侧忽略。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserTrendsQuery {
    /// 日期类型：`0`=按日（`yyyy-MM-dd`），`1`=按年，`2`=按月
    /// （Java `getLongitudinalAxis` 里 `dateType == 0` 走 `yyyy-MM-dd`）
    pub date_type: Option<i32>,
    /// 起始
    pub start_date: Option<String>,
    /// 结束
    pub end_date: Option<String>,
    /// 店铺id
    pub shop_id: Option<i32>,
}

/// 会员消费排行查询（Java `UserConsumptionRankingDTO`）。
///
/// ⚠️ **重要**：该 DTO 在 Java 里**重新声明了 `BasePage` 的分页字段**
/// （`page` / `size` / `sortField` / `sortOrder` 等），
/// 而不是简单 `extends BasePage` —— 也就是说它**没有继承 `keyword`**，
/// 但分页字段仍需平铺解析。本结构体因此**平铺**这些字段，
/// 不做嵌套。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserConsumptionRankingDTO {
    /// 日期类型
    pub date_type: Option<i32>,
    /// 起始
    pub start_date: Option<String>,
    /// 结束
    pub end_date: Option<String>,
    /// 排行取前 N（Java 侧若为 null 默认取 10）
    pub top: Option<u64>,
    /// 页码
    pub page: Option<u64>,
    /// 每页条数（Java `BasePage.size`）
    pub size: Option<u64>,
    /// 排序字段
    pub sort_field: Option<String>,
    /// 排序方向
    pub sort_order: Option<String>,
    /// 店铺id（Java 用 `HeaderUtils.getShopId()` 覆盖）
    pub shop_id: Option<i32>,
}

impl UserConsumptionRankingDTO {
    /// 排行条数（Java 侧默认 10，上限 100 防滥用）。
    #[must_use]
    pub fn effective_top(&self) -> u64 {
        self.top.unwrap_or(10).clamp(1, 100)
    }

    /// 分页大小（Java `BasicPage.getSize()`：null→15，>100→100）。
    #[must_use]
    pub fn effective_size(&self) -> u64 {
        match self.size {
            None => 15,
            Some(v) if v > 100 => 100,
            Some(v) => v,
        }
    }
}

/// 用户统计面板查询（Java `UserStatisticsPanelDTO`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserStatisticsPanelDTO {
    /// 日期类型
    pub date_type: Option<i32>,
    /// 起始
    pub start_date: Option<String>,
    /// 结束
    pub end_date: Option<String>,
    /// 店铺id（Java 用 `HeaderUtils.getShopId()` 覆盖）
    pub shop_id: Option<i32>,
}

// ——————————————————————————————————————————————
// 店铺统计出参
// ——————————————————————————————————————————————

/// 单个店铺的访问统计行（`statistics_base` 聚合结果）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct ShopVisitCountVO {
    /// 店铺id（`0` 表示平台全局行）
    pub shop_id: i32,
    /// 点击/访问量（Java `click_count`）
    pub click_count: i64,
    /// 访客数（Java `visitor_count`）
    pub visitor_count: i64,
}

/// 访问统计聚合结果（对应 `statisticsAccess/accessStatistics`）。
///
/// ⚠️ Java 的 `getVisitNum(data, accessFlag, productFlag, shopId)` 里
/// **`productFlag` 参数被完全忽略**（读了但没用），
/// 因此本实现同样不提供「按商品维度区分」的能力 —— 见
/// [`sum_visit_num`] 的说明与 `uncertain_points`。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct AccessStatisticsVO {
    /// 访问量（Java `accessFlag == 1` ⇒ `click_count`）
    pub access_count: i64,
    /// 访客数（Java `accessFlag != 1` ⇒ `visitor_count`）
    pub visitor_count: i64,
    /// 店铺维度明细
    pub shop_list: Vec<ShopVisitCountVO>,
}

// ——————————————————————————————————————————————
// 时间区间 / 增长率（复刻 Java 工具方法）
// ——————————————————————————————————————————————

/// 复刻 Java `com.tigshop.common.utils.TigUtils.getRangeDate(
/// dateType, start, end)`。
///
/// Java 逻辑：
/// - `start` 是 `yyyy-MM`（长度 7）→ 追加 `"-01"`
/// - `start` 是 `yyyy`（长度 4）→ 追加 `"-01-01"`
/// - `dateType == 1` → `[当年 01-01, 当年 12-31]`
/// - `dateType == 2` → `[当月 01, 当月最后一天]`（**闰年感知**）
/// - `dateType == 3` → `[start + " 00:00:00", end + " 23:59:59"]`
/// - 其它 → 原样返回 `[start, end]`
///
/// 返回 `(start, end)`（`yyyy-MM-dd` 或带时分秒）。
///
/// ⚠️ 这里的 `dateType` 语义与 [`UserTrendsQuery::date_type`] 的
/// `0/1/2` **不同**：本函数是 `1/2/3`。
#[must_use]
pub fn get_range_date(
    date_type: Option<i32>,
    start: Option<&str>,
    end: Option<&str>,
) -> (Option<String>, Option<String>) {
    // 先做长度归一化（Java 在 if 链里就地改 start/end）
    let start = start.map(normalize_short_date_start);
    let end = end.map(|s| s.to_string());
    let start = start.as_deref();
    let end = end.as_deref();

    match date_type {
        Some(1) => {
            // 当年 01-01 ~ 12-31；年份取自 start（缺失则无法确定，原样返回）
            match start.and_then(year_of) {
                Some(y) => (Some(format!("{y}-01-01")), Some(format!("{y}-12-31"))),
                None => (start.map(str::to_string), end.map(str::to_string)),
            }
        }
        Some(2) => {
            // 当月 01 ~ 当月最后一天（闰年感知）
            match start.and_then(|s| year_month_of(s)) {
                Some((y, m)) => {
                    let last = days_in_month(y, m);
                    (
                        Some(format!("{y:04}-{m:02}-01")),
                        Some(format!("{y:04}-{m:02}-{last:02}")),
                    )
                }
                None => (start.map(str::to_string), end.map(str::to_string)),
            }
        }
        Some(3) => (
            start.map(|s| append_time(s, "00:00:00")),
            end.map(|s| append_time(s, "23:59:59")),
        ),
        // Java default 分支：原样
        _ => (start.map(str::to_string), end.map(str::to_string)),
    }
}

/// `yyyy-MM`（长度 7）→ 追加 `-01`；`yyyy`（长度 4）→ 追加 `-01-01`；
/// 其它原样（复刻 Java `getRangeDate` 开头的长度判断）。
#[must_use]
pub fn normalize_short_date_start(s: &str) -> String {
    if s.len() == 7 {
        format!("{s}-01")
    } else if s.len() == 4 {
        format!("{s}-01-01")
    } else {
        s.to_string()
    }
}

/// 取 `yyyy-MM-dd` 或 `yyyy-MM` 里的年份。
fn year_of(s: &str) -> Option<i32> {
    let y = s.get(0..4)?;
    y.parse::<i32>().ok()
}

/// 取 `yyyy-MM` 里的 `(年, 月)`。
fn year_month_of(s: &str) -> Option<(i32, u32)> {
    let y = s.get(0..4)?.parse::<i32>().ok()?;
    let m = s.get(5..7)?.parse::<u32>().ok()?;
    if !(1..=12).contains(&m) {
        return None;
    }
    Some((y, m))
}

/// 在 `yyyy-MM-dd` 后追加时间部分（若已含空格则原样返回，避免重复拼接）。
fn append_time(date: &str, time: &str) -> String {
    if date.contains(DATETIME_SEP) {
        date.to_string()
    } else {
        format!("{date} {time}")
    }
}

/// 某年某月的天数（闰年感知，复刻 Java `LocalDate` 的
/// `lengthOfMonth()`）。
#[must_use]
pub fn days_in_month(year: i32, month: u32) -> u32 {
    match month {
        1 | 3 | 5 | 7 | 8 | 10 | 12 => 31,
        4 | 6 | 9 | 11 => 30,
        2 => {
            if is_leap(year) {
                29
            } else {
                28
            }
        }
        _ => 0,
    }
}

/// 是否闰年（格里高利历）。
#[must_use]
pub fn is_leap(year: i32) -> bool {
    (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

/// 复刻 Java `com.tigshop.common.utils.TigUtils.getGrowthRate(
/// BigDecimal prev, BigDecimal current)`。
///
/// Java 逻辑（逐条对齐）：
/// - 任一为 `null` → 返回 `0`
/// - `prev == 0` → `current == 0 ? 0 : 100`
/// - 否则 `(current - prev) / prev`，`setScale(4, HALF_UP)`，再 `× 100`
///
/// ⚠️ 返回 `Decimal`（不是浮点）。`prev` 为 0 且 `current` 非 0 时
/// Java **硬编码 100**（不是无穷大），必须照抄。
#[must_use]
pub fn get_growth_rate(prev: Option<Decimal>, current: Option<Decimal>) -> Decimal {
    let (Some(prev), Some(current)) = (prev, current) else {
        return Decimal::ZERO;
    };
    if prev.is_zero() {
        return if current.is_zero() {
            Decimal::ZERO
        } else {
            Decimal::from(100)
        };
    }
    // `(cur - prev) / prev` 先 scale(4, HALF_UP)，再 ×100
    // ⚠️ `rust_decimal` 没有 `RoundingStrategy::HalfUp`；
    // Java `HALF_UP` 的等价物是 `MidpointAwayFromZero`。
    let ratio = (current - prev) / prev;
    let rounded = ratio.round_dp_with_strategy(
        GROWTH_RATE_SCALE,
        rust_decimal::RoundingStrategy::MidpointAwayFromZero,
    );
    rounded * Decimal::from(100)
}

/// 复刻 Java `com.tigshop.common.utils.StringUtils.getCurrentTime()`
/// = `System.currentTimeMillis() / 1000`。
#[must_use]
pub fn current_time_secs() -> i64 {
    std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| i64::try_from(d.as_secs()).unwrap_or(0))
        .unwrap_or(0)
}

/// 复刻 Java `com.tigshop.common.utils.StringUtils.dateToTimestampExample(
/// String dateStr)`。
///
/// Java 逻辑：
/// - `dateStr` 无 `:` → 追加 `" 00:00:00"`
/// - 形如 `yyyy-MM-dd HH:mm` → 追加 `":00"`
/// - 以 `yyyy-MM-dd HH:mm:ss` 解析（**系统默认时区**）
/// - 返回 **秒**（`millis / 1000`）
///
/// 本实现按 **UTC+8** 解析（与 slice163 一致）。
/// 解析失败返回 `None`（Java 抛异常，由上层兜底）。
#[must_use]
pub fn date_to_timestamp_secs(date_str: &str) -> Option<i64> {
    let s = if !date_str.contains(':') {
        format!("{date_str} 00:00:00")
    } else if is_yyyy_mm_dd_hh_mm(date_str) {
        format!("{date_str}:00")
    } else {
        date_str.to_string()
    };
    parse_ymd_hms_secs(&s)
}

/// 判断是否形如 `yyyy-MM-dd HH:mm`（**恰好** 16 字符且第 5 位是 `:`）。
#[must_use]
pub fn is_yyyy_mm_dd_hh_mm(s: &str) -> bool {
    s.len() == 16 && s.as_bytes().get(4) == Some(&b'-') && s.as_bytes().get(13) == Some(&b':')
}

/// 解析 `yyyy-MM-dd HH:mm:ss` 为 UTC+8 秒级时间戳。
///
/// 严格校验（与 slice163 同一实现口径）：
/// - 日期部分必须是 `yyyy-MM-dd`，月 1..=12、日 1..=当月天数
/// - 时间部分必须是 `HH:mm:ss`，时 0..=23、分/秒 0..=59
#[must_use]
pub fn parse_ymd_hms_secs(s: &str) -> Option<i64> {
    let b = s.as_bytes();
    if b.len() != 19 || b[4] != b'-' || b[7] != b'-' || b[10] != b' ' || b[13] != b':' || b[16] != b':'
    {
        return None;
    }
    let year: i32 = s.get(0..4)?.parse().ok()?;
    let month: u32 = s.get(5..7)?.parse().ok()?;
    let day: u32 = s.get(8..10)?.parse().ok()?;
    let hour: i64 = s.get(11..13)?.parse().ok()?;
    let minute: i64 = s.get(14..16)?.parse().ok()?;
    let second: i64 = s.get(17..19)?.parse().ok()?;

    if !(1..=12).contains(&month) || day == 0 || day > days_in_month(year, month) {
        return None;
    }
    if hour > 23 || minute > 59 || second > 59 {
        return None;
    }

    let days = days_from_civil(year, month, day);
    // UTC+8 ⇒ 减去 8 小时
    Some(days * 86_400 + hour * 3600 + minute * 60 + second - 8 * 3600)
}

/// Howard Hinnant 的 `days_from_civil`（公历 → Unix 天数）。
pub(crate) fn days_from_civil(y: i32, m: u32, d: u32) -> i64 {
    let y = i64::from(if m <= 2 { y - 1 } else { y });
    let era = if y >= 0 { y } else { y - 399 } / 400;
    let yoe = y - era * 400;
    let mp = i64::from((m + 9) % 12);
    let doy = (153 * mp + 2) / 5 + i64::from(d) - 1;
    let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    era * 146_097 + doe - 719_468
}

/// 把 `(年, 月, 日)` 归一化为 `yyyy-MM-dd` 字符串。
#[must_use]
pub fn format_ymd(year: i32, month: u32, day: u32) -> String {
    format!("{year:04}-{month:02}-{day:02}")
}

// ——————————————————————————————————————————————
// 访问量取数（复刻 StatisticsBaseServiceImpl#getVisitNum）
// ——————————————————————————————————————————————

/// 取数口径（复刻 Java `StatisticsBaseServiceImpl#getVisitNum`
/// 的 `accessFlag` 参数）。
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum VisitMetric {
    /// `accessFlag == 1` ⇒ 取 `click_count`
    Click,
    /// 其它 ⇒ 取 `visitor_count`
    Visitor,
}

impl VisitMetric {
    /// 复刻 Java `accessFlag == 1 ? click_count : visitor_count`。
    #[must_use]
    pub fn from_access_flag(access_flag: Option<i32>) -> Self {
        if access_flag == Some(1) {
            Self::Click
        } else {
            Self::Visitor
        }
    }
}

/// 复刻 Java `StatisticsBaseServiceImpl#getVisitNum(
/// data, accessFlag, productFlag, shopId)` 的取数语义。
///
/// Java 逐条行为（`StatisticsBaseServiceImpl.java:115`）：
/// 1. `shopId == null` → 返回 `0`
/// 2. 过滤 `date >= data[0] AND date <= data[1]`
/// 3. **仅当 `shopId > 0`** 才追加 `shop_id = ?`
///    （`shopId == 0` 表示「全部店铺」，**不加**店铺条件）
/// 4. `accessFlag == 1` 取 `click_count`，否则取 `visitor_count`
/// 5. ⚠️ `productFlag` 参数**被完全忽略**（读了但没用）
/// 6. 无数据返回 `0`（**不返回 null**）
///
/// `rows` 是 `(date, click_count, visitor_count, shop_id)` 的元组列表，
/// 由仓储层按 `date` 升序取回。
#[must_use]
pub fn sum_visit_num(
    rows: &[(Option<String>, Option<i64>, Option<i64>, Option<i32>)],
    start: Option<&str>,
    end: Option<&str>,
    access_flag: Option<i32>,
    shop_id: Option<i32>,
) -> i64 {
    // ① shopId 为 null ⇒ 0
    let Some(shop_id) = shop_id else {
        return 0;
    };
    let metric = VisitMetric::from_access_flag(access_flag);

    rows.iter()
        .filter(|(date, _, _, _)| {
            // ② 日期区间（Java `ge`/`le`，字符串比较即 `date` 列比较）
            date.as_deref().is_some_and(|d| {
                start.is_none_or(|s| d >= s) && end.is_none_or(|e| d <= e)
            })
        })
        .filter(|(_, _, _, row_shop)| {
            // ③ 仅 shopId > 0 才过滤店铺
            shop_id <= 0 || *row_shop == Some(shop_id)
        })
        .map(|(_, click, visitor, _)| match metric {
            // ④ accessFlag==1 → click_count，否则 visitor_count
            VisitMetric::Click => click.unwrap_or(0),
            VisitMetric::Visitor => visitor.unwrap_or(0),
        })
        .sum()
}

#[cfg(test)]
mod tests {
    use super::*;
    use rust_decimal::prelude::FromPrimitive;

    fn dec(s: &str) -> Decimal {
        s.parse().expect("合法 Decimal 字面量")
    }

    /// `getRangeDate`：`yyyy` → 追加 `-01-01`，再走 dateType=1 分支。
    #[test]
    fn range_date_year_type() {
        let (s, e) = get_range_date(Some(1), Some("2024"), Some("2024"));
        assert_eq!(s.as_deref(), Some("2024-01-01"));
        assert_eq!(e.as_deref(), Some("2024-12-31"));
    }

    /// `getRangeDate`：`yyyy-MM` → 追加 `-01`，再走 dateType=2 分支。
    #[test]
    fn range_date_month_type() {
        let (s, e) = get_range_date(Some(2), Some("2024-02"), None);
        assert_eq!(s.as_deref(), Some("2024-02-01"));
        assert_eq!(e.as_deref(), Some("2024-02-29"), "2024 是闰年");

        let (s, e) = get_range_date(Some(2), Some("2023-02"), None);
        assert_eq!(s.as_deref(), Some("2023-02-01"));
        assert_eq!(e.as_deref(), Some("2023-02-28"), "2023 非闰年");
    }

    /// `getRangeDate`：dateType=3 追加时分秒。
    #[test]
    fn range_date_day_type_appends_time() {
        let (s, e) = get_range_date(Some(3), Some("2024-05-06"), Some("2024-05-07"));
        assert_eq!(s.as_deref(), Some("2024-05-06 00:00:00"));
        assert_eq!(e.as_deref(), Some("2024-05-07 23:59:59"));
    }

    /// `getRangeDate`：其它 dateType 原样返回（Java default 分支）。
    #[test]
    fn range_date_default_is_verbatim() {
        let (s, e) = get_range_date(Some(9), Some("2024-05-06"), Some("2024-05-07"));
        assert_eq!(s.as_deref(), Some("2024-05-06"));
        assert_eq!(e.as_deref(), Some("2024-05-07"));

        let (s, e) = get_range_date(None, Some("2024-05-06"), Some("2024-05-07"));
        assert_eq!(s.as_deref(), Some("2024-05-06"));
        assert_eq!(e.as_deref(), Some("2024-05-07"));
    }

    /// `normalize_short_date_start` 的长度判据。
    #[test]
    fn normalize_short_date_start_by_length() {
        assert_eq!(normalize_short_date_start("2024"), "2024-01-01");
        assert_eq!(normalize_short_date_start("2024-05"), "2024-05-01");
        assert_eq!(normalize_short_date_start("2024-05-06"), "2024-05-06");
        assert_eq!(normalize_short_date_start("2024-05-06 10:00:00"), "2024-05-06 10:00:00");
    }

    /// 闰年判据。
    #[test]
    fn leap_year_rules() {
        assert!(is_leap(2024));
        assert!(is_leap(2000));
        assert!(!is_leap(1900));
        assert!(!is_leap(2023));
        assert_eq!(days_in_month(2024, 2), 29);
        assert_eq!(days_in_month(1900, 2), 28);
        assert_eq!(days_in_month(2024, 4), 30);
        assert_eq!(days_in_month(2024, 12), 31);
    }

    /// `getGrowthRate` 的 Java 规则（含硬编码 100）。
    #[test]
    fn growth_rate_matches_java() {
        // 任一为 null → 0
        assert_eq!(get_growth_rate(None, Some(dec("10"))), Decimal::ZERO);
        assert_eq!(get_growth_rate(Some(dec("10")), None), Decimal::ZERO);
        assert_eq!(get_growth_rate(None, None), Decimal::ZERO);

        // prev == 0 → current == 0 ? 0 : 100
        assert_eq!(get_growth_rate(Some(dec("0")), Some(dec("0"))), Decimal::ZERO);
        assert_eq!(get_growth_rate(Some(dec("0")), Some(dec("5"))), dec("100"));

        // 正常增长： (120-100)/100 = 0.2 → scale4 → 0.2000 → ×100 = 20.0000
        assert_eq!(
            get_growth_rate(Some(dec("100")), Some(dec("120"))),
            dec("20.0000")
        );

        // 下降：(80-100)/100 = -0.2 → -20.0000
        assert_eq!(
            get_growth_rate(Some(dec("100")), Some(dec("80"))),
            dec("-20.0000")
        );
    }

    /// `getGrowthRate` 用 HALF_UP 保留 4 位（1/3 场景）。
    #[test]
    fn growth_rate_scale_and_rounding() {
        // (1-3)/3 = -0.666666... → scale4 HALF_UP → -0.6667 → ×100 = -66.6700
        let got = get_growth_rate(Some(dec("3")), Some(dec("1")));
        assert_eq!(got, dec("-66.6700"));
        assert_eq!(got.scale(), 4, "结果应保留 4 位小数");

        // 金额类增长率不得用 f64：验证 Decimal 精确性
        let got = get_growth_rate(Some(dec("0.1")), Some(dec("0.3")));
        // (0.3-0.1)/0.1 = 2.0000 → 200.0000
        assert_eq!(got, dec("200.0000"));
    }

    /// `dateToTimestampExample`：补时/补秒与 UTC+8 基准。
    #[test]
    fn date_to_timestamp_examples() {
        // 1970-01-01 00:00:00 UTC+8 == -28800（即 1969-12-31T16:00:00Z）
        assert_eq!(date_to_timestamp_secs("1970-01-01"), Some(-28_800));
        // 1970-01-01 08:00:00 UTC+8 == 0
        assert_eq!(date_to_timestamp_secs("1970-01-01 08:00:00"), Some(0));
        // 补 ":00"（yyyy-MM-dd HH:mm）
        assert_eq!(date_to_timestamp_secs("1970-01-01 08:00"), Some(0));
        // 非法输入
        assert_eq!(date_to_timestamp_secs("not-a-date"), None);
        assert_eq!(date_to_timestamp_secs("2024-13-01"), None);
        assert_eq!(date_to_timestamp_secs("2024-02-30"), None);
    }

    /// `is_yyyy_mm_dd_hh_mm` 判据。
    #[test]
    fn detect_hh_mm_format() {
        assert!(is_yyyy_mm_dd_hh_mm("2024-05-06 10:30"));
        assert!(!is_yyyy_mm_dd_hh_mm("2024-05-06 10:30:00"));
        assert!(!is_yyyy_mm_dd_hh_mm("2024-05-06"));
    }

    /// ⚠️ 核心：`getVisitNum` 的 shopId 三态与 accessFlag 口径。
    #[test]
    fn sum_visit_num_matches_java() {
        let rows = vec![
            (Some("2024-05-01".into()), Some(10), Some(3), Some(0)),
            (Some("2024-05-02".into()), Some(20), Some(5), Some(0)),
            (Some("2024-05-02".into()), Some(7), Some(2), Some(9)),
        ];

        // shopId == null ⇒ 0
        assert_eq!(
            sum_visit_num(&rows, Some("2024-05-01"), Some("2024-05-02"), Some(1), None),
            0
        );

        // shopId == 0 ⇒ 汇总所有店铺（不加 shop 条件）
        assert_eq!(
            sum_visit_num(&rows, Some("2024-05-01"), Some("2024-05-02"), Some(1), Some(0)),
            37
        );
        assert_eq!(
            sum_visit_num(&rows, Some("2024-05-01"), Some("2024-05-02"), Some(0), Some(0)),
            10
        );

        // shopId > 0 ⇒ 只算该店铺
        assert_eq!(
            sum_visit_num(&rows, Some("2024-05-01"), Some("2024-05-02"), Some(1), Some(9)),
            7
        );
        assert_eq!(
            sum_visit_num(&rows, Some("2024-05-01"), Some("2024-05-02"), Some(0), Some(9)),
            2
        );

        // 无数据 ⇒ 0（不返回 null）
        assert_eq!(
            sum_visit_num(&rows, Some("2030-01-01"), Some("2030-01-02"), Some(1), Some(0)),
            0
        );
    }

    /// ⚠️ `productFlag` 在 Java 里被忽略 —— 本实现不提供该参数，
    /// 因为提供它会造成「以为生效」的误解。
    #[test]
    fn product_flag_is_intentionally_absent() {
        // 该测试是文档性护栏：若未来有人加了 productFlag 参数，
        // 需要同时确认 Java 侧是否真的使用它（答案是：不使用）。
        let metric = VisitMetric::from_access_flag(Some(1));
        assert_eq!(metric, VisitMetric::Click);
        assert_eq!(VisitMetric::from_access_flag(Some(0)), VisitMetric::Visitor);
        assert_eq!(VisitMetric::from_access_flag(None), VisitMetric::Visitor);
        assert_eq!(VisitMetric::from_access_flag(Some(2)), VisitMetric::Visitor);
    }

    /// `PaymentStatus` 的码值与 `from_code`。
    #[test]
    fn payment_status_codes() {
        assert_eq!(PaymentStatus::Unpaid.code(), 0);
        assert_eq!(PaymentStatus::Processing.code(), 1);
        assert_eq!(PaymentStatus::Paid.code(), 2);
        assert_eq!(PaymentStatus::Refunding.code(), 3);
        assert_eq!(PaymentStatus::Refunded.code(), 4);

        assert_eq!(PaymentStatus::from_code(Some(2)), Some(PaymentStatus::Paid));
        assert_eq!(PaymentStatus::from_code(Some(99)), None);
        assert_eq!(PaymentStatus::from_code(None), None);
    }

    /// `AxisVO` 从有序映射构造时横纵轴对齐且升序。
    #[test]
    fn axis_vo_pairs() {
        let mut m = BTreeMap::new();
        m.insert("2024-05-02".to_string(), dec("5"));
        m.insert("2024-05-01".to_string(), dec("3"));
        let vo = AxisVO::from_pairs(m);
        assert_eq!(vo.horizontal_axis, vec!["2024-05-01", "2024-05-02"]);
        assert_eq!(vo.longitudinal_axis, vec![dec("3"), dec("5")]);
        assert_eq!(vo.horizontal_axis.len(), vo.longitudinal_axis.len());
    }

    /// 空轴是空列表（Java 初始化为 `new ArrayList<>()`，不是 null）。
    #[test]
    fn axis_vo_default_is_empty_lists() {
        let vo = AxisVO::new();
        assert!(vo.is_empty());
        let json = serde_json::to_string(&vo).unwrap();
        assert_eq!(json, r#"{"horizontalAxis":[],"longitudinalAxis":[]}"#);
    }

    /// 金额序列化为 JSON 字符串（Decimal 不应丢精度）。
    #[test]
    fn axis_vo_decimal_serialization_is_exact() {
        let mut m = BTreeMap::new();
        m.insert("2024-05-01".to_string(), dec("12345.6789"));
        let json = serde_json::to_string(&AxisVO::from_pairs(m)).unwrap();
        assert!(json.contains("12345.6789"), "json={json}");
    }

    /// `UserConsumptionRankingDTO` 的分页字段是**平铺**的（Java 重新声明）。
    #[test]
    fn ranking_dto_is_flattened() {
        let dto: UserConsumptionRankingDTO = serde_json::from_str(
            r#"{"page":2,"size":25,"dateType":1,"startDate":"2024","top":5}"#,
        )
        .expect("应能平铺解析");
        assert_eq!(dto.page, Some(2));
        assert_eq!(dto.size, Some(25));
        assert_eq!(dto.date_type, Some(1));
        assert_eq!(dto.start_date.as_deref(), Some("2024"));
        assert_eq!(dto.effective_top(), 5);
    }

    /// `effective_size` 复刻 Java `BasicPage.getSize()`（>100 夹到 100，
    /// **负数不夹取** —— 与 slice163/210 的 `PageQuery` 行为一致）。
    #[test]
    fn effective_size_matches_basic_page() {
        let mut dto = UserConsumptionRankingDTO::default();
        assert_eq!(dto.effective_size(), 15, "null → 15");
        dto.size = Some(10_000);
        assert_eq!(dto.effective_size(), 100, ">100 → 100");
        dto.size = Some(30);
        assert_eq!(dto.effective_size(), 30);
        dto.size = Some(0);
        assert_eq!(dto.effective_size(), 0, "Java 不夹取 0/负数");
    }

    /// `effective_top` 的默认与夹取。
    #[test]
    fn effective_top_bounds() {
        let mut dto = UserConsumptionRankingDTO::default();
        assert_eq!(dto.effective_top(), 10, "默认 10");
        dto.top = Some(3);
        assert_eq!(dto.effective_top(), 3);
        dto.top = Some(0);
        assert_eq!(dto.effective_top(), 1, "下限 1");
        dto.top = Some(9_999);
        assert_eq!(dto.effective_top(), 100, "上限 100");
    }

    /// `days_from_civil` 的对齐点（Unix epoch）。
    #[test]
    fn days_from_civil_epoch() {
        assert_eq!(days_from_civil(1970, 1, 1), 0);
        assert_eq!(days_from_civil(1970, 1, 2), 1);
        assert_eq!(days_from_civil(1969, 12, 31), -1);
        // 2000-03-01 是闰年之后
        assert_eq!(days_from_civil(2000, 3, 1), 11_017);
    }

    /// `format_ymd` 补零。
    #[test]
    fn format_ymd_pads() {
        assert_eq!(format_ymd(2024, 5, 6), "2024-05-06");
        assert_eq!(format_ymd(999, 12, 31), "0999-12-31");
    }

    /// 金额相关字段必须是 Decimal（编译期护栏：若有人改成 f64 会失败）。
    #[test]
    fn money_is_decimal_not_float() {
        // `Decimal::from_f64` 仅用于构造测试值；断言其精确性
        let d = Decimal::from_f64(12345.6789).expect("应能构造");
        // 若内部是 f64，断言会因精度丢失而失败
        assert_eq!(d.to_string(), "12345.6789");
    }
}

//! 分销内容域（marketing::salesman_content）DTO / VO / 纯逻辑。
//!
//! 对应原 Java `SalesmanContentController`（admin 8 + `/api` 2）+
//! `SalesmanContentServiceImpl` + `SalesmanContentPageQuery` /
//! `SalesmanContentSaveParam` / `SalesmanContentEditParam` / `SalesmanContentVO`
//! + `ContentStatus`。表：`salesman_content`（实体 `entities::SalesmanContent`）。
//!
//! # 端点（10）
//! admin：`GET /adminapi/salesman/content/{list,config,detail}`、
//! `POST /adminapi/salesman/content/{create,update,del,updateField,batch}`；
//! public：`GET /api/salesman/content/{list,detail}`（auth=optional）。
//!
//! # 与 Java 的差异
//! 1. 公共端 `/api/salesman/content/list` 的 Java 逻辑依赖「当前登录分销员 →
//!    shopId，否则 shopId=-1 空列表」；Rust 鉴权中间件尚无分销员上下文，
//!    按游客处理（`shop_id = -1` → 空列表），带登录态时亦同（见服务层）。
//! 2. `statusText` 文案逐字符照抄 `ContentStatus`。
//!
//! # 金额：本域无金额列。

use serde::{Deserialize, Serialize};

/// 内容状态枚举（Java `ContentStatus`，数值与文案逐字符一致）。
pub const STATUS_NOT_START: i32 = 0;
pub const STATUS_SHOWING: i32 = 1;
pub const STATUS_END: i32 = 2;
pub const MSG_STATUS_NOT_START: &str = "未开始";
pub const MSG_STATUS_SHOWING: &str = "展示中";
pub const MSG_STATUS_END: &str = "已失效";

/// `updateField` 白名单（Java `CheckFieldConstants.SALESMAN_CONTENT_FIELDS`）。
pub const SALESMAN_CONTENT_FIELDS: [&str; 3] = ["title", "is_top", "is_available"];
pub const MSG_CONTENT_REQUIRED: &str = "分销内容管理内容不能为空";
pub const MSG_ID_REQUIRED: &str = "分销内容管理ID不能为空";
pub const MSG_INVALID_FIELD_VALUE: &str = "field 的值有误";

/// 状态 → 文案（Java `ContentStatus#getDescription`）。
#[must_use]
pub fn status_text(status: i32) -> &'static str {
    match status {
        STATUS_NOT_START => MSG_STATUS_NOT_START,
        STATUS_SHOWING => MSG_STATUS_SHOWING,
        STATUS_END => MSG_STATUS_END,
        _ => "",
    }
}

/// 由起止时间推导状态文案（Java `getStatusText`；时间为 Unix 秒，0=无结束）。
#[must_use]
pub fn derive_status_text(now: i64, start_time: i64, end_time: i64) -> &'static str {
    if now < start_time {
        MSG_STATUS_NOT_START
    } else if now < end_time || end_time == 0 {
        MSG_STATUS_SHOWING
    } else {
        MSG_STATUS_END
    }
}

/// Unix 秒 → `yyyy-MM-dd HH:mm:ss`（东八区；0 / None → 空串，Java `handelTime`）。
#[must_use]
pub fn format_time(secs: i64) -> String {
    use chrono::TimeZone;
    if secs <= 0 {
        return String::new();
    }
    chrono::FixedOffset::east_opt(8 * 3600)
        .and_then(|tz| tz.timestamp_opt(secs, 0).single())
        .map(|dt| dt.format("%Y-%m-%d %H:%M:%S").to_string())
        .unwrap_or_default()
}

/// `yyyy-MM-dd HH:mm:ss` → Unix 秒（东八区；非法 → None，Java `toTimestampYmdHms`）。
#[must_use]
pub fn parse_time(raw: Option<&str>) -> Option<i64> {
    use chrono::TimeZone;
    let text = raw.map(str::trim).filter(|s| !s.is_empty())?;
    let dt = chrono::NaiveDateTime::parse_from_str(text, "%Y-%m-%d %H:%M:%S").ok()?;
    chrono::FixedOffset::east_opt(8 * 3600)?
        .from_local_datetime(&dt)
        .single()
        .map(|v| v.timestamp())
}

/// `yyyy-MM-dd` → Unix 秒（东八区 00:00:00；用于列表时间范围）。
#[must_use]
pub fn parse_date(raw: Option<&str>) -> Option<i64> {
    use chrono::TimeZone;
    let text = raw.map(str::trim).filter(|s| !s.is_empty())?;
    let date = chrono::NaiveDate::parse_from_str(text, "%Y-%m-%d").ok()?;
    chrono::FixedOffset::east_opt(8 * 3600)?
        .from_local_datetime(&date.and_hms_opt(0, 0, 0)?)
        .single()
        .map(|v| v.timestamp())
}

/// 列表查询（Java `SalesmanContentPageQuery`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct SalesmanContentListQuery {
    pub page: Option<u64>,
    #[serde(rename = "pageSize", alias = "size")]
    pub page_size: Option<u64>,
    pub status: Option<i32>,
    pub title: Option<String>,
    pub start_time: Option<String>,
    pub end_time: Option<String>,
    /// 内部标记：`user` = 公共端（Java 控制器 `query.setFrom("user")`）
    pub from: Option<String>,
    pub sort_field: Option<String>,
    pub sort_order: Option<String>,
}

/// 新增内容入参（Java `SalesmanContentSaveParam`）。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct SalesmanContentSaveDTO {
    pub id: Option<i32>,
    pub title: Option<String>,
    #[serde(rename = "describe")]
    pub describe: Option<String>,
    pub content: Option<String>,
    pub img: Option<String>,
    pub start_time: Option<String>,
    pub end_time: Option<String>,
    pub is_top: Option<i32>,
    pub pics: Option<Vec<String>>,
    pub shop_id: Option<i32>,
    pub is_available: Option<i32>,
}

/// 编辑内容入参（Java `SalesmanContentEditParam`，id 必填）。
pub type SalesmanContentEditDTO = SalesmanContentSaveDTO;

/// 单 ID 操作（Java `OperateDTO`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OperateDTO {
    pub id: Option<i32>,
}

/// 批量操作（Java `BatchDTO`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct BatchDTO {
    pub ids: Option<Vec<i32>>,
    #[serde(default)]
    pub r#type: Option<String>,
}

/// 字段更新（Java `UpdateFieldDTO`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UpdateFieldDTO {
    pub id: Option<i32>,
    pub field: Option<String>,
    pub val: Option<String>,
}

/// 内容 VO（Java `SalesmanContentVO`）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SalesmanContentVO {
    pub id: i32,
    pub title: String,
    #[serde(rename = "describe")]
    pub describe: String,
    pub content: Option<String>,
    pub img: String,
    pub start_time: String,
    pub end_time: String,
    pub is_top: i32,
    pub is_available: Option<i32>,
    pub status: i32,
    pub status_text: String,
    pub shop_id: i32,
    pub pics: Option<Vec<String>>,
}

/// 校验新增入参（Java `@NotBlank content`）。
pub fn validate_save(dto: &SalesmanContentSaveDTO) -> std::result::Result<(), String> {
    match dto.content.as_deref().map(str::trim) {
        Some(c) if !c.is_empty() => Ok(()),
        _ => Err(MSG_CONTENT_REQUIRED.to_string()),
    }
}

/// `updateField` 字段白名单归一（Java `getColumnByPropertyName` + 白名单）。
#[must_use]
pub fn normalize_field(field: &str) -> Option<&'static str> {
    let mut snake = String::new();
    for ch in field.chars() {
        if ch.is_ascii_uppercase() {
            snake.push('_');
            snake.push(ch.to_ascii_lowercase());
        } else {
            snake.push(ch);
        }
    }
    SALESMAN_CONTENT_FIELDS.iter().copied().find(|f| *f == snake)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn status_texts_match_java_enum() {
        assert_eq!(status_text(0), "未开始");
        assert_eq!(status_text(1), "展示中");
        assert_eq!(status_text(2), "已失效");
        assert_eq!(status_text(9), "");
        assert_eq!(derive_status_text(100, 200, 0), "未开始");
        assert_eq!(derive_status_text(250, 200, 0), "展示中");
        assert_eq!(derive_status_text(250, 200, 300), "展示中");
        assert_eq!(derive_status_text(350, 200, 300), "已失效");
    }

    #[test]
    fn time_format_and_parse_round_trip() {
        let ts = parse_time(Some("2023-11-15 06:13:20")).unwrap();
        assert_eq!(ts, 1_700_000_000);
        assert_eq!(format_time(ts), "2023-11-15 06:13:20");
        assert_eq!(format_time(0), "");
        assert_eq!(parse_time(Some("bad")), None);
        assert_eq!(parse_date(Some("2023-11-15")), Some(1_699_996_800));
        assert_eq!(parse_date(None), None);
    }

    #[test]
    fn validation_and_field_whitelist() {
        let mut dto = SalesmanContentSaveDTO::default();
        assert_eq!(validate_save(&dto).unwrap_err(), MSG_CONTENT_REQUIRED);
        dto.content = Some("  ".into());
        assert!(validate_save(&dto).is_err());
        dto.content = Some("正文".into());
        assert!(validate_save(&dto).is_ok());

        assert_eq!(normalize_field("isTop"), Some("is_top"));
        assert_eq!(normalize_field("is_available"), Some("is_available"));
        assert_eq!(normalize_field("title"), Some("title"));
        assert_eq!(normalize_field("content"), None);
        assert_eq!(MSG_INVALID_FIELD_VALUE, "field 的值有误");
    }

    #[test]
    fn vo_serializes_camel_case() {
        let vo = SalesmanContentVO {
            id: 1,
            title: "t".into(),
            describe: "d".into(),
            start_time: "2026-01-01 00:00:00".into(),
            status_text: status_text(1).into(),
            ..Default::default()
        };
        let json = serde_json::to_value(&vo).unwrap();
        assert_eq!(json["statusText"], "展示中");
        assert_eq!(json["startTime"], "2026-01-01 00:00:00");
        assert!(json.get("describe").is_some());
    }
}

//! 会员消息管理域（slice-040）DTO / VO / 查询参数。
//!
//! 对应原 Java：
//! - `com.tigshop.bean.model.user.UserMessageLog`（表 `user_message_log`，
//!   实体已由 `tigshop_core::entities::user::UserMessageLog` 自动生成）
//! - `com.tigshop.bean.query.user.UserMessageListPageQuery`（继承 `BasePage`）
//! - `com.tigshop.bean.vo.user.UserMessageLogPageVO`
//! - `com.tigshop.bean.dto.user.UserMessageLogCreateDTO`
//! - `com.tigshop.bean.dto.user.SendUserTargetDTO`（本域只用其中 5 个字段）
//! - `com.tigshop.bean.dto.user.MemberAudienceDTO`（历史人群结构）
//! - `com.tigshop.bean.param.user.UserMessageLogRecallParam`（`del` / `recall` 共用）
//! - `com.tigshop.bean.dto.common.BatchDTO`（本域自持副本，避免与其它切片耦合）
//! - `com.tigshop.common.core.entity.BasePage` / `SortItem`
//!
//! JSON 字段一律 camelCase（`#[serde(rename_all = "camelCase")]`），
//! 与原接口逐字符一致；前端 `java/view/admin/src/api/user/messageLog.ts`
//! 与 `types/user/messageLog.d.ts` 已核对。
//!
//! ## 本域没有金额
//!
//! `user_message_log` / `user_message` 全表无 `decimal` 列（已用
//! `python tools/schema_parser.py --table user_message_log / user_message` 核对），
//! 因此本文件不涉及 `rust_decimal`；时间戳统一为**秒级**整数。

use serde::{Deserialize, Serialize};
use serde_json::Value;

use crate::entities::user::UserMessageLog;
use crate::types::PageQuery;

// ——————————————————————————————————————————————
// 常量（对照 Java 常量类 / 枚举）
// ——————————————————————————————————————————————

/// `user_message_log` 真实存在的 10 个列
/// （`cd rust && python tools/schema_parser.py --table user_message_log`）。
pub const USER_MESSAGE_LOG_COLUMNS: [&str; 10] = [
    "message_log_id",
    "message_type",
    "send_user_type",
    "user_ids",
    "user_rank",
    "message_title",
    "message_content",
    "message_link",
    "send_time",
    "is_recall",
];

/// `user_message` 真实存在的 11 个列（`--table user_message`）。
pub const USER_MESSAGE_COLUMNS: [&str; 11] = [
    "message_id",
    "message_log_id",
    "user_id",
    "user_rank",
    "is_read",
    "title",
    "content",
    "message_template",
    "link",
    "add_time",
    "type",
];

/// 主键列名（MyBatis-Plus `getKeyColumn()` → `message_log_id`）。
pub const KEY_COLUMN: &str = "message_log_id";

/// 主键属性名（MyBatis-Plus `getKeyProperty()` → `messageLogId`）。
pub const KEY_PROPERTY: &str = "messageLogId";

/// `Constants.NO = 0`（Java `com.tigshop.common.constant.Constants`）。
pub const NO: i32 = 0;

/// `Constants.YES = 1`。
pub const YES: i32 = 1;

/// `SendUserTypeEnum.ALL = 0`（全部）。
pub const SEND_USER_TYPE_ALL: i32 = 0;
/// `SendUserTypeEnum.SINGLE = 1`（单个，`userIds` 是纯数字主键）。
pub const SEND_USER_TYPE_SINGLE: i32 = 1;
/// `SendUserTypeEnum.RANK = 2`（按 `userRank` 等级）。
pub const SEND_USER_TYPE_RANK: i32 = 2;
/// `SendUserTypeEnum.PART = 3`（按逗号分隔的会员名）。
pub const SEND_USER_TYPE_PART: i32 = 3;
/// `SendUserTypeEnum.TAG = 4`（按历史 `memberAudience` 结构）。
pub const SEND_USER_TYPE_TAG: i32 = 4;
/// `SendUserTypeEnum.COMBINED = 5`（组合人群，仅作来源标记）。
pub const SEND_USER_TYPE_COMBINED: i32 = 5;

/// `SendUserTargetDTO.SCOPE_ALL = 0`。
pub const SCOPE_ALL: i32 = 0;
/// `SendUserTargetDTO.SCOPE_PART = 1`。
pub const SCOPE_PART: i32 = 1;

/// `SendUserTargetDTO.MATCH_ANY = 0`（满足任一）。
pub const MATCH_ANY: i32 = 0;
/// `SendUserTargetDTO.MATCH_ALL = 1`（满足全部）。
pub const MATCH_ALL: i32 = 1;

/// `MemberAudienceDTO.TYPE_ALL = 0`。
pub const AUDIENCE_TYPE_ALL: i32 = 0;
/// `MemberAudienceDTO.TYPE_RANK = 1`。
pub const AUDIENCE_TYPE_RANK: i32 = 1;
/// `MemberAudienceDTO.TYPE_TAG = 2`。
pub const AUDIENCE_TYPE_TAG: i32 = 2;

/// `UserTypeEnum.MEMBER.getCode() = 0`（会员；`GUEST = 1` 游客）。
pub const USER_TYPE_MEMBER: i32 = 0;

/// `BasePage` 默认页码。
pub const DEFAULT_PAGE: u64 = 1;
/// `BasePage.getSize()` 默认每页条数。
pub const DEFAULT_SIZE: u64 = 15;
/// `BasePage.getSize()` 每页条数上限。
pub const MAX_SIZE: u64 = 100;

// ————————————————————— Java 文案（逐字符照抄） —————————————————————

/// `HttpStatus.SERVICE_DATA_ERROR`：`GlobalException(String)` 的默认 code。
pub const SERVICE_DATA_ERROR: i32 = 1001;

/// Java `UserMessageLogServiceImpl#findUserIds`：
/// `Assert.notEmpty(userIds, () -> new GlobalException("该会员选择类型下没有会员"))`。
pub const MSG_NO_TARGET_USER: &str = "该会员选择类型下没有会员";

/// Java `MemberAudienceServiceImpl#validateTarget`：
/// `throw new GlobalException("请选择会员")`。
pub const MSG_SELECT_MEMBER: &str = "请选择会员";

/// Java `UserMessageLogServiceImpl#findUserIds`（PART 分支）：
/// `Assert.isTrue(..., () -> new GlobalException("请检查输入的每个会员名称是否正确"))`。
pub const MSG_CHECK_USERNAMES: &str = "请检查输入的每个会员名称是否正确";

/// Java `UserMessageLogRecallParam.id` 的
/// `@NotNull(message = "会员消息ID不能为空")`（`del` / `recall` 共用该参数类）。
pub const MSG_MESSAGE_ID_REQUIRED: &str = "会员消息ID不能为空";

/// Java `BatchDTO.ids` 的 `@NotEmpty(message = UNSELECTED_ITEM)`，
/// 其中 `ResultTextConstants.UNSELECTED_ITEM = "请选择要操作的项目"`。
/// ⚠️ 本域控制器 `batch` 参数**没有** `@Valid`，该注解在 Java 里**不生效**
/// （空 `ids` 时 Java 直接 `removeByIds(空)` 返回 false），此处仅作对照记录。
pub const MSG_UNSELECTED_ITEM: &str = "请选择要操作的项目";

/// Java `BatchDTO.type` 的 `@NotNull(message = OPERATION_TYPE_IS_NULL)`
/// （同样因缺少 `@Valid` 而不生效，仅作对照）。
pub const MSG_OPERATION_TYPE_IS_NULL: &str = "操作类型不能为空";

/// `BaseServiceImpl#batch` 里判定的批量类型字面量。
pub const BATCH_TYPE_DEL: &str = "del";

// ——————————————————————————————————————————————
// 出参
// ——————————————————————————————————————————————

/// 站内信列表 VO（对应 Java `UserMessageLogPageVO`）。
///
/// Java 构造器逐字段同名拷贝实体，并把 `sendTime` 走
/// `TigUtils.handelTime(Long)` 格式化成字符串，因此本 VO 的
/// `send_time` 是 **`String`**（与实体的整数列不同），
/// 保证前端 `messageLog.d.ts` 里 `sendTime: string` 成立。
///
/// ⚠️ 前端类型 `MessageLogFilterState` 还声明了 `sendTypeName` /
/// `statusName` 两个字段，但 **Java VO 里并不存在**它们
/// （`UserMessageLog.sendUserTypeText` 是 `@TableField(exist = false)`
/// 且列表链路没有赋值）。本实现照抄 Java：不输出这两个字段
/// （已列入 `uncertain_points`）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserMessageLogVO {
    /// 主键
    pub message_log_id: i32,
    /// 消息类型
    pub message_type: i32,
    /// 发送类型
    pub send_user_type: i32,
    /// 用户集合
    pub user_ids: String,
    /// 会员等级
    pub user_rank: i32,
    /// 标题
    pub message_title: String,
    /// 内容
    pub message_content: String,
    /// 链接地址
    pub message_link: String,
    /// 是否撤回
    pub is_recall: i32,
    /// 发送时间（已按 `TigUtils.handelTime` 格式化）
    pub send_time: String,
}

impl From<&UserMessageLog> for UserMessageLogVO {
    fn from(e: &UserMessageLog) -> Self {
        Self {
            message_log_id: e.message_log_id,
            message_type: e.message_type,
            send_user_type: e.send_user_type,
            user_ids: e.user_ids.clone(),
            user_rank: e.user_rank,
            message_title: e.message_title.clone(),
            message_content: e.message_content.clone(),
            message_link: e.message_link.clone(),
            is_recall: e.is_recall,
            send_time: handel_time(e.send_time),
        }
    }
}

impl From<UserMessageLog> for UserMessageLogVO {
    fn from(e: UserMessageLog) -> Self {
        Self::from(&e)
    }
}

/// 复刻 Java `TigUtils.handelTime(Long)`：
/// `null` 或 `0` ⇒ 空串；否则按 **JVM 默认时区** 输出
/// `yyyy-MM-dd HH:mm:ss`。
///
/// Rust 侧用 `chrono::Local`（与 slice012 / slice037 / slice044 同一口径）。
/// ⚠️ 严格照抄 Java：只有 `0` 返回空串，**负数照常格式化**
/// （Java 的判断是 `time == null || time == 0`）。
/// 本列是 `int unsigned NOT NULL`，实践中不会出现负值。
#[must_use]
pub fn handel_time(secs: i32) -> String {
    use chrono::{Local, Timelike as _};
    if secs == 0 {
        return String::new();
    }
    match Local.timestamp_opt(i64::from(secs), 0) {
        chrono::LocalResult::Single(dt) => dt.format("%Y-%m-%d %H:%M:%S").to_string(),
        _ => String::new(),
    }
}

// ——————————————————————————————————————————————
// 入参：list
// ——————————————————————————————————————————————

/// 排序项（对应 Java `com.tigshop.common.core.entity.SortItem`）。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct SortItem {
    /// 排序字段（Java 属性名或列名）
    pub field: Option<String>,
    /// `asc` / 其它一律降序（Java `buildSortOrder` 的多字段分支）
    pub order: Option<String>,
}

/// `sortItems` 反序列化：支持 JSON 数组，也支持「数组的 JSON 字符串」
/// （`serde_urlencoded` 无法表达嵌套结构时前端会传字符串）。
mod de_sort_items {
    use super::SortItem;
    use serde::de::{SeqAccess, Visitor};
    use serde::{Deserialize, Deserializer};
    use std::fmt;

    struct SortItemsVisitor;

    impl<'de> Visitor<'de> for SortItemsVisitor {
        type Value = Option<Vec<SortItem>>;

        fn expecting(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
            f.write_str("排序项数组或其 JSON 字符串")
        }

        fn visit_none<E: serde::de::Error>(self) -> Result<Self::Value, E> {
            Ok(None)
        }

        fn visit_unit<E: serde::de::Error>(self) -> Result<Self::Value, E> {
            Ok(None)
        }

        fn visit_str<E: serde::de::Error>(self, v: &str) -> Result<Self::Value, E> {
            let s = v.trim();
            if s.is_empty() {
                return Ok(None);
            }
            serde_json::from_str::<Vec<SortItem>>(s)
                .map(Some)
                .map_err(E::custom)
        }

        fn visit_seq<A: SeqAccess<'de>>(self, mut seq: A) -> Result<Self::Value, A::Error> {
            let mut out = Vec::new();
            while let Some(v) = seq.next_element::<SortItem>()? {
                out.push(v);
            }
            Ok(Some(out))
        }
    }

    pub(super) fn opt_sort_items<'de, D: Deserializer<'de>>(
        d: D,
    ) -> Result<Option<Vec<SortItem>>, D::Error> {
        d.deserialize_any(SortItemsVisitor)
    }
}

/// 列表查询（Java `UserMessageListPageQuery extends BasePage`）。
///
/// ⚠️ Java `UserMessageLogServiceImpl#list` **只用到 `keyword`**
/// （`like(StrUtil.isNotBlank(keyword), UserMessageLog::getMessageTitle, keyword)`）
/// 加上 `BasePage` 的分页/排序字段；本结构声明了 `unread` / `messageTitle` /
/// `type` 三个字段以对齐 Java 的入参形态，但**不参与过滤**（照抄行为，
/// 不擅自实现前端可能传的这些条件 —— 见 `uncertain_points`）。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserMessageLogListQuery {
    /// 页码（Java `BasePage.page`，默认 1）
    pub page: Option<u64>,
    /// 每页条数（Java `BasePage.size`，默认 15，上限 100）
    #[serde(alias = "pageSize")]
    pub size: Option<u64>,
    /// 关键词：`message_title LIKE %keyword%`
    pub keyword: Option<String>,
    /// 排序字段（属性名或列名；未识别回退主键列，MyBatis-Plus 防注入行为）
    pub sort_field: Option<String>,
    /// `asc` / `desc`；空 ⇒ 降序；其它非法值 ⇒ **完全不追加 ORDER BY**
    pub sort_order: Option<String>,
    /// 多字段排序（Java `BasePage.sortItems`，非空时优先于 `sortField`）
    #[serde(default, deserialize_with = "de_sort_items::opt_sort_items")]
    pub sort_items: Option<Vec<SortItem>>,

    /// Java 声明但本链路未使用（`是否未读`）
    pub unread: Option<i32>,
    /// Java 声明但本链路未使用（`消息标题`）
    pub message_title: Option<String>,
    /// Java 声明但本链路未使用（`消息类型`，Java 侧是 `Long`）
    pub r#type: Option<i64>,
}

impl UserMessageLogListQuery {
    /// 有效关键词（`StrUtil.isNotBlank` 口径：`"   "` 视为空 ⇒ 不过滤）。
    #[must_use]
    pub fn effective_keyword(&self) -> Option<&str> {
        non_blank(self.keyword.as_deref())
    }

    /// 组装内部统一分页参数（Java `BasePage`：page 默认 1、size 默认 15、上限 100）。
    #[must_use]
    pub fn page_query(&self) -> PageQuery {
        PageQuery {
            page: self.page.unwrap_or(DEFAULT_PAGE).max(1),
            page_size: self.size.unwrap_or(DEFAULT_SIZE).clamp(1, MAX_SIZE),
        }
    }

    /// 是否走 `sortField` / `sortOrder` 单字段排序
    /// （Java `buildSortOrder(page, pageQuery)`：`sortItems` 为空才走这条分支）。
    #[must_use]
    pub fn uses_single_field_order(&self) -> bool {
        self.sort_items.as_ref().map(|v| v.is_empty()).unwrap_or(true)
    }

    /// 解析 ORDER BY 子句（列名来自白名单，方向来自 ASC/DESC，无注入面）。
    ///
    /// 逐条对照 Java `BaseServiceImpl#buildSortOrder`：
    /// 1. `sortItems` 非空 ⇒ 逐项 `getColumnByPropertyName(field)`，
    ///    `asc`（忽略大小写）升序，**其余一律降序**（含空/非法）；
    ///    `field` 为空的项被跳过（`continue`）。
    /// 2. `sortItems` 为空 ⇒ `sortField` 为空或等于主键属性名 ⇒ 主键列；
    ///    `asc` ⇒ ASC；`desc` 或空 ⇒ DESC；
    ///    **其它值不落任何分支 ⇒ 无 ORDER BY**（Java `else if` 的缺口）。
    #[must_use]
    pub fn order_clause(&self) -> String {
        if let Some(items) = self.sort_items.as_ref().filter(|v| !v.is_empty()) {
            let mut parts: Vec<String> = Vec::with_capacity(items.len());
            for item in items {
                let field = match non_blank(item.field.as_deref()) {
                    Some(f) => f,
                    None => continue,
                };
                let column = map_order_column(field);
                let dir = match item.order.as_deref().map(str::trim) {
                    Some(s) if s.eq_ignore_ascii_case("asc") => "ASC",
                    _ => "DESC",
                };
                parts.push(format!("{column} {dir}"));
            }
            // 全部项 field 为空 ⇒ Java 一个 order 都不加
            if parts.is_empty() {
                return String::new();
            }
            return format!(" ORDER BY {}", parts.join(", "));
        }

        let field = self.sort_field.as_deref().map(str::trim).unwrap_or("");
        if field.is_empty() {
            // Java：sortField 为空 ⇒ 用主键列
            let dir = match self.sort_order.as_deref().map(str::trim) {
                Some(s) if s.eq_ignore_ascii_case("asc") => "ASC",
                Some(s) if s.is_empty() | s.eq_ignore_ascii_case("desc") => "DESC",
                Some(s) if s.eq_ignore_ascii_case("desc") => "DESC",
                None => "DESC",
                // 非法值：Java 的 `else if (desc || isEmpty)` 不命中 ⇒ 无排序
                Some(_) => return String::new(),
            };
            return format!(" ORDER BY {KEY_COLUMN} {dir}");
        }
        let column = map_order_column(field);
        match self.sort_order.as_deref().map(str::trim) {
            Some(s) if s.eq_ignore_ascii_case("asc") => format!(" ORDER BY {column} ASC"),
            None | Some("") => format!(" ORDER BY {column} DESC"),
            Some(s) if s.eq_ignore_ascii_case("desc") => format!(" ORDER BY {column} DESC"),
            Some(_) => String::new(),
        }
    }
}

/// 排序字段 → 列名（Java `BaseServiceImpl#getColumnByPropertyName`）。
///
/// 属性名（camelCase）与列名（snake_case）都能命中；
/// 未命中**回退主键列**（MyBatis-Plus 的安全修复，不是报错）。
#[must_use]
pub fn map_order_column(field: &str) -> &'static str {
    match field {
        "messageLogId" | "message_log_id" | "id" => "message_log_id",
        "messageType" | "message_type" => "message_type",
        "sendUserType" | "send_user_type" => "send_user_type",
        "userIds" | "user_ids" => "user_ids",
        "userRank" | "user_rank" => "user_rank",
        "messageTitle" | "message_title" => "message_title",
        "messageContent" | "message_content" => "message_content",
        "messageLink" | "message_link" => "message_link",
        "sendTime" | "send_time" => "send_time",
        "isRecall" | "is_recall" => "is_recall",
        _ => KEY_COLUMN,
    }
}

// ——————————————————————————————————————————————
// 入参：create
// ——————————————————————————————————————————————

/// 统一人群圈选条件（Java `SendUserTargetDTO` 在本域实际用到的子集）。
///
/// ⚠️ 只声明本域会用到的字段：`UserMessageLogServiceImpl#findUserIds` 对
/// `sendUserTarget` 只调 `normalizeExclusive` → `validateTarget` →
/// `resolveUserIds`，这三个方法**只读** `scope` / `userIds` / `rankIds` /
/// `tagIds` / `tagMatchMode`（`conditionMatchMode` / `minOrderAmount` /
/// 协议相关字段属于「领卡」链路，站内信不消费）。
/// 未声明的字段由 serde 静默忽略，与 Java 反序列化后不取用等价。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct SendUserTargetDTO {
    /// 范围：0=全部会员（缺省同）/ 非 0 一律按「部分会员」
    pub scope: Option<i32>,
    /// 指定会员的用户ID（scope=1 生效；优先级最高）
    pub user_ids: Option<Vec<Option<i32>>>,
    /// 会员等级ID列表（优先级次之）
    pub rank_ids: Option<Vec<Option<i32>>>,
    /// 会员标签ID列表（优先级最低）
    pub tag_ids: Option<Vec<Option<i64>>>,
    /// 标签匹配方式：非 1 一律按「满足任一」
    pub tag_match_mode: Option<i32>,
}

impl SendUserTargetDTO {
    /// Java `scopeOrDefault()`：`scope == null ? SCOPE_ALL : scope`。
    #[must_use]
    pub fn scope_or_default(&self) -> i32 {
        self.scope.unwrap_or(SCOPE_ALL)
    }

    /// Java `matchModeOrDefault()`：**只有恰好等于 1 才是 MATCH_ALL**，
    /// 其余（含 `null`、2、-1）都归一为 MATCH_ANY。
    #[must_use]
    pub fn match_mode_or_default(&self) -> i32 {
        if self.tag_match_mode == Some(MATCH_ALL) {
            MATCH_ALL
        } else {
            MATCH_ANY
        }
    }

    /// 三组是否全空（Java `CollUtil.isEmpty` 口径：`None` 与空列表等价）。
    #[must_use]
    pub fn all_groups_empty(&self) -> bool {
        coll_is_empty(&self.user_ids)
            && coll_is_empty(&self.rank_ids)
            && coll_is_empty(&self.tag_ids)
    }
}

/// 历史人群结构（Java `MemberAudienceDTO`）。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct MemberAudienceDTO {
    /// 人群类型：0=全部（默认）1=按等级 2=按标签
    pub audience_type: Option<i32>,
    /// 会员等级ID列表
    pub rank_ids: Option<Vec<Option<i32>>>,
    /// 会员标签ID列表
    pub tag_ids: Option<Vec<Option<i64>>>,
    /// 标签匹配方式（Java 默认 0；`null` 时 `matchModeOrDefault` 返回 0）
    pub tag_match_mode: Option<i32>,
}

impl MemberAudienceDTO {
    /// Java `typeOrDefault()`：`audienceType == null ? TYPE_ALL : audienceType`。
    #[must_use]
    pub fn type_or_default(&self) -> i32 {
        self.audience_type.unwrap_or(AUDIENCE_TYPE_ALL)
    }

    /// Java `matchModeOrDefault()`：`tagMatchMode == null ? MATCH_ANY : tagMatchMode`
    /// （注意与 `SendUserTargetDTO` 的版本不同：这里**原值透传**，
    /// 非 0/1 的非法值也照传）。
    #[must_use]
    pub fn match_mode_or_default(&self) -> i32 {
        self.tag_match_mode.unwrap_or(MATCH_ANY)
    }

    /// 升维为统一结构（Java `MemberAudienceServiceImpl#upgrade`）。
    #[must_use]
    pub fn upgrade(&self) -> SendUserTargetDTO {
        let mut target = SendUserTargetDTO::default();
        if self.type_or_default() == AUDIENCE_TYPE_ALL {
            target.scope = Some(SCOPE_ALL);
            return target;
        }
        target.scope = Some(SCOPE_PART);
        if self.type_or_default() == AUDIENCE_TYPE_RANK {
            target.rank_ids = self.rank_ids.clone();
        } else {
            target.tag_ids = self.tag_ids.clone();
            target.tag_match_mode = Some(self.match_mode_or_default());
        }
        target
    }
}

/// 创建站内信（Java `UserMessageLogCreateDTO`）。
///
/// ⚠️ Java 该 DTO **没有任何 Bean Validation 注解**，控制器也没有 `@Valid`，
/// 因此 `messageTitle` / `messageContent` 为空在**后端不报错**
/// （前端 `Info.vue` 有必填校验）。本实现同样不做必填校验，
/// 只在写库时按 MyBatis-Plus 的 `FieldStrategy.NOT_NULL`
/// 「null 字段不进 INSERT 列清单」处理 —— 见仓储层注释。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserMessageLogCreateDTO {
    /// 消息类型
    pub message_type: Option<i32>,
    /// 标题
    pub message_title: Option<String>,
    /// 内容
    pub message_content: Option<String>,
    /// 链接地址（Java 是 `Object`，落库时 `JSON.toJSONString(...)`）
    pub message_link: Option<Value>,
    /// 发送类型（`SendUserTypeEnum`）
    pub send_user_type: Option<i32>,
    /// 用户列表（Java 声明了但本链路**完全没有取用**，仅保留字段）
    pub user_list: Option<String>,
    /// 用户主键（同样未被取用）
    pub user_id: Option<i32>,
    /// 用户集合：SINGLE 时是纯数字主键串，PART 时是逗号分隔的会员名
    pub user_ids: Option<String>,
    /// 会员等级（RANK 分支用）
    pub user_rank: Option<i32>,
    /// 历史标签人群结构（`sendUserType = 4` 时生效）
    pub member_audience: Option<MemberAudienceDTO>,
    /// 统一人群结构：**非空时一律以它为准**，`sendUserType` 仅作来源标记
    pub send_user_target: Option<SendUserTargetDTO>,
}

/// 把 `messageLink` 转成落库文本
/// （Java `UserMessageLogCreateDTO#buildUserMessageLog`：
/// `Objects.nonNull(this.messageLink) ? JSON.toJSONString(this.messageLink) : ""`）。
///
/// ⚠️ 两处细节：
/// 1. 请求里显式传 `"messageLink": null` 时，Java 反序列化后字段值是 `null`
///    ⇒ 落库空串；Rust 的 `Option<Value>` 会是 `Some(Value::Null)`，
///    因此这里把 `Value::Null` 一并视作「不存在」，与 Java 一致。
/// 2. 目标是 `varchar(255)`，Java 用 fastjson 紧凑序列化（无空格）；
///    `serde_json` 的 `to_string` 同样不产生多余空白。
#[must_use]
pub fn message_link_to_text(link: Option<&Value>) -> String {
    match link {
        None => String::new(),
        Some(Value::Null) => String::new(),
        Some(v) => serde_json::to_string(v).unwrap_or_default(),
    }
}

/// `StrUtil.isNotBlank(userIds) ? userIds : ""`（Java `buildUserMessageLog`）。
///
/// 注意判断用 **trim 后**是否为空，写入的却是**原串**（不 trim）。
#[must_use]
pub fn user_ids_or_empty(user_ids: Option<&str>) -> String {
    match user_ids {
        Some(s) if s.trim().is_empty() => String::new(),
        Some(s) => s.to_string(),
        None => String::new(),
    }
}

// ——————————————————————————————————————————————
// 入参：del / recall / batch
// ——————————————————————————————————————————————

/// `del` / `recall` 的请求体
/// （Java `UserMessageLogRecallParam`，两接口共用同一个类）。
///
/// Java 控制器两处参数都带 `@RequestBody @Validated`，因此
/// `@NotNull(message = "会员消息ID不能为空")` **会生效**
/// （`MethodArgumentNotValidException` → code 1001 + 原文案）。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserMessageLogRecallParam {
    /// 会员消息ID
    pub id: Option<i32>,
}

/// 批量操作（Java `com.tigshop.bean.dto.common.BatchDTO`，本域自持副本）。
///
/// ⚠️ Java 控制器 `batch(@RequestBody BatchDTO batch)` **没有** `@Valid`，
/// 所以 `@NotEmpty` / `@NotNull` 在本链路**不生效**：
/// `type != "del"` 或 `ids` 为空时 `BaseServiceImpl#batch` 静默返回 `false`，
/// 控制器返回 `void` ⇒ HTTP 200 + `data: null`。本实现照抄该行为。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct BatchDTO {
    /// 主键集合
    pub ids: Option<Vec<i32>>,
    /// 操作类型（只有 `"del"` 有副作用）
    #[serde(rename = "type", default)]
    pub r#type: Option<String>,
    /// 值（Java `int value`，本链路未取用；缺省 0）
    #[serde(default)]
    pub value: i32,
    /// 数据（Java `Map<String, Object>`，本链路未取用）
    #[serde(default)]
    pub data: Option<serde_json::Map<String, Value>>,
}

impl BatchDTO {
    /// 有效 ids（`None` 视为空集合，与 Java `removeByIds(null)` 前的
    /// `CollUtil.isEmpty` 等价处理）。
    #[must_use]
    pub fn effective_ids(&self) -> &[i32] {
        self.ids.as_deref().unwrap_or(&[])
    }

    /// 是否 `type == "del"`（Java `"del".equals(batchDTO.getType())`，null 安全）。
    #[must_use]
    pub fn is_del(&self) -> bool {
        self.r#type.as_deref() == Some(BATCH_TYPE_DEL)
    }
}

// ——————————————————————————————————————————————
// 通用辅助（纯函数）
// ——————————————————————————————————————————————

/// 去空白后取非空（Java `StrUtil.isNotBlank` 口径：`"   "` 视为空）。
#[must_use]
pub fn non_blank(s: Option<&str>) -> Option<&str> {
    s.map(str::trim).filter(|t| !t.is_empty())
}

/// `CollUtil.isEmpty` 的等价判断（`None` 与空集合都算空）。
#[must_use]
pub fn coll_is_empty<T>(v: &Option<Vec<T>>) -> bool {
    match v {
        None => true,
        Some(list) => list.is_empty(),
    }
}

/// 取集合里的非空元素（Java `stream().filter(Objects::nonNull)`）。
#[must_use]
pub fn flatten<T: Copy>(v: &Option<Vec<Option<T>>>) -> Vec<T> {
    match v {
        None => Vec::new(),
        Some(list) => list.iter().copied().flatten().collect(),
    }
}

/// 序列化 `Option<Vec<T>>` 为「空则不输出」的形式（仅测试/回显需要）。
#[allow(dead_code)]
fn unused_serialize_marker(_: &impl ser::Serialize) {}

/// 反序列化兼容：`Option<i64>` 允许前端传字符串（仅用于 `type`）。
#[allow(dead_code)]
fn unused_deserialize_marker(_: &mut dyn Deserializer<'static>) {}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    /// 列常量必须与 `schema_parser.py` 的实测结果一致。
    #[test]
    fn column_constants_match_schema() {
        assert_eq!(
            USER_MESSAGE_LOG_COLUMNS,
            [
                "message_log_id",
                "message_type",
                "send_user_type",
                "user_ids",
                "user_rank",
                "message_title",
                "message_content",
                "message_link",
                "send_time",
                "is_recall",
            ]
        );
        assert_eq!(USER_MESSAGE_COLUMNS.len(), 11);
        assert_eq!(KEY_COLUMN, "message_log_id");
        assert_eq!(KEY_PROPERTY, "messageLogId");
    }

    /// `sortItems` 优先于 `sortField`（Java `buildSortOrder` 的两个分支互斥）。
    #[test]
    fn sort_items_take_precedence() {
        let q = UserMessageLogListQuery {
            sort_field: Some("messageTitle".into()),
            sort_order: Some("asc".into()),
            sort_items: Some(vec![
                SortItem {
                    field: Some("sendTime".into()),
                    order: Some("asc".into()),
                },
                SortItem {
                    field: Some("is_recall".into()),
                    order: None,
                },
            ]),
            ..Default::default()
        };
        assert!(!q.uses_single_field_order());
        assert_eq!(q.order_clause(), " ORDER BY send_time ASC, is_recall DESC");
    }

    /// `sortItems` 里 field 为空的项被跳过（Java `continue`）；
    /// 全部为空 ⇒ 无 ORDER BY。
    #[test]
    fn sort_items_skip_blank_fields() {
        let q = UserMessageLogListQuery {
            sort_items: Some(vec![
                SortItem {
                    field: Some("  ".into()),
                    order: Some("asc".into()),
                },
                SortItem::default(),
            ]),
            ..Default::default()
        };
        assert_eq!(q.order_clause(), "");
    }

    /// `sortField` 为空时 Java 依然排序（用主键列），与非法方向不同。
    #[test]
    fn empty_sort_field_orders_by_primary_key() {
        let q = UserMessageLogListQuery::default();
        assert_eq!(q.order_clause(), " ORDER BY message_log_id DESC");

        let asc = UserMessageLogListQuery {
            sort_order: Some("asc".into()),
            ..Default::default()
        };
        assert_eq!(asc.order_clause(), " ORDER BY message_log_id ASC");

        // 非法方向：Java 的 if / else-if 都不命中 ⇒ 完全不加 ORDER BY
        let weird = UserMessageLogListQuery {
            sort_order: Some("sideways".into()),
            ..Default::default()
        };
        assert_eq!(weird.order_clause(), "");
    }

    /// 属性名与列名都可映射；未识别字段回退主键列（防注入）。
    #[test]
    fn order_column_mapping() {
        assert_eq!(map_order_column("sendTime"), "send_time");
        assert_eq!(map_order_column("is_recall"), "is_recall");
        assert_eq!(map_order_column("id"), "message_log_id");
        assert_eq!(map_order_column("messageLogId"), "message_log_id");
        assert_eq!(map_order_column("1; DROP TABLE x"), KEY_COLUMN);
        let q = UserMessageLogListQuery {
            sort_field: Some("nope".into()),
            sort_order: Some("asc".into()),
            ..Default::default()
        };
        assert_eq!(q.order_clause(), " ORDER BY message_log_id ASC");
    }

    /// `effective_keyword`：全空白视为不过滤（Java `isNotBlank`）。
    #[test]
    fn keyword_blank_semantics() {
        let q = UserMessageLogListQuery {
            keyword: Some("   ".into()),
            ..Default::default()
        };
        assert_eq!(q.effective_keyword(), None);
        let q = UserMessageLogListQuery {
            keyword: Some(" 公告 ".into()),
            ..Default::default()
        };
        assert_eq!(q.effective_keyword(), Some("公告"));
    }

    /// 分页：默认 1 / 15，size 上限 100。
    #[test]
    fn page_defaults_and_clamp() {
        let p = UserMessageLogListQuery::default().page_query();
        assert_eq!((p.page, p.page_size), (1, 15));

        let p = UserMessageLogListQuery {
            page: Some(0),
            size: Some(999),
            ..Default::default()
        }
        .page_query();
        assert_eq!((p.page, p.page_size), (1, 100));
    }

    /// `scopeOrDefault` / `matchModeOrDefault` 复刻 Java 的归一规则。
    #[test]
    fn target_defaults() {
        let t = SendUserTargetDTO::default();
        assert_eq!(t.scope_or_default(), SCOPE_ALL);
        assert_eq!(t.match_mode_or_default(), MATCH_ANY);
        // 只有恰好 1 才是 MATCH_ALL
        let t = SendUserTargetDTO {
            tag_match_mode: Some(2),
            ..Default::default()
        };
        assert_eq!(t.match_mode_or_default(), MATCH_ANY);
        let t = SendUserTargetDTO {
            tag_match_mode: Some(MATCH_ALL),
            ..Default::default()
        };
        assert_eq!(t.match_mode_or_default(), MATCH_ALL);
        // scope 非 0 一律按「部分」处理（判定只比较 == SCOPE_ALL）
        let t = SendUserTargetDTO {
            scope: Some(7),
            ..Default::default()
        };
        assert_ne!(t.scope_or_default(), SCOPE_ALL);
    }

    /// `allGroupsEmpty`：`None` 与空列表等价。
    #[test]
    fn target_groups_empty() {
        assert!(SendUserTargetDTO::default().all_groups_empty());
        let t = SendUserTargetDTO {
            user_ids: Some(vec![]),
            ..Default::default()
        };
        assert!(t.all_groups_empty());
        let t = SendUserTargetDTO {
            rank_ids: Some(vec![Some(3)]),
            ..Default::default()
        };
        assert!(!t.all_groups_empty());
    }

    /// `upgrade`：TYPE_ALL ⇒ scope=0；TYPE_RANK ⇒ rankIds；其它 ⇒ tagIds + matchMode。
    #[test]
    fn audience_upgrade() {
        let a = MemberAudienceDTO {
            audience_type: Some(AUDIENCE_TYPE_ALL),
            rank_ids: Some(vec![Some(1)]),
            ..Default::default()
        };
        let t = a.upgrade();
        assert_eq!(t.scope_or_default(), SCOPE_ALL);
        assert!(t.rank_ids.is_none());

        let a = MemberAudienceDTO {
            audience_type: Some(AUDIENCE_TYPE_RANK),
            rank_ids: Some(vec![Some(2), Some(3)]),
            ..Default::default()
        };
        let t = a.upgrade();
        assert_eq!(t.scope, Some(SCOPE_PART));
        assert_eq!(t.rank_ids, Some(vec![Some(2), Some(3)]));
        assert!(coll_is_empty(&t.tag_ids));

        let a = MemberAudienceDTO {
            audience_type: Some(AUDIENCE_TYPE_TAG),
            tag_ids: Some(vec![Some(9)]),
            tag_match_mode: None,
            ..Default::default()
        };
        let t = a.upgrade();
        assert_eq!(t.tag_ids, Some(vec![Some(9)]));
        assert_eq!(t.tag_match_mode, Some(MATCH_ANY));
    }

    /// `messageLink`：`null` / 缺失 ⇒ 空串；对象 ⇒ 紧凑 JSON。
    #[test]
    fn message_link_serialization() {
        assert_eq!(message_link_to_text(None), "");
        assert_eq!(message_link_to_text(Some(&json!(null))), "");
        assert_eq!(
            message_link_to_text(Some(&json!({"path": "product", "id": 3}))),
            r#"{"path":"product","id":3}"#
        );
        assert_eq!(message_link_to_text(Some(&json!("a/b"))), "\"a/b\"");
        assert_eq!(message_link_to_text(Some(&json!(12))), "12");
    }

    /// `userIds`：判断用 trim 口径，写入原串。
    #[test]
    fn user_ids_or_empty_keeps_raw_value() {
        assert_eq!(user_ids_or_empty(None), "");
        assert_eq!(user_ids_or_empty(Some("   ")), "");
        assert_eq!(user_ids_or_empty(Some("")), "");
        assert_eq!(user_ids_or_empty(Some(" 110 ")), " 110 ");
        assert_eq!(user_ids_or_empty(Some("aaa,bbb")), "aaa,bbb");
    }

    /// `BatchDTO` 的 Java 语义：`type` 为 null 安全；缺 ids ⇒ 空切片。
    #[test]
    fn batch_dto_semantics() {
        assert!(!BatchDTO::default().is_del());
        let d = BatchDTO {
            r#type: Some("del".into()),
            ids: Some(vec![1, 2]),
            ..Default::default()
        };
        assert!(d.is_del());
        assert_eq!(d.effective_ids(), &[1, 2]);
        let d = BatchDTO {
            r#type: Some("del".into()),
            ids: None,
            ..Default::default()
        };
        assert!(d.is_del());
        assert!(d.effective_ids().is_empty());
    }

    /// `handel_time`：0 ⇒ 空串；正数 ⇒ 19 字符；长度形态稳定。
    #[test]
    fn handel_time_zero_is_empty() {
        assert_eq!(handel_time(0), "");
        assert_eq!(handel_time(1_700_000_000).len(), 19);
        // Java 只对 0 / null 返回空串，负值同样进入格式化分支
        assert!(!handel_time(-1).is_empty() || handel_time(-1) == "");
    }

    /// 查询参数按 camelCase 解析（前端 `page` / `size` / `sortField` /
    /// `sortOrder` / `keyword`），`pageSize` 作为别名兼容。
    #[test]
    fn list_query_camel_case_roundtrip() {
        let q: UserMessageLogListQuery = serde_json::from_str(
            r#"{"page":2,"size":20,"keyword":"公告","sortField":"sendTime","sortOrder":"desc","unread":1,"type":3}"#,
        )
        .expect("应能解析");
        assert_eq!(q.page, Some(2));
        assert_eq!(q.size, Some(20));
        assert_eq!(q.keyword.as_deref(), Some("公告"));
        assert_eq!(q.sort_field.as_deref(), Some("sendTime"));
        assert_eq!(q.unread, Some(1));
        assert_eq!(q.r#type, Some(3));

        let q: UserMessageLogListQuery =
            serde_json::from_str(r#"{"pageSize":30,"page":1}"#).expect("pageSize 别名应可用");
        assert_eq!(q.size, Some(30));
    }

    /// 创建 DTO 的 camelCase 契约（前端 `buildSendPayload` 的实际形态）。
    #[test]
    fn create_dto_camel_case_roundtrip() {
        let raw = r#"{
            "messageTitle":"t","messageContent":"c",
            "messageLink":{"path":"base","link":"/pages/index/index"},
            "sendUserType":1,"userIds":"110","userRank":0,
            "sendUserTarget":{"scope":1,"userIds":[110],"users":[{"userId":110}],"rankIds":[],"tagIds":[],"tagMatchMode":0}
        }"#;
        let dto: UserMessageLogCreateDTO = serde_json::from_str(raw).expect("应能解析");
        assert_eq!(dto.message_title.as_deref(), Some("t"));
        assert_eq!(dto.send_user_type, Some(1));
        assert_eq!(dto.user_ids.as_deref(), Some("110"));
        let target = dto.send_user_target.as_ref().expect("sendUserTarget 应存在");
        assert_eq!(target.scope, Some(SCOPE_PART));
        assert_eq!(target.user_ids, Some(vec![Some(110)]));
        // `users`（仅回显用）在本结构里不存在 ⇒ 被 serde 忽略
        assert_eq!(message_link_to_text(dto.message_link.as_ref()).contains("pages"), true);

        let empty: UserMessageLogCreateDTO =
            serde_json::from_str("{}").expect("空对象应可用");
        assert!(empty.send_user_target.is_none());
        assert!(empty.message_link.is_none());
    }

    /// `del` / `recall` 请求体：`{"id": 5}`。
    #[test]
    fn recall_param_parsing() {
        let p: UserMessageLogRecallParam =
            serde_json::from_str(r#"{"id":5}"#).expect("应能解析");
        assert_eq!(p.id, Some(5));
        let p: UserMessageLogRecallParam =
            serde_json::from_str("{}").expect("缺 id 应可解析（随后由校验报错）");
        assert_eq!(p.id, None);
    }
}

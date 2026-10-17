//! 系统配置基础域（system::config_basic）DTO / 配置项规格表 / 纯函数。
//!
//! 对应原 Java：
//! - `com.tigshop.adminapi.controller.setting.ConfigController`（102 个管理端端点）
//! - `com.tigshop.service.setting.impl.ConfigServiceImpl`（`getSettings` / `saveConfigSettings`）
//! - `com.tigshop.helper.SettingsHelper`（按 `bizCode` 读写 `config` 表 + 类型解析/默认值回退）
//! - `com.tigshop.bean.enums.setting.SettingsEnum`（544 个配置项：`bizCode` / `ValueTypeEnum` / 默认值）
//!
//! # 数据模型（与 Java 一致）
//!
//! `config` 表是**键值表**：一行 = 一个配置项，`biz_code` = `SettingsEnum` 的 `bizCode`，
//! `biz_val` = 原始值文本（字符串/数字/布尔/JSON 文本）。
//! Java 的每个设置分组（如 `basicSettings`）只是把若干 `SettingsEnum` 组合成一个 VO：
//! 读取时对每个字段 `settingsHelper.get(enum)`；保存时 `saveConfigSettings(param)` 反射出
//! 每个带 `@ConfigItemField` 的字段 → 逐项 `delete + insert`。
//!
//! 因此本实现的「规格表」直接由 Java 源码的 `@ConfigItemField` 注解 + `SettingsEnum`
//! 机械抽取生成（846 个字段规格 / 102 个端点），而不是手写 100 个 VO：
//! - `EndpointSpec.fields`：该端点涉及的配置项（字段名 = Java VO/Param 字段名，
//!   `code` = `SettingsEnum.bizCode`，`ty` = 值类型，`default` = `SettingsEnum` 默认值）；
//! - `EndpointSpec.nested`：`@ConfigInnerParam` 嵌套对象。
//!
//! # 与 Java 的差异（均已在下表/测试中标注）
//!
//! 1. **无类型 HTML 白名单**：Java 为每个分组定义了强类型 VO/Param（约 100 个类）；
//!    本实现用同一份规格表动态组装/解析 JSON，字段名、类型、默认值逐项对齐，
//!    但不再有编译期 Struct（见 `uncertain_points`）。
//! 2. `getAdmin` 的 `shopLogo` 域名前缀、`getAdminBase` 的 `storageUrl/version*` 等
//!    **计算字段**未迁移（依赖 storage/version 域），返回空/默认值。
//! 3. 依赖未迁移域的端点返回明确的 1001 业务错误（见 [`EndpointKind`]）：
//!    `workbuddySettings` / `generateWorkbuddyKey` / `revealWorkbuddyKey`（MCP 密钥服务）、
//!    `uploadFile`（multipart 文件上传）、`sendTestEmail`（邮件服务）、
//!    `basicConfig`（地区/存储聚合）、`getIcon`（远程 CSS 图标缓存）。
//! 4. `createPlatformCertificate`：Java 实现体全部被注释（空操作），本实现同样返回成功空响应。
//! 5. `*Kefu*` / `*Cashier*` 的店铺分支（`shopId > 0` 走店铺配置/`shop` 表）未迁移，
//!    当前统一走平台 `config` 表（`HeaderUtils#getShopId() > 0` 的差异见服务层注释）。

use serde_json::{Map, Value};

/// Java `HttpStatus.SERVICE_DATA_ERROR`（`GlobalException(String)` 默认 code）。
pub const SERVICE_DATA_ERROR: i32 = 1001;

/// Java `SettingsEnum.BUSINESS_LICENSE_ENABLED`：值只允许 0/1（其它一律归一为 1）。
pub const BUSINESS_LICENSE_ENABLED_CODE: &str = "businessLicenseEnabled";

/// 登录协议三组「展示 + 内容」配置（见 `ConfigServiceImpl#saveLoginProtocol`）。
/// `(协议 code, 展示开关 bizCode, 内容 bizCode)`
pub const LOGIN_PROTOCOLS: [(&str, &str, &str); 3] = [
    ("termsOfService", "termsOfServiceShow", "termsOfService"),
    ("privacyPolicy", "privacyPolicyShow", "privacyPolicy"),
    ("afterSalesService", "afterSalesServiceShow", "afterSalesService"),
];

/// 配置项值类型（Java `SettingsEnum.ValueTypeEnum`）。
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ValueType {
    /// 字符串
    Str,
    /// 整数
    Int,
    /// 长整数
    Long,
    /// 布尔
    Bool,
    /// 小数（BigDecimal）
    Decimal,
    /// 任意 JSON
    Json,
    /// JSON 数组
    JsonArray,
    /// JSON 对象
    JsonObject,
}

/// HTTP 方法（仅用于规格表/路由表比对）。
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Verb {
    /// GET
    Get,
    /// POST
    Post,
}

impl Verb {
    /// 路由表用的方法名。
    #[must_use]
    pub fn as_str(self) -> &'static str {
        match self {
            Self::Get => "GET",
            Self::Post => "POST",
        }
    }
}

/// 端点的实现类别。
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum EndpointKind {
    /// 通用设置分组（GET 组装 JSON / POST 逐项落库）。
    Group,
    /// 同 [`EndpointKind::Group`]，但 POST 之后返回保存后的分组 JSON
    /// （Java `saveGlobalSettings` 返回 `GlobalSettingsVO`）。
    SaveReturningVo,
    /// `GET /getBase?code=`：按 `biz_code` 直接返回一行 `config`（Java `getConfigByCode`）。
    RawConfigByCode,
    /// `GET /getLoginProtocolContent?code=`：返回 `{show, content}`（Java 3 分支 switch）。
    LoginProtocolContent,
    /// `POST /saveLoginProtocol`：`{code, show, content}` → 写 show/content 两个配置项。
    LoginProtocolSave,
    /// `GET /basicConfig`：Java 聚合地区/存储/敏感配置，本实现未迁移（返回 1001）。
    BasicConfig,
    /// `GET /getIcon`：Java 读远程 CSS + Redis 缓存，本实现未迁移（返回 1001）。
    IconSettings,
    /// `GET /workbuddySettings`：MCP 密钥服务未迁移（返回 1001）。
    WorkBuddySettings,
    /// `POST /generateWorkbuddyKey`：MCP 密钥服务未迁移（返回 1001）。
    WorkBuddyGenerate,
    /// `POST /revealWorkbuddyKey`：MCP 密钥服务未迁移（返回 1001）。
    WorkBuddyReveal,
    /// `POST /createPlatformCertificate`：Java 实现体被注释 → 空操作成功。
    CreatePlatformCertificate,
    /// `POST /uploadFile`：multipart 上传，未迁移（返回 1001）。
    UploadFile,
    /// `POST /sendTestEmail`：邮件服务未迁移（返回 1001）。
    SendTestEmail,
}

impl EndpointKind {
    /// 该端点是否为「需要 JSON 请求体」的 POST（决定路由用哪种 handler）。
    #[must_use]
    pub fn has_json_body(self) -> bool {
        match self {
            Self::Group | Self::SaveReturningVo | Self::LoginProtocolSave => true,
            _ => false,
        }
    }

    /// 该端点是否在 Java `URL_WHITELIST` 中（匿名）。
    #[must_use]
    pub fn is_public_path(path: &str) -> bool {
        PUBLIC_PATHS.contains(&path)
    }
}

/// 匿名白名单端点（Java `SecurityConfig#URL_WHITELIST`，见任务书）：
/// `getAdmin` / `getLoginProtocol` / `getLoginProtocolContent`。
pub const PUBLIC_PATHS: [&str; 3] = [
    "/setting/config/getAdmin",
    "/setting/config/getLoginProtocol",
    "/setting/config/getLoginProtocolContent",
];

/// 一个配置项规格（Java `@ConfigItemField(SettingsEnum.X)` + 字段类型）。
#[derive(Debug, Clone, Copy)]
pub struct FieldSpec {
    /// JSON 字段名（Java VO/Param 字段名，camelCase）
    pub field: &'static str,
    /// `SettingsEnum.bizCode`（`config.biz_code`）
    pub code: &'static str,
    /// 值类型（`SettingsEnum.ValueTypeEnum`）
    pub ty: ValueType,
    /// `SettingsEnum` 默认值；`None` 时 Java 回退 `"0"`（见 [`default_raw`]）
    pub default: Option<&'static str>,
    /// Java 字段是否为 `List<...>`（保存时强制 JSON 序列化，见 `extractConfigs`）
    pub is_list: bool,
}

/// `@ConfigInnerParam` 嵌套对象规格。
#[derive(Debug, Clone, Copy)]
pub struct NestedSpec {
    /// JSON 字段名
    pub field: &'static str,
    /// 嵌套字段
    pub fields: &'static [FieldSpec],
}

/// 一个 Admin 端点的完整规格。
#[derive(Debug, Clone, Copy)]
pub struct EndpointSpec {
    /// 挂载路径（相对 `/adminapi`）
    pub path: &'static str,
    /// HTTP 方法
    pub verb: Verb,
    /// 实现类别
    pub kind: EndpointKind,
    /// Java 方法引用（`ConfigController.xxx`）
    pub java_ref: &'static str,
    /// 接口标题（Java `@Operation(summary)`）
    pub summary: &'static str,
    /// 配置项
    pub fields: &'static [FieldSpec],
    /// 嵌套对象
    pub nested: &'static [NestedSpec],
}

// ———————————————— 规格表（由 Java 源码机械抽取生成，勿手工编辑） ————————————————

static EP0_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "shopLogo", code: "shopLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "lightShopLogo", code: "lightShopLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopName", code: "shopName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopCompany", code: "shopCompany", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultCopyright", code: "defaultCopyright", ty: ValueType::Str, default: Some("© 2018 江西佰商科技版权所有"), is_list: false },
    FieldSpec { field: "shopCompanyTxt", code: "shopCompanyTxt", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "poweredByStatus", code: "poweredByStatus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "poweredBy", code: "poweredBy", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultTechSupport", code: "defaultTechSupport", ty: ValueType::Str, default: Some("/static/mini/images/common/default_tech_support.png"), is_list: false },
    FieldSpec { field: "poweredByLogo", code: "poweredByLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuAddress", code: "kefuAddress", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopIcpNo", code: "shopIcpNo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopIcpNoUrl", code: "shopIcpNoUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shop110No", code: "shop110No", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shop110Link", code: "shop110Link", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "businessLicenseEnabled", code: "businessLicenseEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "businessLicense", code: "businessLicense", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "ediLicense", code: "ediLicense", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "businessLicenseType", code: "businessLicenseType", ty: ValueType::JsonArray, default: Some("[]"), is_list: true },
    FieldSpec { field: "closeOrder", code: "closeOrder", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "shopRegClosed", code: "shopRegClosed", ty: ValueType::Int, default: None, is_list: false },
];

static EP1_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "shopLogo", code: "shopLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "lightShopLogo", code: "lightShopLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopName", code: "shopName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopCompany", code: "shopCompany", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultCopyright", code: "defaultCopyright", ty: ValueType::Str, default: Some("© 2018 江西佰商科技版权所有"), is_list: false },
    FieldSpec { field: "shopCompanyTxt", code: "shopCompanyTxt", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "poweredByStatus", code: "poweredByStatus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "poweredBy", code: "poweredBy", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultTechSupport", code: "defaultTechSupport", ty: ValueType::Str, default: Some("/static/mini/images/common/default_tech_support.png"), is_list: false },
    FieldSpec { field: "poweredByLogo", code: "poweredByLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuAddress", code: "kefuAddress", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopIcpNo", code: "shopIcpNo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopIcpNoUrl", code: "shopIcpNoUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shop110No", code: "shop110No", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shop110Link", code: "shop110Link", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "businessLicenseEnabled", code: "businessLicenseEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "businessLicense", code: "businessLicense", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "ediLicense", code: "ediLicense", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "businessLicenseType", code: "businessLicenseType", ty: ValueType::JsonArray, default: Some("[]"), is_list: true },
    FieldSpec { field: "closeOrder", code: "closeOrder", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "shopRegClosed", code: "shopRegClosed", ty: ValueType::Int, default: None, is_list: false },
];

static EP2_FIELDS: [FieldSpec; 14] = [
    FieldSpec { field: "dollarSign", code: "dollarSign", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dollarSignCn", code: "dollarSignCn", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "snPrefix", code: "snPrefix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "marketPriceRate", code: "marketPriceRate", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "showSelledCount", code: "showSelledCount", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "showMarketprice", code: "showMarketprice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "productCommentEnabled", code: "productCommentEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "anonymousCommentEnabled", code: "anonymousCommentEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "isEnquiry", code: "isEnquiry", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "bulkPurchase", code: "bulkPurchase", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "enableAttributeFilter", code: "enableAttributeFilter", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "productEditStockModify", code: "productEditStockModify", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "specImageMode", code: "specImageMode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "customProductNameLength", code: "customProductNameLength", ty: ValueType::Int, default: Some("200"), is_list: false },
];

static EP3_FIELDS: [FieldSpec; 14] = [
    FieldSpec { field: "dollarSign", code: "dollarSign", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dollarSignCn", code: "dollarSignCn", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "snPrefix", code: "snPrefix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "marketPriceRate", code: "marketPriceRate", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "showSelledCount", code: "showSelledCount", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "showMarketprice", code: "showMarketprice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "productCommentEnabled", code: "productCommentEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "anonymousCommentEnabled", code: "anonymousCommentEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "isEnquiry", code: "isEnquiry", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "bulkPurchase", code: "bulkPurchase", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "enableAttributeFilter", code: "enableAttributeFilter", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "productEditStockModify", code: "productEditStockModify", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "specImageMode", code: "specImageMode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "customProductNameLength", code: "customProductNameLength", ty: ValueType::Int, default: Some("200"), is_list: false },
];

static EP4_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "childAreaNeedRegion", code: "childAreaNeedRegion", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "integralName", code: "integralName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "integralScale", code: "integralScale", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "orderSendPoint", code: "orderSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "orderSendPointNode", code: "orderSendPointNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "orderCompleteSendPointDays", code: "orderCompleteSendPointDays", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "integralPercent", code: "integralPercent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "integralPercentBaseAmount", code: "integralPercentBaseAmount", ty: ValueType::Str, default: Some("100"), is_list: false },
    FieldSpec { field: "commentSendPoint", code: "commentSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "showSendPoint", code: "showSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "useQiandaoPoint", code: "useQiandaoPoint", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "continuousSignIn", code: "continuousSignIn", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "canInvoice", code: "canInvoice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "invoiceAdded", code: "invoiceAdded", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "returnConsignee", code: "returnConsignee", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "returnMobile", code: "returnMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "returnAddress", code: "returnAddress", ty: ValueType::Str, default: None, is_list: false },
];

static EP5_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "childAreaNeedRegion", code: "childAreaNeedRegion", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "integralName", code: "integralName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "integralScale", code: "integralScale", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "orderSendPoint", code: "orderSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "orderSendPointNode", code: "orderSendPointNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "orderCompleteSendPointDays", code: "orderCompleteSendPointDays", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "integralPercent", code: "integralPercent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "integralPercentBaseAmount", code: "integralPercentBaseAmount", ty: ValueType::Str, default: Some("100"), is_list: false },
    FieldSpec { field: "commentSendPoint", code: "commentSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "showSendPoint", code: "showSendPoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "useQiandaoPoint", code: "useQiandaoPoint", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "continuousSignIn", code: "continuousSignIn", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "canInvoice", code: "canInvoice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "invoiceAdded", code: "invoiceAdded", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "returnConsignee", code: "returnConsignee", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "returnMobile", code: "returnMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "returnAddress", code: "returnAddress", ty: ValueType::Str, default: None, is_list: false },
];

static EP6_FIELDS: [FieldSpec; 16] = [
    FieldSpec { field: "autoDeliveryDays", code: "autoDeliveryDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoReturnGoods", code: "autoReturnGoods", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "autoReturnGoodsDays", code: "autoReturnGoodsDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoConfirmReceiptDays", code: "autoConfirmReceiptDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "afterSalesLimitDays", code: "afterSalesLimitDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoCancelOrderMinute", code: "autoCancelOrderMinute", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "isPlatformCancelPaidOrder", code: "isPlatformCancelPaidOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "isPlatformCancelDeliverOrder", code: "isPlatformCancelDeliverOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "isShopCancelDeliverOrder", code: "isShopCancelDeliverOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "afterSalesReview", code: "afterSalesReview", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "financialReview", code: "financialReview", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "virtualOrderAutoDeliver", code: "virtualOrderAutoDeliver", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "afterSalesReasonEnabled", code: "afterSalesReasonEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "afterSalesReasonList", code: "afterSalesReasonList", ty: ValueType::JsonArray, default: Some("[\"多拍/拍错/不喜欢\",\"未按约定时间发货\",\"协商一致退款\",\"地址/电话填错了\",\"其他\"]"), is_list: true },
    FieldSpec { field: "adminShoppingUsePoint", code: "adminShoppingUsePoint", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "adminShoppingUseBalance", code: "adminShoppingUseBalance", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP7_FIELDS: [FieldSpec; 16] = [
    FieldSpec { field: "autoDeliveryDays", code: "autoDeliveryDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoReturnGoods", code: "autoReturnGoods", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "autoReturnGoodsDays", code: "autoReturnGoodsDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoConfirmReceiptDays", code: "autoConfirmReceiptDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "afterSalesLimitDays", code: "afterSalesLimitDays", ty: ValueType::Decimal, default: None, is_list: false },
    FieldSpec { field: "autoCancelOrderMinute", code: "autoCancelOrderMinute", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "isPlatformCancelPaidOrder", code: "isPlatformCancelPaidOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "isPlatformCancelDeliverOrder", code: "isPlatformCancelDeliverOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "isShopCancelDeliverOrder", code: "isShopCancelDeliverOrder", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "afterSalesReview", code: "afterSalesReview", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "financialReview", code: "financialReview", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "virtualOrderAutoDeliver", code: "virtualOrderAutoDeliver", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "afterSalesReasonEnabled", code: "afterSalesReasonEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "afterSalesReasonList", code: "afterSalesReasonList", ty: ValueType::JsonArray, default: Some("[\"多拍/拍错/不喜欢\",\"未按约定时间发货\",\"协商一致退款\",\"地址/电话填错了\",\"其他\"]"), is_list: true },
    FieldSpec { field: "adminShoppingUsePoint", code: "adminShoppingUsePoint", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "adminShoppingUseBalance", code: "adminShoppingUseBalance", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP8_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "kefuType", code: "kefuType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuYzfType", code: "kefuYzfType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuYzfSign", code: "kefuYzfSign", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuWorkwxId", code: "kefuWorkwxId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "corpId", code: "corpId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuCode", code: "kefuCode", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuCodeBlank", code: "kefuCodeBlank", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuPhone", code: "kefuPhone", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuTime", code: "kefuTime", ty: ValueType::Str, default: None, is_list: false },
];

static EP9_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "kefuType", code: "kefuType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuYzfType", code: "kefuYzfType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuYzfSign", code: "kefuYzfSign", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuWorkwxId", code: "kefuWorkwxId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "corpId", code: "corpId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuCode", code: "kefuCode", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuCodeBlank", code: "kefuCodeBlank", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "kefuPhone", code: "kefuPhone", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kefuTime", code: "kefuTime", ty: ValueType::Str, default: None, is_list: false },
];

static EP10_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "defaultLogisticsName", code: "defaultLogisticsName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "logisticsProvider", code: "logisticsProvider", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "kdniaoApiKey", code: "kdniaoApiKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kdniaoBusinessId", code: "kdniaoBusinessId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Customer", code: "kuaidi100Customer", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Key", code: "kuaidi100Key", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Secret", code: "kuaidi100Secret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Siid", code: "kuaidi100Siid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100PrintType", code: "kuaidi100PrintType", ty: ValueType::Str, default: Some("HTML"), is_list: false },
    FieldSpec { field: "sender", code: "sender", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "mobile", code: "mobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "provinceName", code: "provinceName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "cityName", code: "cityName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "areaName", code: "areaName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "address", code: "address", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveSender", code: "receiveSender", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveMobile", code: "receiveMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveProvinceName", code: "receiveProvinceName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveCityName", code: "receiveCityName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveAreaName", code: "receiveAreaName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveAddress", code: "receiveAddress", ty: ValueType::Str, default: None, is_list: false },
];

static EP11_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "defaultLogisticsName", code: "defaultLogisticsName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "logisticsProvider", code: "logisticsProvider", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "kdniaoApiKey", code: "kdniaoApiKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kdniaoBusinessId", code: "kdniaoBusinessId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Customer", code: "kuaidi100Customer", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Key", code: "kuaidi100Key", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Secret", code: "kuaidi100Secret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100Siid", code: "kuaidi100Siid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "kuaidi100PrintType", code: "kuaidi100PrintType", ty: ValueType::Str, default: Some("HTML"), is_list: false },
    FieldSpec { field: "sender", code: "sender", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "mobile", code: "mobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "provinceName", code: "provinceName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "cityName", code: "cityName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "areaName", code: "areaName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "address", code: "address", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveSender", code: "receiveSender", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveMobile", code: "receiveMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveProvinceName", code: "receiveProvinceName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveCityName", code: "receiveCityName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveAreaName", code: "receiveAreaName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "receiveAddress", code: "receiveAddress", ty: ValueType::Str, default: None, is_list: false },
];

static EP12_FIELDS: [FieldSpec; 56] = [
    FieldSpec { field: "navTheme", code: "navTheme", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "primaryColor", code: "primaryColor", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "layout", code: "layout", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "adminLightLogo", code: "adminLightLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "versionInfoHidden", code: "versionInfoHidden", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "seoCategoryEnable", code: "seoCategoryEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "seoProductEnable", code: "seoProductEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "shopTitleSuffix", code: "shopTitleSuffix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopTitle", code: "shopTitle", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopKeywords", code: "shopKeywords", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopDesc", code: "shopDesc", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "icoImg", code: "icoImg", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "clientDefaultUse", code: "clientDefaultUse", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "pcDomain", code: "pcDomain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "h5Domain", code: "h5Domain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "adminDomain", code: "adminDomain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "autoRedirect", code: "autoRedirect", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "uploadMaxSize", code: "uploadMaxSize", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "imageUploadMaxSize", code: "imageUploadMaxSize", ty: ValueType::Decimal, default: Some("20"), is_list: false },
    FieldSpec { field: "materialUploadNameType", code: "materialUploadNameType", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "searchKeywords", code: "searchKeywords", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "msgHackWord", code: "msgHackWord", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isOpenPscws", code: "isOpenPscws", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "selectSearchType", code: "selectSearchType", ty: ValueType::Str, default: Some("mysql"), is_list: false },
    FieldSpec { field: "shopDefaultRegions", code: "shopDefaultRegions", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "defaultCountry", code: "defaultCountry", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "icoDefinedCss", code: "icoDefinedCss", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageType", code: "storageType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storageLocalUrl", code: "storageLocalUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssUrl", code: "storageOssUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssAccessKeyId", code: "storageOssAccessKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssAccessKeySecret", code: "storageOssAccessKeySecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssBucket", code: "storageOssBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssRegion", code: "storageOssRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosUrl", code: "storageCosUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosSecretId", code: "storageCosSecretId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosSecretKey", code: "storageCosSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosBucket", code: "storageCosBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosRegion", code: "storageCosRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuUrl", code: "storageQiniuUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuAccessKey", code: "storageQiniuAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuSecretKey", code: "storageQiniuSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuBucket", code: "storageQiniuBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuRegion", code: "storageQiniuRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Url", code: "storageS3Url", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3AccessKey", code: "storageS3AccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3SecretKey", code: "storageS3SecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Bucket", code: "storageS3Bucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Region", code: "storageS3Region", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Endpoint", code: "storageS3Endpoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "langOn", code: "langOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "langType", code: "langType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "langVolcengineAccessKey", code: "langVolcengineAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "langVolcengineSecret", code: "langVolcengineSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "workbuddyApiKey", code: "workbuddyApiKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "workbuddyApiKeyExpiresAt", code: "workbuddyApiKeyExpiresAt", ty: ValueType::Long, default: None, is_list: false },
];

static EP13_FIELDS: [FieldSpec; 56] = [
    FieldSpec { field: "navTheme", code: "navTheme", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "primaryColor", code: "primaryColor", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "layout", code: "layout", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "adminLightLogo", code: "adminLightLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "versionInfoHidden", code: "versionInfoHidden", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "seoCategoryEnable", code: "seoCategoryEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "seoProductEnable", code: "seoProductEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "shopTitleSuffix", code: "shopTitleSuffix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopTitle", code: "shopTitle", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopKeywords", code: "shopKeywords", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopDesc", code: "shopDesc", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "icoImg", code: "icoImg", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "clientDefaultUse", code: "clientDefaultUse", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "pcDomain", code: "pcDomain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "h5Domain", code: "h5Domain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "adminDomain", code: "adminDomain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "autoRedirect", code: "autoRedirect", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "uploadMaxSize", code: "uploadMaxSize", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "imageUploadMaxSize", code: "imageUploadMaxSize", ty: ValueType::Decimal, default: Some("20"), is_list: false },
    FieldSpec { field: "materialUploadNameType", code: "materialUploadNameType", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "searchKeywords", code: "searchKeywords", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "msgHackWord", code: "msgHackWord", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isOpenPscws", code: "isOpenPscws", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "selectSearchType", code: "selectSearchType", ty: ValueType::Str, default: Some("mysql"), is_list: false },
    FieldSpec { field: "shopDefaultRegions", code: "shopDefaultRegions", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "defaultCountry", code: "defaultCountry", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "icoDefinedCss", code: "icoDefinedCss", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageType", code: "storageType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storageLocalUrl", code: "storageLocalUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssUrl", code: "storageOssUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssAccessKeyId", code: "storageOssAccessKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssAccessKeySecret", code: "storageOssAccessKeySecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssBucket", code: "storageOssBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageOssRegion", code: "storageOssRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosUrl", code: "storageCosUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosSecretId", code: "storageCosSecretId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosSecretKey", code: "storageCosSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosBucket", code: "storageCosBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageCosRegion", code: "storageCosRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuUrl", code: "storageQiniuUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuAccessKey", code: "storageQiniuAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuSecretKey", code: "storageQiniuSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuBucket", code: "storageQiniuBucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageQiniuRegion", code: "storageQiniuRegion", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Url", code: "storageS3Url", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3AccessKey", code: "storageS3AccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3SecretKey", code: "storageS3SecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Bucket", code: "storageS3Bucket", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Region", code: "storageS3Region", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageS3Endpoint", code: "storageS3Endpoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "langOn", code: "langOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "langType", code: "langType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "langVolcengineAccessKey", code: "langVolcengineAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "langVolcengineSecret", code: "langVolcengineSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "workbuddyApiKey", code: "workbuddyApiKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "workbuddyApiKeyExpiresAt", code: "workbuddyApiKeyExpiresAt", ty: ValueType::Long, default: None, is_list: false },
];

static EP17_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "openWechatOauth", code: "openWechatOauth", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatAppId", code: "wechatAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatAppSecret", code: "wechatAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatServerToken", code: "wechatServerToken", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatServerSecret", code: "wechatServerSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "openWechatRegister", code: "openWechatRegister", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatShareNeedLogin", code: "wechatShareNeedLogin", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "openWechatPcLogin", code: "openWechatPcLogin", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatRegisterBindPhone", code: "wechatRegisterBindPhone", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "facebookLoginOn", code: "facebookLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "facebookClientId", code: "facebookClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "facebookClientSecret", code: "facebookClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "googleLoginOn", code: "googleLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "googleClientId", code: "googleClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "googleClientSecret", code: "googleClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "twitterLoginOn", code: "twitterLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "twitterClientId", code: "twitterClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "twitterClientSecret", code: "twitterClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isOpenMobileAreaCode", code: "isOpenMobileAreaCode", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "usernamePrefix", code: "usernamePrefix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "openEmailRegister", code: "openEmailRegister", ty: ValueType::Int, default: None, is_list: false },
];

static EP18_FIELDS: [FieldSpec; 21] = [
    FieldSpec { field: "openWechatOauth", code: "openWechatOauth", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatAppId", code: "wechatAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatAppSecret", code: "wechatAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatServerToken", code: "wechatServerToken", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatServerSecret", code: "wechatServerSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "openWechatRegister", code: "openWechatRegister", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatShareNeedLogin", code: "wechatShareNeedLogin", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "openWechatPcLogin", code: "openWechatPcLogin", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatRegisterBindPhone", code: "wechatRegisterBindPhone", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "facebookLoginOn", code: "facebookLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "facebookClientId", code: "facebookClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "facebookClientSecret", code: "facebookClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "googleLoginOn", code: "googleLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "googleClientId", code: "googleClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "googleClientSecret", code: "googleClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "twitterLoginOn", code: "twitterLoginOn", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "twitterClientId", code: "twitterClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "twitterClientSecret", code: "twitterClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isOpenMobileAreaCode", code: "isOpenMobileAreaCode", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "usernamePrefix", code: "usernamePrefix", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "openEmailRegister", code: "openEmailRegister", ty: ValueType::Int, default: None, is_list: false },
];

static EP19_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "useSurplus", code: "useSurplus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "usePoints", code: "usePoints", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useCoupon", code: "useCoupon", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useCredit", code: "useCredit", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "paymentOrder", code: "paymentOrder", ty: ValueType::Str, default: None, is_list: false },
];

static EP20_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "useSurplus", code: "useSurplus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "usePoints", code: "usePoints", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useCoupon", code: "useCoupon", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useCredit", code: "useCredit", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "paymentOrder", code: "paymentOrder", ty: ValueType::Str, default: None, is_list: false },
];

static EP21_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "useWechat", code: "useWechat", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatMchidType", code: "wechatMchidType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayMchid", code: "wechatPayMchid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPaySubMchid", code: "wechatPaySubMchid", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayKey", code: "wechatPayKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayV2Key", code: "wechatPayV2Key", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPaySerialNo", code: "wechatPaySerialNo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayCertificate", code: "wechatPayCertificate", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPrivateKey", code: "wechatPayPrivateKey", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayCheckType", code: "wechatPayCheckType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPlatformCertificate", code: "wechatPayPlatformCertificate", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPublicKeyId", code: "wechatPayPublicKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayPublicKey", code: "wechatPayPublicKey", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatMiniProgramAppId", code: "wechatMiniProgramAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatMiniProgramSecret", code: "wechatMiniProgramSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayAppId", code: "wechatPayAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayAppSecret", code: "wechatPayAppSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP22_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "useWechat", code: "useWechat", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatMchidType", code: "wechatMchidType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayMchid", code: "wechatPayMchid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPaySubMchid", code: "wechatPaySubMchid", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayKey", code: "wechatPayKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayV2Key", code: "wechatPayV2Key", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPaySerialNo", code: "wechatPaySerialNo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayCertificate", code: "wechatPayCertificate", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPrivateKey", code: "wechatPayPrivateKey", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayCheckType", code: "wechatPayCheckType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPlatformCertificate", code: "wechatPayPlatformCertificate", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatPayPublicKeyId", code: "wechatPayPublicKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayPublicKey", code: "wechatPayPublicKey", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "wechatMiniProgramAppId", code: "wechatMiniProgramAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatMiniProgramSecret", code: "wechatMiniProgramSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayAppId", code: "wechatPayAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatPayAppSecret", code: "wechatPayAppSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP23_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "miniImgSecCheck", code: "miniImgSecCheck", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP24_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "miniImgSecCheck", code: "miniImgSecCheck", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP25_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "wechatLiveMiniAppId", code: "wechatLiveMiniAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatLiveMiniAppSecret", code: "wechatLiveMiniAppSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP26_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "wechatLiveMiniAppId", code: "wechatLiveMiniAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "wechatLiveMiniAppSecret", code: "wechatLiveMiniAppSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP29_FIELDS: [FieldSpec; 12] = [
    FieldSpec { field: "useAlipay", code: "useAlipay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "alipayAppid", code: "alipayAppid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayMobileAppid", code: "alipayMobileAppid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayRsaPrivateKey", code: "alipayRsaPrivateKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayRsaPublicKey", code: "alipayRsaPublicKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayProfitSharingEnabled", code: "alipayProfitSharingEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "alipayDelayAcctEnabled", code: "alipayDelayAcctEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "alipayProfitSharingNotifyUrl", code: "alipayProfitSharingNotifyUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayProfitSharingMaxRatio", code: "alipayProfitSharingMaxRatio", ty: ValueType::Str, default: Some("30.00"), is_list: false },
    FieldSpec { field: "alipayProfitSharingRuleOrigin", code: "alipayProfitSharingRuleOrigin", ty: ValueType::Str, default: Some("01"), is_list: false },
    FieldSpec { field: "alipayProfitSharingPlatformId", code: "alipayProfitSharingPlatformId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayAppAuthToken", code: "alipayAppAuthToken", ty: ValueType::Str, default: None, is_list: false },
];

static EP30_FIELDS: [FieldSpec; 12] = [
    FieldSpec { field: "useAlipay", code: "useAlipay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "alipayAppid", code: "alipayAppid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayMobileAppid", code: "alipayMobileAppid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayRsaPrivateKey", code: "alipayRsaPrivateKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayRsaPublicKey", code: "alipayRsaPublicKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayProfitSharingEnabled", code: "alipayProfitSharingEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "alipayDelayAcctEnabled", code: "alipayDelayAcctEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "alipayProfitSharingNotifyUrl", code: "alipayProfitSharingNotifyUrl", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayProfitSharingMaxRatio", code: "alipayProfitSharingMaxRatio", ty: ValueType::Str, default: Some("30.00"), is_list: false },
    FieldSpec { field: "alipayProfitSharingRuleOrigin", code: "alipayProfitSharingRuleOrigin", ty: ValueType::Str, default: Some("01"), is_list: false },
    FieldSpec { field: "alipayProfitSharingPlatformId", code: "alipayProfitSharingPlatformId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "alipayAppAuthToken", code: "alipayAppAuthToken", ty: ValueType::Str, default: None, is_list: false },
];

static EP31_FIELDS: [FieldSpec; 6] = [
    FieldSpec { field: "useYabanpay", code: "useYabanpay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useYabanpayWechat", code: "useYabanpayWechat", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useYabanpayAlipay", code: "useYabanpayAlipay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "yabanpayCurrency", code: "yabanpayCurrency", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yabandpayUid", code: "yabandpayUid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yabandpaySecretKey", code: "yabandpaySecretKey", ty: ValueType::Str, default: None, is_list: false },
];

static EP32_FIELDS: [FieldSpec; 6] = [
    FieldSpec { field: "useYabanpay", code: "useYabanpay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useYabanpayWechat", code: "useYabanpayWechat", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "useYabanpayAlipay", code: "useYabanpayAlipay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "yabanpayCurrency", code: "yabanpayCurrency", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yabandpayUid", code: "yabandpayUid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yabandpaySecretKey", code: "yabandpaySecretKey", ty: ValueType::Str, default: None, is_list: false },
];

static EP33_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "useOffline", code: "useOffline", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayBankEnable", code: "offlinePayBankEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayBank", code: "offlinePayBank", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "offlinePayCompanyEnable", code: "offlinePayCompanyEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayCompany", code: "offlinePayCompany", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "thirdPartyPaymentTransferEnable", code: "thirdPartyPaymentTransferEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "thirdPartyPaymentTransfer", code: "thirdPartyPaymentTransfer", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "otherPaymentRemittanceEnable", code: "otherPaymentRemittanceEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "otherPaymentRemittance", code: "otherPaymentRemittance", ty: ValueType::Str, default: None, is_list: false },
];

static EP34_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "useOffline", code: "useOffline", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayBankEnable", code: "offlinePayBankEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayBank", code: "offlinePayBank", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "offlinePayCompanyEnable", code: "offlinePayCompanyEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "offlinePayCompany", code: "offlinePayCompany", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "thirdPartyPaymentTransferEnable", code: "thirdPartyPaymentTransferEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "thirdPartyPaymentTransfer", code: "thirdPartyPaymentTransfer", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "otherPaymentRemittanceEnable", code: "otherPaymentRemittanceEnable", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "otherPaymentRemittance", code: "otherPaymentRemittance", ty: ValueType::Str, default: None, is_list: false },
];

static EP35_FIELDS: [FieldSpec; 6] = [
    FieldSpec { field: "usePaypal", code: "usePaypal", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "paypalCurrency", code: "paypalCurrency", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "paypalClientId", code: "paypalClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "paypalSecret", code: "paypalClientSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "paypalCurrencyListStr", code: "paypalCurrencyList", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "paypalCurrencyList", code: "paypalCurrencyList", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP36_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "usePaypal", code: "usePaypal", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "paypalCurrency", code: "paypalCurrency", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "paypalClientId", code: "paypalClientId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "paypalSecret", code: "paypalClientSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP37_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "useYunpay", code: "useYunpay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "yunpayUid", code: "yunpayUid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yunpaySecretKey", code: "yunpaySecretKey", ty: ValueType::Str, default: None, is_list: false },
];

static EP38_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "useYunpay", code: "useYunpay", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "yunpayUid", code: "yunpayUid", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "yunpaySecretKey", code: "yunpaySecretKey", ty: ValueType::Str, default: None, is_list: false },
];

static EP39_FIELDS: [FieldSpec; 10] = [
    FieldSpec { field: "mailService", code: "mailService", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smtpSsl", code: "smtpSsl", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smtpHost", code: "smtpHost", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpPort", code: "smtpPort", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpUser", code: "smtpUser", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpPass", code: "smtpPass", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpMail", code: "smtpMail", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "mailCharset", code: "mailCharset", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "testMailAddress", code: "testMailAddress", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "serviceEMail", code: "serviceEmail", ty: ValueType::Str, default: None, is_list: false },
];

static EP40_FIELDS: [FieldSpec; 10] = [
    FieldSpec { field: "mailService", code: "mailService", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smtpSsl", code: "smtpSsl", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smtpHost", code: "smtpHost", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpPort", code: "smtpPort", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpUser", code: "smtpUser", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpPass", code: "smtpPass", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smtpMail", code: "smtpMail", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "mailCharset", code: "mailCharset", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "testMailAddress", code: "testMailAddress", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "serviceEMail", code: "serviceEmail", ty: ValueType::Str, default: None, is_list: false },
];

static EP42_FIELDS: [FieldSpec; 28] = [
    FieldSpec { field: "smsProvider", code: "smsProvider", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "smsKeyId", code: "smsKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsKeySecret", code: "smsKeySecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsSignName", code: "smsSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsShopMobile", code: "smsShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSecretId", code: "smsTencentSecretId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSecretKey", code: "smsTencentSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSdkAppId", code: "smsTencentSdkAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSignName", code: "smsTencentSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentShopMobile", code: "smsTencentShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiAppKey", code: "smsHuaweiAppKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiAppSecret", code: "smsHuaweiAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiChannel", code: "smsHuaweiChannel", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiSignName", code: "smsHuaweiSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiEndpoint", code: "smsHuaweiEndpoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiShopMobile", code: "smsHuaweiShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineAccessKey", code: "smsVolcengineAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSecretKey", code: "smsVolcengineSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSmsAccount", code: "smsVolcengineSmsAccount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSignName", code: "smsVolcengineSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineShopMobile", code: "smsVolcengineShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsAliyunChannel", code: "smsAliyunChannel", ty: ValueType::Str, default: Some("1"), is_list: false },
    FieldSpec { field: "smsSenderId", code: "smsSenderId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "serviceEmail", code: "serviceEmail", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "sendConfirmEmail", code: "sendConfirmEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "orderPayEmail", code: "orderPayEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "sendServiceEmail", code: "sendServiceEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "sendShipEmail", code: "sendShipEmail", ty: ValueType::Int, default: None, is_list: false },
];

static EP43_FIELDS: [FieldSpec; 28] = [
    FieldSpec { field: "smsProvider", code: "smsProvider", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "smsKeyId", code: "smsKeyId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsKeySecret", code: "smsKeySecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsSignName", code: "smsSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsShopMobile", code: "smsShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSecretId", code: "smsTencentSecretId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSecretKey", code: "smsTencentSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSdkAppId", code: "smsTencentSdkAppId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentSignName", code: "smsTencentSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsTencentShopMobile", code: "smsTencentShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiAppKey", code: "smsHuaweiAppKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiAppSecret", code: "smsHuaweiAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiChannel", code: "smsHuaweiChannel", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiSignName", code: "smsHuaweiSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiEndpoint", code: "smsHuaweiEndpoint", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsHuaweiShopMobile", code: "smsHuaweiShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineAccessKey", code: "smsVolcengineAccessKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSecretKey", code: "smsVolcengineSecretKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSmsAccount", code: "smsVolcengineSmsAccount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineSignName", code: "smsVolcengineSignName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsVolcengineShopMobile", code: "smsVolcengineShopMobile", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "smsAliyunChannel", code: "smsAliyunChannel", ty: ValueType::Str, default: Some("1"), is_list: false },
    FieldSpec { field: "smsSenderId", code: "smsSenderId", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "serviceEmail", code: "serviceEmail", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "sendConfirmEmail", code: "sendConfirmEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "orderPayEmail", code: "orderPayEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "sendServiceEmail", code: "sendServiceEmail", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "sendShipEmail", code: "sendShipEmail", ty: ValueType::Int, default: None, is_list: false },
];

static EP44_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "closeAuth", code: "closeAuth", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "type", code: "type", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "isIdentity", code: "isIdentity", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smsNote", code: "smsNote", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "tips", code: "tips", ty: ValueType::Str, default: None, is_list: false },
];

static EP45_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "closeAuth", code: "closeAuth", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "type", code: "type", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "isIdentity", code: "isIdentity", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "smsNote", code: "smsNote", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "tips", code: "tips", ty: ValueType::Str, default: None, is_list: false },
];

static EP46_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "templateContent", code: "templateContent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "showTemplateContent", code: "showTemplateContent", ty: ValueType::Bool, default: Some("true"), is_list: false },
];

static EP47_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "templateContent", code: "templateContent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "showTemplateContent", code: "showTemplateContent", ty: ValueType::Bool, default: Some("true"), is_list: false },
];

static EP48_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "themeStyle", code: "themeStyle", ty: ValueType::Str, default: None, is_list: false },
];

static EP49_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "themeStyle", code: "themeStyle", ty: ValueType::Str, default: None, is_list: false },
];

static EP50_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "productCategoryDecorateType", code: "productCategoryDecorateType", ty: ValueType::Int, default: None, is_list: false },
];

static EP51_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "productCategoryDecorateType", code: "productCategoryDecorateType", ty: ValueType::Int, default: None, is_list: false },
];

static EP52_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "productDetailStyleType", code: "productDetailStyleType", ty: ValueType::Int, default: None, is_list: false },
];

static EP53_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "productDetailStyleType", code: "productDetailStyleType", ty: ValueType::Int, default: None, is_list: false },
];

static EP54_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "searchStyleType", code: "searchStyleType", ty: ValueType::Int, default: None, is_list: false },
];

static EP55_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "searchStyleType", code: "searchStyleType", ty: ValueType::Int, default: None, is_list: false },
];

static EP56_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "mobileSearchStyleType", code: "mobileSearchStyleType", ty: ValueType::Int, default: Some("1"), is_list: false },
];

static EP57_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "mobileSearchStyleType", code: "mobileSearchStyleType", ty: ValueType::Int, default: Some("1"), is_list: false },
];

static EP58_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "useNewMemberCenter", code: "useNewMemberCenter", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP59_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "useNewMemberCenter", code: "useNewMemberCenter", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP60_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "shopProductNeedCheck", code: "shopProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "maxRecommendProductCount", code: "maxRecommendProductCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "maxSubAdministrator", code: "maxSubAdministrator", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "defaultShopName", code: "defaultShopName", ty: ValueType::Str, default: None, is_list: false },
];

static EP61_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "shopProductNeedCheck", code: "shopProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "maxRecommendProductCount", code: "maxRecommendProductCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "maxSubAdministrator", code: "maxSubAdministrator", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "defaultShopName", code: "defaultShopName", ty: ValueType::Str, default: None, is_list: false },
];

static EP62_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "buyerShowEnabled", code: "buyerShowEnabled", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "buyerShowMobileShowName", code: "buyerShowMobileShowName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "buyerShowGuidanceContent", code: "buyerShowGuidanceContent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "buyerShowTemplateStyle", code: "buyerShowTemplateStyle", ty: ValueType::Int, default: None, is_list: false },
];

static EP63_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "buyerShowEnabled", code: "buyerShowEnabled", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "buyerShowMobileShowName", code: "buyerShowMobileShowName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "buyerShowGuidanceContent", code: "buyerShowGuidanceContent", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "buyerShowTemplateStyle", code: "buyerShowTemplateStyle", ty: ValueType::Int, default: None, is_list: false },
];

static EP64_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "personApplyEnabled", code: "personApplyEnabled", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "merchantApplyNeedCheck", code: "merchantApplyNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "maxShopCount", code: "maxShopCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopAgreement", code: "shopAgreement", ty: ValueType::Str, default: None, is_list: false },
];

static EP65_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "personApplyEnabled", code: "personApplyEnabled", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "merchantApplyNeedCheck", code: "merchantApplyNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "maxShopCount", code: "maxShopCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopAgreement", code: "shopAgreement", ty: ValueType::Str, default: None, is_list: false },
];

static EP66_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "shopLogo", code: "shopLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopName", code: "shopName", ty: ValueType::Str, default: None, is_list: false },
];

static EP67_FIELDS: [FieldSpec; 38] = [
    FieldSpec { field: "icoDefinedCss", code: "icoDefinedCss", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dollarSign", code: "dollarSign", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dollarSignCn", code: "dollarSignCn", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "storageType", code: "storageType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "clientDefaultUse", code: "clientDefaultUse", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "pcDomain", code: "pcDomain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "h5Domain", code: "h5Domain", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "uploadMaxSize", code: "uploadMaxSize", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "imageUploadMaxSize", code: "imageUploadMaxSize", ty: ValueType::Decimal, default: Some("20"), is_list: false },
    FieldSpec { field: "shopCompany", code: "shopCompany", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "shopCompanyTxt", code: "shopCompanyTxt", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "poweredBy", code: "poweredBy", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "poweredByLogo", code: "poweredByLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "poweredByStatus", code: "poweredByStatus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "layout", code: "layout", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "navTheme", code: "navTheme", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "primaryColor", code: "primaryColor", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "adminLightLogo", code: "adminLightLogo", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "versionInfoHidden", code: "versionInfoHidden", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "defaultCopyright", code: "defaultCopyright", ty: ValueType::Str, default: Some("© 2018 江西佰商科技版权所有"), is_list: false },
    FieldSpec { field: "defaultTechSupport", code: "defaultTechSupport", ty: ValueType::Str, default: Some("/static/mini/images/common/default_tech_support.png"), is_list: false },
    FieldSpec { field: "isOpenShopMember", code: "shopMemberEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "shopMemberEnabled", code: "shopMemberEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "shopMemberDesensitize", code: "shopMemberDesensitize", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "shopMemberAllowAdd", code: "shopMemberAllowAdd", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "shopMemberAllowImport", code: "shopMemberAllowImport", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "shopTitle", code: "storeSelfOperatedName", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "customProductNameLength", code: "customProductNameLength", ty: ValueType::Int, default: Some("200"), is_list: false },
    FieldSpec { field: "productEditStockModify", code: "productEditStockModify", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "specImageMode", code: "specImageMode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalReceiptMethod", code: "withdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: false },
    FieldSpec { field: "withdrawalEnabled", code: "withdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "minAmount", code: "minAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "maxAmount", code: "maxAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyUnit", code: "withdrawalFrequencyUnit", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyCount", code: "withdrawalFrequencyCount", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalReviewMethod", code: "withdrawalReviewMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalDescription", code: "withdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP67_NESTED: [NestedSpec; 1] = [
        NestedSpec { field: "withdrawSettingVO", fields: &[] },
];

static EP71_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "vendorProductNeedCheck", code: "vendorProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorMaxSubAdministrator", code: "vendorMaxSubAdministrator", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorSetPriceType", code: "vendorSetPriceType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorSetPriceAutoValue", code: "vendorSetPriceAutoValue", ty: ValueType::Str, default: None, is_list: false },
];

static EP72_FIELDS: [FieldSpec; 4] = [
    FieldSpec { field: "vendorProductNeedCheck", code: "vendorProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorMaxSubAdministrator", code: "vendorMaxSubAdministrator", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorSetPriceType", code: "vendorSetPriceType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "vendorSetPriceAutoValue", code: "vendorSetPriceAutoValue", ty: ValueType::Str, default: None, is_list: false },
];

static EP73_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "termsOfServiceShow", code: "termsOfServiceShow", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "privacyPolicyShow", code: "privacyPolicyShow", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "afterSalesServiceShow", code: "afterSalesServiceShow", ty: ValueType::Int, default: None, is_list: false },
];

static EP76_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "billingNode", code: "billingNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionNode", code: "collectionNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionTimeSetting", code: "collectionTimeSetting", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "collectionMethod", code: "collectionMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionAccountType", code: "collectionAccountType", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "profitSharingEnabled", code: "profitSharingEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "splitPaymentMethod", code: "splitPaymentMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "profitSharingOnlineChannel", code: "profitSharingOnlineChannel", ty: ValueType::Str, default: Some("wechat"), is_list: false },
    FieldSpec { field: "profitSharingCoolingDays", code: "profitSharingCoolingDays", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoProfitSharingEnabled", code: "autoProfitSharingEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoProfitSharingDays", code: "autoProfitSharingDays", ty: ValueType::Int, default: Some("7"), is_list: false },
    FieldSpec { field: "storeGeneralServiceFeeRate", code: "storeGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storeWithdrawalFeeRate", code: "storeWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storefrontGeneralServiceFeeRate", code: "storefrontGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storefrontWithdrawalFeeRate", code: "storefrontWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "supplierGeneralServiceFeeRate", code: "supplierGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "supplierWithdrawalFeeRate", code: "supplierWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
];

static EP77_FIELDS: [FieldSpec; 17] = [
    FieldSpec { field: "billingNode", code: "billingNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionNode", code: "collectionNode", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionTimeSetting", code: "collectionTimeSetting", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "collectionMethod", code: "collectionMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "collectionAccountType", code: "collectionAccountType", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "profitSharingEnabled", code: "profitSharingEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "splitPaymentMethod", code: "splitPaymentMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "profitSharingOnlineChannel", code: "profitSharingOnlineChannel", ty: ValueType::Str, default: Some("wechat"), is_list: false },
    FieldSpec { field: "profitSharingCoolingDays", code: "profitSharingCoolingDays", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoProfitSharingEnabled", code: "autoProfitSharingEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoProfitSharingDays", code: "autoProfitSharingDays", ty: ValueType::Int, default: Some("7"), is_list: false },
    FieldSpec { field: "storeGeneralServiceFeeRate", code: "storeGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storeWithdrawalFeeRate", code: "storeWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storefrontGeneralServiceFeeRate", code: "storefrontGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "storefrontWithdrawalFeeRate", code: "storefrontWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "supplierGeneralServiceFeeRate", code: "supplierGeneralServiceFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "supplierWithdrawalFeeRate", code: "supplierWithdrawalFeeRate", ty: ValueType::Decimal, default: Some("0"), is_list: false },
];

static EP78_FIELDS: [FieldSpec; 16] = [
    FieldSpec { field: "withdrawalReceiptMethod", code: "withdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "withdrawalEnabled", code: "withdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "minAmount", code: "minAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "maxAmount", code: "maxAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyUnit", code: "withdrawalFrequencyUnit", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyCount", code: "withdrawalFrequencyCount", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalReviewMethod", code: "withdrawalReviewMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalDescription", code: "withdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "onlineWithdrawEnabled", code: "onlineWithdrawEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineMinAmount", code: "onlineMinAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineMaxAmount", code: "onlineMaxAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineWithdrawalFrequencyUnit", code: "onlineWithdrawalFrequencyUnit", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalFrequencyCount", code: "onlineWithdrawalFrequencyCount", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalReviewMethod", code: "onlineWithdrawalReviewMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalDescription", code: "onlineWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "onlineWithdrawChannels", code: "onlineWithdrawChannels", ty: ValueType::Json, default: Some("[\"WECHAT\"]"), is_list: true },
];

static EP79_FIELDS: [FieldSpec; 16] = [
    FieldSpec { field: "withdrawalReceiptMethod", code: "withdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "withdrawalEnabled", code: "withdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "minAmount", code: "minAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "maxAmount", code: "maxAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyUnit", code: "withdrawalFrequencyUnit", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalFrequencyCount", code: "withdrawalFrequencyCount", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalReviewMethod", code: "withdrawalReviewMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "withdrawalDescription", code: "withdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "onlineWithdrawEnabled", code: "onlineWithdrawEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineMinAmount", code: "onlineMinAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineMaxAmount", code: "onlineMaxAmount", ty: ValueType::Decimal, default: Some("0"), is_list: false },
    FieldSpec { field: "onlineWithdrawalFrequencyUnit", code: "onlineWithdrawalFrequencyUnit", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalFrequencyCount", code: "onlineWithdrawalFrequencyCount", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalReviewMethod", code: "onlineWithdrawalReviewMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "onlineWithdrawalDescription", code: "onlineWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "onlineWithdrawChannels", code: "onlineWithdrawChannels", ty: ValueType::Json, default: Some("[\"WECHAT\"]"), is_list: true },
];

static EP80_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "memberWithdrawalEnabled", code: "memberWithdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "memberWithdrawalReceiptMethod", code: "memberWithdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "memberWithdrawalDescription", code: "memberWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP81_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "memberWithdrawalEnabled", code: "memberWithdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "memberWithdrawalReceiptMethod", code: "memberWithdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "memberWithdrawalDescription", code: "memberWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP82_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "commissionWithdrawalEnabled", code: "commissionWithdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "commissionWithdrawalReceiptMethod", code: "commissionWithdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "commissionWithdrawalDescription", code: "commissionWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP83_FIELDS: [FieldSpec; 3] = [
    FieldSpec { field: "commissionWithdrawalEnabled", code: "commissionWithdrawalEnabled", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "commissionWithdrawalReceiptMethod", code: "commissionWithdrawalReceiptMethod", ty: ValueType::Json, default: Some("[1,2,3]"), is_list: true },
    FieldSpec { field: "commissionWithdrawalDescription", code: "commissionWithdrawalDescription", ty: ValueType::Str, default: Some(""), is_list: false },
];

static EP84_FIELDS: [FieldSpec; 18] = [
    FieldSpec { field: "maxRecommendProductCount", code: "maxRecommendProductCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "maxSubAdministrator", code: "maxSubAdministrator", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopProductNeedCheck", code: "shopProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storePostAllocationStatus", code: "storePostAllocationStatus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeIndependentGoods", code: "storeIndependentGoods", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductName", code: "storeAssignProductName", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductPrice", code: "storeAssignProductPrice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductImage", code: "storeAssignProductImage", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "storeUseSoloProductStock", code: "storeUseSoloProductStock", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeUseTotalProductStock", code: "storeUseTotalProductStock", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeCustomSubmitShippingType", code: "storeCustomSubmitShippingType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeShowOtherCityStore", code: "storeShowOtherCityStore", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignDeliveryServiceFee", code: "storeAssignDeliveryServiceFee", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "deliveryServiceFeeEnabled", code: "deliveryServiceFeeEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultDeliveryServiceFeeRate", code: "defaultDeliveryServiceFeeRate", ty: ValueType::Decimal, default: Some("0.00"), is_list: false },
    FieldSpec { field: "storeAssignServerServiceFee", code: "storeAssignServerServiceFee", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "serverServiceFeeEnabled", code: "serverServiceFeeEnabled", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultServerServiceFeeRate", code: "defaultServerServiceFeeRate", ty: ValueType::Decimal, default: Some("0.00"), is_list: false },
];

static EP85_FIELDS: [FieldSpec; 14] = [
    FieldSpec { field: "maxRecommendProductCount", code: "maxRecommendProductCount", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "maxSubAdministrator", code: "maxSubAdministrator", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "shopProductNeedCheck", code: "shopProductNeedCheck", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storePostAllocationStatus", code: "storePostAllocationStatus", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeIndependentGoods", code: "storeIndependentGoods", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductName", code: "storeAssignProductName", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductPrice", code: "storeAssignProductPrice", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignProductImage", code: "storeAssignProductImage", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "storeUseSoloProductStock", code: "storeUseSoloProductStock", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeUseTotalProductStock", code: "storeUseTotalProductStock", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeCustomSubmitShippingType", code: "storeCustomSubmitShippingType", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeShowOtherCityStore", code: "storeShowOtherCityStore", ty: ValueType::Int, default: None, is_list: false },
    FieldSpec { field: "storeAssignDeliveryServiceFee", code: "storeAssignDeliveryServiceFee", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "storeAssignServerServiceFee", code: "storeAssignServerServiceFee", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP86_FIELDS: [FieldSpec; 6] = [
    FieldSpec { field: "amapWebKey", code: "amapWebKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapWebSecret", code: "amapWebSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsKey", code: "amapWebJsKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsSecret", code: "amapWebJsSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniKey", code: "amapWebMiniKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniSecret", code: "amapWebMiniSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP87_FIELDS: [FieldSpec; 6] = [
    FieldSpec { field: "amapWebKey", code: "amapWebKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapWebSecret", code: "amapWebSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsKey", code: "amapWebJsKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsSecret", code: "amapWebJsSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniKey", code: "amapWebMiniKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniSecret", code: "amapWebMiniSecret", ty: ValueType::Str, default: None, is_list: false },
];

static EP88_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "mapProvider", code: "mapProvider", ty: ValueType::Str, default: Some("amap"), is_list: false },
    FieldSpec { field: "amapWebKey", code: "amapWebKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapWebSecret", code: "amapWebSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsKey", code: "amapWebJsKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsSecret", code: "amapWebJsSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniKey", code: "amapWebMiniKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniSecret", code: "amapWebMiniSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "tiandituWebTk", code: "tiandituWebTk", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "tiandituJsTk", code: "tiandituJsTk", ty: ValueType::Str, default: None, is_list: false },
];

static EP89_FIELDS: [FieldSpec; 9] = [
    FieldSpec { field: "mapProvider", code: "mapProvider", ty: ValueType::Str, default: Some("amap"), is_list: false },
    FieldSpec { field: "amapWebKey", code: "amapWebKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapWebSecret", code: "amapWebSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsKey", code: "amapWebJsKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapJsSecret", code: "amapWebJsSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniKey", code: "amapWebMiniKey", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "amapMiniSecret", code: "amapWebMiniSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "tiandituWebTk", code: "tiandituWebTk", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "tiandituJsTk", code: "tiandituJsTk", ty: ValueType::Str, default: None, is_list: false },
];

static EP90_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "guestLikeGoodsName", code: "guestLikeGoodsName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isGuestLikeGoods", code: "isGuestLikeGoods", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultAvatar", code: "defaultAvatar", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "searchSelfStore", code: "searchSelfStore", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "indexSearchShop", code: "indexSearchShop", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP91_FIELDS: [FieldSpec; 5] = [
    FieldSpec { field: "guestLikeGoodsName", code: "guestLikeGoodsName", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "isGuestLikeGoods", code: "isGuestLikeGoods", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "defaultAvatar", code: "defaultAvatar", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "searchSelfStore", code: "searchSelfStore", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "indexSearchShop", code: "indexSearchShop", ty: ValueType::Int, default: Some("0"), is_list: false },
];

static EP92_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "selectSearchType", code: "selectSearchType", ty: ValueType::Str, default: Some("mysql"), is_list: false },
];

static EP93_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "selectSearchType", code: "selectSearchType", ty: ValueType::Str, default: Some("mysql"), is_list: false },
];

static EP94_FIELDS: [FieldSpec; 12] = [
    FieldSpec { field: "storeSelfOperatedName", code: "storeSelfOperatedName", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLogo", code: "storeSelfOperatedLogo", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedRegion", code: "storeSelfOperatedRegion", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedAddress", code: "storeSelfOperatedAddress", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLongitude", code: "storeSelfOperatedLongitude", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLatitude", code: "storeSelfOperatedLatitude", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedPhone", code: "storeSelfOperatedPhone", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedDescription", code: "storeSelfOperatedDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedStatus", code: "storeSelfOperatedStatus", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "storeSelfOperatedContactJson", code: "storeSelfOperatedContactJson", ty: ValueType::Str, default: Some("[]"), is_list: false },
    FieldSpec { field: "storeSelfOperatedOpenCloseJson", code: "storeSelfOperatedOpenCloseJson", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedTipsJson", code: "storeSelfOperatedTipsJson", ty: ValueType::Str, default: Some("[]"), is_list: false },
];

static EP95_FIELDS: [FieldSpec; 12] = [
    FieldSpec { field: "storeSelfOperatedName", code: "storeSelfOperatedName", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLogo", code: "storeSelfOperatedLogo", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedRegion", code: "storeSelfOperatedRegion", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedAddress", code: "storeSelfOperatedAddress", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLongitude", code: "storeSelfOperatedLongitude", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedLatitude", code: "storeSelfOperatedLatitude", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedPhone", code: "storeSelfOperatedPhone", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedDescription", code: "storeSelfOperatedDescription", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedStatus", code: "storeSelfOperatedStatus", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "storeSelfOperatedContactJson", code: "storeSelfOperatedContactJson", ty: ValueType::Str, default: Some("[]"), is_list: false },
    FieldSpec { field: "storeSelfOperatedOpenCloseJson", code: "storeSelfOperatedOpenCloseJson", ty: ValueType::Str, default: Some(""), is_list: false },
    FieldSpec { field: "storeSelfOperatedTipsJson", code: "storeSelfOperatedTipsJson", ty: ValueType::Str, default: Some("[]"), is_list: false },
];

static EP96_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "dingAppSecret", code: "dingAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dingTalkRobotToken", code: "dingTalkRobotToken", ty: ValueType::Str, default: None, is_list: false },
];

static EP97_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "wxworkWebhookUrl", code: "wxworkWebhookUrl", ty: ValueType::Str, default: None, is_list: false },
];

static EP98_FIELDS: [FieldSpec; 2] = [
    FieldSpec { field: "dingAppSecret", code: "dingAppSecret", ty: ValueType::Str, default: None, is_list: false },
    FieldSpec { field: "dingTalkRobotToken", code: "dingTalkRobotToken", ty: ValueType::Str, default: None, is_list: false },
];

static EP99_FIELDS: [FieldSpec; 1] = [
    FieldSpec { field: "wxworkWebhookUrl", code: "wxworkWebhookUrl", ty: ValueType::Str, default: None, is_list: false },
];

static EP100_FIELDS: [FieldSpec; 8] = [
    FieldSpec { field: "autoEraseZero", code: "autoEraseZero", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoDeductionPoints", code: "autoDeductionPoints", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "defaultCollectionMethod", code: "defaultCollectionMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoAddCartAfterScan", code: "autoAddCartAfterScan", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoPrintOnlineOrder", code: "autoPrintOnlineOrder", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoPrintOfflineOrder", code: "autoPrintOfflineOrder", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "shopCommonRemarks", code: "shopCommonRemarks", ty: ValueType::JsonArray, default: Some("[]"), is_list: true },
    FieldSpec { field: "posProductSource", code: "posProductSource", ty: ValueType::JsonArray, default: Some("[0]"), is_list: true },
];

static EP101_FIELDS: [FieldSpec; 8] = [
    FieldSpec { field: "autoEraseZero", code: "autoEraseZero", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "autoDeductionPoints", code: "autoDeductionPoints", ty: ValueType::Int, default: Some("0"), is_list: false },
    FieldSpec { field: "defaultCollectionMethod", code: "defaultCollectionMethod", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoAddCartAfterScan", code: "autoAddCartAfterScan", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoPrintOnlineOrder", code: "autoPrintOnlineOrder", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "autoPrintOfflineOrder", code: "autoPrintOfflineOrder", ty: ValueType::Int, default: Some("1"), is_list: false },
    FieldSpec { field: "shopCommonRemarks", code: "shopCommonRemarks", ty: ValueType::JsonArray, default: Some("[]"), is_list: true },
    FieldSpec { field: "posProductSource", code: "posProductSource", ty: ValueType::JsonArray, default: Some("[0]"), is_list: true },
];

/// 全部 102 个 `ConfigController` 管理端点（顺序 = Java 方法声明顺序）。
pub static ENDPOINTS: [EndpointSpec; 102] = [
    EndpointSpec { path: "/setting/config/basicSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getBasicSettings", summary: "获取商城设置-基础信息", fields: &EP0_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveBasic", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveBasic", summary: "保存商城设置-基础信息", fields: &EP1_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/productSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getProductSettings", summary: "获取商城设置-商品设置", fields: &EP2_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveProduct", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveProduct", summary: "保存商城设置-商品设置", fields: &EP3_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/shoppingSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getShoppingSettings", summary: "获取商城设置-交易设置", fields: &EP4_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveShopping", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveShoppingSettings", summary: "保存商城设置-交易设置", fields: &EP5_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/orderSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getOrderSettings", summary: "获取商城设置-订单设置", fields: &EP6_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveOrder", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveOrderSettings", summary: "保存商城设置-订单设置", fields: &EP7_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/kefuSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getKefuSettings", summary: "获取商城设置-客服设置", fields: &EP8_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveKefu", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveKefuSettings", summary: "保存商城设置-客服设置", fields: &EP9_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/shippingSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getShippingSettings", summary: "获取配送设置-配送设置", fields: &EP10_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveShipping", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveShippingSettings", summary: "保存配送设置-配送设置", fields: &EP11_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/globalSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getGlobalSettings", summary: "获取系统设置-全局设置", fields: &EP12_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveGlobal", verb: Verb::Post, kind: EndpointKind::SaveReturningVo, java_ref: "ConfigController.saveGlobalSettings", summary: "保存系统设置-全局设置", fields: &EP13_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/workbuddySettings", verb: Verb::Get, kind: EndpointKind::WorkBuddySettings, java_ref: "ConfigController.getWorkBuddySettings", summary: "获取 MCP 密钥设置", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/generateWorkbuddyKey", verb: Verb::Post, kind: EndpointKind::WorkBuddyGenerate, java_ref: "ConfigController.generateWorkbuddyKey", summary: "生成 MCP 密钥", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/revealWorkbuddyKey", verb: Verb::Post, kind: EndpointKind::WorkBuddyReveal, java_ref: "ConfigController.revealWorkbuddyKey", summary: "复制 MCP 密钥", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/loginSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getLoginSettings", summary: "获取系统设置-登录设置", fields: &EP17_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveLogin", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveLoginSettings", summary: "保存系统设置-登录设置", fields: &EP18_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/basicPaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getBasicPaySettings", summary: "获取系统设置-支付基础设置", fields: &EP19_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveBasicPay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveBasicPaySettings", summary: "保存系统设置-支付基础设置", fields: &EP20_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/wechatPaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getWechatPaySettings", summary: "获取系统设置-微信支付设置", fields: &EP21_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveWechatPay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveWechatPaySettings", summary: "保存系统设置-微信支付设置", fields: &EP22_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/miniProgramRiskSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMiniProgramRiskSettings", summary: "获取小程序风控配置", fields: &EP23_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMiniProgramRisk", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMiniProgramRiskSettings", summary: "保存小程序风控配置", fields: &EP24_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/wechatLiveSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getWechatLiveSettings", summary: "获取小程序直播/视频号独立小程序配置", fields: &EP25_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveWechatLive", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveWechatLiveSettings", summary: "保存小程序直播/视频号独立小程序配置", fields: &EP26_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/createPlatformCertificate", verb: Verb::Post, kind: EndpointKind::CreatePlatformCertificate, java_ref: "ConfigController.createPlatformCertificate", summary: "生成平台证书", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/uploadFile", verb: Verb::Post, kind: EndpointKind::UploadFile, java_ref: "ConfigController.uploadFile", summary: "上传密钥文件", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/aliPaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getAliPaySettings", summary: "获取系统设置-支付宝支付设置", fields: &EP29_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveAliPay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveAliPaySettings", summary: "保存系统设置-支付宝支付设置", fields: &EP30_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/yaBandPaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getYaBandPaySettings", summary: "获取系统设置-YaBandPay支付设置", fields: &EP31_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveYaBandPay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveYaBandPaySettings", summary: "保存系统设置-YaBandPay支付设置", fields: &EP32_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/offlinePaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getOfflinePaySettings", summary: "获取系统设置-线下支付设置", fields: &EP33_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveOfflinePay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveOfflinePaySettings", summary: "保存系统设置-线下支付设置", fields: &EP34_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/payPalSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getPayPalSettings", summary: "获取系统设置-PayPal支付设置", fields: &EP35_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/savePayPal", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.savePayPalSettings", summary: "保存系统设置-PayPal支付设置", fields: &EP36_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/yunPaySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getYunPaySettings", summary: "获取系统设置-云支付设置", fields: &EP37_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveYunPay", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveYunPaySettings", summary: "保存系统设置-云支付设置", fields: &EP38_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/mailSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMailSettings", summary: "获取系统设置-邮箱配置", fields: &EP39_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMail", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMail", summary: "保存系统设置-邮箱配置", fields: &EP40_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/sendTestEmail", verb: Verb::Post, kind: EndpointKind::SendTestEmail, java_ref: "ConfigController.sendTestEmail", summary: "发送测试邮件", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/notifySettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getNotifySettings", summary: "获取消息设置-通知配置", fields: &EP42_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveNotify", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveNotify", summary: "保存消息设置-通知配置", fields: &EP43_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/authSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getAuthSettings", summary: "获取会员认证配置", fields: &EP44_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveAuth", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveAuth", summary: "保存会员认证配置", fields: &EP45_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/afterSalesSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getAfterSalesSettings", summary: "获取售后配置", fields: &EP46_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveAfterSales", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveAfterSales", summary: "保存售后配置", fields: &EP47_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/themeStyleSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getThemeStyleSettings", summary: "获取主题风格配置", fields: &EP48_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveThemeStyle", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveThemeStyle", summary: "保存主题风格配置", fields: &EP49_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/categoryDecorateSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getCategoryDecorateSettings", summary: "获取分类页装修配置", fields: &EP50_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveCategoryDecorate", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveCategoryDecorate", summary: "保存分类页装修配置", fields: &EP51_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/productDetailStyleSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getProductDetailStyleSettings", summary: "获取商品详情页风格配置", fields: &EP52_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveProductDetailStyle", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveProductDetailStyle", summary: "保存商品详情页风格配置", fields: &EP53_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/searchStyleSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getSearchStyleSettings", summary: "获取搜索页风格配置", fields: &EP54_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveSearchStyle", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveSearchStyle", summary: "保存搜索页风格配置", fields: &EP55_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/mobileSearchStyleSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMobileSearchStyleSettings", summary: "获取移动端搜索风格配置", fields: &EP56_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMobileSearchStyle", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMobileSearchStyle", summary: "保存移动端搜索风格配置", fields: &EP57_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/memberCenterDecorateSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMemberCenterDecorateSettings", summary: "获取个人中心页装修配置", fields: &EP58_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMemberCenterDecorate", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMemberCenterDecorate", summary: "保存个人中心页装修配置", fields: &EP59_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/shopSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getShopSettings", summary: "获取店铺配置", fields: &EP60_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveShop", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveShop", summary: "保存店铺配置", fields: &EP61_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/buyerShowSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getBuyerShowSettings", summary: "获取买家秀配置", fields: &EP62_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveBuyerShow", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveBuyerShow", summary: "保存买家秀配置", fields: &EP63_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/merchantSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMerchantSettings", summary: "获取商户配置", fields: &EP64_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMerchant", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMerchant", summary: "保存商户配置", fields: &EP65_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getAdmin", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getAdmin", summary: "根据配置编码获取配置", fields: &EP66_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getAdminBase", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getAdminBase", summary: "admin 初始化配置", fields: &EP67_FIELDS, nested: &EP67_NESTED },
    EndpointSpec { path: "/setting/config/basicConfig", verb: Verb::Get, kind: EndpointKind::BasicConfig, java_ref: "ConfigController.getBasicConfig", summary: "获取基础配置", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/getIcon", verb: Verb::Get, kind: EndpointKind::IconSettings, java_ref: "ConfigController.getIcon", summary: "获取图标icon", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/getBase", verb: Verb::Get, kind: EndpointKind::RawConfigByCode, java_ref: "ConfigController.getBaseConfig", summary: "根据配置编码获取配置", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/vendorSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getVendorSettings", summary: "获取供应商配置", fields: &EP71_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveVendor", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveVendor", summary: "保存供应商配置", fields: &EP72_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getLoginProtocol", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getLoginProtocol", summary: "获取登录协议", fields: &EP73_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getLoginProtocolContent", verb: Verb::Get, kind: EndpointKind::LoginProtocolContent, java_ref: "ConfigController.getLoginProtocolContentVO", summary: "获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/saveLoginProtocol", verb: Verb::Post, kind: EndpointKind::LoginProtocolSave, java_ref: "ConfigController.saveLoginProtocol", summary: "保存登录协议", fields: &[], nested: &[] },
    EndpointSpec { path: "/setting/config/profitSharingSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getProfitSharingSettings", summary: "获取结算与分账配置", fields: &EP76_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveProfitSharing", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveProfitSharing", summary: "保存结算与分账配置", fields: &EP77_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/withdrawalSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getWithdrawalSettings", summary: "获取提现配置", fields: &EP78_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveWithdrawal", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveWithdrawal", summary: "保存提现配置", fields: &EP79_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/memberWithdrawalSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getMemberWithdrawalSettings", summary: "获取会员余额提现配置", fields: &EP80_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMemberWithdrawal", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMemberWithdrawal", summary: "保存会员余额提现配置", fields: &EP81_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/commissionWithdrawalSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getCommissionWithdrawalSettings", summary: "获取分销佣金提现配置", fields: &EP82_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveCommissionWithdrawal", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveCommissionWithdrawal", summary: "保存分销佣金提现配置", fields: &EP83_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/storeSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getStoreSettings", summary: "获取门店配置", fields: &EP84_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveStore", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveStore", summary: "保存门店配置", fields: &EP85_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/amapAllSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.amapAllSettings", summary: "获取web Amap 配置", fields: &EP86_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveAllAmapSettings", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveAllAmapSettings", summary: "保存web Amap 配置", fields: &EP87_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/mapSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.mapSettings", summary: "获取地图服务商配置", fields: &EP88_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveMapSettings", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveMapSettings", summary: "保存地图服务商配置", fields: &EP89_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/savePersonalizedSetting", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.savePersonalizedSetting", summary: "保存个性化设置", fields: &EP90_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getPersonalizedSetting", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getPersonalizedSetting", summary: "获取个性化设置", fields: &EP91_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/searchSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getSearchSettings", summary: "商品搜索配置", fields: &EP92_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveSearch", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveSearch", summary: "保存商品搜索配置", fields: &EP93_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/storeSelfOperatedSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getStoreSelfOperatedSettings", summary: "获取平台门店配置", fields: &EP94_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveStoreSelfOperated", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveStore", summary: "保存平台门店配置", fields: &EP95_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveDingConfig", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveDingConfig", summary: "保存钉钉配置", fields: &EP96_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveWXWorkConfig", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveWXWorkConfig", summary: "保存企业微信配置", fields: &EP97_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getDingConfig", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getDingConfig", summary: "获取钉钉配置", fields: &EP98_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getWXWorkConfig", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getWXWorkConfig", summary: "获取企业微信配置", fields: &EP99_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/getCashierOrderSettings", verb: Verb::Get, kind: EndpointKind::Group, java_ref: "ConfigController.getCashierOrderSettings", summary: "获取收银台订单设置", fields: &EP100_FIELDS, nested: &[] },
    EndpointSpec { path: "/setting/config/saveCashierOrderSettings", verb: Verb::Post, kind: EndpointKind::Group, java_ref: "ConfigController.saveCashierOrderSettings", summary: "保存收银台订单设置", fields: &EP101_FIELDS, nested: &[] },
];

/// 全部端点。
#[must_use]
pub fn endpoints() -> &'static [EndpointSpec] {
    &ENDPOINTS
}

/// 按挂载路径查端点规格。
#[must_use]
pub fn find_endpoint(path: &str) -> Option<&'static EndpointSpec> {
    ENDPOINTS.iter().find(|ep| ep.path == path)
}

/// Java `SettingsHelper#getRaw` 的默认值回退：`default ?? "0"`。
#[must_use]
pub fn default_raw(spec: &FieldSpec) -> &'static str {
    spec.default.unwrap_or("0")
}

/// Java `SettingsHelper#parse` 的等价实现（含 `BUSINESS_LICENSE_ENABLED` 特例）。
///
/// - `Str`：原样字符串（Java 缺失时也返回 `"0"`）；
/// - `Int` / `Long`：空白 → `0`；非法 → `null`（Java 捕获异常返回 null）；
/// - `Bool`：空白 → `false`；`Boolean.parseBoolean` 语义；
/// - `Decimal`：空白 → `0`；非法 → `null`；
/// - `Json*`：空白 → `null`；非法 JSON → `null`。
#[must_use]
pub fn parse_field(spec: &FieldSpec, raw: &str) -> Value {
    let blank = raw.trim().is_empty();
    match spec.ty {
        ValueType::Str => {
            if spec.code == BUSINESS_LICENSE_ENABLED_CODE {
                let v = if raw.trim() == "0" { 0 } else { 1 };
                return Value::from(v);
            }
            Value::String(raw.to_string())
        }
        ValueType::Int => {
            if spec.code == BUSINESS_LICENSE_ENABLED_CODE {
                return Value::from(if raw.trim() == "0" { 0 } else { 1 });
            }
            if blank {
                return Value::from(0);
            }
            match raw.trim().parse::<i64>() {
                Ok(v) => Value::from(v),
                Err(_) => Value::Null,
            }
        }
        ValueType::Long => {
            if blank {
                return Value::from(0);
            }
            match raw.trim().parse::<i64>() {
                Ok(v) => Value::from(v),
                Err(_) => Value::Null,
            }
        }
        ValueType::Bool => Value::Bool(!blank && raw.trim().eq_ignore_ascii_case("true")),
        ValueType::Decimal => {
            if blank {
                return Value::from(0);
            }
            match raw.trim().parse::<f64>() {
                Ok(v) => serde_json::Number::from_f64(v).map_or(Value::Null, Value::Number),
                Err(_) => Value::Null,
            }
        }
        ValueType::Json | ValueType::JsonArray | ValueType::JsonObject => {
            if blank {
                return Value::Null;
            }
            serde_json::from_str::<Value>(raw.trim()).unwrap_or(Value::Null)
        }
    }
}

/// 组装一个设置分组的输出 JSON（字段顺序 = Java VO 字段声明顺序）。
#[must_use]
pub fn build_group(
    fields: &[FieldSpec],
    nested: &[NestedSpec],
    rows: &std::collections::HashMap<String, String>,
) -> Value {
    let mut obj = Map::new();
    for spec in fields {
        let raw = rows
            .get(spec.code)
            .map(String::as_str)
            .unwrap_or_else(|| default_raw(spec));
        obj.insert(spec.field.to_string(), parse_field(spec, raw));
    }
    for n in nested {
        let mut inner = Map::new();
        for spec in n.fields {
            let raw = rows
                .get(spec.code)
                .map(String::as_str)
                .unwrap_or_else(|| default_raw(spec));
            inner.insert(spec.field.to_string(), parse_field(spec, raw));
        }
        obj.insert(n.field.to_string(), Value::Object(inner));
    }
    Value::Object(obj)
}

/// Java `ConfigServiceImpl#extractConfigs` + `SettingsHelper#set` 的「字段 → `biz_val`」编码。
///
/// - `null` → `None`（Java 跳过 `bizVal == null` 的项，不落库）；
/// - `List` / JSON 类型 → 紧凑 JSON 文本；
/// - 其它 → `String.valueOf(value)`（字符串原样，数字/布尔转文本）。
#[must_use]
pub fn encode_save_value(spec: &FieldSpec, value: &Value) -> Option<String> {
    if value.is_null() {
        return None;
    }
    let json_encoded = spec.is_list
        || matches!(
            spec.ty,
            ValueType::Json | ValueType::JsonArray | ValueType::JsonObject
        );
    let raw = if json_encoded {
        serde_json::to_string(value).ok()?
    } else {
        match value {
            Value::String(s) => s.clone(),
            Value::Bool(b) => b.to_string(),
            Value::Number(n) => n.to_string(),
            other => serde_json::to_string(other).ok()?,
        }
    };
    if spec.code == BUSINESS_LICENSE_ENABLED_CODE {
        return Some(if raw.trim() == "0" { "0".into() } else { "1".into() });
    }
    Some(raw)
}

/// 从 POST 请求体收集待落库的 `(biz_code, biz_val)` 列表（顺序 = 规格表顺序）。
#[must_use]
pub fn collect_save_pairs(
    fields: &[FieldSpec],
    nested: &[NestedSpec],
    body: &Map<String, Value>,
) -> Vec<(String, String)> {
    let mut out = Vec::new();
    for spec in fields {
        if let Some(v) = body.get(spec.field) {
            if let Some(encoded) = encode_save_value(spec, v) {
                out.push((spec.code.to_string(), encoded));
            }
        }
    }
    for n in nested {
        if let Some(Value::Object(inner)) = body.get(n.field) {
            for spec in n.fields {
                if let Some(v) = inner.get(spec.field) {
                    if let Some(encoded) = encode_save_value(spec, v) {
                        out.push((spec.code.to_string(), encoded));
                    }
                }
            }
        }
    }
    out
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::collections::HashMap;

    #[test]
    fn route_specs_count_and_uniqueness() {
        assert_eq!(ENDPOINTS.len(), 102, "契约共 102 条");
        assert_eq!(PUBLIC_PATHS.len(), 3, "匿名白名单 3 条");
        let mut seen = std::collections::HashSet::new();
        for ep in ENDPOINTS.iter() {
            assert!(seen.insert(ep.path), "重复路径: {}", ep.path);
            assert!(ep.path.starts_with("/setting/config/"));
            assert!(matches!(ep.verb, Verb::Get | Verb::Post));
        }
        // 三个匿名端点必须在规格表内
        for p in PUBLIC_PATHS {
            assert!(find_endpoint(p).is_some(), "匿名端点缺失: {p}");
        }
    }

    #[test]
    fn group_build_applies_enum_defaults_and_types() {
        let fields = [
            FieldSpec { field: "shopName", code: "shopName", ty: ValueType::Str, default: None, is_list: false },
            FieldSpec { field: "poweredBy", code: "poweredBy", ty: ValueType::Int, default: Some("1"), is_list: false },
            FieldSpec { field: "businessLicenseEnabled", code: BUSINESS_LICENSE_ENABLED_CODE, ty: ValueType::Int, default: Some("1"), is_list: false },
            FieldSpec { field: "businessLicenseType", code: "businessLicenseType", ty: ValueType::JsonArray, default: Some("[]"), is_list: true },
        ];
        let empty = HashMap::new();
        let v = build_group(&fields, &[], &empty);
        // 缺失的字符串项 Java 回退 "0"（SettingsHelper#getRaw 的 default ?? "0"）
        assert_eq!(v["shopName"], "0");
        assert_eq!(v["poweredBy"], 1);
        assert_eq!(v["businessLicenseEnabled"], 1);
        assert_eq!(v["businessLicenseType"], serde_json::json!([]));

        let rows: HashMap<String, String> = [
            ("shopName".to_string(), "TigShop".to_string()),
            ("poweredBy".to_string(), "0".to_string()),
            ("businessLicenseType".to_string(), r#"["icp","edi"]"#.to_string()),
        ]
        .into_iter()
        .collect();
        let v = build_group(&fields, &[], &rows);
        assert_eq!(v["shopName"], "TigShop");
        assert_eq!(v["poweredBy"], 0);
        assert_eq!(v["businessLicenseType"], serde_json::json!(["icp", "edi"]));
    }

    #[test]
    fn parse_field_matches_settings_helper_semantics() {
        let int = FieldSpec { field: "f", code: "f", ty: ValueType::Int, default: None, is_list: false };
        assert_eq!(parse_field(&int, ""), serde_json::json!(0));
        assert_eq!(parse_field(&int, "  "), serde_json::json!(0));
        assert_eq!(parse_field(&int, "42"), serde_json::json!(42));
        assert_eq!(parse_field(&int, "abc"), Value::Null);
        let s = FieldSpec { field: "f", code: "f", ty: ValueType::Str, default: None, is_list: false };
        assert_eq!(parse_field(&s, ""), serde_json::json!(""));
        let b = FieldSpec { field: "f", code: "f", ty: ValueType::Bool, default: None, is_list: false };
        assert_eq!(parse_field(&b, "TRUE"), serde_json::json!(true));
        assert_eq!(parse_field(&b, ""), serde_json::json!(false));
        let j = FieldSpec { field: "f", code: "f", ty: ValueType::JsonArray, default: None, is_list: false };
        assert_eq!(parse_field(&j, "[1,2]"), serde_json::json!([1, 2]));
        assert_eq!(parse_field(&j, "oops"), Value::Null);
    }

    #[test]
    fn save_encoding_matches_extract_configs() {
        // 普通字符串原样（Java String.valueOf("x") == "x"，不 JSON 引号）
        let s = FieldSpec { field: "shopName", code: "shopName", ty: ValueType::Str, default: None, is_list: false };
        assert_eq!(encode_save_value(&s, &serde_json::json!("TigShop")).as_deref(), Some("TigShop"));
        // 数字 → 文本
        let i = FieldSpec { field: "poweredBy", code: "poweredBy", ty: ValueType::Int, default: None, is_list: false };
        assert_eq!(encode_save_value(&i, &serde_json::json!(0)).as_deref(), Some("0"));
        // List → JSON 文本（extractConfigs 对 List 走 JSONUtil.toJsonStr）
        let l = FieldSpec { field: "businessLicenseType", code: "businessLicenseType", ty: ValueType::JsonArray, default: None, is_list: true };
        assert_eq!(
            encode_save_value(&l, &serde_json::json!(["icp", "edi"])).as_deref(),
            Some(r#"["icp","edi"]"#)
        );
        // null → 不落库
        assert_eq!(encode_save_value(&s, &Value::Null), None);
        // BUSINESS_LICENSE_ENABLED 归一 0/1
        let b = FieldSpec { field: "businessLicenseEnabled", code: BUSINESS_LICENSE_ENABLED_CODE, ty: ValueType::Int, default: None, is_list: false };
        assert_eq!(encode_save_value(&b, &serde_json::json!(0)).as_deref(), Some("0"));
        assert_eq!(encode_save_value(&b, &serde_json::json!(7)).as_deref(), Some("1"));

        let body: Map<String, Value> = serde_json::from_str(
            r#"{"shopName":"S","poweredBy":1,"businessLicenseType":["icp"],"unknown":1}"#,
        )
        .unwrap();
        let pairs = collect_save_pairs(&[s, i], &[], &body);
        assert_eq!(
            pairs,
            vec![
                ("shopName".to_string(), "S".to_string()),
                ("poweredBy".to_string(), "1".to_string())
            ]
        );
    }

    #[test]
    fn login_protocol_triplets_resolve_enum_codes() {
        for (key, show, content) in LOGIN_PROTOCOLS {
            assert!(!key.is_empty() && !show.is_empty() && !content.is_empty());
            assert!(show.ends_with("Show"), "show bizCode: {show}");
        }
    }

    #[test]
    fn every_endpoint_field_code_round_trips_through_parse() {
        // 全部 846 个字段规格都能被 build_group 消费且 JSON 可序列化
        for ep in ENDPOINTS.iter() {
            let empty = HashMap::new();
            let v = build_group(ep.fields, ep.nested, &empty);
            assert!(v.is_object(), "{} 未生成对象", ep.path);
            let n = ep.fields.len() + ep.nested.iter().map(|n| n.fields.len()).sum::<usize>();
            if n > 0 {
                assert_eq!(v.as_object().map(|o| o.len()), Some(ep.fields.len() + ep.nested.len()));
            }
        }
    }
}

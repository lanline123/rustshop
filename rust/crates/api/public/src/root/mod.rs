//! 根级路由（**不带** `/api` 前缀）。
//!
//! 原 Java 绝大多数 api 模块控制器的 `@RequestMapping` 里已写死 `/api/...` 前缀，
//! 这些切片放 [`crate::routes`]（由 `router()` 挂进 `nest("/api")`）。
//! 但有少数控制器的映射**没有**模块前缀：
//!
//! | Java 控制器 | `@RequestMapping` | 真实 URL | 是否免登录 |
//! |-------------|-------------------|----------|-----------|
//! | `ImConversationController` | `/im/conversation/conversation` | `/im/conversation/conversation/list` | 否 |
//! | `ImMessageController` | `/im/conversation/message` | `/im/conversation/message/send` | 否 |
//! | `ShortUrlController` | `/s` | `/s/{code}` | **是** |
//!
//! （已核对 `java/tigshop-security-api/.../WhiteListConfig.java`：
//! `"/s/**"` 在白名单内免登录，`/im/**` 不在，需会员登录态。）
//!
//! 约定与 `crate::routes` 一致：每个文件提供
//! `pub fn routes(state: &PublicState) -> Router<PublicState>` 与
//! `pub fn implemented_routes() -> Vec<RouteDoc>`；`RouteDoc.path` 写**完整
//! 契约路径**（以 `/im`、`/s` 开头，不含 `/api`）。

pub mod slice097;
pub mod slice140;
pub mod slice156;

use axum::Router;

use crate::PublicState;

/// 需登录的根级路由（Java `URL_WHITELIST` 未覆盖的 `/im/**`）。
///
/// 认证中间件由 `crate::router()` 附加，本函数只聚合路由、保持
/// `Router<PublicState>`。
pub fn merge_protected(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .merge(slice097::routes(state))
        .merge(slice140::routes(state))
}

/// 免登录的根级路由（`/s/{code}` 短链跳转）。
pub fn merge_public(state: &PublicState) -> Router<PublicState> {
    Router::new().merge(slice156::routes(state))
}

/// 汇总所有根级已实现路由，供覆盖率与接口文档统计。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(slice097::implemented_routes());
    out.extend(slice140::implemented_routes());
    out.extend(slice156::implemented_routes());
    out
}

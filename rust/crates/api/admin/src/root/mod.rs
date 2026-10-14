//! 根级路由（**不带** `/adminapi` 前缀）。
//!
//! 原 Java 绝大多数 adminapi 控制器的 `@RequestMapping` 里已写死 `/adminapi/...`
//! 前缀，这些切片放 [`crate::routes`]（由 `router()` 挂进 `nest("/adminapi")`）。
//! 但有少数控制器的映射**没有**模块前缀：
//!
//! | Java 控制器 | `@RequestMapping` | 真实 URL | 是否免登录 |
//! |-------------|-------------------|----------|-----------|
//! | `IMConfigController` | `im/config/config` | `/im/config/config/detail` | 否 |
//! | `ImServantController` | `/im/servant/servant` | `/im/servant/servant/getStatus` | 否 |
//! | `ImConversationController` | `/im/conversation/conversation` | `/im/conversation/conversation/list` | 否 |
//! | `ImMessageController` | `/im/conversation/message` | `/im/conversation/message/send` | 否 |
//! | `FilesController` | 方法级完整路径 | `/img/gallery/{timestamp}/{filename}` | **是** |
//!
//! 它们的真实 URL 就是 adminapi 应用根路径下的 `/im/...`、`/img/...`，
//! **不在** `/adminapi` 之下（已核对
//! `java/tigshop-security-admin/.../SecurityConfig.java` 的 `URL_WHITELIST`：
//! `/img/**`、`/video/**`、`/app/**` 免登录，`/im/**` 需登录）。
//! 若误挂进 `routes::merge_all`，实际服务路径会变成 `/adminapi/im/...`
//! （前端 404），并且 `tools/check_api_coverage.py` 会同时判为
//! 「未实现」+「疑似多余路由」。
//!
//! 约定与 `crate::routes` 一致：每个文件提供
//! `pub fn routes(state: &AdminState) -> Router<AdminState>` 与
//! `pub fn implemented_routes() -> Vec<RouteDoc>`；`RouteDoc.path` 写**完整
//! 契约路径**（以 `/im`、`/img` 开头，不含 `/adminapi`）。

pub mod slice020;
pub mod slice095;
pub mod slice097;
pub mod slice134;
pub mod slice140;

use axum::Router;

use crate::AdminState;

/// 需登录的根级路由（对应 Java `URL_WHITELIST` 未覆盖的 `/im/**`）。
///
/// 认证中间件由 `crate::router()` 在本 Router 上统一附加，因此本函数
/// 只负责聚合路由、保持 `Router<AdminState>`。
pub fn merge_protected(state: &AdminState) -> Router<AdminState> {
    Router::new()
        .merge(slice020::routes(state))
        .merge(slice095::routes(state))
        .merge(slice097::routes(state))
        .merge(slice140::routes(state))
}

/// 免登录的根级路由（`/img/**`、`/video/**`、`/app/**` 静态资源）。
pub fn merge_public(state: &AdminState) -> Router<AdminState> {
    Router::new().merge(slice134::routes(state))
}

/// 汇总所有根级已实现路由，供覆盖率与接口文档统计。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(slice020::implemented_routes());
    out.extend(slice095::implemented_routes());
    out.extend(slice097::implemented_routes());
    out.extend(slice134::implemented_routes());
    out.extend(slice140::implemented_routes());
    out
}

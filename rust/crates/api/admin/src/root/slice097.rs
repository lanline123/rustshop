//! IM 会话（ImConversationController） —— 根级路由（slice-097，admin 端）。
//!
//! **路径约定**：原 Java 该控制器的 `@RequestMapping` **不含** `/admin` 前缀，
//! 真实 URL 直接挂在应用根路径上（见 `crate::root` 模块文档）。
//! 因此本文件的路由串以 `/im`、`/img`、`/s` 等开头，**不要**再写
//! `/admin/...`；`implemented_routes()` 里的 `RouteDoc.path` 写完整契约路径。
//!
//! 状态：骨架已注册并可编译，handler 待域实现者填充。

use axum::Router;

use crate::AdminState;

/// 根级路由（挂载于应用根路径，不在 `/admin` 之下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 已实现路由清单（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

//! im 根级路由（不带模块前缀）。

pub mod im_config;
pub mod im_message;
pub mod im_servant;

use axum::Router;

use super::super::AdminState;

/// 需登录的根级路由。
pub fn merge_protected(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 免登录的根级路由。
pub fn merge_public(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 汇总本域根级已实现路由。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(im_config::implemented_routes());
    out.extend(im_message::implemented_routes());
    out.extend(im_servant::implemented_routes());
    out
}

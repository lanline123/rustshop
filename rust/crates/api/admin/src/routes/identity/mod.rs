//! 管理端 identity 路由。

pub mod admin_log;
pub mod admin_user;
pub mod authority;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(admin_log::routes(state))
        .merge(admin_user::routes(state))
        .merge(authority::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(admin_log::implemented_routes());
    out.extend(admin_user::implemented_routes());
    out.extend(authority::implemented_routes());
    out
}

//! 管理端 system 路由。

pub mod app_version_admin;
pub mod area_code;
pub mod cache_manage;
pub mod example;
pub mod mail_templates;
pub mod message_type;
pub mod region;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(app_version_admin::routes(state))
        .merge(area_code::routes(state))
        .merge(cache_manage::routes(state))
        .merge(example::routes(state))
        .merge(mail_templates::routes(state))
        .merge(message_type::routes(state))
        .merge(region::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(app_version_admin::implemented_routes());
    out.extend(area_code::implemented_routes());
    out.extend(cache_manage::implemented_routes());
    out.extend(example::implemented_routes());
    out.extend(mail_templates::implemented_routes());
    out.extend(message_type::implemented_routes());
    out.extend(region::implemented_routes());
    out
}

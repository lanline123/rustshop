//! 管理端 content 路由。

pub mod article;
pub mod decorate;
pub mod mobile_cat_nav;
pub mod seo;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(article::routes(state))
        .merge(decorate::routes(state))
        .merge(mobile_cat_nav::routes(state))
        .merge(seo::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(article::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(mobile_cat_nav::implemented_routes());
    out.extend(seo::implemented_routes());
    out
}

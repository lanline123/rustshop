//! 管理端 content 路由。

pub mod article;
pub mod decorate;
pub mod gallery;
pub mod gallery_pic;
pub mod mobile_cat_nav;
pub mod seo;
pub mod wechat_live;

use axum::Router;

use super::super::AdminState;

/// 需登录的域路由（外层统一套 require_auth）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(article::routes(state))
        .merge(decorate::routes(state))
        .merge(gallery::routes(state))
        .merge(gallery_pic::routes(state))
        .merge(mobile_cat_nav::routes(state))
        .merge(seo::routes(state))
        .merge(wechat_live::routes(state))
}

/// 免登录的域路由（对应 Java `URL_WHITELIST`，外层不套 require_auth）。
pub fn public_routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(article::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(gallery::implemented_routes());
    out.extend(gallery_pic::implemented_routes());
    out.extend(mobile_cat_nav::implemented_routes());
    out.extend(seo::implemented_routes());
    out.extend(wechat_live::implemented_routes());
    out
}

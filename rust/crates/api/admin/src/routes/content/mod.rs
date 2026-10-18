//! 管理端 content 路由。

pub mod article;
pub mod decorate;
pub mod friend_links;
pub mod gallery;
pub mod gallery_pic;
pub mod gallery_video;
pub mod mobile_cat_nav;
pub mod seo;
pub mod wechat_auto_reply;
pub mod wechat_live;

use axum::Router;

use super::super::AdminState;

/// 需登录的域路由（外层统一套 require_auth）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(article::routes(state))
        .merge(decorate::routes(state))
        .merge(friend_links::routes(state))
        .merge(gallery::routes(state))
        .merge(gallery_pic::routes(state))
        .merge(gallery_video::routes(state))
        .merge(mobile_cat_nav::routes(state))
        .merge(seo::routes(state))
        .merge(wechat_auto_reply::routes(state))
        .merge(wechat_live::routes(state))
}

/// 免登录的域路由（对应 Java `URL_WHITELIST`，外层不套 require_auth）。
pub fn public_routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(wechat_auto_reply::public_routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(article::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(friend_links::implemented_routes());
    out.extend(gallery::implemented_routes());
    out.extend(gallery_pic::implemented_routes());
    out.extend(gallery_video::implemented_routes());
    out.extend(mobile_cat_nav::implemented_routes());
    out.extend(seo::implemented_routes());
    out.extend(wechat_auto_reply::implemented_routes());
    out.extend(wechat_live::implemented_routes());
    out
}

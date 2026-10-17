//! 商城端 content 路由。

pub mod article;
pub mod decorate;
pub mod home;
pub mod seo;
pub mod share;
pub mod wechat_live_sync_callback;

use axum::Router;

use super::super::PublicState;

/// 自带鉴权中间件的域路由（并入 merge_public，外层不再套 require_auth）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(home::routes(state))
        .merge(seo::routes(state))
        .merge(share::routes(state))
        .merge(wechat_live_sync_callback::routes(state))
}

/// 匿名可访问路由（无状态）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
        .merge(article::public_routes())
        .merge(decorate::public_routes())
}

/// 匿名可访问路由（需要 state，外层不套 require_auth）。
pub fn anonymous_routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(home::anonymous_routes(state))
}

/// 需登录路由（外层统一套 require_auth）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
        .merge(article::authorized_routes())
        .merge(decorate::authorized_routes())
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(article::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(home::implemented_routes());
    out.extend(seo::implemented_routes());
    out.extend(share::implemented_routes());
    out.extend(wechat_live_sync_callback::implemented_routes());
    out
}

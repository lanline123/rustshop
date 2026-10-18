//! 商城端 member 路由。

pub mod login;
pub mod member_lists;
pub mod user_address_client;
pub mod user_center;
pub mod user_message;

use axum::Router;

use super::super::PublicState;

/// 自带鉴权中间件的域路由（并入 merge_public，外层不再套 require_auth）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(login::routes(state))
        .merge(user_address_client::routes(state))
        .merge(user_center::routes(state))
        .merge(user_message::routes(state))
}

/// 匿名可访问路由（无状态）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
        .merge(member_lists::public_routes())
}

/// 匿名可访问路由（需要 state，外层不套 require_auth）。
pub fn anonymous_routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(login::anonymous_routes(state))
}

/// 需登录路由（外层统一套 require_auth）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
        .merge(member_lists::authorized_routes())
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(login::implemented_routes());
    out.extend(member_lists::implemented_routes());
    out.extend(user_address_client::implemented_routes());
    out.extend(user_center::implemented_routes());
    out.extend(user_message::implemented_routes());
    out
}

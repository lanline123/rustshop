//! 商城端 merchant 路由。

pub mod merchant_client;
pub mod shop_client;
pub mod shop_client_category;

use axum::Router;

use super::super::PublicState;

/// 自带鉴权中间件的域路由（并入 merge_public）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(merchant_client::routes(state))
        .merge(shop_client::routes(state))
        .merge(shop_client_category::routes(state))
}

/// 匿名可访问路由（外层不套 require_auth）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
}

/// 需登录路由（外层统一套 require_auth）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(merchant_client::implemented_routes());
    out.extend(shop_client::implemented_routes());
    out.extend(shop_client_category::implemented_routes());
    out
}

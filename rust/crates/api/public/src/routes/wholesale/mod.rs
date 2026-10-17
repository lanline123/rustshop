//! 商城端 wholesale 路由。

pub mod wholesale_client;

use axum::Router;

use super::super::PublicState;

/// 自带鉴权中间件的域路由（并入 merge_public）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(wholesale_client::routes(state))
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
    out.extend(wholesale_client::implemented_routes());
    out
}

//! 商城端 catalog 路由。

pub mod category;
pub mod category_all;
pub mod product_manage;

use axum::Router;

use super::super::PublicState;

/// 自带鉴权中间件的域路由（并入 merge_public）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .merge(category::routes(state))
        .merge(product_manage::routes(state))
}

/// 匿名可访问路由（外层不套 require_auth）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
        .merge(category_all::public_routes())
}

/// 需登录路由（外层统一套 require_auth）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
        .merge(category_all::authorized_routes())
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(category::implemented_routes());
    out.extend(category_all::implemented_routes());
    out.extend(product_manage::implemented_routes());
    out
}

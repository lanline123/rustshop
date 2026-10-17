//! 管理端 merchant 路由。

pub mod deposit_account;
pub mod deposit_recharge;
pub mod deposit_refund;
pub mod merchant_apply;
pub mod order_config;
pub mod shop;
pub mod shop_manage;
pub mod shop_product_category;
pub mod vendor_deposit_shop;

use axum::Router;

use super::super::AdminState;

/// 需登录的域路由（外层统一套 require_auth）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(deposit_account::routes(state))
        .merge(deposit_recharge::routes(state))
        .merge(deposit_refund::routes(state))
        .merge(merchant_apply::routes(state))
        .merge(order_config::routes(state))
        .merge(shop::routes(state))
        .merge(shop_manage::routes(state))
        .merge(shop_product_category::routes(state))
        .merge(vendor_deposit_shop::routes(state))
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
    out.extend(deposit_account::implemented_routes());
    out.extend(deposit_recharge::implemented_routes());
    out.extend(deposit_refund::implemented_routes());
    out.extend(merchant_apply::implemented_routes());
    out.extend(order_config::implemented_routes());
    out.extend(shop::implemented_routes());
    out.extend(shop_manage::implemented_routes());
    out.extend(shop_product_category::implemented_routes());
    out.extend(vendor_deposit_shop::implemented_routes());
    out
}

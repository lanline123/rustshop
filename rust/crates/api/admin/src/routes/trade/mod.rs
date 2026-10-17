//! 管理端 trade 路由。

pub mod aftersales;
pub mod order_admin;
pub mod proxy_shopping_order_check;
pub mod proxy_shopping_pickup_contact;
pub mod proxy_shopping_product;
pub mod proxy_shopping_user;
pub mod proxy_shopping_user_address;
pub mod proxy_shopping_user_coupon;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(aftersales::routes(state))
        .merge(order_admin::routes(state))
        .merge(proxy_shopping_order_check::routes(state))
        .merge(proxy_shopping_pickup_contact::routes(state))
        .merge(proxy_shopping_product::routes(state))
        .merge(proxy_shopping_user::routes(state))
        .merge(proxy_shopping_user_address::routes(state))
        .merge(proxy_shopping_user_coupon::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(aftersales::implemented_routes());
    out.extend(order_admin::implemented_routes());
    out.extend(proxy_shopping_order_check::implemented_routes());
    out.extend(proxy_shopping_pickup_contact::implemented_routes());
    out.extend(proxy_shopping_product::implemented_routes());
    out.extend(proxy_shopping_user::implemented_routes());
    out.extend(proxy_shopping_user_address::implemented_routes());
    out.extend(proxy_shopping_user_coupon::implemented_routes());
    out
}

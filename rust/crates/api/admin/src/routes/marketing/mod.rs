//! 管理端 marketing 路由。

pub mod buyer_show;
pub mod buyer_show_comment;
pub mod buyer_show_group;
pub mod coupon;
pub mod product_style_admin;
pub mod salesman;
pub mod salesman_material_category;
pub mod seckill;
pub mod time_discount;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(buyer_show::routes(state))
        .merge(buyer_show_comment::routes(state))
        .merge(buyer_show_group::routes(state))
        .merge(coupon::routes(state))
        .merge(product_style_admin::routes(state))
        .merge(salesman::routes(state))
        .merge(salesman_material_category::routes(state))
        .merge(seckill::routes(state))
        .merge(time_discount::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(buyer_show::implemented_routes());
    out.extend(buyer_show_comment::implemented_routes());
    out.extend(buyer_show_group::implemented_routes());
    out.extend(coupon::implemented_routes());
    out.extend(product_style_admin::implemented_routes());
    out.extend(salesman::implemented_routes());
    out.extend(salesman_material_category::implemented_routes());
    out.extend(seckill::implemented_routes());
    out.extend(time_discount::implemented_routes());
    out
}

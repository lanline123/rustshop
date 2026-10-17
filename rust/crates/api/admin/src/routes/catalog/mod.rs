//! 管理端 catalog 路由。

pub mod attributes_tpl;
pub mod brand;
pub mod category;
pub mod category_all;
pub mod product_manage;
pub mod product_stock;
pub mod stock_warning;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(attributes_tpl::routes(state))
        .merge(brand::routes(state))
        .merge(category::routes(state))
        .merge(category_all::routes(state))
        .merge(product_manage::routes(state))
        .merge(product_stock::routes(state))
        .merge(stock_warning::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(attributes_tpl::implemented_routes());
    out.extend(brand::implemented_routes());
    out.extend(category::implemented_routes());
    out.extend(category_all::implemented_routes());
    out.extend(product_manage::implemented_routes());
    out.extend(product_stock::implemented_routes());
    out.extend(stock_warning::implemented_routes());
    out
}

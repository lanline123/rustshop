//! 管理端 logistics 路由。

pub mod logistics_company;
pub mod logistics_company_config;
pub mod print_config;
pub mod shipping_list;
pub mod shipping_tpl;
pub mod shipping_type;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(logistics_company::routes(state))
        .merge(logistics_company_config::routes(state))
        .merge(print_config::routes(state))
        .merge(shipping_list::routes(state))
        .merge(shipping_tpl::routes(state))
        .merge(shipping_type::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(logistics_company::implemented_routes());
    out.extend(logistics_company_config::implemented_routes());
    out.extend(print_config::implemented_routes());
    out.extend(shipping_list::implemented_routes());
    out.extend(shipping_tpl::implemented_routes());
    out.extend(shipping_type::implemented_routes());
    out
}

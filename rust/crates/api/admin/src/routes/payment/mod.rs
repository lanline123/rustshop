//! 管理端 payment 路由。

pub mod category_fee_rule;
pub mod merchant_application;
pub mod order_invoice;
pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod tax_rate;
pub mod user_balance_log;

use axum::Router;

use super::super::AdminState;

/// 需登录的域路由（外层统一套 require_auth）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(category_fee_rule::routes(state))
        .merge(merchant_application::routes(state))
        .merge(order_invoice::routes(state))
        .merge(paylog::routes(state))
        .merge(paylog_batch::routes(state))
        .merge(refund_apply::routes(state))
        .merge(tax_rate::routes(state))
        .merge(user_balance_log::routes(state))
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
    out.extend(category_fee_rule::implemented_routes());
    out.extend(merchant_application::implemented_routes());
    out.extend(order_invoice::implemented_routes());
    out.extend(paylog::implemented_routes());
    out.extend(paylog_batch::implemented_routes());
    out.extend(refund_apply::implemented_routes());
    out.extend(tax_rate::implemented_routes());
    out.extend(user_balance_log::implemented_routes());
    out
}

//! 管理端 payment 路由。

pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod tax_rate;
pub mod user_balance_log;

use axum::Router;

use super::super::AdminState;

/// 汇总本域路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(paylog::routes(state))
        .merge(paylog_batch::routes(state))
        .merge(refund_apply::routes(state))
        .merge(tax_rate::routes(state))
        .merge(user_balance_log::routes(state))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(paylog::implemented_routes());
    out.extend(paylog_batch::implemented_routes());
    out.extend(refund_apply::implemented_routes());
    out.extend(tax_rate::implemented_routes());
    out.extend(user_balance_log::implemented_routes());
    out
}

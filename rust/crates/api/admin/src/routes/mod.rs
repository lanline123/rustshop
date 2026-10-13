//! 管理端路由按业务域拆分的子模块。
//!
//! 并行开发约定：每个业务域一个独立文件，只改自己域的文件，
//! 新增域路由通过 [`merge_all`] 统一挂载到 `/adminapi` 下。

pub mod catalog;
pub mod content;
pub mod coupon;
pub mod logistics;
pub mod member;
pub mod paylog;
pub mod refund_apply;
pub mod seckill;
pub mod time_discount;

use axum::Router;

use super::AdminState;

/// 汇总所有域路由（协调者维护，域实现者不改此函数）。
pub fn merge_all(state: &AdminState) -> Router<AdminState> {
    Router::new()
        .merge(coupon::routes(state))
        .merge(content::routes(state))
        .merge(catalog::routes(state))
        .merge(logistics::routes(state))
        .merge(member::routes(state))
        .merge(seckill::routes(state))
        .merge(time_discount::routes(state))
        .merge(paylog::routes(state))
        .merge(refund_apply::routes(state))
}

/// 汇总所有域已实现路由，供覆盖率统计使用。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(coupon::implemented_routes());
    out.extend(content::implemented_routes());
    out.extend(catalog::implemented_routes());
    out.extend(logistics::implemented_routes());
    out.extend(member::implemented_routes());
    out.extend(seckill::implemented_routes());
    out.extend(time_discount::implemented_routes());
    out.extend(paylog::implemented_routes());
    out.extend(refund_apply::implemented_routes());
    out
}

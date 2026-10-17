//! 管理端路由，按限界上下文分组。

pub mod identity;
pub mod merchant;
pub mod catalog;
pub mod trade;
pub mod payment;
pub mod marketing;
pub mod member;
pub mod logistics;
pub mod content;
pub mod system;
pub mod analytics;
pub mod openapi;

use axum::Router;

use super::AdminState;

/// 汇总所有需登录路由。
pub fn merge_all(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(identity::routes(state))
        .merge(merchant::routes(state))
        .merge(catalog::routes(state))
        .merge(trade::routes(state))
        .merge(payment::routes(state))
        .merge(marketing::routes(state))
        .merge(member::routes(state))
        .merge(logistics::routes(state))
        .merge(content::routes(state))
        .merge(system::routes(state))
        .merge(analytics::routes(state))
        .merge(openapi::routes(state))
}

/// 汇总所有免登录路由（`router()` 单独 nest 到 `/adminapi`）。
pub fn merge_public(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .merge(identity::public_routes(state))
        .merge(merchant::public_routes(state))
        .merge(catalog::public_routes(state))
        .merge(trade::public_routes(state))
        .merge(payment::public_routes(state))
        .merge(marketing::public_routes(state))
        .merge(member::public_routes(state))
        .merge(logistics::public_routes(state))
        .merge(content::public_routes(state))
        .merge(system::public_routes(state))
        .merge(analytics::public_routes(state))
        .merge(openapi::public_routes(state))
}

/// 汇总所有已实现路由，供覆盖率统计使用。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let mut out = Vec::new();
    out.extend(identity::implemented_routes());
    out.extend(merchant::implemented_routes());
    out.extend(catalog::implemented_routes());
    out.extend(trade::implemented_routes());
    out.extend(payment::implemented_routes());
    out.extend(marketing::implemented_routes());
    out.extend(member::implemented_routes());
    out.extend(logistics::implemented_routes());
    out.extend(content::implemented_routes());
    out.extend(system::implemented_routes());
    out.extend(analytics::implemented_routes());
    out.extend(openapi::implemented_routes());
    out
}

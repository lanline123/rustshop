//! 商城前台路由按业务域拆分的子模块。
//!
//! 并行开发约定：每个业务域一个独立文件，只改自己域的文件，
//! 新增域路由通过 [`merge_all`] 统一挂载到 `/api` 下（需登录的接口由
//! 各域文件自行套 `require_auth` 中间件）。

pub mod catalog;
pub mod content;
pub mod coupon;
pub mod logistics;
pub mod member;
pub mod paylog;
pub mod seckill;
pub mod time_discount;

use axum::Router;

use super::PublicState;

/// 汇总**公开**域路由：挂载于 `/api` 下，无需登录。
/// 域实现者把公开接口加到各自文件的 `public_routes`。
pub fn merge_public(_state: &PublicState) -> Router<PublicState> {
    Router::new()
        .merge(coupon::public_routes())
        .merge(content::public_routes())
        .merge(catalog::public_routes())
        .merge(logistics::public_routes())
        .merge(member::public_routes())
        .merge(seckill::public_routes())
        .merge(time_discount::public_routes())
        .merge(paylog::public_routes())
}

/// 汇总**需登录**域路由：挂载于 `/api` 下，外层已套 `require_auth`。
/// 域实现者把需登录接口加到各自文件的 `authorized_routes`。
pub fn merge_authorized(_state: &PublicState) -> Router<PublicState> {
    Router::new()
        .merge(coupon::authorized_routes())
        .merge(content::authorized_routes())
        .merge(catalog::authorized_routes())
        .merge(logistics::authorized_routes())
        .merge(member::authorized_routes())
        .merge(seckill::authorized_routes())
        .merge(time_discount::authorized_routes())
        .merge(paylog::authorized_routes())
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
    out
}

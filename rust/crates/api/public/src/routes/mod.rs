//! 商城前台路由按业务域拆分的子模块。
//!
//! ## 两种并存的约定
//!
//! 1. **早期域**（`public_routes()` / `authorized_routes()`）：无状态函数，
//!    需登录的接口由 [`merge_authorized`] 外层统一套 `require_auth`。
//! 2. **切片域**（`routes(state)`）：并行重构引入，接收 `&PublicState`；
//!    这些模块**自带鉴权中间件**（`require_auth` 或 `optional_auth`），
//!    因此一律并入 [`merge_public`]，**外层不得再套 `require_auth`**，
//!    否则会双重鉴权或把匿名可访问的接口变成必须登录。
//!
//! ## 新增切片域时
//!
//! 请用 `python tools/register_slices.py` 生成注册代码，它会按
//! `has_public` 把 `pub mod` / `.merge(...::routes(state))` /
//! `out.extend(...::implemented_routes())` 插到本文件正确位置。
//! 插入后必须运行 `python tools/check_route_conflicts.py --strict`。

pub mod balance_pay;
pub mod catalog;
pub mod content;
pub mod coupon;
pub mod decorate;
pub mod logistics;
pub mod member;
pub mod paylog;
pub mod salesman;
pub mod seckill;
// —— 切片域（自带鉴权，见 [merge_public]）——
pub mod shop;
pub mod slice001;
pub mod slice172;
pub mod slice091;
pub mod slice170;
pub mod slice082;
pub mod slice214;
pub mod slice213;
pub mod slice206;
pub mod slice200;
pub mod slice171;
pub mod slice168;
pub mod slice160;
pub mod slice148;
pub mod slice135;
pub mod slice130;
pub mod slice084;
pub mod slice073;
pub mod slice066;
pub mod slice065;
pub mod slice059;
pub mod slice143;
pub mod slice141;
pub mod slice117;
pub mod slice115;
pub mod slice106;
pub mod slice090;
pub mod slice022;
pub mod slice021;
pub mod slice016;
pub mod slice015;
pub mod slice004;
pub mod slice007;
pub mod slice009;
pub mod slice013;
pub mod time_discount;

use axum::Router;

use super::PublicState;

/// 汇总**公开**路由：挂载于 `/api` 下。
///
/// 注意：切片域 `routes(state)` 自带鉴权中间件，故一并放这里，
/// 由模块自己决定哪些接口必须登录（见模块头说明）。
pub fn merge_public(state: &PublicState) -> Router<PublicState> {
    Router::new()
        // —— 早期无状态域 ——
        .merge(coupon::public_routes())
        .merge(content::public_routes())
        .merge(catalog::public_routes())
        .merge(logistics::public_routes())
        .merge(member::public_routes())
        .merge(seckill::public_routes())
        .merge(time_discount::public_routes())
        .merge(paylog::public_routes())
        .merge(shop::public_routes())
        .merge(decorate::public_routes())
        .merge(salesman::public_routes())
        // —— 切片域（自带鉴权中间件，外层不要再套）——
        .merge(slice001::routes(state))
.merge(slice172::routes(state))
.merge(slice091::routes(state))
.merge(slice170::routes(state))
.merge(slice082::routes(state))
.merge(slice214::routes(state))
.merge(slice213::routes(state))
.merge(slice206::routes(state))
.merge(slice200::routes(state))
.merge(slice171::routes(state))
.merge(slice168::routes(state))
.merge(slice160::routes(state))
.merge(slice148::routes(state))
.merge(slice135::routes(state))
.merge(slice130::routes(state))
.merge(slice084::routes(state))
.merge(slice073::routes(state))
.merge(slice066::routes(state))
.merge(slice065::routes(state))
.merge(slice059::routes(state))
.merge(slice143::routes(state))
.merge(slice141::routes(state))
.merge(slice117::routes(state))
.merge(slice115::routes(state))
.merge(slice106::routes(state))
.merge(slice090::routes(state))
.merge(slice022::routes(state))
.merge(slice021::routes(state))
.merge(slice016::routes(state))
.merge(slice015::routes(state))
        .merge(slice004::routes(state))
        .merge(slice007::routes(state))
        .merge(slice009::routes(state))
        .merge(slice013::routes(state))
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
        .merge(balance_pay::authorized_routes())
        .merge(shop::authorized_routes())
        .merge(decorate::authorized_routes())
        .merge(salesman::authorized_routes())
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
    out.extend(balance_pay::implemented_routes());
    out.extend(shop::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(salesman::implemented_routes());
    // —— 切片域 ——
    out.extend(slice001::implemented_routes());
out.extend(slice172::implemented_routes());
out.extend(slice091::implemented_routes());
out.extend(slice170::implemented_routes());
out.extend(slice082::implemented_routes());
out.extend(slice214::implemented_routes());
out.extend(slice213::implemented_routes());
out.extend(slice206::implemented_routes());
out.extend(slice200::implemented_routes());
out.extend(slice171::implemented_routes());
out.extend(slice168::implemented_routes());
out.extend(slice160::implemented_routes());
out.extend(slice148::implemented_routes());
out.extend(slice135::implemented_routes());
out.extend(slice130::implemented_routes());
out.extend(slice084::implemented_routes());
out.extend(slice073::implemented_routes());
out.extend(slice066::implemented_routes());
out.extend(slice065::implemented_routes());
out.extend(slice059::implemented_routes());
out.extend(slice143::implemented_routes());
out.extend(slice141::implemented_routes());
out.extend(slice117::implemented_routes());
out.extend(slice115::implemented_routes());
out.extend(slice106::implemented_routes());
out.extend(slice090::implemented_routes());
out.extend(slice022::implemented_routes());
out.extend(slice021::implemented_routes());
out.extend(slice016::implemented_routes());
out.extend(slice015::implemented_routes());
    out.extend(slice004::implemented_routes());
    out.extend(slice007::implemented_routes());
    out.extend(slice009::implemented_routes());
    out.extend(slice013::implemented_routes());
    out
}

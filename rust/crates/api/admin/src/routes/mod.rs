//! 管理端路由按业务域拆分的子模块。
//!
//! ## 约定
//!
//! - 每个业务域/切片一个独立文件，域实现者只改自己的文件。
//! - 新增域路由通过 [`merge_all`] 统一挂载到 `/adminapi` 下。
//! - 切片域统一签名 `pub fn routes(state: &AdminState) -> Router<AdminState>`
//!   并提供 `pub fn implemented_routes() -> Vec<RouteDoc>`。
//!
//! ## 合并方式
//!
//! 用 `python tools/register_slices.py --dir docs/registrations` 生成注册代码；
//! 合并后**必须**运行 `python tools/check_route_conflicts.py --strict`
//! （同一 `(method, path)` 被两处注册会让 axum 在服务启动时 panic）。

pub mod catalog;
pub mod content;
pub mod coupon;
pub mod decorate;
pub mod logistics;
pub mod member;
pub mod paylog;
pub mod refund_apply;
pub mod salesman;
pub mod seckill;
pub mod shop;
pub mod time_discount;
// —— 切片域（并行重构，按序号）——
pub mod slice001;
pub mod slice002;
pub mod slice044;
pub mod slice039;
pub mod slice210;
pub mod slice163;
pub mod slice157;
pub mod slice137;
pub mod slice108;
pub mod slice085;
pub mod slice038;
pub mod slice037;
pub mod slice220;
pub mod slice209;
pub mod slice201;
pub mod slice185;
pub mod slice092;
pub mod slice081;
pub mod slice166;
pub mod slice144;
pub mod slice139;
pub mod slice075;
pub mod slice217;
pub mod slice205;
pub mod slice192;
pub mod slice186;
pub mod slice120;
pub mod slice093;
pub mod slice056;
pub mod slice216;
pub mod slice208;
pub mod slice203;
pub mod slice199;
pub mod slice197;
pub mod slice193;
pub mod slice191;
pub mod slice190;
pub mod slice189;
pub mod slice181;
pub mod slice176;
pub mod slice174;
pub mod slice171;
pub mod slice136;
pub mod slice135;
pub mod slice133;
pub mod slice124;
pub mod slice116;
pub mod slice114;
pub mod slice113;
pub mod slice102;
pub mod slice098;
pub mod slice064;
pub mod slice063;
pub mod slice051;
pub mod slice047;
pub mod slice036;
pub mod slice034;
pub mod slice164;
pub mod slice147;
pub mod slice132;
pub mod slice118;
pub mod slice111;
pub mod slice094;
pub mod slice053;
pub mod slice100;
pub mod slice042;
pub mod slice032;
pub mod slice031;
pub mod slice030;
pub mod slice029;
pub mod slice028;
pub mod slice027;
pub mod slice188;
pub mod slice187;
pub mod slice026;
pub mod slice025;
pub mod slice024;
pub mod slice023;
pub mod slice021;
pub mod slice019;
pub mod slice018;
pub mod slice017;
pub mod slice003;
pub mod slice004;
pub mod slice005;
pub mod slice006;
pub mod slice008;
pub mod slice010;
pub mod slice011;
pub mod slice012;
pub mod slice014;

use axum::Router;

use super::AdminState;

/// 汇总所有域路由（协调者维护，域实现者不改此函数）。
pub fn merge_all(state: &AdminState) -> Router<AdminState> {
    Router::new()
        // —— 早期域 ——
        .merge(coupon::routes(state))
        .merge(content::routes(state))
        .merge(catalog::routes(state))
        .merge(logistics::routes(state))
        .merge(member::routes(state))
        .merge(seckill::routes(state))
        .merge(time_discount::routes(state))
        .merge(paylog::routes(state))
        .merge(refund_apply::routes(state))
        .merge(shop::routes(state))
        .merge(decorate::routes(state))
        .merge(salesman::routes(state))
        // —— 切片域 ——
        .merge(slice001::routes(state))
        .merge(slice002::routes(state))
        .merge(slice003::routes(state))
        .merge(slice004::routes(state))
        .merge(slice005::routes(state))
.merge(slice044::routes(state))
.merge(slice039::routes(state))
.merge(slice210::routes(state))
.merge(slice163::routes(state))
.merge(slice157::routes(state))
.merge(slice137::routes(state))
.merge(slice108::routes(state))
.merge(slice085::routes(state))
.merge(slice038::routes(state))
.merge(slice037::routes(state))
.merge(slice220::routes(state))
.merge(slice209::routes(state))
.merge(slice201::routes(state))
.merge(slice185::routes(state))
.merge(slice092::routes(state))
.merge(slice081::routes(state))
.merge(slice166::routes(state))
.merge(slice144::routes(state))
.merge(slice139::routes(state))
.merge(slice075::routes(state))
.merge(slice217::routes(state))
.merge(slice205::routes(state))
.merge(slice192::routes(state))
.merge(slice186::routes(state))
.merge(slice120::routes(state))
.merge(slice093::routes(state))
.merge(slice056::routes(state))
.merge(slice216::routes(state))
.merge(slice208::routes(state))
.merge(slice203::routes(state))
.merge(slice199::routes(state))
.merge(slice197::routes(state))
.merge(slice193::routes(state))
.merge(slice191::routes(state))
.merge(slice190::routes(state))
.merge(slice189::routes(state))
.merge(slice181::routes(state))
.merge(slice176::routes(state))
.merge(slice174::routes(state))
.merge(slice171::routes(state))
.merge(slice136::routes(state))
.merge(slice135::routes(state))
.merge(slice133::routes(state))
.merge(slice124::routes(state))
.merge(slice116::routes(state))
.merge(slice114::routes(state))
.merge(slice113::routes(state))
.merge(slice102::routes(state))
.merge(slice098::routes(state))
.merge(slice064::routes(state))
.merge(slice063::routes(state))
.merge(slice051::routes(state))
.merge(slice047::routes(state))
.merge(slice036::routes(state))
.merge(slice034::routes(state))
.merge(slice164::routes(state))
.merge(slice147::routes(state))
.merge(slice132::routes(state))
.merge(slice118::routes(state))
.merge(slice111::routes(state))
.merge(slice094::routes(state))
.merge(slice053::routes(state))
.merge(slice100::routes(state))
.merge(slice042::routes(state))
.merge(slice032::routes(state))
.merge(slice031::routes(state))
.merge(slice030::routes(state))
.merge(slice029::routes(state))
.merge(slice028::routes(state))
.merge(slice027::routes(state))
.merge(slice188::routes(state))
.merge(slice187::routes(state))
.merge(slice026::routes(state))
.merge(slice025::routes(state))
.merge(slice024::routes(state))
.merge(slice023::routes(state))
.merge(slice021::routes(state))
.merge(slice019::routes(state))
.merge(slice018::routes(state))
.merge(slice017::routes(state))
        .merge(slice006::routes(state))
        .merge(slice008::routes(state))
        .merge(slice010::routes(state))
        .merge(slice011::routes(state))
        .merge(slice012::routes(state))
        .merge(slice014::routes(state))
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
    out.extend(shop::implemented_routes());
    out.extend(decorate::implemented_routes());
    out.extend(salesman::implemented_routes());
    out.extend(slice001::implemented_routes());
    out.extend(slice002::implemented_routes());
    out.extend(slice003::implemented_routes());
    out.extend(slice004::implemented_routes());
    out.extend(slice005::implemented_routes());
out.extend(slice044::implemented_routes());
out.extend(slice039::implemented_routes());
out.extend(slice210::implemented_routes());
out.extend(slice163::implemented_routes());
out.extend(slice157::implemented_routes());
out.extend(slice137::implemented_routes());
out.extend(slice108::implemented_routes());
out.extend(slice085::implemented_routes());
out.extend(slice038::implemented_routes());
out.extend(slice037::implemented_routes());
out.extend(slice220::implemented_routes());
out.extend(slice209::implemented_routes());
out.extend(slice201::implemented_routes());
out.extend(slice185::implemented_routes());
out.extend(slice092::implemented_routes());
out.extend(slice081::implemented_routes());
out.extend(slice166::implemented_routes());
out.extend(slice144::implemented_routes());
out.extend(slice139::implemented_routes());
out.extend(slice075::implemented_routes());
out.extend(slice217::implemented_routes());
out.extend(slice205::implemented_routes());
out.extend(slice192::implemented_routes());
out.extend(slice186::implemented_routes());
out.extend(slice120::implemented_routes());
out.extend(slice093::implemented_routes());
out.extend(slice056::implemented_routes());
out.extend(slice216::implemented_routes());
out.extend(slice208::implemented_routes());
out.extend(slice203::implemented_routes());
out.extend(slice199::implemented_routes());
out.extend(slice197::implemented_routes());
out.extend(slice193::implemented_routes());
out.extend(slice191::implemented_routes());
out.extend(slice190::implemented_routes());
out.extend(slice189::implemented_routes());
out.extend(slice181::implemented_routes());
out.extend(slice176::implemented_routes());
out.extend(slice174::implemented_routes());
out.extend(slice171::implemented_routes());
out.extend(slice136::implemented_routes());
out.extend(slice135::implemented_routes());
out.extend(slice133::implemented_routes());
out.extend(slice124::implemented_routes());
out.extend(slice116::implemented_routes());
out.extend(slice114::implemented_routes());
out.extend(slice113::implemented_routes());
out.extend(slice102::implemented_routes());
out.extend(slice098::implemented_routes());
out.extend(slice064::implemented_routes());
out.extend(slice063::implemented_routes());
out.extend(slice051::implemented_routes());
out.extend(slice047::implemented_routes());
out.extend(slice036::implemented_routes());
out.extend(slice034::implemented_routes());
out.extend(slice164::implemented_routes());
out.extend(slice147::implemented_routes());
out.extend(slice132::implemented_routes());
out.extend(slice118::implemented_routes());
out.extend(slice111::implemented_routes());
out.extend(slice094::implemented_routes());
out.extend(slice053::implemented_routes());
out.extend(slice100::implemented_routes());
out.extend(slice042::implemented_routes());
out.extend(slice032::implemented_routes());
out.extend(slice031::implemented_routes());
out.extend(slice030::implemented_routes());
out.extend(slice029::implemented_routes());
out.extend(slice028::implemented_routes());
out.extend(slice027::implemented_routes());
out.extend(slice188::implemented_routes());
out.extend(slice187::implemented_routes());
out.extend(slice026::implemented_routes());
out.extend(slice025::implemented_routes());
out.extend(slice024::implemented_routes());
out.extend(slice023::implemented_routes());
out.extend(slice021::implemented_routes());
out.extend(slice019::implemented_routes());
out.extend(slice018::implemented_routes());
out.extend(slice017::implemented_routes());
    out.extend(slice006::implemented_routes());
    out.extend(slice008::implemented_routes());
    out.extend(slice010::implemented_routes());
    out.extend(slice011::implemented_routes());
    out.extend(slice012::implemented_routes());
    out.extend(slice014::implemented_routes());
    out
}

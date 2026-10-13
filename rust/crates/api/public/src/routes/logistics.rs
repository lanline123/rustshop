//! 商城前台物流路由。
//! 对应原 Java `tigshop-api` 中运费计算相关接口（如有）。
//!
//! TODO(域实现者)：对照 Java 源补全前台可用配送方式/运费试算接口。

use axum::Router;

use super::super::PublicState;

/// 公开路由：暂无（TODO 按 Java 源补全）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
}

/// 需登录路由：暂无（TODO 按 Java 源补全）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
}

/// 本域已实现路由（当前为空；补全前台运费接口后在此登记）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

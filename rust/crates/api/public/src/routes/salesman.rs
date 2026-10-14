//! 商城前台分销员域路由。
//! 对应原 Java `controller.salesman.SalesmanController` /
//! `SalesmanContentController` / `SalesmanMaterialController` /
//! `SalesmanOrderController` / `SalesmanProductController` /
//! `SalesmanCardInviteController`。
//!
//! TODO(域实现者)：对照 Java 控制器补全分销员中心、推广商品、佣金订单等接口。

use axum::Router;

use super::super::PublicState;

/// 公开路由：暂无（TODO 按 Java 源补全）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
}

/// 需登录路由：分销员中心（TODO 按 Java 源补全）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
}

/// 本域已实现路由（用于覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

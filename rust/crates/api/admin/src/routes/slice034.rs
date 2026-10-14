//! 代客下单配送方式 —— 管理端路由（slice-034）骨架。
//!
//! 原 Java：`ProxyShoppingDeliveryController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/proxyshopping/ProxyShoppingDeliveryController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /proxyShopping/delivery/getDeliveryOptions` | `GET /adminapi/proxyShopping/delivery/getDeliveryOptions` | `ProxyShoppingDeliveryController.getDeliveryOptions` | jwt+perm |
//! | `GET /proxyShopping/delivery/getStoreShippingType` | `GET /adminapi/proxyShopping/delivery/getStoreShippingType` | `ProxyShoppingDeliveryController.getStoreShippingType` | jwt+perm |
//!
//! 状态：骨架已注册并可编译，handler 待域实现者填充。
//! 路由串写**去掉 `/adminapi` 前缀**后的形式，`RouteDoc.path` 写完整契约路径。

use axum::Router;

use super::super::AdminState;

/// 域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 已实现路由清单（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

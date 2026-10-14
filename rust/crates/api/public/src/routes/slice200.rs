//! 购物车管理 —— 商城前台路由（slice-200）骨架。
//!
//! 原 Java：`CartController`（`tigshop-api/src/main/java/com/tigshop/api/controller/cart/CartController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /cart/cart/getCount` | `GET /api/cart/cart/getCount` | `CartController.getCount` | optional |
//! | `GET /cart/cart/getCouponDiscount` | `GET /api/cart/cart/getCouponDiscount` | `CartController.getCouponDiscount` | optional |
//!
//! 状态：骨架已注册并可编译，handler 待域实现者填充。
//! 路由串写**去掉 `/api` 前缀**后的形式，`RouteDoc.path` 写完整契约路径。

use axum::Router;

use super::super::PublicState;

/// 域路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
}

/// 已实现路由清单（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

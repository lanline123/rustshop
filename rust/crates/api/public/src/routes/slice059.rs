//! 分销员分组 —— 商城前台路由（slice-059）骨架。
//!
//! 原 Java：`SalesmanMaterialController`（`tigshop-api/src/main/java/com/tigshop/api/controller/salesman/SalesmanMaterialController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /salesman/material/list` | `GET /api/salesman/material/list` | `SalesmanMaterialController.apiPage` | optional |
//! | `GET /salesman/material/detail` | `GET /api/salesman/material/detail` | `SalesmanMaterialController.detail` | optional |
//! | `GET /salesman/material/category` | `GET /api/salesman/material/category` | `SalesmanMaterialController.category` | optional |
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

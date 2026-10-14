//! 部分通用功能的控制器 —— 商城前台路由（slice-206）骨架。
//!
//! 原 Java：`UtilController`（`tigshop-api/src/main/java/com/tigshop/api/controller/common/UtilController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /common/util/qrCode` | `GET /api/common/util/qrCode` | `UtilController.qrCode` | optional |
//! | `GET /common/util/miniCode` | `GET /api/common/util/miniCode` | `UtilController.miniCode` | optional |
//! | `GET /common/util/miniScene` | `GET /api/common/util/miniScene` | `UtilController.miniScene` | optional |
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

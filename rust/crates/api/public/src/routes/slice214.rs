//! 鐢ㄦ埛绛惧埌涓績 —— 商城前台路由（slice-214）骨架。
//!
//! 原 Java：`UserSignController`（`tigshop-api/src/main/java/com/tigshop/api/controller/user/UserSignController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /user/sign/index` | `GET /api/user/sign/index` | `UserSignController.getUserSignInfoVo` | optional |
//! | `GET /user/sign/sign` | `GET /api/user/sign/sign` | `UserSignController.sign` | optional |
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

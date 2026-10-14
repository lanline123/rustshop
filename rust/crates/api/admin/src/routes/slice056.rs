//! 分账结算 —— 管理端路由（slice-056）骨架。
//!
//! 原 Java：`ProfitSharingController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/finance/ProfitSharingController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /finance/profitSharing/list` | `GET /adminapi/finance/profitSharing/list` | `ProfitSharingController.list` | jwt |
//! | `POST /finance/profitSharing/submitBatch` | `POST /adminapi/finance/profitSharing/submitBatch` | `ProfitSharingController.submitBatch` | jwt+perm |
//! | `POST /finance/profitSharing/manualFinish` | `POST /adminapi/finance/profitSharing/manualFinish` | `ProfitSharingController.manualFinish` | jwt+perm |
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

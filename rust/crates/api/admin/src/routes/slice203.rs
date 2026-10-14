//! 退款申请 —— 管理端路由（slice-203）骨架。
//!
//! 原 Java：`RefundApplyController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/finance/RefundApplyController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /finance/refundApply/config` | `GET /adminapi/finance/refundApply/config` | `RefundApplyController.config` | jwt |
//! | `GET /finance/refundApply/list` | `GET /adminapi/finance/refundApply/list` | `RefundApplyController.list` | jwt |
//! | `GET /finance/refundApply/detail` | `GET /adminapi/finance/refundApply/detail` | `RefundApplyController.detail` | jwt |
//! | `POST /finance/refundApply/audit` | `POST /adminapi/finance/refundApply/audit` | `RefundApplyController.audit` | jwt+perm |
//! | `POST /finance/refundApply/offlineAudit` | `POST /adminapi/finance/refundApply/offlineAudit` | `RefundApplyController.offlineAudit` | jwt+perm |
//! | `POST /finance/refundApply/create` | `POST /adminapi/finance/refundApply/create` | `RefundApplyController.create` | jwt |
//! | `POST /finance/refundApply/update` | `POST /adminapi/finance/refundApply/update` | `RefundApplyController.update` | jwt+perm |
//! | `POST /finance/refundApply/del` | `POST /adminapi/finance/refundApply/del` | `RefundApplyController.del` | jwt |
//! | `POST /finance/refundApply/updateField` | `POST /adminapi/finance/refundApply/updateField` | `RefundApplyController.updateField` | jwt |
//! | `POST /finance/refundApply/batch` | `POST /adminapi/finance/refundApply/batch` | `RefundApplyController.batch` | jwt |
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

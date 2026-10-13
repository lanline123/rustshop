//! 管理端退款申请路由。
//! 对应原 Java `controller.finance.RefundApplyController`
//! （`@RequestMapping("/adminapi/finance/refundApply")`）。
//!
//! | 本模块路由 | 原 Java |
//! |-----------|--------|
//! | `GET  /finance/refundApply/config` | `RefundApplyController.config` |
//! | `GET  /finance/refundApply/list` | `RefundApplyController.list` |
//! | `GET  /finance/refundApply/detail` | `RefundApplyController.detail` |
//! | `POST /finance/refundApply/audit` | `RefundApplyController.audit` |
//! | `POST /finance/refundApply/offlineAudit` | `RefundApplyController.offlineAudit` |
//! | `POST /finance/refundApply/create` | `RefundApplyController.create` |
//! | `POST /finance/refundApply/update` | `RefundApplyController.update` |
//!
//! 未迁移：`del` / `updateField` / `batch`（退款申请为审计数据，
//! 原系统同样对删除有严格限制；待确认业务口径后补齐）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::refund_apply::{
    OfflineAuditDTO, RefundApplyAuditDTO, RefundApplyCreateDTO, RefundApplyDetailVO,
    RefundApplyListQuery, RefundApplyUpdateDTO, RefundApplyVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 退款申请域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/finance/refundApply/config", get(config))
        .route("/finance/refundApply/list", get(list))
        .route("/finance/refundApply/detail", get(detail))
        .route("/finance/refundApply/audit", post(audit))
        .route("/finance/refundApply/offlineAudit", post(offline_audit))
        .route("/finance/refundApply/create", post(create))
        .route("/finance/refundApply/update", post(update))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/finance/refundApply/config", "RefundApplyController.config"),
        ("GET", "/adminapi/finance/refundApply/list", "RefundApplyController.list"),
        ("GET", "/adminapi/finance/refundApply/detail", "RefundApplyController.detail"),
        ("POST", "/adminapi/finance/refundApply/audit", "RefundApplyController.audit"),
        ("POST", "/adminapi/finance/refundApply/offlineAudit", "RefundApplyController.offlineAudit"),
        ("POST", "/adminapi/finance/refundApply/create", "RefundApplyController.create"),
        ("POST", "/adminapi/finance/refundApply/update", "RefundApplyController.update"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/finance/refundApply/config`：审核状态配置。
///
/// 返回 `[{status: 0, name: "待处理"}, ...]`。
async fn config() -> ApiResult<Vec<serde_json::Value>> {
    let cfg: Vec<serde_json::Value> = tigshop_services::refund_apply::RefundApplyService::status_config()
        .into_iter()
        .map(|(status, name)| {
            serde_json::json!({ "status": status, "name": name })
        })
        .collect();
    ok(cfg)
}

/// `GET /adminapi/finance/refundApply/list`：退款申请分页。
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<RefundApplyListQuery>,
) -> ApiResult<PageResult<RefundApplyVO>> {
    ok(state.refund_apply.list(&q).await?)
}

/// `GET /adminapi/finance/refundApply/detail?id=`：申请详情（含日志）。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<RefundApplyDetailVO> {
    ok(state.refund_apply.detail(q.id).await?)
}

/// `POST /adminapi/finance/refundApply/audit`：财务审核。
async fn audit(
    State(state): State<AdminState>,
    Json(dto): Json<RefundApplyAuditDTO>,
) -> ApiResult<&'static str> {
    state.refund_apply.audit(&dto).await?;
    ok("审核完成")
}

/// `POST /adminapi/finance/refundApply/offlineAudit`：确认线下转账。
async fn offline_audit(
    State(state): State<AdminState>,
    Json(dto): Json<OfflineAuditDTO>,
) -> ApiResult<&'static str> {
    state.refund_apply.offline_audit(&dto).await?;
    ok("已确认线下转账")
}

/// `POST /adminapi/finance/refundApply/create`：创建退款申请。
async fn create(
    State(state): State<AdminState>,
    Json(dto): Json<RefundApplyCreateDTO>,
) -> ApiResult<i32> {
    ok(state.refund_apply.create(&dto).await?)
}

/// `POST /adminapi/finance/refundApply/update`：更新申请（仅待处理）。
async fn update(
    State(state): State<AdminState>,
    Json(dto): Json<RefundApplyUpdateDTO>,
) -> ApiResult<&'static str> {
    state.refund_apply.update(&dto).await?;
    ok("更新成功")
}

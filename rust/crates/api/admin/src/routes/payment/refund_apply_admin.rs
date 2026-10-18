//! 管理端退款申请路由（payment::refund_apply_admin，3 条）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.finance.RefundApplyController`
//! 的 `del` / `updateField` / `batch`（其余 refundApply 端点已由
//! `payment::refund_apply` 实现，本 feature 只补这 3 条）：
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java 方法 |
//! |----------------------------------|--------------|
//! | `POST /finance/refundApply/del` | `del` |
//! | `POST /finance/refundApply/updateField` | `updateField` |
//! | `POST /finance/refundApply/batch` | `batch` |

use axum::{
    extract::State,
    routing::post,
    Json, Router,
};
use tigshop_core::models::marketing::groupon_promotion::OperateDTO;
use tigshop_core::models::merchant::shop_manage::{BatchDTO, UpdateFieldDTO};
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 3 条路由（`/adminapi` 前缀之后的路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    ("POST", "/finance/refundApply/del", "RefundApplyController.del"),
    (
        "POST",
        "/finance/refundApply/updateField",
        "RefundApplyController.updateField",
    ),
    (
        "POST",
        "/finance/refundApply/batch",
        "RefundApplyController.batch",
    ),
];

/// 退款申请管理路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/finance/refundApply/del", post(del))
        .route("/finance/refundApply/updateField", post(update_field))
        .route("/finance/refundApply/batch", post(batch))
}

/// 本域已实现路由（完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

async fn del(State(state): State<AdminState>, Json(dto): Json<OperateDTO>) -> ApiResult<()> {
    state
        .payment
        .refund_apply_admin
        .del(dto.id.map(|v| v as i32))
        .await?;
    ok(())
}

async fn update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<()> {
    state.payment.refund_apply_admin.update_field(&dto).await?;
    ok(())
}

async fn batch(State(state): State<AdminState>, Json(dto): Json<BatchDTO>) -> ApiResult<()> {
    state.payment.refund_apply_admin.batch(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("POST", "/adminapi/finance/refundApply/del"),
            ("POST", "/adminapi/finance/refundApply/updateField"),
            ("POST", "/adminapi/finance/refundApply/batch"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 3);
        for (doc, (method, path)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *method);
            assert_eq!(doc.path, *path);
        }
    }

    #[test]
    fn route_table_slice_is_exact() {
        assert_eq!(ROUTE_TABLE.len(), 3);
        assert!(ROUTE_TABLE
            .iter()
            .all(|(_, p, _)| p.starts_with("/finance/refundApply/")));
    }
}

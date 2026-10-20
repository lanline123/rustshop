//! 管理端分账结算路由（payment::profit_sharing）。
//! Java `ProfitSharingController`：`/adminapi/finance/profitSharing/{list,submitBatch,manualFinish}`。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::payment::profit_sharing::{
    ProfitSharingOrderVO, ProfitSharingPageQuery, ProfitSharingSubmitParam,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    (
        "GET",
        "/finance/profitSharing/list",
        "ProfitSharingController.list",
    ),
    (
        "POST",
        "/finance/profitSharing/submitBatch",
        "ProfitSharingController.submitBatch",
    ),
    (
        "POST",
        "/finance/profitSharing/manualFinish",
        "ProfitSharingController.manualFinish",
    ),
];

/// 分账结算域路由（挂载于 `/adminapi` 下，外层 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/finance/profitSharing/list", get(list))
        .route("/finance/profitSharing/submitBatch", post(submit_batch))
        .route("/finance/profitSharing/manualFinish", post(manual_finish))
}

/// 覆盖率统计用路由清单。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, &format!("/adminapi{p}"), j))
        .collect()
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct IdQuery {
    id: i64,
}

async fn list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(mut q): Query<ProfitSharingPageQuery>,
) -> ApiResult<PageResult<ProfitSharingOrderVO>> {
    q.shop_id = auth.resolve_shop_scope(q.shop_id)?;
    ok(state.payment.profit_sharing.list(&q).await?)
}

async fn submit_batch(
    State(state): State<AdminState>,
    Json(param): Json<ProfitSharingSubmitParam>,
) -> ApiResult<()> {
    // 提交参数只含 settlementIds（无 shopId）；归属校验由 service 按结算单逐一执行
    // （见模块头「多租户」说明：本域剩余缺口）。
    state.payment.profit_sharing.submit_batch(&param).await?;
    ok(())
}

async fn manual_finish(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<IdQuery>,
) -> ApiResult<()> {
    state
        .payment
        .profit_sharing
        .manual_finish(q.id, i64::from(auth.id))
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_java_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 3);
        for (m, p) in [
            ("GET", "/adminapi/finance/profitSharing/list"),
            ("POST", "/adminapi/finance/profitSharing/submitBatch"),
            ("POST", "/adminapi/finance/profitSharing/manualFinish"),
        ] {
            assert!(docs.iter().any(|d| d.method == m && d.path == p), "{m} {p}");
        }
        let mut keys: Vec<String> = docs.iter().map(tigshop_openapi::RouteDoc::key).collect();
        let mut unique = keys.clone();
        unique.sort();
        unique.dedup();
        keys.sort();
        assert_eq!(unique, keys);
    }

    #[test]
    fn id_query_deserializes() {
        let q: IdQuery = serde_json::from_str(r#"{"id": 12}"#).unwrap();
        assert_eq!(q.id, 12);
    }
}

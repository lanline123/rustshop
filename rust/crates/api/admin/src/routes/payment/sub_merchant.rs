//! 管理端子商户路由（payment::sub_merchant）。
//! Java `SubMerchantController`：`/adminapi/finance/subMerchant/{list,bind,unbind}`。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::payment::sub_merchant::{
    SubMerchantBindParam, SubMerchantPageQuery, SubMerchantVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    ("GET", "/finance/subMerchant/list", "SubMerchantController.list"),
    ("POST", "/finance/subMerchant/bind", "SubMerchantController.bind"),
    (
        "POST",
        "/finance/subMerchant/unbind",
        "SubMerchantController.unbind",
    ),
];

/// 子商户域路由（挂载于 `/adminapi` 下，外层 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/finance/subMerchant/list", get(list))
        .route("/finance/subMerchant/bind", post(bind))
        .route("/finance/subMerchant/unbind", post(unbind))
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
    Query(q): Query<SubMerchantPageQuery>,
) -> ApiResult<PageResult<SubMerchantVO>> {
    ok(state.payment.sub_merchant.list(&q).await?)
}

async fn bind(
    State(state): State<AdminState>,
    Json(param): Json<SubMerchantBindParam>,
) -> ApiResult<()> {
    state.payment.sub_merchant.bind(&param).await?;
    ok(())
}

async fn unbind(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<()> {
    state.payment.sub_merchant.unbind(q.id).await?;
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
            ("GET", "/adminapi/finance/subMerchant/list"),
            ("POST", "/adminapi/finance/subMerchant/bind"),
            ("POST", "/adminapi/finance/subMerchant/unbind"),
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
        assert!(serde_json::from_str::<IdQuery>(r#"{"id": "x"}"#).is_err());
    }
}

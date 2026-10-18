//! 店铺保证金交易明细 —— 管理端路由（merchant::deposit_log）—— wave9。
//!
//! 原 Java `DepositLogController`（`@RequestMapping("/adminapi/deposit/log")`）：
//! `GET /adminapi/deposit/log/list`、`GET /adminapi/deposit/log/detail`。

use axum::{
    extract::{Query, State},
    routing::get,
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::merchant::deposit_log::{DepositLogListQuery, DepositLogVO};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    ("GET", "/deposit/log/list", "DepositLogController.list"),
    ("GET", "/deposit/log/detail", "DepositLogController.detail"),
];

/// 保证金明细路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/log/list", get(list))
        .route("/deposit/log/detail", get(detail))
}

/// 已实现路由（覆盖率统计）。
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
    id: i32,
}

/// `GET /adminapi/deposit/log/list`
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<DepositLogListQuery>,
) -> ApiResult<PageResult<DepositLogVO>> {
    ok(state.merchant.deposit_log.list(&q).await?)
}

/// `GET /adminapi/deposit/log/detail?id=`
async fn detail(State(state): State<AdminState>, Query(q): Query<IdQuery>) -> ApiResult<DepositLogVO> {
    ok(state.merchant.deposit_log.detail(q.id).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract() {
        assert_eq!(ROUTE_TABLE.len(), 2);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/deposit/log/list");
        assert_eq!(docs[1].path, "/adminapi/deposit/log/detail");
    }

    #[test]
    fn both_are_get() {
        assert!(ROUTE_TABLE.iter().all(|(m, _, _)| *m == "GET"));
    }
}

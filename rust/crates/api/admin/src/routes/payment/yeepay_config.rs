//! 管理端汇付斗拱设置路由（payment::yeepay_config，2 条）。
//!
//! `GET /adminapi/setting/config/yeepaySettings`、`POST /adminapi/setting/config/saveYeepay`。

use axum::{
    extract::State,
    routing::{get, post},
    Json, Router,
};
use serde_json::Value;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    (
        "GET",
        "/setting/config/yeepaySettings",
        "YeepayConfigController.getHuifuSettings",
    ),
    (
        "POST",
        "/setting/config/saveYeepay",
        "YeepayConfigController.saveYeepaySettings",
    ),
];

/// 汇付设置路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/setting/config/yeepaySettings", get(get_settings))
        .route("/setting/config/saveYeepay", post(save_settings))
}

/// 本域已实现路由（完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

async fn get_settings(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(state.payment.yeepay_config.get_settings().await?)
}

async fn save_settings(
    State(state): State<AdminState>,
    Json(payload): Json<Value>,
) -> ApiResult<()> {
    state
        .payment
        .yeepay_config
        .save_settings(&payload)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/setting/config/yeepaySettings"),
            ("POST", "/adminapi/setting/config/saveYeepay"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 2);
        for (doc, (method, path)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *method);
            assert_eq!(doc.path, *path);
        }
    }

    #[test]
    fn route_table_slice_is_exact() {
        assert_eq!(ROUTE_TABLE.len(), 2);
        assert!(ROUTE_TABLE.iter().all(|(_, p, _)| p.starts_with("/setting/config/")));
    }
}

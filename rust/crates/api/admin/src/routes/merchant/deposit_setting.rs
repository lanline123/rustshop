//! 店铺保证金设置 —— 管理端路由（merchant::deposit_setting）—— wave9。
//!
//! 原 Java `DepositSettingController`：
//! `GET /adminapi/deposit/setting/detail`、`POST /adminapi/deposit/setting/save`。

use axum::{
    extract::State,
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::merchant::deposit_setting::DepositSetting;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    ("GET", "/deposit/setting/detail", "DepositSettingController.detail"),
    ("POST", "/deposit/setting/save", "DepositSettingController.save"),
];

/// 保证金设置路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/setting/detail", get(detail))
        .route("/deposit/setting/save", post(save))
}

/// 已实现路由（覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, &format!("/adminapi{p}"), j))
        .collect()
}

/// `GET /adminapi/deposit/setting/detail`
async fn detail(State(state): State<AdminState>) -> ApiResult<DepositSetting> {
    ok(state.merchant.deposit_setting.detail().await?)
}

/// `POST /adminapi/deposit/setting/save`
async fn save(
    State(state): State<AdminState>,
    Json(dto): Json<DepositSetting>,
) -> ApiResult<()> {
    state.merchant.deposit_setting.save(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract() {
        assert_eq!(ROUTE_TABLE.len(), 2);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/deposit/setting/detail");
        assert_eq!(docs[1].path, "/adminapi/deposit/setting/save");
    }

    #[test]
    fn methods_match_java() {
        assert_eq!(ROUTE_TABLE[0].0, "GET");
        assert_eq!(ROUTE_TABLE[1].0, "POST");
    }
}

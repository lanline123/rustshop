//! 保证金收款账户 —— 管理端路由（merchant::deposit_payee_account）—— wave9。
//!
//! 原 Java `DepositPayeeAccountController`（`@RequestMapping("/adminapi/deposit/payeeAccount")`）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::merchant::deposit_payee_account::{
    DepositPayeeAccountDTO, DepositPayeeAccountDeleteDTO, DepositPayeeAccountListQuery,
    DepositPayeeAccountVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 4] = [
    ("GET", "/deposit/payeeAccount/list", "DepositPayeeAccountController.list"),
    ("POST", "/deposit/payeeAccount/create", "DepositPayeeAccountController.create"),
    ("POST", "/deposit/payeeAccount/update", "DepositPayeeAccountController.update"),
    ("POST", "/deposit/payeeAccount/del", "DepositPayeeAccountController.del"),
];

/// 收款账户路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/payeeAccount/list", get(list))
        .route("/deposit/payeeAccount/create", post(create))
        .route("/deposit/payeeAccount/update", post(update))
        .route("/deposit/payeeAccount/del", post(del))
}

/// 已实现路由（覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, &format!("/adminapi{p}"), j))
        .collect()
}

/// `GET /adminapi/deposit/payeeAccount/list`
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<DepositPayeeAccountListQuery>,
) -> ApiResult<PageResult<DepositPayeeAccountVO>> {
    ok(state.merchant.deposit_payee_account.list(&q).await?)
}

/// `POST /adminapi/deposit/payeeAccount/create`
async fn create(
    State(state): State<AdminState>,
    Json(dto): Json<DepositPayeeAccountDTO>,
) -> ApiResult<()> {
    state.merchant.deposit_payee_account.create(&dto).await?;
    ok(())
}

/// `POST /adminapi/deposit/payeeAccount/update`
async fn update(
    State(state): State<AdminState>,
    Json(dto): Json<DepositPayeeAccountDTO>,
) -> ApiResult<()> {
    state.merchant.deposit_payee_account.update(&dto).await?;
    ok(())
}

/// `POST /adminapi/deposit/payeeAccount/del`
async fn del(
    State(state): State<AdminState>,
    Json(dto): Json<DepositPayeeAccountDeleteDTO>,
) -> ApiResult<()> {
    state.merchant.deposit_payee_account.delete(dto.id).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract() {
        assert_eq!(ROUTE_TABLE.len(), 4);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/deposit/payeeAccount/list");
        assert_eq!(docs[3].path, "/adminapi/deposit/payeeAccount/del");
    }

    #[test]
    fn methods_match_java_controller() {
        assert_eq!(ROUTE_TABLE.iter().filter(|(m, _, _)| *m == "GET").count(), 1);
        assert_eq!(ROUTE_TABLE.iter().filter(|(m, _, _)| *m == "POST").count(), 3);
    }
}

//! 订单日志 —— 管理端路由（trade::order_log）—— wave9。
//!
//! 原 Java `OrderLogController`（`@RequestMapping("/adminapi/order/orderLog")`）：
//!
//! | 本模块路由（挂载于 `/adminapi`） | 契约 |
//! |-----------------------------------|------|
//! | `GET  /order/orderLog/list` | jwt |
//! | `POST /order/orderLog/create` | jwt+perm（orderLogModifyManage） |
//!
//! > 权限矩阵未实现（仅 `require_auth`）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::trade::order_log::{
    OrderLogCreateDTO, OrderLogListQuery, OrderLogVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 2 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    ("GET", "/order/orderLog/list", "OrderLogController.list"),
    ("POST", "/order/orderLog/create", "OrderLogController.create"),
];

/// 订单日志路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/order/orderLog/list", get(list))
        .route("/order/orderLog/create", post(create))
}

/// 本域已实现路由（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, &format!("/adminapi{p}"), j))
        .collect()
}

/// `GET /adminapi/order/orderLog/list`
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<OrderLogListQuery>,
) -> ApiResult<PageResult<OrderLogVO>> {
    ok(state.trade.order_log.list(&q).await?)
}

/// `POST /adminapi/order/orderLog/create`
async fn create(
    State(state): State<AdminState>,
    Json(dto): Json<OrderLogCreateDTO>,
) -> ApiResult<()> {
    state.trade.order_log.create(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract() {
        assert_eq!(ROUTE_TABLE.len(), 2);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/order/orderLog/list");
        assert_eq!(docs[1].path, "/adminapi/order/orderLog/create");
        assert!(ROUTE_TABLE.iter().all(|(_, p, _)| !p.contains("/adminapi")));
    }

    #[test]
    fn methods_match_java() {
        assert_eq!(ROUTE_TABLE[0].0, "GET");
        assert_eq!(ROUTE_TABLE[1].0, "POST");
    }
}

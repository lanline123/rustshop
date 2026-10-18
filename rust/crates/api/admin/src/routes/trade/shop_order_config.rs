//! 管理端店铺订单配置路由（trade::shop_order_config）。
//!
//! 对应 Java `ShopOrderConfigController`（`/adminapi/order/config`）：
//! `GET /adminapi/order/config/detail`、`POST /adminapi/order/config/save`。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::trade::shop_order_config::{
    OrderConfigDTO, OrderConfigDetailQuery,
};
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 2 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    ("GET", "/order/config/detail", "ShopOrderConfigController.detail"),
    ("POST", "/order/config/save", "ShopOrderConfigController.save"),
];

/// 店铺订单配置路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/order/config/detail", get(detail))
        .route("/order/config/save", post(save))
}

/// 已实现路由（契约完整路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

/// `GET /adminapi/order/config/detail`：配置详情。
async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<OrderConfigDetailQuery>,
) -> ApiResult<Option<serde_json::Value>> {
    ok(state
        .trade
        .shop_order_config
        .detail(q.shop_id, q.code.as_deref())
        .await?)
}

/// `POST /adminapi/order/config/save`：保存配置。
async fn save(
    State(state): State<AdminState>,
    Json(dto): Json<OrderConfigDTO>,
) -> ApiResult<()> {
    state.trade.shop_order_config.save(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 2);
        assert_eq!(docs[0].method, "GET");
        assert_eq!(docs[0].path, "/adminapi/order/config/detail");
        assert_eq!(docs[1].method, "POST");
        assert_eq!(docs[1].path, "/adminapi/order/config/save");
        assert_eq!(ROUTE_TABLE[0].1, "/order/config/detail");
        assert_eq!(ROUTE_TABLE[1].1, "/order/config/save");
    }

    #[test]
    fn dto_parsing_and_route_table() {
        let dto: OrderConfigDTO = serde_json::from_str(r#"{"shopId":1,"code":"order"}"#).unwrap();
        assert_eq!(dto.shop_id, Some(1));
        assert_eq!(ROUTE_TABLE.len(), 2);
    }
}

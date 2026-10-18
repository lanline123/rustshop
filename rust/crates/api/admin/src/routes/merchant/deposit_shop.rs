//! 管理端店铺端保证金聚合路由（merchant::deposit_shop，5 条只读）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.deposit.DepositShopController`
//! （`@RequestMapping("/adminapi/deposit/shop")`）：
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java 方法 |
//! |----------------------------------|--------------|
//! | `GET /deposit/shop/overview` | `DepositShopController.overview` |
//! | `GET /deposit/shop/logList` | `DepositShopController.logList` |
//! | `GET /deposit/shop/frozenDetailList` | `DepositShopController.frozenDetailList` |
//! | `GET /deposit/shop/warning` | `DepositShopController.warning` |
//! | `GET /deposit/shop/protocol` | `DepositShopController.protocol` |
//!
//! > **店铺上下文偏差**：Java 从请求头 `X-Shop-Id`（`X-Admin-Type=shop`）解析
//! > 当前店铺并拒绝越权；Rust JWT 无 shop_id，5 个端点改用可选查询参数
//! > `?shopId=`（缺失返回业务错误「无法获取当前店铺信息」，见服务层模块头偏差 1）。

use axum::{
    extract::{Query, State},
    routing::get,
    Router,
};
use serde::Deserialize;
use tigshop_core::models::merchant::deposit_shop::{
    DepositShopFrozenDetailVO, DepositShopLogQuery, DepositShopLogVO, DepositShopOverviewVO,
    DepositShopProtocolVO, DepositShopWarningVO,
};
use tigshop_core::types::PageResult;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};
use tigshop_services::merchant::deposit_shop::DepositShopService;

use crate::AdminState;

/// 本域 5 条路由（`/adminapi` 前缀之后的路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 5] = [
    (
        "GET",
        "/deposit/shop/overview",
        "DepositShopController.overview",
    ),
    (
        "GET",
        "/deposit/shop/logList",
        "DepositShopController.logList",
    ),
    (
        "GET",
        "/deposit/shop/frozenDetailList",
        "DepositShopController.frozenDetailList",
    ),
    (
        "GET",
        "/deposit/shop/warning",
        "DepositShopController.warning",
    ),
    (
        "GET",
        "/deposit/shop/protocol",
        "DepositShopController.protocol",
    ),
];

/// 店铺端保证金聚合路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/shop/overview", get(overview))
        .route("/deposit/shop/logList", get(log_list))
        .route("/deposit/shop/frozenDetailList", get(frozen_detail_list))
        .route("/deposit/shop/warning", get(warning))
        .route("/deposit/shop/protocol", get(protocol))
}

/// 本域已实现路由（完整契约路径，用于覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

/// 店铺上下文查询参数（Java `X-Shop-Id` 请求头的替身）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct ShopContextQuery {
    shop_id: Option<i32>,
}

/// `GET /adminapi/deposit/shop/overview`：保证金总览。
async fn overview(
    State(state): State<AdminState>,
    Query(ctx): Query<ShopContextQuery>,
) -> ApiResult<DepositShopOverviewVO> {
    let shop_id = DepositShopService::resolve_shop_id(ctx.shop_id)?;
    ok(state.merchant.deposit_shop.overview(shop_id).await?)
}

/// `GET /adminapi/deposit/shop/logList`：店铺端交易流水。
async fn log_list(
    State(state): State<AdminState>,
    Query(q): Query<DepositShopLogQuery>,
) -> ApiResult<PageResult<DepositShopLogVO>> {
    let shop_id = DepositShopService::resolve_shop_id(q.shop_id)?;
    ok(state.merchant.deposit_shop.log_list(shop_id, &q).await?)
}

/// `GET /adminapi/deposit/shop/frozenDetailList`：冻结明细。
async fn frozen_detail_list(
    State(state): State<AdminState>,
    Query(q): Query<DepositShopLogQuery>,
) -> ApiResult<PageResult<DepositShopFrozenDetailVO>> {
    let shop_id = DepositShopService::resolve_shop_id(q.shop_id)?;
    ok(state
        .merchant
        .deposit_shop
        .frozen_detail_list(shop_id, &q)
        .await?)
}

/// `GET /adminapi/deposit/shop/warning`：首页保证金预警横幅。
async fn warning(
    State(state): State<AdminState>,
    Query(ctx): Query<ShopContextQuery>,
) -> ApiResult<DepositShopWarningVO> {
    let shop_id = DepositShopService::resolve_shop_id(ctx.shop_id)?;
    ok(state.merchant.deposit_shop.warning(shop_id).await?)
}

/// `GET /adminapi/deposit/shop/protocol`：保证金协议。
async fn protocol(State(state): State<AdminState>) -> ApiResult<DepositShopProtocolVO> {
    ok(state.merchant.deposit_shop.protocol().await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/deposit/shop/overview"),
            ("GET", "/adminapi/deposit/shop/logList"),
            ("GET", "/adminapi/deposit/shop/frozenDetailList"),
            ("GET", "/adminapi/deposit/shop/warning"),
            ("GET", "/adminapi/deposit/shop/protocol"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 5);
        for (doc, (method, path)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *method);
            assert_eq!(doc.path, *path);
        }
    }

    #[test]
    fn route_table_matches_java_controller_methods() {
        let handlers: Vec<&str> = ROUTE_TABLE.iter().map(|(_, _, h)| *h).collect();
        assert_eq!(handlers.len(), 5);
        assert!(handlers.contains(&"DepositShopController.overview"));
        assert!(handlers.contains(&"DepositShopController.protocol"));
    }
}

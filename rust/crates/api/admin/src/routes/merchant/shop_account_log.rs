//! 店铺账户 —— 管理端路由（merchant::shop_account_log）—— wave9。
//!
//! 原 Java `com.tigshop.adminapi.controller.shop.AccountController`
//! （`@RequestMapping("/adminapi/merchant/shopAccount")`）：
//! `GET /adminapi/merchant/shopAccount/index`（店铺余额汇总）。
//!
//! > 契约中其余 2 条为 public（`/api/user/account/*`），见
//! > `api/public/src/routes/merchant/shop_account_log.rs`。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::get,
    Json, Router,
};
use tigshop_core::models::merchant::shop_account_log::{ShopAccountQuery, ShopAccountVO};
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const ROUTE_TABLE: [(&str, &str, &str); 1] = [(
    "GET",
    "/merchant/shopAccount/index",
    "AccountController.index",
)];

/// 店铺账户路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new().route("/merchant/shopAccount/index", get(index))
}

/// 已实现路由（覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, &format!("/adminapi{p}"), j))
        .collect()
}

/// `GET /adminapi/merchant/shopAccount/index?shopId=`
async fn index(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<ShopAccountQuery>,
) -> ApiResult<ShopAccountVO> {
    let header_shop_id = headers
        .get("x-shop-id")
        .and_then(|v| v.to_str().ok())
        .and_then(|v| v.parse::<i32>().ok());
    ok(state.merchant.shop_account_log.index(&q, header_shop_id).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract() {
        assert_eq!(ROUTE_TABLE.len(), 1);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/merchant/shopAccount/index");
        assert_eq!(ROUTE_TABLE[0].0, "GET");
    }

    #[test]
    fn path_has_no_adminapi_prefix() {
        assert!(!ROUTE_TABLE[0].1.contains("/adminapi"));
    }
}

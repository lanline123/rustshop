//! 管理端商户账户路由（merchant::merchant_account）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.merchant.MerchantAccountController`
//! （`@RequestMapping("/adminapi/merchant/account")`，7 个端点，全部需登录）。
//!
//! > **多租户说明**：Java 用请求头 `x-shop-id`（`HeaderUtils.getShopId()`）定位店铺；
//! > Rust JWT 未签发 `shop_id`，各接口接受查询参数 `shopId` 兜底。
//! > 权限矩阵（`accountModifyManage`）尚未在 Rust 侧实现，本模块只做登录态校验。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use serde_json::Value;
use tigshop_core::models::merchant::merchant_account::{
    MerchantAccountCreateDTO, MerchantAccountListQuery, MerchantAccountUpdateDTO, MerchantAccountVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域 7 条路由（挂载路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 7] = [
    ("GET", "/merchant/account/list", "MerchantAccountController.list"),
    ("GET", "/merchant/account/config", "MerchantAccountController.config"),
    ("GET", "/merchant/account/detail", "MerchantAccountController.detail"),
    ("POST", "/merchant/account/create", "MerchantAccountController.create"),
    ("POST", "/merchant/account/update", "MerchantAccountController.update"),
    ("POST", "/merchant/account/del", "MerchantAccountController.del"),
    ("POST", "/merchant/account/batch", "MerchantAccountController.batch"),
];

/// 商户账户域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/merchant/account/list", get(list))
        .route("/merchant/account/config", get(config))
        .route("/merchant/account/detail", get(detail))
        .route("/merchant/account/create", post(create))
        .route("/merchant/account/update", post(update))
        .route("/merchant/account/del", post(del))
        .route("/merchant/account/batch", post(batch))
}

/// 本域已实现路由（完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

/// 店铺兜底参数。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct ShopQuery {
    shop_id: Option<i32>,
}

/// `detail?id=`.
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct IdQuery {
    id: Option<i32>,
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<MerchantAccountListQuery>,
) -> ApiResult<PageResult<MerchantAccountVO>> {
    ok(state
        .merchant
        .merchant_account
        .list(&q, q.shop_id)
        .await?)
}

async fn config(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(state.merchant.merchant_account.config())
}

async fn detail(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<IdQuery>,
) -> ApiResult<Option<MerchantAccountVO>> {
    ok(state
        .merchant
        .merchant_account
        .detail(q.id, auth.id)
        .await?)
}

async fn create(
    State(state): State<AdminState>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<MerchantAccountCreateDTO>,
) -> ApiResult<i32> {
    ok(state
        .merchant
        .merchant_account
        .create(&dto, q.shop_id)
        .await?)
}

async fn update(
    State(state): State<AdminState>,
    Json(dto): Json<MerchantAccountUpdateDTO>,
) -> ApiResult<()> {
    state.merchant.merchant_account.update(&dto).await?;
    ok(())
}

async fn del(State(state): State<AdminState>, Json(dto): Json<Value>) -> ApiResult<()> {
    let id = dto
        .get("id")
        .and_then(Value::as_i64)
        .or_else(|| dto.get("accountId").and_then(Value::as_i64))
        .map(|v| v as i32);
    state.merchant.merchant_account.del(id).await?;
    ok(())
}

async fn batch(State(state): State<AdminState>, Json(dto): Json<Value>) -> ApiResult<()> {
    let ids: Option<Vec<i32>> = dto.get("ids").and_then(Value::as_array).map(|arr| {
        arr.iter()
            .filter_map(Value::as_i64)
            .map(|v| v as i32)
            .collect()
    });
    let op_type = dto.get("type").and_then(Value::as_str);
    state
        .merchant
        .merchant_account
        .batch(ids.as_deref(), op_type)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/merchant/account/list"),
            ("GET", "/adminapi/merchant/account/config"),
            ("GET", "/adminapi/merchant/account/detail"),
            ("POST", "/adminapi/merchant/account/create"),
            ("POST", "/adminapi/merchant/account/update"),
            ("POST", "/adminapi/merchant/account/del"),
            ("POST", "/adminapi/merchant/account/batch"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 7);
        for (doc, (m, p)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *m);
            assert_eq!(doc.path, *p);
        }
    }
}

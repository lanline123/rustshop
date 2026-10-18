//! 管理端店铺员工路由（merchant::admin_user_shop）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.shop.AdminUserShopController`
//! （`@RequestMapping("/adminapi/merchant/adminUserShop")`，9 个端点，全部需登录）。
//!
//! > **多租户说明**：Java 用请求头 `x-shop-id`（`HeaderUtils.getShopId()`）确定当前店铺；
//! > Rust JWT 未签发 `shop_id`，故各接口接受查询参数 `shopId` 兜底（与 wave1 约定一致）。
//! > 缺省时：`list` 返回空页，其它接口按 Java 语义返回「店铺员工信息不存在」。
//!
//! 权限矩阵（`adminUserShopModifyManage`）在 Rust 侧尚未实现，本模块只做登录态校验。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use serde_json::Value;
use tigshop_core::models::merchant::admin_user_shop::{
    AdminUserShopCreateDTO, AdminUserShopModifyDTO, AdminUserShopPageQuery, AdminUserShopUpdateDTO,
    AdminUserShopVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域 9 条路由（挂载路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 9] = [
    ("GET", "/merchant/adminUserShop/list", "AdminUserShopController.list"),
    ("GET", "/merchant/adminUserShop/detail", "AdminUserShopController.detail"),
    ("GET", "/merchant/adminUserShop/info", "AdminUserShopController.info"),
    (
        "GET",
        "/merchant/adminUserShop/checkIsAdmin",
        "AdminUserShopController.checkIsAdmin",
    ),
    ("POST", "/merchant/adminUserShop/create", "AdminUserShopController.create"),
    ("POST", "/merchant/adminUserShop/update", "AdminUserShopController.update"),
    ("POST", "/merchant/adminUserShop/del", "AdminUserShopController.del"),
    ("POST", "/merchant/adminUserShop/batch", "AdminUserShopController.batch"),
    (
        "POST",
        "/merchant/adminUserShop/modifyUser",
        "AdminUserShopController.modifyUser",
    ),
];

/// 店铺员工域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/merchant/adminUserShop/list", get(list))
        .route("/merchant/adminUserShop/detail", get(detail))
        .route("/merchant/adminUserShop/info", get(info))
        .route("/merchant/adminUserShop/checkIsAdmin", get(check_is_admin))
        .route("/merchant/adminUserShop/create", post(create))
        .route("/merchant/adminUserShop/update", post(update))
        .route("/merchant/adminUserShop/del", post(del))
        .route("/merchant/adminUserShop/batch", post(batch))
        .route("/merchant/adminUserShop/modifyUser", post(modify_user))
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

/// 店铺 + id 查询参数（`shopId` 兜底 `x-shop-id`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct ShopQuery {
    shop_id: Option<i32>,
}

/// `detail` / `info` 的 `id` 参数。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct IdShopQuery {
    id: Option<i32>,
    shop_id: Option<i32>,
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<AdminUserShopPageQuery>,
) -> ApiResult<PageResult<AdminUserShopVO>> {
    ok(state
        .merchant
        .admin_user_shop
        .list(&q, q.shop_id)
        .await?)
}

async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<IdShopQuery>,
) -> ApiResult<AdminUserShopVO> {
    ok(state.merchant.admin_user_shop.detail(q.id, q.shop_id).await?)
}

async fn info(
    State(state): State<AdminState>,
    Query(q): Query<IdShopQuery>,
) -> ApiResult<AdminUserShopVO> {
    ok(state.merchant.admin_user_shop.info(q.id, q.shop_id).await?)
}

async fn check_is_admin(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<ShopQuery>,
) -> ApiResult<Value> {
    let is_admin = state
        .merchant
        .admin_user_shop
        .check_is_admin(auth.id, q.shop_id)
        .await?;
    let body: Value = serde_json::json!({ "isAdmin": is_admin });
    ok(body)
}

async fn create(
    State(state): State<AdminState>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<AdminUserShopCreateDTO>,
) -> ApiResult<()> {
    state
        .merchant
        .admin_user_shop
        .create(&dto, q.shop_id)
        .await?;
    ok(())
}

async fn update(
    State(state): State<AdminState>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<AdminUserShopUpdateDTO>,
) -> ApiResult<()> {
    state
        .merchant
        .admin_user_shop
        .update(&dto, q.shop_id)
        .await?;
    ok(())
}

async fn del(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<Value>,
) -> ApiResult<()> {
    let id = dto.get("id").and_then(Value::as_i64).map(|v| v as i32);
    state
        .merchant
        .admin_user_shop
        .del(id, auth.id, q.shop_id)
        .await?;
    ok(())
}

async fn batch(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<Value>,
) -> ApiResult<()> {
    let ids: Option<Vec<i32>> = dto.get("ids").and_then(Value::as_array).map(|arr| {
        arr.iter()
            .filter_map(Value::as_i64)
            .map(|v| v as i32)
            .collect()
    });
    let op_type = dto.get("type").and_then(Value::as_str);
    state
        .merchant
        .admin_user_shop
        .batch(ids.as_deref(), op_type, auth.id, q.shop_id)
        .await?;
    ok(())
}

async fn modify_user(
    State(state): State<AdminState>,
    Query(q): Query<ShopQuery>,
    Json(dto): Json<AdminUserShopModifyDTO>,
) -> ApiResult<()> {
    state
        .merchant
        .admin_user_shop
        .modify_user(&dto, q.shop_id)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/merchant/adminUserShop/list"),
            ("GET", "/adminapi/merchant/adminUserShop/detail"),
            ("GET", "/adminapi/merchant/adminUserShop/info"),
            ("GET", "/adminapi/merchant/adminUserShop/checkIsAdmin"),
            ("POST", "/adminapi/merchant/adminUserShop/create"),
            ("POST", "/adminapi/merchant/adminUserShop/update"),
            ("POST", "/adminapi/merchant/adminUserShop/del"),
            ("POST", "/adminapi/merchant/adminUserShop/batch"),
            ("POST", "/adminapi/merchant/adminUserShop/modifyUser"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 9);
        for (doc, (m, p)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *m);
            assert_eq!(doc.path, *p);
        }
        let mut keys: Vec<String> = docs.iter().map(|d| d.key()).collect();
        keys.sort();
        keys.dedup();
        assert_eq!(keys.len(), 9);
    }
}

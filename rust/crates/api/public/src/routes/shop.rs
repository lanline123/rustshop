//! 商城前台商户/店铺域路由。
//! 对应原 Java `controller.shop.ShopClientCategoryController`。
//!
//! | 路由 | 原 Java |
//! |------|---------|
//! | `GET /api/shop/category/tree` | `ShopClientCategoryController.tree` |
//!
//! > **归属说明**：`ShopClientController` 的其余端点（`list`/`detail`/`collect`/
//! > `head`/`decorate`/`pickupList`/`pickupDetail`/`storeList`/`storeDetail`/
//! > `getCommentList`）已迁至 `routes/slice013.rs`。
//! > 本文件**不得**再注册 `GET /api/shop/shop/detail`，否则 axum
//! > `Router::merge` 会因重复注册在服务启动时 panic。
//! > 自查：`cd rust && python tools/check_route_conflicts.py --strict`。

use axum::{
    extract::{Query, State},
    routing::get,
    Router,
};
use serde::Deserialize;
use tigshop_core::models::shop::ShopCategoryTreeNode;
use tigshop_security::web::{ok, ApiResult};

use super::super::PublicState;

/// 公开路由：经营类目树（无需登录）。
pub fn public_routes() -> Router<PublicState> {
    Router::new().route("/shop/category/tree", get(category_tree))
}

/// 需登录路由：暂无（本域接口均为公开）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [(
        "GET",
        "/api/shop/category/tree",
        "ShopClientCategoryController.tree",
    )]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// 经营类目树入参（原 Java `@RequestParam("shopId")`）。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct CategoryTreeQuery {
    shop_id: i32,
}

/// `GET /api/shop/category/tree?shopId=`：店铺经营类目树
/// （仅 `is_show = 1`，根为 `parent_id = 0`）。
async fn category_tree(
    State(state): State<PublicState>,
    Query(q): Query<CategoryTreeQuery>,
) -> ApiResult<Vec<ShopCategoryTreeNode>> {
    ok(state.shop.category_tree(q.shop_id).await?)
}

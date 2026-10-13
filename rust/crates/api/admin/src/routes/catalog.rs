//! 管理端分类/品牌路由。
//! 对应原 Java `com.tigshop.adminapi.controller.product.CategoryController`
//! （`/adminapi/product/category`）与 `BrandController`（`/adminapi/product/brand`）。
//!
//! 原 Java `CategoryController` 的 10 个接口全部对齐：
//!
//! | 路由 | 原 Java | 权限 |
//! |------|---------|------|
//! | `GET  /adminapi/product/category/list` | `CategoryController.list` | `categoryManage`/`productManage`/`vendorProductManage` |
//! | `GET  /adminapi/product/category/getParentName` | `getParentName` | 同上 |
//! | `GET  /adminapi/product/category/detail` | `detail` | 同上 |
//! | `POST /adminapi/product/category/create` | `create` | `productCategoryModifyManage` |
//! | `POST /adminapi/product/category/update` | `update` | `productCategoryModifyManage` |
//! | `POST /adminapi/product/category/del` | `del` | `productCategoryModifyManage` |
//! | `POST /adminapi/product/category/batch` | `batch` | `productCategoryModifyManage` |
//! | `POST /adminapi/product/category/updateField` | `updateField` | `productCategoryModifyManage` |
//! | `GET  /adminapi/product/category/getAllCategory` | `getAllCategory` | `decorate`/`categoryManage`/... |
//! | `POST /adminapi/product/category/moveCat` | `moveCat` | 类级权限 |
//!
//! > **权限说明**：Java 用 `@PreAuthorize("@pms.hasPermission(...)")` 在方法级
//! > 校验。Rust 侧当前 `require_auth` 只做登录态校验，**权限矩阵尚未实现**，
//! > 属已知缺口（见 README「待办」）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::{BrandVO, CategoryDTO, CategoryListQuery, CategoryTreeNode};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 分类/品牌域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        // —— 分类（CategoryController）——
        .route("/product/category/list", get(category_list))
        .route("/product/category/getParentName", get(parent_name))
        .route("/product/category/detail", get(category_detail))
        .route("/product/category/create", post(category_create))
        .route("/product/category/update", post(category_update))
        .route("/product/category/del", post(category_del))
        .route("/product/category/batch", post(category_batch))
        .route("/product/category/updateField", post(category_update_field))
        .route("/product/category/getAllCategory", get(all_category))
        .route("/product/category/moveCat", post(move_cat))
        // —— 品牌（BrandController）——
        .route("/product/brand/list", get(brand_list))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/product/category/list", "CategoryController.list"),
        ("GET", "/adminapi/product/category/getParentName", "CategoryController.getParentName"),
        ("GET", "/adminapi/product/category/detail", "CategoryController.detail"),
        ("POST", "/adminapi/product/category/create", "CategoryController.create"),
        ("POST", "/adminapi/product/category/update", "CategoryController.update"),
        ("POST", "/adminapi/product/category/del", "CategoryController.del"),
        ("POST", "/adminapi/product/category/batch", "CategoryController.batch"),
        ("POST", "/adminapi/product/category/updateField", "CategoryController.updateField"),
        ("GET", "/adminapi/product/category/getAllCategory", "CategoryController.getAllCategory"),
        ("POST", "/adminapi/product/category/moveCat", "CategoryController.moveCat"),
        ("GET", "/adminapi/product/brand/list", "BrandController.list"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

// ————————————————— 分类 —————————————————

/// `GET /adminapi/product/category/list`：商品分类分页列表。
async fn category_list(
    State(state): State<AdminState>,
    Query(q): Query<CategoryListQuery>,
) -> ApiResult<PageResult<CategoryDTO>> {
    ok(state.catalog.category_list(&q).await?)
}

/// `GET /adminapi/product/category/getParentName?parentId=`：父分类名称。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ParentQuery {
    parent_id: Option<i32>,
}

/// 返回父分类名称；Java 返回 `String`（可能为 null），
/// 这里统一包成对象以便前端处理空值。
async fn parent_name(
    State(state): State<AdminState>,
    Query(q): Query<ParentQuery>,
) -> ApiResult<serde_json::Value> {
    let name = state.catalog.parent_name(q.parent_id).await?;
    ok(serde_json::json!({ "parentName": name }))
}

/// `GET /adminapi/product/category/detail?id=`：分类详情。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

async fn category_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<CategoryDTO> {
    ok(state.catalog.category_detail_dto(q.id).await?)
}

/// `POST /adminapi/product/category/create`：创建分类。
async fn category_create(
    State(state): State<AdminState>,
    Json(dto): Json<CategoryDTO>,
) -> ApiResult<i32> {
    let id = state.catalog.create_category(&dto).await?;
    ok(id)
}

/// `POST /adminapi/product/category/update`：更新分类。
async fn category_update(
    State(state): State<AdminState>,
    Json(dto): Json<CategoryDTO>,
) -> ApiResult<&'static str> {
    state.catalog.update_category(&dto).await?;
    ok("更新成功")
}

/// `POST /adminapi/product/category/del`：删除分类（有子分类/有商品则拒绝）。
async fn category_del(
    State(state): State<AdminState>,
    Json(dto): Json<tigshop_core::models::OperateDTO>,
) -> ApiResult<&'static str> {
    state.catalog.delete_category(&dto).await?;
    ok("删除成功")
}

/// `POST /adminapi/product/category/batch`：批量操作（目前仅 `type=del`）。
async fn category_batch(
    State(state): State<AdminState>,
    Json(dto): Json<tigshop_core::models::BatchDTO>,
) -> ApiResult<&'static str> {
    state.catalog.batch_operate(&dto).await?;
    ok("操作成功")
}

/// `POST /adminapi/product/category/updateField`：更新单个字段。
async fn category_update_field(
    State(state): State<AdminState>,
    Json(dto): Json<tigshop_core::models::UpdateFieldDTO>,
) -> ApiResult<bool> {
    ok(state.catalog.update_field(&dto).await?)
}

/// `GET /adminapi/product/category/getAllCategory`：全部分类树（管理端含隐藏）。
async fn all_category(
    State(state): State<AdminState>,
) -> ApiResult<Vec<CategoryTreeNode>> {
    ok(state.catalog.category_tree(true).await?)
}

/// `POST /adminapi/product/category/moveCat`：移动分类（迁移商品到目标分类）。
async fn move_cat(
    State(state): State<AdminState>,
    Json(dto): Json<tigshop_core::models::MoveCatDTO>,
) -> ApiResult<u64> {
    let moved = state.catalog.move_category(&dto).await?;
    ok(moved)
}

// ————————————————— 品牌 —————————————————

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct BrandQuery {
    keyword: Option<String>,
    page: Option<u64>,
    page_size: Option<u64>,
}

/// `GET /adminapi/product/brand/list`：品牌分页。
async fn brand_list(
    State(state): State<AdminState>,
    Query(q): Query<BrandQuery>,
) -> ApiResult<PageResult<BrandVO>> {
    let page = PageQuery {
        page: q.page.unwrap_or(1),
        page_size: q.page_size.unwrap_or(20),
    };
    ok(state.catalog.brands(q.keyword.as_deref(), &page).await?)
}

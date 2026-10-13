//! 管理端分类/品牌路由。
//! 对应原 Java `com.tigshop.adminapi.controller.product.CategoryController`
//! 与 `BrandController`。
//!
//! TODO(域实现者)：对照 Java 控制器补全分类/品牌增删改、排序接口。

use axum::{
    extract::{Query, State},
    routing::get,
    Router,
};
use serde::Deserialize;
use tigshop_core::models::{BrandVO, CategoryTreeNode};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 分类/品牌域路由（挂载于 `/adminapi` 下）。
///
/// 原 Java `CategoryController` 没有 `tree` 端点，树形数据由
/// `GET /adminapi/product/category/getAllCategory` 提供。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/product/category/getAllCategory", get(category_tree))
        .route("/product/brand/list", get(brand_list))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/product/category/getAllCategory", "CategoryController.getAllCategory"),
        ("GET", "/adminapi/product/brand/list", "BrandController.list"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/product/category/getAllCategory`：分类树（管理端含隐藏分类）。
async fn category_tree(
    State(state): State<AdminState>,
) -> ApiResult<Vec<CategoryTreeNode>> {
    ok(state.catalog.category_tree(true).await?)
}

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
    let page = tigshop_core::types::PageQuery {
        page: q.page.unwrap_or(1),
        page_size: q.page_size.unwrap_or(20),
    };
    ok(state.catalog.brands(q.keyword.as_deref(), &page).await?)
}

//! 管理端内容（文章）路由。
//! 对应原 Java `com.tigshop.adminapi.controller.content.ArticleController` 与
//! `ArticleCategoryController`（`/adminapi/content`）。
//!
//! | 路由 | 原 Java |
//! |------|---------|
//! | `GET  /adminapi/content/article/list`        | `ArticleController.list` |
//! | `GET  /adminapi/content/article/detail`      | `ArticleController.detail` |
//! | `POST /adminapi/content/article/create`      | `ArticleController.create` |
//! | `POST /adminapi/content/article/update`      | `ArticleController.update` |
//! | `POST /adminapi/content/article/updateField` | `ArticleController.updateField`（显示/隐藏） |
//! | `POST /adminapi/content/article/del`         | `ArticleController.del` |
//! | `GET  /adminapi/content/articleCategory/list`   | `ArticleCategoryController.list` |
//! | `GET  /adminapi/content/articleCategory/tree`   | `ArticleCategoryController.tree` |
//! | `GET  /adminapi/content/articleCategory/detail` | `ArticleCategoryController.detail` |
//! | `POST /adminapi/content/articleCategory/create` | `ArticleCategoryController.create` |
//! | `POST /adminapi/content/articleCategory/del`    | `ArticleCategoryController.del` |
//!
//! 注意：原 Java 分类的「新增/更新」是两个端点（`create` / `update`），
//! 本 crate 目前用单个 `create` 承担 upsert，属实现差异，待补 `update`。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::entities::{Article, ArticleCategory};
use tigshop_core::models::{
    ArticleCategoryTreeNode, ArticleCategoryVO, ArticleDetailVO, ArticleListQuery, ArticleVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 内容域路由（挂载于 `/adminapi` 下）。
pub fn routes(_state: &AdminState) -> Router<AdminState> {
    Router::new()
        .route("/content/article/list", get(article_list))
        .route("/content/article/detail", get(article_detail))
        .route("/content/article/create", post(article_create))
        .route("/content/article/update", post(article_update))
        .route("/content/article/updateField", post(article_set_show))
        .route("/content/article/del", post(article_delete))
        .route("/content/articleCategory/list", get(category_list))
        .route("/content/articleCategory/tree", get(category_tree))
        .route("/content/articleCategory/detail", get(category_detail))
        .route("/content/articleCategory/create", post(category_save))
        .route("/content/articleCategory/del", post(category_delete))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/content/article/list", "ArticleController.list"),
        ("GET", "/adminapi/content/article/detail", "ArticleController.detail"),
        ("POST", "/adminapi/content/article/create", "ArticleController.create"),
        ("POST", "/adminapi/content/article/update", "ArticleController.update"),
        ("POST", "/adminapi/content/article/updateField", "ArticleController.updateField"),
        ("POST", "/adminapi/content/article/del", "ArticleController.del"),
        ("GET", "/adminapi/content/articleCategory/list", "ArticleCategoryController.list"),
        ("GET", "/adminapi/content/articleCategory/tree", "ArticleCategoryController.tree"),
        ("GET", "/adminapi/content/articleCategory/detail", "ArticleCategoryController.detail"),
        ("POST", "/adminapi/content/articleCategory/create", "ArticleCategoryController.create"),
        ("POST", "/adminapi/content/articleCategory/del", "ArticleCategoryController.del"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/content/article/list`：文章分页（管理端含隐藏文章）。
async fn article_list(
    State(state): State<AdminState>,
    Query(q): Query<ArticleListQuery>,
) -> ApiResult<PageResult<ArticleVO>> {
    ok(state.content_article.list(&q, false).await?)
}

#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

/// `GET /adminapi/content/article/detail?id=`：文章详情（含正文）。
async fn article_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<ArticleDetailVO> {
    ok(state.content_article.detail(q.id, false).await?)
}

/// `POST /adminapi/content/article/create`：新增文章。
async fn article_create(
    State(state): State<AdminState>,
    Json(a): Json<Article>,
) -> ApiResult<i32> {
    ok(state.content_article.create(a).await?)
}

/// `POST /adminapi/content/article/update`：更新文章。
async fn article_update(
    State(state): State<AdminState>,
    Json(a): Json<Article>,
) -> ApiResult<&'static str> {
    state.content_article.update(a).await?;
    ok("修改成功")
}

/// 显示状态切换请求。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct SetShowDTO {
    article_id: i32,
    is_show: i32,
}

/// `POST /adminapi/content/article/setShow`：显示/隐藏切换。
async fn article_set_show(
    State(state): State<AdminState>,
    Json(dto): Json<SetShowDTO>,
) -> ApiResult<&'static str> {
    state.content_article.set_show(dto.article_id, dto.is_show).await?;
    ok("操作成功")
}

/// `POST /adminapi/content/article/delete`：删除文章。
async fn article_delete(
    State(state): State<AdminState>,
    Json(dto): Json<IdBody>,
) -> ApiResult<&'static str> {
    state.content_article.delete(dto.id).await?;
    ok("删除成功")
}

#[derive(Debug, Deserialize)]
struct IdBody {
    id: i32,
}

/// `GET /adminapi/content/articleCategory/list`：文章分类平铺列表。
async fn category_list(
    State(state): State<AdminState>,
) -> ApiResult<Vec<ArticleCategoryVO>> {
    ok(state.content_article.categories().await?)
}

/// `GET /adminapi/content/articleCategory/tree`：文章分类树。
async fn category_tree(
    State(state): State<AdminState>,
) -> ApiResult<Vec<ArticleCategoryTreeNode>> {
    ok(state.content_article.category_tree().await?)
}

/// `GET /adminapi/content/articleCategory/detail?id=`：分类详情。
async fn category_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<ArticleCategoryVO> {
    ok(state.content_article.category_detail(q.id).await?)
}

/// `POST /adminapi/content/articleCategory/save`：新增或更新分类。
///
/// `articleCategoryId` 为 0 或缺省时新增，否则更新。
async fn category_save(
    State(state): State<AdminState>,
    Json(c): Json<ArticleCategory>,
) -> ApiResult<i32> {
    if c.article_category_id > 0 {
        let id = c.article_category_id;
        state.content_article.update_category(c).await?;
        ok(id)
    } else {
        ok(state.content_article.create_category(c).await?)
    }
}

/// `POST /adminapi/content/articleCategory/delete`：删除分类（有子分类/文章时拒绝）。
async fn category_delete(
    State(state): State<AdminState>,
    Json(dto): Json<IdBody>,
) -> ApiResult<&'static str> {
    state.content_article.delete_category(dto.id).await?;
    ok("删除成功")
}
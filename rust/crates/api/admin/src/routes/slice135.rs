//! 文章 —— 管理端路由（slice-135）骨架。
//!
//! 原 Java：`ArticleController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/content/ArticleController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `POST /content/article/batch` | `POST /adminapi/content/article/batch` | `ArticleController.batch` | jwt+perm |
//! | `GET /api/article/article/issueInfo` | `GET /api/article/article/issueInfo` | `ArticleController.issueInfo` | optional |
//! | `GET /api/article/category/indexBzzxList` | `GET /api/article/category/indexBzzxList` | `ArticleController.childrenArticleListWithCategory` | optional |
//!
//! 状态：骨架已注册并可编译，handler 待域实现者填充。
//! 路由串写**去掉 `/adminapi` 前缀**后的形式，`RouteDoc.path` 写完整契约路径。

use axum::Router;

use super::super::AdminState;

/// 域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
}

/// 已实现路由清单（覆盖率统计用）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

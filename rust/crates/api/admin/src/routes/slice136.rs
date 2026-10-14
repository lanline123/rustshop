//! 文章分类 —— 管理端路由（slice-136）骨架。
//!
//! 原 Java：`ArticleCategoryController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/content/ArticleCategoryController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `POST /content/articleCategory/update` | `POST /adminapi/content/articleCategory/update` | `ArticleCategoryController.update` | jwt+perm |
//! | `POST /content/articleCategory/updateField` | `POST /adminapi/content/articleCategory/updateField` | `ArticleCategoryController.updateField` | jwt+perm |
//! | `POST /content/articleCategory/batch` | `POST /adminapi/content/articleCategory/batch` | `ArticleCategoryController.batch` | jwt+perm |
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

//! 商城前台商户/店铺域路由。
//!
//! > **归属说明**：`ShopClientCategoryController` 的 `tree` / `parentTree`
//! > 两个端点已**整体迁至** `routes/slice106.rs`（本文件原 `tree` 内联实现
//! > 于 2026-10-17 移交 slice106）。
//! > 本文件保留为空聚合点，**不得**再注册任何店铺分类路由，否则 axum
//! > `Router::merge` 会因与 slice106 重复在服务启动时 panic。
//! > 自查：`cd rust && python tools/check_route_conflicts.py --strict`。

use axum::Router;

use super::super::PublicState;

/// 公开路由：暂无（店铺域接口已全部迁至切片模块）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
}

/// 需登录路由：暂无（本域接口均为公开）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    Vec::new()
}

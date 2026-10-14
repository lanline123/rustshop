//! 微信直播订单同步 —— 管理端路由（slice-114）骨架。
//!
//! 原 Java：`WechatLiveOrderSyncController`（`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/content/WechatLiveOrderSyncController.java`）
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /content/wechatLiveOrderSync/logs` | `GET /adminapi/content/wechatLiveOrderSync/logs` | `WechatLiveOrderSyncController.logs` | jwt+perm |
//! | `POST /content/wechatLiveOrderSync/retry/{logId}` | `POST /adminapi/content/wechatLiveOrderSync/retry/{logId}` | `WechatLiveOrderSyncController.retry` | jwt+perm |
//! | `POST /content/wechatLiveOrderSync/reconcile` | `POST /adminapi/content/wechatLiveOrderSync/reconcile` | `WechatLiveOrderSyncController.reconcile` | jwt+perm |
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

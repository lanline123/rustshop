//! 系统设置（基础/商品/购物等配置读写） —— 管理端路由（slice-187）。
//!
//! 原 Java：`com.tigshop.adminapi.controller.setting.ConfigController`
//! （`@RequestMapping("/adminapi/setting/config")`）。
//! 状态：骨架已注册并可编译，handler 待域实现者填充。
//! 路由串写**去掉 `/adminapi` 前缀**后的形式（`/setting/config/xxx`），
//! `RouteDoc.path` 写完整契约路径。

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

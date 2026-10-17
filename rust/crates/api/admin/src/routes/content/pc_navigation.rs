//! 管理端 PC 导航栏路由（content::pc_navigation）。
//! 对应原 Java `com.tigshop.adminapi.controller.decorate.PcNavigationController`
//! （`@RequestMapping("/adminapi/decorate/pcNavigation")`）。
//!
//! | 本模块路由 | 原 Java | 权限 |
//! |-----------|--------|------|
//! | `GET  /adminapi/decorate/pcNavigation/list` | `PcNavigationController.list` | `pcNavigationManage` |
//! | `GET  /adminapi/decorate/pcNavigation/detail` | `PcNavigationController.detail` | `pcNavigationManage` |
//! | `POST /adminapi/decorate/pcNavigation/create` | `PcNavigationController.create` | `pcNavigationModifyManage` |
//! | `POST /adminapi/decorate/pcNavigation/update` | `PcNavigationController.update` | `pcNavigationModifyManage` |
//! | `POST /adminapi/decorate/pcNavigation/del` | `PcNavigationController.del` | `pcNavigationModifyManage` |
//! | `POST /adminapi/decorate/pcNavigation/updateField` | `PcNavigationController.updateField` | `pcNavigationModifyManage` |
//! | `POST /adminapi/decorate/pcNavigation/batch` | `PcNavigationController.batch` | `pcNavigationModifyManage` |
//! | `GET  /adminapi/decorate/pcNavigation/selectLink` | `PcNavigationController.selectLink` | `pcNavigationManage` |
//! | `GET  /adminapi/decorate/pcNavigation/getParentNav` | `PcNavigationController.getParentNav` | `pcNavigationManage` |
//!
//! > **权限说明**：Java 用 `@PreAuthorize("@pms.hasPermission(...)")` 在类级 +
//! > 方法级校验；Rust 侧 `require_auth` 只做登录态校验，权限矩阵尚未实现
//! > （与 seckill.rs / order_admin.rs / brand.rs / shop_manage.rs 的已知缺口一致）。
//! > 上表「权限」列仅作对照，当前**任何已登录管理员**都能调用本域全部接口。
//!
//! > **返回结构说明**：Java `list` 返回 MyBatis-Plus 的 `Page<PcNavigationVO>`
//! > （`records/total/size/current/pages`），Rust 统一使用项目既有的
//! > `PageResult`（`list/total/totalPage/pageSize/page`），与 product_manage/002/005/014 一致。
//!
//! # ⚠️ 合并必读（路由冲突，注册被**故意**留空）
//!
//! 本域 9 条 `(method, path)` 在共享文件
//! `crates/api/admin/src/routes/decorate.rs` 中**已全部存在一份引导阶段的内联实现**
//! （该文件 L75-L83 的 `.route("/decorate/pcNavigation/…")` 9 行，
//! 以及其 `implemented_routes()` 的 L145-L153 9 条声明）。
//!
//! axum 0.8 的 `Router::merge` 对「同路径 + 同 method」重复注册**直接 panic**
//! （`Overlapping method route`），而 `routes/mod.rs::merge_all` 会把两者都合并
//! → **服务启动即崩**。`decorate.rs` 属共享文件，本切片不得修改，故：
//!
//! - [`routes`] 当前返回**空 Router**（9 条注册清单以注释形式留在函数体内），
//!   保证 `.merge(tigshop_core::models::content::pc_navigation::routes(state))` 合并后服务能正常启动，
//!   同时 `python tools/check_route_conflicts.py --strict` 保持通过；
//! - 9 个 handler 已按 Java 语义写全（标记 `#[allow(dead_code)]`，
//!   待去重后启用）；
//! - [`implemented_routes`] 仍声明全部 9 条（契约覆盖率不受影响）。
//!
//! ## 启用步骤（主会话二选一）
//!
//! 1. **推荐**：删除 `crates/api/admin/src/routes/decorate.rs` 中
//!    L75-L83 的 9 行 `.route("/decorate/pcNavigation/…")` 与
//!    `decorate::implemented_routes()` 中对应的 9 条记录，
//!    再把 [`routes`] 函数体末尾那 9 行注释取消注释。
//!    理由：`decorate.rs` 的 `pc_navigation_select_link` 返回的是
//!    `{label,value}` 且带 `TODO(域实现者)` 的占位清单（与 Java 的
//!    `{name,app_link,link}` 26 项完全不同）、`list` 未实现
//!    `type=2` 才回填 `hasChildren`、`getParentNav` 缺少 `is_show=1`
//!    与 `id != 42` 条件、`updateField` 白名单把 `title` 错列为可改。
//! 2. 或保持现状（`decorate.rs` 继续响应这 9 条），删除本模块的 9 个 handler
//!    与 `ROUTE_TABLE`。
//!
//! 主会话合并后**必须**运行 `python tools/check_route_conflicts.py --strict`
//! （同 `(method, path)` 被两处注册会让 axum 在服务启动时 panic）。

// `get` / `post` 只被 [`routes`] 中注释保留的 9 条注册用到：
// 去重启用时取消注释即可生效，无需再改 import（故此处显式 allow）。
#[allow(unused_imports)]
use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::content::pc_navigation::{
    NavSelectLinkVO, PcNavigationCreateDTO, PcNavigationListDTO, PcNavigationTreeVO,
    PcNavigationUpdateDTO, PcNavigationVO,
};
use tigshop_core::models::{BatchDTO, OperateDTO, UpdateFieldDTO};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域全部 9 条路由（去 `/adminapi` 前缀的路径 + 原 Java handler）。
///
/// **[`implemented_routes`] 的唯一来源**；顺序与原 Java `PcNavigationController`
/// 一致。注册清单见 [`routes`]（当前因与 `decorate.rs` 冲突而注释保留）。
const ROUTE_TABLE: [(&str, &str, &str); 9] = [
    // (HTTP method, 本模块路径, 原 Java handler)
    (
        "GET",
        "/decorate/pcNavigation/list",
        "PcNavigationController.list",
    ),
    (
        "GET",
        "/decorate/pcNavigation/detail",
        "PcNavigationController.detail",
    ),
    (
        "POST",
        "/decorate/pcNavigation/create",
        "PcNavigationController.create",
    ),
    (
        "POST",
        "/decorate/pcNavigation/update",
        "PcNavigationController.update",
    ),
    (
        "POST",
        "/decorate/pcNavigation/del",
        "PcNavigationController.del",
    ),
    (
        "POST",
        "/decorate/pcNavigation/updateField",
        "PcNavigationController.updateField",
    ),
    (
        "POST",
        "/decorate/pcNavigation/batch",
        "PcNavigationController.batch",
    ),
    (
        "GET",
        "/decorate/pcNavigation/selectLink",
        "PcNavigationController.selectLink",
    ),
    (
        "GET",
        "/decorate/pcNavigation/getParentNav",
        "PcNavigationController.getParentNav",
    ),
];

/// PC 导航栏域路由入口（挂载于 `/adminapi` 下）。
///
/// ⚠️ 当前**故意返回空 Router**：本域 9 条路径在共享文件
/// `crates/api/admin/src/routes/decorate.rs`（L75-L83）中已注册同一
/// `(method, path)`，同时合并会让 axum `Router::merge` **启动即 panic**
/// （`tools/check_route_conflicts.py` 会把这 9 条报为重复）。
/// `decorate.rs` 属共享文件，本切片不得修改，故此处暂不注册，
/// 但 9 个 handler 与 [`implemented_routes`] 的声明都已就绪。
///
/// **主会话启用步骤（二选一）**：
/// 1. **推荐**：删除 `decorate.rs` L75-L83 的 9 行
///    `.route("/decorate/pcNavigation/…", …)`，以及
///    `decorate::implemented_routes()` 中对应的 9 条记录，
///    然后把本函数体末尾被注释的 9 行 `.route(...)` 取消注释；
/// 2. 或保持现状（`decorate.rs` 继续响应这 9 条），删除本模块的
///    9 个 handler 与 `ROUTE_TABLE`。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    // ⚠️ 以下 9 行是**权威实现**的注册清单：只有在 `decorate.rs` 的同路径注册
    //    被删除后才能启用（否则 axum 启动即 panic）。启用 = 取消注释。
    // .route("/decorate/pcNavigation/list", get(nav_list))
    // .route("/decorate/pcNavigation/detail", get(nav_detail))
    // .route("/decorate/pcNavigation/create", post(nav_create))
    // .route("/decorate/pcNavigation/update", post(nav_update))
    // .route("/decorate/pcNavigation/del", post(nav_del))
    // .route("/decorate/pcNavigation/updateField", post(nav_update_field))
    // .route("/decorate/pcNavigation/batch", post(nav_batch))
    // .route("/decorate/pcNavigation/selectLink", get(nav_select_link))
    // .route("/decorate/pcNavigation/getParentNav", get(nav_parent_tree))
    Router::new()
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

// ————————————————————— 查询参数 —————————————————————

/// `detail?id=`：Java `@RequestParam Integer id`（必填）。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

/// `getParentNav?type=`：Java `@RequestParam(name = "type", defaultValue = "0")`。
#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct TypeQuery {
    r#type: Option<i32>,
}

// ————————————————————— Handlers —————————————————————

/// `GET /adminapi/decorate/pcNavigation/list`：PC 导航分页。
///
/// 对应 Java `PcNavigationController.list(PcNavigationListDTO listDTO)`。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_list(
    State(state): State<AdminState>,
    Query(q): Query<PcNavigationListDTO>,
) -> ApiResult<PageResult<PcNavigationVO>> {
    ok(state.content_pc_navigation.list(&q).await?)
}

/// `GET /adminapi/decorate/pcNavigation/detail?id=`：PC 导航详情。
///
/// 对应 Java `PcNavigationController.detail(@RequestParam Integer id)`。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<PcNavigationVO> {
    ok(state.content_pc_navigation.detail(q.id).await?)
}

/// `POST /adminapi/decorate/pcNavigation/create`：新建 PC 导航。
///
/// 对应 Java `PcNavigationController.create(@RequestBody PcNavigationCreateDTO)`
/// （原接口返回 `void`，此处回传布尔结果）。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_create(
    State(state): State<AdminState>,
    Json(dto): Json<PcNavigationCreateDTO>,
) -> ApiResult<bool> {
    ok(state.content_pc_navigation.create(&dto).await?)
}

/// `POST /adminapi/decorate/pcNavigation/update`：更新 PC 导航。
///
/// 对应 Java `PcNavigationController.update(@RequestBody PcNavigationUpdateDTO)`。
/// ⚠️ 与 Java 一致：请求体**未带 `link` 时会把链接写为 `[]`**（见服务层模块头）。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_update(
    State(state): State<AdminState>,
    Json(dto): Json<PcNavigationUpdateDTO>,
) -> ApiResult<bool> {
    ok(state.content_pc_navigation.update(&dto).await?)
}

/// `POST /adminapi/decorate/pcNavigation/del`：删除 PC 导航。
///
/// 对应 Java `PcNavigationController.del(@RequestBody OperateDTO)`。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_del(State(state): State<AdminState>, Json(dto): Json<OperateDTO>) -> ApiResult<bool> {
    ok(state.content_pc_navigation.delete(&dto).await?)
}

/// `POST /adminapi/decorate/pcNavigation/updateField`：通用字段更新。
///
/// 对应 Java `PcNavigationController.updateField(@RequestBody UpdateFieldDTO)`
/// （白名单：`is_show` / `is_blank` / `sort_order`）。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<bool> {
    ok(state.content_pc_navigation.update_field(&dto).await?)
}

/// `POST /adminapi/decorate/pcNavigation/batch`：批量操作（仅 `type = "del"`）。
///
/// 对应 Java `PcNavigationController.batch(@RequestBody BatchDTO)`。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_batch(State(state): State<AdminState>, Json(dto): Json<BatchDTO>) -> ApiResult<bool> {
    ok(state.content_pc_navigation.batch(&dto).await?)
}

/// `GET /adminapi/decorate/pcNavigation/selectLink`：可选链接地址。
///
/// 对应 Java `PcNavigationController.selectLink()`，返回
/// `[{name, app_link, link}]`（商户端版本额外含「店铺列表」）。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_select_link(State(state): State<AdminState>) -> ApiResult<Vec<NavSelectLinkVO>> {
    ok(state.content_pc_navigation.select_link())
}

/// `GET /adminapi/decorate/pcNavigation/getParentNav?type=`：上级导航树。
///
/// 对应 Java `PcNavigationController.getParentNav(type = 0)` →
/// `PcNavigationServiceImpl#getParentNav(type, 0)`。
#[allow(dead_code)] // 权威实现；待 decorate.rs 去重后启用（见模块头「合并必读」）
async fn nav_parent_tree(
    State(state): State<AdminState>,
    Query(q): Query<TypeQuery>,
) -> ApiResult<Vec<PcNavigationTreeVO>> {
    ok(state.content_pc_navigation.parent_nav(q.r#type.unwrap_or(0)).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    /// 守住「路径逐字符照抄 Java 契约」：9 条 `(method, path)` 必须完全一致。
    #[test]
    fn implemented_routes_match_java_contract_paths() {
        let docs = implemented_routes();
        let got: Vec<(String, String)> = docs
            .iter()
            .map(|d| (d.method.clone(), d.path.clone()))
            .collect();
        let want: Vec<(String, String)> = [
            ("GET", "/adminapi/decorate/pcNavigation/list"),
            ("GET", "/adminapi/decorate/pcNavigation/detail"),
            ("POST", "/adminapi/decorate/pcNavigation/create"),
            ("POST", "/adminapi/decorate/pcNavigation/update"),
            ("POST", "/adminapi/decorate/pcNavigation/del"),
            ("POST", "/adminapi/decorate/pcNavigation/updateField"),
            ("POST", "/adminapi/decorate/pcNavigation/batch"),
            ("GET", "/adminapi/decorate/pcNavigation/selectLink"),
            ("GET", "/adminapi/decorate/pcNavigation/getParentNav"),
        ]
        .iter()
        .map(|(m, p)| ((*m).to_string(), (*p).to_string()))
        .collect();
        assert_eq!(got, want);
        assert_eq!(docs.len(), 9);
    }

    /// 注册用路径必须是「去掉 `/adminapi` 前缀」且带控制器前缀
    /// `decorate/pcNavigation` 的形式。
    #[test]
    fn route_table_paths_are_relative_to_adminapi() {
        for (method, path, java_ref) in ROUTE_TABLE {
            assert!(path.starts_with("/decorate/pcNavigation/"), "{path}");
            assert!(!path.starts_with("/adminapi"), "{path} 不应带前缀");
            assert!(matches!(method, "GET" | "POST"));
            assert!(java_ref.starts_with("PcNavigationController."));
        }
        assert_eq!(ROUTE_TABLE.len(), 9);
    }
}
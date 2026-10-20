//! 许可证 —— 管理端路由（system::licensed）。
//!
//! 原 Java `LicensedController`
//! （`java/tigshop-adminapi/.../controller/setting/LicensedController.java`，
//! `@RequestMapping("/adminapi/setting/licensed")`），3 个端点：
//!
//! | 本模块路由（挂载于 `/adminapi`） | 契约权限 |
//! |-----------------------------------|----------|
//! | `GET  /setting/licensed/index` | jwt |
//! | `POST /setting/licensed/update` | licensedModifyManage |
//! | `POST /setting/licensed/saveLicensed` | licensed |
//!
//! > 权限矩阵未实现（仅 `require_auth`）；权限码来自契约，供后续 RBAC 接入。
//!
//! > `update` 依赖官网在线校验（`TigshopApiClient`），当前返回 1001
//! > 「授权在线校验未迁移」，**不伪造成功**（见服务层模块头偏差 1）。

use axum::{
    extract::State,
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::system::licensed::{
    LicensedEditParam, LicensedSaveParam, LicensedSettingsVO,
};
use tigshop_security::web::{ok, ApiResult};

use crate::support::tenant::platform_only;

use crate::AdminState;

/// 本域 3 条路由（去 `/adminapi` 前缀的路径 + 原 Java handler）。
const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    ("GET", "/setting/licensed/index", "LicensedController.getLicensedIndex"),
    ("POST", "/setting/licensed/update", "LicensedController.update"),
    ("POST", "/setting/licensed/saveLicensed", "LicensedController.saveLicensed"),
];

/// 许可证路由（挂载于 `/adminapi` 下，外层已统一 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/setting/licensed/index", get(index))
        .route("/setting/licensed/update", post(update))
        .route("/setting/licensed/saveLicensed", post(save_licensed))
        .layer(axum::middleware::from_fn(platform_only))
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

/// `GET /adminapi/setting/licensed/index`：授权信息。
async fn index(State(state): State<AdminState>) -> ApiResult<LicensedSettingsVO> {
    ok(state.system.licensed.get_licensed_index().await?)
}

/// `POST /adminapi/setting/licensed/update`：更新授权（当前返回「未迁移」）。
async fn update(
    State(state): State<AdminState>,
    Json(param): Json<LicensedEditParam>,
) -> ApiResult<()> {
    state.system.licensed.update(&param).await?;
    ok(())
}

/// `POST /adminapi/setting/licensed/saveLicensed`：保存授权。
async fn save_licensed(
    State(state): State<AdminState>,
    Json(param): Json<LicensedSaveParam>,
) -> ApiResult<()> {
    state.system.licensed.save_licensed(&param).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_contract_literally() {
        assert_eq!(ROUTE_TABLE.len(), 3);
        let docs = implemented_routes();
        assert_eq!(docs[0].path, "/adminapi/setting/licensed/index");
        assert_eq!(docs[1].method, "POST");
        assert_eq!(docs[2].path, "/adminapi/setting/licensed/saveLicensed");
        assert!(ROUTE_TABLE.iter().all(|(_, p, _)| !p.contains("/adminapi")));
    }

    #[test]
    fn only_index_is_get() {
        assert_eq!(ROUTE_TABLE.iter().filter(|(m, _, _)| *m == "GET").count(), 1);
    }
}

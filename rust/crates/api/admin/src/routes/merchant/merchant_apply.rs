//! 管理端入驻申请路由（merchant::merchant_apply）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.merchant.MerchantApplyController`
//! （`@RequestMapping("/adminapi/merchant/apply")`，8 个端点，全部需登录）。
//!
//! | 路由（挂载于 `/adminapi`） | 原 Java | 权限（`@PreAuthorize`） |
//! |---|---|---|
//! | `GET  /merchant/apply/list` | `MerchantApplyController.list` | 登录 |
//! | `GET  /merchant/apply/detail?id=` | `MerchantApplyController.detail` | 登录 |
//! | `POST /merchant/apply/update` | `MerchantApplyController.update` | `merchantApplyModifyManage` |
//! | `POST /merchant/apply/del` | `MerchantApplyController.del` | `merchantApplyModifyManage` |
//! | `POST /merchant/apply/updateField` | `MerchantApplyController.updateField` | `merchantApplyModifyManage` |
//! | `POST /merchant/apply/batch` | `MerchantApplyController.batch` | `merchantApplyModifyManage` |
//! | `POST /merchant/apply/audit` | `MerchantApplyController.audit` | `merchantApplyModifyManage` |
//! | `GET  /merchant/apply/config` | `MerchantApplyController.config` | 登录 |
//!
//! 权限矩阵尚未在 Rust 侧实现（项目已知缺口），本模块只做登录态校验。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use serde_json::Value;
use tigshop_core::models::merchant::merchant_apply::{
    MerchantApplyAuditParam, MerchantApplyListQuery, MerchantApplyUpdateDTO, MerchantApplyVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域 8 条路由（挂载路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 8] = [
    ("GET", "/merchant/apply/list", "MerchantApplyController.list"),
    ("GET", "/merchant/apply/detail", "MerchantApplyController.detail"),
    ("POST", "/merchant/apply/update", "MerchantApplyController.update"),
    ("POST", "/merchant/apply/del", "MerchantApplyController.del"),
    (
        "POST",
        "/merchant/apply/updateField",
        "MerchantApplyController.updateField",
    ),
    ("POST", "/merchant/apply/batch", "MerchantApplyController.batch"),
    ("POST", "/merchant/apply/audit", "MerchantApplyController.audit"),
    ("GET", "/merchant/apply/config", "MerchantApplyController.config"),
];

/// 入驻申请域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/merchant/apply/list", get(list))
        .route("/merchant/apply/detail", get(detail))
        .route("/merchant/apply/update", post(update))
        .route("/merchant/apply/del", post(del))
        .route("/merchant/apply/updateField", post(update_field))
        .route("/merchant/apply/batch", post(batch))
        .route("/merchant/apply/audit", post(audit))
        .route("/merchant/apply/config", get(config))
}

/// 本域已实现路由（完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

/// `GET /adminapi/merchant/apply/list`：入驻申请分页。
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<MerchantApplyListQuery>,
) -> ApiResult<PageResult<MerchantApplyVO>> {
    ok(state.merchant.merchant_apply.list(&q).await?)
}

/// `GET /adminapi/merchant/apply/detail?id=`：申请详情。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct IdQuery {
    id: i32,
}

async fn detail(State(state): State<AdminState>, Query(q): Query<IdQuery>) -> ApiResult<MerchantApplyVO> {
    ok(state.merchant.merchant_apply.detail(q.id).await?)
}

/// `POST /adminapi/merchant/apply/update`：更新（Java DTO 为空类 → 无操作）。
async fn update(
    State(state): State<AdminState>,
    Json(dto): Json<MerchantApplyUpdateDTO>,
) -> ApiResult<()> {
    state.merchant.merchant_apply.update(&dto)?;
    ok(())
}

/// `POST /adminapi/merchant/apply/del`：删除。
async fn del(State(state): State<AdminState>, Json(dto): Json<Value>) -> ApiResult<()> {
    let id = dto.get("id").and_then(Value::as_i64).map(|v| v as i32);
    state.merchant.merchant_apply.del(id).await?;
    ok(())
}

/// `POST /adminapi/merchant/apply/updateField`：更新字段。
async fn update_field(State(state): State<AdminState>, Json(dto): Json<Value>) -> ApiResult<()> {
    let id = dto.get("id").and_then(Value::as_i64).map(|v| v as i32);
    let field = dto.get("field").and_then(Value::as_str);
    let val = dto.get("val").and_then(|v| v.as_str());
    state
        .merchant
        .merchant_apply
        .update_field(id, field, val)
        .await?;
    ok(())
}

/// `POST /adminapi/merchant/apply/batch`：批量操作（仅 `type=del`）。
async fn batch(State(state): State<AdminState>, Json(dto): Json<Value>) -> ApiResult<()> {
    let ids: Option<Vec<i32>> = dto.get("ids").and_then(Value::as_array).map(|arr| {
        arr.iter()
            .filter_map(|v| v.as_i64())
            .map(|v| v as i32)
            .collect()
    });
    let op_type = dto.get("type").and_then(Value::as_str);
    state
        .merchant
        .merchant_apply
        .batch(ids.as_deref(), op_type)
        .await?;
    ok(())
}

/// `POST /adminapi/merchant/apply/audit`：审核。
async fn audit(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    _headers: HeaderMap,
    Json(param): Json<MerchantApplyAuditParam>,
) -> ApiResult<()> {
    state
        .merchant
        .merchant_apply
        .audit(&param, auth.id)
        .await?;
    ok(())
}

/// `GET /adminapi/merchant/apply/config`：状态参数。
async fn config(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(state.merchant.merchant_apply.config())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/merchant/apply/list"),
            ("GET", "/adminapi/merchant/apply/detail"),
            ("POST", "/adminapi/merchant/apply/update"),
            ("POST", "/adminapi/merchant/apply/del"),
            ("POST", "/adminapi/merchant/apply/updateField"),
            ("POST", "/adminapi/merchant/apply/batch"),
            ("POST", "/adminapi/merchant/apply/audit"),
            ("GET", "/adminapi/merchant/apply/config"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 8);
        for (doc, (m, p)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *m);
            assert_eq!(doc.path, *p);
        }
        let mut keys: Vec<String> = docs.iter().map(|d| d.key()).collect();
        keys.sort();
        keys.dedup();
        assert_eq!(keys.len(), 8);
    }

    #[test]
    fn mounted_paths_are_contract_minus_adminapi() {
        for (_, path, java_ref) in ROUTE_TABLE.iter() {
            let mounted = format!("/adminapi{path}");
            assert!(implemented_routes().iter().any(|d| d.path == mounted));
            assert!(java_ref.starts_with("MerchantApplyController."));
        }
    }
}

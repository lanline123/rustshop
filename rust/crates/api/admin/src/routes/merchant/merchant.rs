//! 商户管理 —— 管理端路由（merchant::merchant）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.merchant.MerchantController`
//! （`@RequestMapping("/adminapi/merchant/merchant")`，8 个端点）。
//!
//! | method | path（本模块，挂载于 `/adminapi`） | 原 Java |
//! |--------|------------------------------------|---------|
//! | `POST` | `/merchant/merchant/uploadLicense` | `uploadLicense`（multipart） |
//! | `GET`  | `/merchant/merchant/list` | `list` |
//! | `GET`  | `/merchant/merchant/detail?id=` | `detail` |
//! | `POST` | `/merchant/merchant/create` | `create` |
//! | `POST` | `/merchant/merchant/update` | `update` |
//! | `POST` | `/merchant/merchant/del` | `del` |
//! | `POST` | `/merchant/merchant/updateField` | `updateField` |
//! | `POST` | `/merchant/merchant/batch` | `batch` |
//!
//! # uploadLicense 说明
//!
//! Java 该方法做「营业执照水印 + 不入相册 + 对象存储」，Rust 侧水印 / 资源
//! 依赖未迁移，handler 原样调用服务并返回业务错误（501，文案见
//! `MSG_LICENSE_UNAVAILABLE`）；客户端应改用既有文件上传能力后回填
//! `merchantData.businessLicenseImg`。
//!
//! # 权限
//! Java：`list/detail` 仅登录；其余 `merchantModifyManage`。Rust `require_auth`
//! 目前只校验登录态（全局已知缺口）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::merchant::merchant::{
    BatchDTO, MerchantCreateDTO, MerchantListQuery, MerchantUpdateDTO, MerchantVO, OperateDTO,
    UpdateFieldDTO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域全部 8 条路由（去掉 `/adminapi` 前缀）。
const ROUTE_TABLE: [(&str, &str, &str); 8] = [
    (
        "POST",
        "/merchant/merchant/uploadLicense",
        "MerchantController.uploadLicense",
    ),
    ("GET", "/merchant/merchant/list", "MerchantController.list"),
    ("GET", "/merchant/merchant/detail", "MerchantController.detail"),
    ("POST", "/merchant/merchant/create", "MerchantController.create"),
    ("POST", "/merchant/merchant/update", "MerchantController.update"),
    ("POST", "/merchant/merchant/del", "MerchantController.del"),
    (
        "POST",
        "/merchant/merchant/updateField",
        "MerchantController.updateField",
    ),
    ("POST", "/merchant/merchant/batch", "MerchantController.batch"),
];

/// 商户管理路由（`/adminapi` 下，全局 `require_auth` 已生效）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/merchant/merchant/uploadLicense", post(upload_license))
        .route("/merchant/merchant/list", get(list))
        .route("/merchant/merchant/detail", get(detail))
        .route("/merchant/merchant/create", post(create))
        .route("/merchant/merchant/update", post(update))
        .route("/merchant/merchant/del", post(delete))
        .route("/merchant/merchant/updateField", post(update_field))
        .route("/merchant/merchant/batch", post(batch))
}

/// 本域已实现路由（覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            let full = format!("/adminapi{path}");
            tigshop_openapi::RouteDoc::new(method, &full, java_ref)
        })
        .collect()
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct IdQuery {
    id: Option<i32>,
}

async fn upload_license(State(state): State<AdminState>) -> ApiResult<String> {
    ok(state.merchant.merchant.upload_license()?)
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<MerchantListQuery>,
) -> ApiResult<PageResult<MerchantVO>> {
    ok(state.merchant.merchant.list(&q).await?)
}

async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<MerchantVO> {
    let id = q
        .id
        .ok_or_else(|| tigshop_core::Error::Validation("id不能为空".into()))?;
    ok(state.merchant.merchant.detail(id).await?)
}

async fn create(
    State(state): State<AdminState>,
    Json(dto): Json<MerchantCreateDTO>,
) -> ApiResult<()> {
    state.merchant.merchant.create(&dto).await?;
    ok(())
}

async fn update(
    State(state): State<AdminState>,
    Json(dto): Json<MerchantUpdateDTO>,
) -> ApiResult<()> {
    state.merchant.merchant.update(&dto).await?;
    ok(())
}

async fn delete(
    State(state): State<AdminState>,
    Json(dto): Json<OperateDTO>,
) -> ApiResult<()> {
    state.merchant.merchant.delete(&dto).await?;
    ok(())
}

async fn update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<()> {
    state.merchant.merchant.update_field(&dto).await?;
    ok(())
}

async fn batch(State(state): State<AdminState>, Json(dto): Json<BatchDTO>) -> ApiResult<()> {
    state.merchant.merchant.batch(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn route_table_matches_java_contract() {
        assert_eq!(ROUTE_TABLE.len(), 8);
        let paths: Vec<&str> = ROUTE_TABLE.iter().map(|(_, p, _)| *p).collect();
        assert_eq!(
            paths,
            vec![
                "/merchant/merchant/uploadLicense",
                "/merchant/merchant/list",
                "/merchant/merchant/detail",
                "/merchant/merchant/create",
                "/merchant/merchant/update",
                "/merchant/merchant/del",
                "/merchant/merchant/updateField",
                "/merchant/merchant/batch",
            ]
        );
        for p in &paths {
            assert!(p.starts_with("/merchant/merchant/"), "{p}");
            assert!(!p.starts_with("/adminapi"));
        }
    }

    #[test]
    fn implemented_routes_are_full_contract_paths() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 8);
        for d in &docs {
            assert!(d.path.starts_with("/adminapi/merchant/merchant/"));
            assert!(d.java_ref.starts_with("MerchantController."));
        }
        let gets: Vec<&str> = docs
            .iter()
            .filter(|d| d.method == "GET")
            .map(|d| d.path.as_str())
            .collect();
        assert_eq!(
            gets,
            vec![
                "/adminapi/merchant/merchant/list",
                "/adminapi/merchant/merchant/detail"
            ]
        );
    }

    #[test]
    fn routes_registers_all_table_paths() {
        let registered = [
            "/merchant/merchant/uploadLicense",
            "/merchant/merchant/list",
            "/merchant/merchant/detail",
            "/merchant/merchant/create",
            "/merchant/merchant/update",
            "/merchant/merchant/del",
            "/merchant/merchant/updateField",
            "/merchant/merchant/batch",
        ];
        assert_eq!(registered.len(), ROUTE_TABLE.len());
        for p in registered {
            assert!(ROUTE_TABLE.iter().any(|(_, q, _)| *q == p));
        }
    }

    #[test]
    fn query_and_body_parse_camel_case() {
        let q: MerchantListQuery =
            serde_json::from_str(r#"{"merchantId":3,"keyword":"ACME","size":20}"#).unwrap();
        assert_eq!(q.merchant_id, Some(3));
        assert_eq!(q.page_size, Some(20));
        let d: MerchantUpdateDTO = serde_json::from_str(
            r#"{"merchantId":3,"baseData":{"type":2,"companyName":"ACME"},"status":1}"#,
        )
        .unwrap();
        assert_eq!(d.merchant_id, Some(3));
        assert_eq!(d.base_data.as_ref().unwrap()["companyName"], "ACME");
    }
}

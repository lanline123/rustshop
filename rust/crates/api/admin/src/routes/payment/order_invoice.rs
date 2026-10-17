//! 管理端发票申请路由（payment::order_invoice）。
//! 对应原 Java `com.tigshop.adminapi.controller.finance.OrderInvoiceController`
//! （`@RequestMapping("/adminapi/finance/orderInvoice")`，7 个端点）。
//!
//! | 本模块路由 | 原 Java | 权限 |
//! |-----------|--------|------|
//! | `GET  /adminapi/finance/orderInvoice/list` | `list` | 登录 |
//! | `GET  /adminapi/finance/orderInvoice/detail` | `detail` | 登录 |
//! | `POST /adminapi/finance/orderInvoice/create` | `create` | `orderInvoiceUpdateManage` |
//! | `POST /adminapi/finance/orderInvoice/update` | `update` | `orderInvoiceUpdateManage` |
//! | `POST /adminapi/finance/orderInvoice/del` | `del` | `orderInvoiceDelManage` |
//! | `POST /adminapi/finance/orderInvoice/updateField` | `updateField` | `orderInvoiceUpdateManage` |
//! | `POST /adminapi/finance/orderInvoice/batch` | `batch` | `orderInvoiceBatchManage` |
//!
//! > 权限矩阵未迁移（Rust 只校验登录态）；`list` 返回项目 `PageResult`。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::payment::order_invoice::{
    BatchDTO, OperateDTO, OrderInvoiceEditParam, OrderInvoicePageQuery, OrderInvoiceSaveParam,
    OrderInvoiceVO, UpdateFieldDTO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 7 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java handler）。
const ROUTE_TABLE: [(&str, &str, &str); 7] = [
    ("GET", "/finance/orderInvoice/list", "OrderInvoiceController.list"),
    (
        "GET",
        "/finance/orderInvoice/detail",
        "OrderInvoiceController.detail",
    ),
    (
        "POST",
        "/finance/orderInvoice/create",
        "OrderInvoiceController.create",
    ),
    (
        "POST",
        "/finance/orderInvoice/update",
        "OrderInvoiceController.update",
    ),
    ("POST", "/finance/orderInvoice/del", "OrderInvoiceController.del"),
    (
        "POST",
        "/finance/orderInvoice/updateField",
        "OrderInvoiceController.updateField",
    ),
    (
        "POST",
        "/finance/orderInvoice/batch",
        "OrderInvoiceController.batch",
    ),
];

/// 发票申请域路由（挂载于 `/adminapi` 下，外层 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/finance/orderInvoice/list", get(list))
        .route("/finance/orderInvoice/detail", get(detail))
        .route("/finance/orderInvoice/create", post(create))
        .route("/finance/orderInvoice/update", post(update))
        .route("/finance/orderInvoice/del", post(del))
        .route("/finance/orderInvoice/updateField", post(update_field))
        .route("/finance/orderInvoice/batch", post(batch))
}

/// 本模块已实现路由（覆盖率统计，完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

/// `detail?id=` 查询参数（Java `@RequestParam Integer id`）。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct IdQuery {
    id: i32,
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<OrderInvoicePageQuery>,
) -> ApiResult<PageResult<OrderInvoiceVO>> {
    ok(state.payment.order_invoice.list(&q).await?)
}

async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<OrderInvoiceVO> {
    ok(state.payment.order_invoice.detail(q.id).await?)
}

async fn create(
    State(state): State<AdminState>,
    Json(param): Json<OrderInvoiceSaveParam>,
) -> ApiResult<()> {
    let _id = state.payment.order_invoice.create(&param).await?;
    ok(())
}

async fn update(
    State(state): State<AdminState>,
    Json(param): Json<OrderInvoiceEditParam>,
) -> ApiResult<()> {
    state.payment.order_invoice.update(&param).await?;
    ok(())
}

async fn del(
    State(state): State<AdminState>,
    Json(dto): Json<OperateDTO>,
) -> ApiResult<()> {
    state.payment.order_invoice.delete(&dto).await?;
    ok(())
}

async fn update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<()> {
    state.payment.order_invoice.update_field(&dto).await?;
    ok(())
}

async fn batch(State(state): State<AdminState>, Json(dto): Json<BatchDTO>) -> ApiResult<()> {
    state.payment.order_invoice.batch(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_java_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 7);
        let expected = [
            ("GET", "/adminapi/finance/orderInvoice/list"),
            ("GET", "/adminapi/finance/orderInvoice/detail"),
            ("POST", "/adminapi/finance/orderInvoice/create"),
            ("POST", "/adminapi/finance/orderInvoice/update"),
            ("POST", "/adminapi/finance/orderInvoice/del"),
            ("POST", "/adminapi/finance/orderInvoice/updateField"),
            ("POST", "/adminapi/finance/orderInvoice/batch"),
        ];
        for (method, path) in expected {
            assert!(
                docs.iter().any(|d| d.method == method && d.path == path),
                "缺少 {method} {path}"
            );
        }
        let mut keys: Vec<String> = docs.iter().map(tigshop_openapi::RouteDoc::key).collect();
        let mut unique = keys.clone();
        unique.sort();
        unique.dedup();
        keys.sort();
        assert_eq!(unique, keys);
    }

    #[test]
    fn route_table_matches_registrations() {
        let paths = [
            ("GET", "/finance/orderInvoice/list"),
            ("GET", "/finance/orderInvoice/detail"),
            ("POST", "/finance/orderInvoice/create"),
            ("POST", "/finance/orderInvoice/update"),
            ("POST", "/finance/orderInvoice/del"),
            ("POST", "/finance/orderInvoice/updateField"),
            ("POST", "/finance/orderInvoice/batch"),
        ];
        assert_eq!(paths.len(), ROUTE_TABLE.len());
        for (m, p) in paths {
            assert!(ROUTE_TABLE.iter().any(|(mm, pp, _)| *mm == m && *pp == p));
        }
    }
}

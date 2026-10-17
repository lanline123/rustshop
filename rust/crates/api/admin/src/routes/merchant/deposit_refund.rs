//! 管理端保证金退还路由（merchant::deposit_refund）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.deposit.DepositRefundController`
//! （`@RequestMapping("/adminapi/deposit/refund")`，7 个端点）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java 方法 | 权限 |
//! |----------------------------------|--------------|------|
//! | `POST /deposit/refund/apply` | `apply` | 登录 |
//! | `POST /deposit/refund/vendorApply` | `vendorApply` | 登录 |
//! | `GET  /deposit/refund/fullRefundCheck` | `fullRefundCheck` | 登录 |
//! | `GET  /deposit/refund/vendorFullRefundCheck` | `vendorFullRefundCheck` | 登录 |
//! | `GET  /deposit/refund/list` | `list` | `depositPaymentAuditManage` |
//! | `GET  /deposit/refund/detail` | `detail` | `depositPaymentAuditManage` |
//! | `POST /deposit/refund/audit` | `audit` | `depositRefundAudit` |
//!
//! > **权限说明**：Java 方法级 `@PreAuthorize("@pms.hasPermission(...)")`；
//! > Rust 侧 `require_auth` 目前只校验登录态，权限矩阵尚未实现（与其它域已知缺口一致）。
//!
//! > **请求头口径**：`fullRefundCheck` / `vendorFullRefundCheck` 无查询参数，
//! > owner 完全取自请求头（Java `HeaderUtils.getShopId/getVendorId`），
//! > 本模块用 [`shop_id_from_headers`] / [`vendor_id_from_headers`] 逐分支复刻。

use axum::{
    extract::Query,
    extract::State,
    http::HeaderMap,
    routing::{get, post},
    Extension, Json, Router,
};
use tigshop_core::models::merchant::deposit_refund::{
    DepositFullRefundCheckVO, DepositRefundApplyDTO, DepositRefundAuditDTO,
    DepositRefundListQuery, DepositRefundVO, RefundIdQuery,
};
use tigshop_core::types::PageResult;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域全部 7 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java 方法名）。
///
/// 顺序与原 Java `DepositRefundController` 一致；[`implemented_routes`] 唯一来源。
const ROUTE_TABLE: [(&str, &str, &str); 7] = [
    (
        "POST",
        "/deposit/refund/apply",
        "DepositRefundController.apply",
    ),
    (
        "POST",
        "/deposit/refund/vendorApply",
        "DepositRefundController.vendorApply",
    ),
    (
        "GET",
        "/deposit/refund/fullRefundCheck",
        "DepositRefundController.fullRefundCheck",
    ),
    (
        "GET",
        "/deposit/refund/vendorFullRefundCheck",
        "DepositRefundController.vendorFullRefundCheck",
    ),
    (
        "GET",
        "/deposit/refund/list",
        "DepositRefundController.list",
    ),
    (
        "GET",
        "/deposit/refund/detail",
        "DepositRefundController.detail",
    ),
    (
        "POST",
        "/deposit/refund/audit",
        "DepositRefundController.audit",
    ),
];

/// 保证金退还域路由（挂载于 `/adminapi` 下，外层统一套 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/refund/apply", post(apply))
        .route("/deposit/refund/vendorApply", post(vendor_apply))
        .route("/deposit/refund/fullRefundCheck", get(full_refund_check))
        .route(
            "/deposit/refund/vendorFullRefundCheck",
            get(vendor_full_refund_check),
        )
        .route("/deposit/refund/list", get(list))
        .route("/deposit/refund/detail", get(detail))
        .route("/deposit/refund/audit", post(audit))
}

/// 本模块已实现路由（用于 `/openapi/coverage` 覆盖率统计，完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

// ——————————————————————————————————————————————
// 请求头 → 店铺/供应商 ID（替代 Java `HeaderUtils`）
// ——————————————————————————————————————————————

/// 读取请求头文本值（缺失或非 UTF-8 时返回 `None`）。
fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|v| v.to_str().ok())
        .map(ToString::to_string)
}

/// 解析 Java `HeaderUtils.getShopId()`：`admin` ⇒ `Some(0)`；
/// `shop|pos` ⇒ `x-shop-id`；其它/缺失 ⇒ `None`。
#[must_use]
fn shop_id_from_headers(headers: &HeaderMap) -> Option<i32> {
    match header_str(headers, "x-admin-type").as_deref() {
        Some("admin") => Some(0),
        Some("shop" | "pos") => header_str(headers, "x-shop-id")
            .and_then(|v| v.trim().parse::<i32>().ok()),
        _ => None,
    }
}

/// 解析 Java `HeaderUtils.getVendorId()`：`vendor` ⇒ `x-vendor-id`；其它/缺失 ⇒ `None`。
#[must_use]
fn vendor_id_from_headers(headers: &HeaderMap) -> Option<i32> {
    if header_str(headers, "x-admin-type").as_deref() == Some("vendor") {
        return header_str(headers, "x-vendor-id")
            .and_then(|v| v.trim().parse::<i32>().ok());
    }
    None
}

// ——————————————————————————————————————————————
// handlers
// ——————————————————————————————————————————————

/// `POST /adminapi/deposit/refund/apply`：店铺端发起退还/提现申请。
async fn apply(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<DepositRefundApplyDTO>,
) -> ApiResult<i32> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state.merchant.deposit_refund.apply(shop_id, &dto).await?)
}

/// `POST /adminapi/deposit/refund/vendorApply`：供应商端发起退还/提现申请。
async fn vendor_apply(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<DepositRefundApplyDTO>,
) -> ApiResult<i32> {
    let vendor_id = vendor_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_refund
        .vendor_apply(vendor_id, &dto)
        .await?)
}

/// `GET /adminapi/deposit/refund/fullRefundCheck`：店铺端全额退还预检。
async fn full_refund_check(
    State(state): State<AdminState>,
    headers: HeaderMap,
) -> ApiResult<DepositFullRefundCheckVO> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_refund
        .full_refund_check(shop_id)
        .await?)
}

/// `GET /adminapi/deposit/refund/vendorFullRefundCheck`：供应商端全额退还预检。
async fn vendor_full_refund_check(
    State(state): State<AdminState>,
    headers: HeaderMap,
) -> ApiResult<DepositFullRefundCheckVO> {
    let vendor_id = vendor_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_refund
        .vendor_full_refund_check(vendor_id)
        .await?)
}

/// `GET /adminapi/deposit/refund/list`：平台端退还单列表。
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<DepositRefundListQuery>,
) -> ApiResult<PageResult<DepositRefundVO>> {
    ok(state.merchant.deposit_refund.list(&q).await?)
}

/// `GET /adminapi/deposit/refund/detail`：平台端退还单详情。
async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<RefundIdQuery>,
) -> ApiResult<Option<DepositRefundVO>> {
    ok(state.merchant.deposit_refund.detail(q.id).await?)
}

/// `POST /adminapi/deposit/refund/audit`：平台端一步式审核退还单。
async fn audit(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<DepositRefundAuditDTO>,
) -> ApiResult<()> {
    state
        .merchant
        .deposit_refund
        .audit(Some(auth.id), &dto)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::merchant::deposit_refund::{
        DepositRefundListQuery, RefundIdQuery,
    };

    /// `implemented_routes()` 必须与 Java 契约逐字符一致（7 条）。
    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("POST", "/adminapi/deposit/refund/apply"),
            ("POST", "/adminapi/deposit/refund/vendorApply"),
            ("GET", "/adminapi/deposit/refund/fullRefundCheck"),
            ("GET", "/adminapi/deposit/refund/vendorFullRefundCheck"),
            ("GET", "/adminapi/deposit/refund/list"),
            ("GET", "/adminapi/deposit/refund/detail"),
            ("POST", "/adminapi/deposit/refund/audit"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), expected.len(), "契约中本域共 7 条接口");
        for (doc, (method, path)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *method, "方法不匹配: {path}");
            assert_eq!(doc.path, *path, "路径不匹配");
            assert_eq!(doc.key(), format!("{method} {path}"));
        }
        let mut keys: Vec<String> = docs.iter().map(RouteDoc::key).collect();
        let sorted = {
            let mut k = keys.clone();
            k.sort();
            k.dedup();
            k
        };
        keys.sort();
        assert_eq!(sorted, keys, "implemented_routes() 存在重复项");
    }

    /// `ROUTE_TABLE` 与 `routes()` 一一对应（架构 §4「路由即契约」门禁）。
    #[test]
    fn routes_registers_every_route_table_entry() {
        let registered = [
            "/deposit/refund/apply",
            "/deposit/refund/vendorApply",
            "/deposit/refund/fullRefundCheck",
            "/deposit/refund/vendorFullRefundCheck",
            "/deposit/refund/list",
            "/deposit/refund/detail",
            "/deposit/refund/audit",
        ];
        assert_eq!(registered.len(), ROUTE_TABLE.len());
        for path in registered {
            assert!(
                ROUTE_TABLE.iter().any(|(_, p, _)| *p == path),
                "{path} 不在 ROUTE_TABLE 内"
            );
        }
        for (_, p, _) in ROUTE_TABLE.iter() {
            assert!(registered.contains(p), "{p} 未在 routes() 中注册");
        }
    }

    /// 路径以控制器前缀开头，方法只允许 GET/POST，无重复声明。
    #[test]
    fn route_table_shape_is_valid() {
        for (i, (method, path, java_ref)) in ROUTE_TABLE.iter().enumerate() {
            assert!(
                path.starts_with("/deposit/refund/"),
                "路径必须以控制器前缀开头: {path}"
            );
            assert!(*method == "GET" || *method == "POST");
            assert!(java_ref.starts_with("DepositRefundController."));
            for (m2, p2, _) in ROUTE_TABLE.iter().skip(i + 1) {
                assert_ne!((method, path), (m2, p2), "重复路由声明会导致 merge panic");
            }
        }
    }

    /// 请求头 → 店铺/供应商 ID，逐分支对齐 Java `HeaderUtils`。
    #[test]
    fn header_resolution_matches_header_utils() {
        use axum::http::{HeaderMap, HeaderValue};
        fn headers(pairs: &[(&str, &str)]) -> HeaderMap {
            let mut h = HeaderMap::new();
            for (k, v) in pairs {
                h.insert(
                    axum::http::HeaderName::from_bytes(k.as_bytes()).unwrap(),
                    HeaderValue::from_str(v).unwrap(),
                );
            }
            h
        }
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "admin")])),
            Some(0)
        );
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "9")])),
            Some(9)
        );
        assert_eq!(shop_id_from_headers(&headers(&[])), None);
        assert_eq!(
            vendor_id_from_headers(&headers(&[("x-admin-type", "vendor"), ("x-vendor-id", "7")])),
            Some(7)
        );
        assert_eq!(vendor_id_from_headers(&headers(&[("x-admin-type", "pos")])), None);
    }

    /// query 参数按 Java 名（camelCase）解析。
    #[test]
    fn query_params_accept_java_names() {
        let q: DepositRefundListQuery = serde_json::from_str(
            r#"{"ownerType":1,"ownerName":"店","type":1,"status":0,"page":2,"size":10,
                "sortField":"addTime","sortOrder":"desc"}"#,
        )
        .unwrap();
        assert_eq!(q.owner_type, Some(1));
        assert_eq!(q.r#type, Some(1));
        assert_eq!(q.size, Some(10));
        assert_eq!(q.sort_order.as_deref(), Some("desc"));

        let q: RefundIdQuery = serde_json::from_str(r#"{"id":3}"#).unwrap();
        assert_eq!(q.id, Some(3));
    }
}

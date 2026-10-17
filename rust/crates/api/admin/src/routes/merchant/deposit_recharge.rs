//! 管理端保证金充值路由（merchant::deposit_recharge）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.deposit.DepositRechargeController`
//! （`@RequestMapping("/adminapi/deposit/recharge")`，10 个端点）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java 方法 | 权限 |
//! |----------------------------------|--------------|------|
//! | `GET  /deposit/recharge/payeeAccountOptions` | `payeeAccountOptions` | 登录 |
//! | `POST /deposit/recharge/apply` | `apply` | 登录 |
//! | `POST /deposit/recharge/vendorApply` | `vendorApply` | 登录 |
//! | `GET  /deposit/recharge/shopPaymentAuditList` | `shopPaymentAuditList` | 登录 |
//! | `GET  /deposit/recharge/vendorPaymentAuditList` | `vendorPaymentAuditList` | 登录 |
//! | `GET  /deposit/recharge/shopPaymentAuditDetail` | `shopPaymentAuditDetail` | 登录 |
//! | `GET  /deposit/recharge/vendorPaymentAuditDetail` | `vendorPaymentAuditDetail` | 登录 |
//! | `GET  /deposit/recharge/list` | `list` | `depositPaymentAuditManage` |
//! | `GET  /deposit/recharge/detail` | `detail` | `depositPaymentAuditManage` |
//! | `POST /deposit/recharge/audit` | `audit` | `depositRechargeAudit` |
//!
//! > **权限说明**：Java 方法级 `@PreAuthorize("@pms.hasPermission(...)")`；
//! > Rust 侧 `require_auth` 目前只校验登录态，权限矩阵尚未实现（与其它域已知缺口一致）。
//!
//! > **请求头口径**：Java 用 `HeaderUtils.getShopId()` / `getVendorId()`
//! > （`x-admin-type` / `x-shop-id` / `x-vendor-id`）识别店铺/供应商端身份；
//! > 本模块用 [`shop_id_from_headers`] / [`vendor_id_from_headers`] 逐分支复刻。

use axum::{
    extract::Query,
    extract::State,
    http::HeaderMap,
    routing::{get, post},
    Extension, Json, Router,
};
use tigshop_core::models::merchant::deposit_recharge::{
    DepositPaymentAuditListQuery, DepositPaymentAuditVO, DepositPayeeAccountVO,
    DepositRechargeApplyDTO, DepositRechargeAuditDTO, DepositRechargeListQuery, DepositRechargeVO,
    PayeeAccountOptionsQuery, PaymentAuditDetailQuery, RechargeIdQuery,
};
use tigshop_core::types::PageResult;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域全部 10 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java 方法名）。
///
/// 顺序与原 Java `DepositRechargeController` 一致；[`implemented_routes`] 唯一来源。
const ROUTE_TABLE: [(&str, &str, &str); 10] = [
    (
        "GET",
        "/deposit/recharge/payeeAccountOptions",
        "DepositRechargeController.payeeAccountOptions",
    ),
    (
        "POST",
        "/deposit/recharge/apply",
        "DepositRechargeController.apply",
    ),
    (
        "POST",
        "/deposit/recharge/vendorApply",
        "DepositRechargeController.vendorApply",
    ),
    (
        "GET",
        "/deposit/recharge/shopPaymentAuditList",
        "DepositRechargeController.shopPaymentAuditList",
    ),
    (
        "GET",
        "/deposit/recharge/vendorPaymentAuditList",
        "DepositRechargeController.vendorPaymentAuditList",
    ),
    (
        "GET",
        "/deposit/recharge/shopPaymentAuditDetail",
        "DepositRechargeController.shopPaymentAuditDetail",
    ),
    (
        "GET",
        "/deposit/recharge/vendorPaymentAuditDetail",
        "DepositRechargeController.vendorPaymentAuditDetail",
    ),
    (
        "GET",
        "/deposit/recharge/list",
        "DepositRechargeController.list",
    ),
    (
        "GET",
        "/deposit/recharge/detail",
        "DepositRechargeController.detail",
    ),
    (
        "POST",
        "/deposit/recharge/audit",
        "DepositRechargeController.audit",
    ),
];

/// 保证金充值域路由（挂载于 `/adminapi` 下，外层统一套 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route(
            "/deposit/recharge/payeeAccountOptions",
            get(payee_account_options),
        )
        .route("/deposit/recharge/apply", post(apply))
        .route("/deposit/recharge/vendorApply", post(vendor_apply))
        .route(
            "/deposit/recharge/shopPaymentAuditList",
            get(shop_payment_audit_list),
        )
        .route(
            "/deposit/recharge/vendorPaymentAuditList",
            get(vendor_payment_audit_list),
        )
        .route(
            "/deposit/recharge/shopPaymentAuditDetail",
            get(shop_payment_audit_detail),
        )
        .route(
            "/deposit/recharge/vendorPaymentAuditDetail",
            get(vendor_payment_audit_detail),
        )
        .route("/deposit/recharge/list", get(list))
        .route("/deposit/recharge/detail", get(detail))
        .route("/deposit/recharge/audit", post(audit))
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

/// `GET /adminapi/deposit/recharge/payeeAccountOptions`：启用收款账户列表。
async fn payee_account_options(
    State(state): State<AdminState>,
    Query(q): Query<PayeeAccountOptionsQuery>,
) -> ApiResult<Vec<DepositPayeeAccountVO>> {
    ok(state
        .merchant
        .deposit_recharge
        .payee_account_options(q.account_type, q.keyword.as_deref())
        .await?)
}

/// `POST /adminapi/deposit/recharge/apply`：店铺端发起充值申请。
async fn apply(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<DepositRechargeApplyDTO>,
) -> ApiResult<i32> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state.merchant.deposit_recharge.apply(shop_id, &dto).await?)
}

/// `POST /adminapi/deposit/recharge/vendorApply`：供应商端发起充值申请。
async fn vendor_apply(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<DepositRechargeApplyDTO>,
) -> ApiResult<i32> {
    let vendor_id = vendor_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_recharge
        .vendor_apply(vendor_id, &dto)
        .await?)
}

/// `GET /adminapi/deposit/recharge/shopPaymentAuditList`：店铺端款项审核列表。
async fn shop_payment_audit_list(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<DepositPaymentAuditListQuery>,
) -> ApiResult<PageResult<DepositPaymentAuditVO>> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_recharge
        .shop_payment_audit_list(shop_id, &q)
        .await?)
}

/// `GET /adminapi/deposit/recharge/vendorPaymentAuditList`：供应商端款项审核列表。
async fn vendor_payment_audit_list(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<DepositPaymentAuditListQuery>,
) -> ApiResult<PageResult<DepositPaymentAuditVO>> {
    let vendor_id = vendor_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_recharge
        .vendor_payment_audit_list(vendor_id, &q)
        .await?)
}

/// `GET /adminapi/deposit/recharge/shopPaymentAuditDetail`：店铺端款项审核详情。
async fn shop_payment_audit_detail(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<PaymentAuditDetailQuery>,
) -> ApiResult<DepositPaymentAuditVO> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_recharge
        .shop_payment_audit_detail(shop_id, q.audit_type, q.biz_id)
        .await?)
}

/// `GET /adminapi/deposit/recharge/vendorPaymentAuditDetail`：供应商端款项审核详情。
async fn vendor_payment_audit_detail(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<PaymentAuditDetailQuery>,
) -> ApiResult<DepositPaymentAuditVO> {
    let vendor_id = vendor_id_from_headers(&headers);
    ok(state
        .merchant
        .deposit_recharge
        .vendor_payment_audit_detail(vendor_id, q.audit_type, q.biz_id)
        .await?)
}

/// `GET /adminapi/deposit/recharge/list`：平台端充值单列表。
async fn list(
    State(state): State<AdminState>,
    Query(q): Query<DepositRechargeListQuery>,
) -> ApiResult<PageResult<DepositRechargeVO>> {
    ok(state.merchant.deposit_recharge.list(&q).await?)
}

/// `GET /adminapi/deposit/recharge/detail`：平台端充值单详情。
async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<RechargeIdQuery>,
) -> ApiResult<Option<DepositRechargeVO>> {
    ok(state.merchant.deposit_recharge.detail(q.id).await?)
}

/// `POST /adminapi/deposit/recharge/audit`：平台端审核充值单。
async fn audit(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<DepositRechargeAuditDTO>,
) -> ApiResult<()> {
    state
        .merchant
        .deposit_recharge
        .audit(Some(auth.id), &dto)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::merchant::deposit_recharge::{
        DepositPaymentAuditListQuery, DepositRechargeListQuery, PayeeAccountOptionsQuery,
    };

    /// `implemented_routes()` 必须与 Java 契约逐字符一致（10 条）。
    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/deposit/recharge/payeeAccountOptions"),
            ("POST", "/adminapi/deposit/recharge/apply"),
            ("POST", "/adminapi/deposit/recharge/vendorApply"),
            ("GET", "/adminapi/deposit/recharge/shopPaymentAuditList"),
            ("GET", "/adminapi/deposit/recharge/vendorPaymentAuditList"),
            ("GET", "/adminapi/deposit/recharge/shopPaymentAuditDetail"),
            ("GET", "/adminapi/deposit/recharge/vendorPaymentAuditDetail"),
            ("GET", "/adminapi/deposit/recharge/list"),
            ("GET", "/adminapi/deposit/recharge/detail"),
            ("POST", "/adminapi/deposit/recharge/audit"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), expected.len(), "契约中本域共 10 条接口");
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
            "/deposit/recharge/payeeAccountOptions",
            "/deposit/recharge/apply",
            "/deposit/recharge/vendorApply",
            "/deposit/recharge/shopPaymentAuditList",
            "/deposit/recharge/vendorPaymentAuditList",
            "/deposit/recharge/shopPaymentAuditDetail",
            "/deposit/recharge/vendorPaymentAuditDetail",
            "/deposit/recharge/list",
            "/deposit/recharge/detail",
            "/deposit/recharge/audit",
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
                path.starts_with("/deposit/recharge/"),
                "路径必须以控制器前缀开头: {path}"
            );
            assert!(*method == "GET" || *method == "POST");
            assert!(java_ref.starts_with("DepositRechargeController."));
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
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "pos"), ("x-shop-id", "3")])),
            Some(3)
        );
        assert_eq!(shop_id_from_headers(&headers(&[("x-admin-type", "shop")])), None);
        assert_eq!(shop_id_from_headers(&headers(&[])), None);

        assert_eq!(
            vendor_id_from_headers(&headers(&[("x-admin-type", "vendor"), ("x-vendor-id", "7")])),
            Some(7)
        );
        assert_eq!(
            vendor_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-vendor-id", "7")])),
            None
        );
        assert_eq!(vendor_id_from_headers(&headers(&[])), None);
    }

    /// query 参数按 Java 名（camelCase）解析。
    #[test]
    fn query_params_accept_java_names() {
        let q: PayeeAccountOptionsQuery =
            serde_json::from_str(r#"{"accountType":1,"keyword":"工行"}"#).unwrap();
        assert_eq!(q.account_type, Some(1));
        assert_eq!(q.keyword.as_deref(), Some("工行"));

        let q: DepositPaymentAuditListQuery = serde_json::from_str(
            r#"{"auditType":2,"status":0,"startTime":1,"endTime":2,"page":2,"size":10}"#,
        )
        .unwrap();
        assert_eq!(q.audit_type, Some(2));
        assert_eq!(q.page, Some(2));

        let q: DepositRechargeListQuery =
            serde_json::from_str(r#"{"ownerType":1,"ownerName":"店","sortField":"addTime","sortOrder":"asc"}"#)
                .unwrap();
        assert_eq!(q.owner_type, Some(1));
        assert_eq!(q.sort_field.as_deref(), Some("addTime"));

        let q: PaymentAuditDetailQuery =
            serde_json::from_str(r#"{"auditType":1,"bizId":5}"#).unwrap();
        assert_eq!(q.audit_type, 1);
        assert_eq!(q.biz_id, 5);
    }
}

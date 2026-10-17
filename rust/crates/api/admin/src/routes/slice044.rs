//! 供应商端保证金 —— 管理端路由（slice-044）。
//!
//! 原 Java：`VendorDepositShopController`
//! （`tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/deposit/VendorDepositShopController.java`，
//! `@RestController` + `@RequestMapping("/adminapi/deposit/vendor")`，
//! **无**类级 / 方法级 `@PreAuthorize`）。
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /deposit/vendor/overview` | `GET /adminapi/deposit/vendor/overview` | `VendorDepositShopController.overview`（`:32`） | jwt |
//! | `GET /deposit/vendor/logList` | `GET /adminapi/deposit/vendor/logList` | `VendorDepositShopController.logList`（`:38`） | jwt |
//! | `GET /deposit/vendor/frozenDetailList` | `GET /adminapi/deposit/vendor/frozenDetailList` | `VendorDepositShopController.frozenDetailList`（`:44`） | jwt |
//! | `GET /deposit/vendor/warning` | `GET /adminapi/deposit/vendor/warning` | `VendorDepositShopController.warning`（`:50`） | jwt |
//! | `GET /deposit/vendor/protocol` | `GET /adminapi/deposit/vendor/protocol` | `VendorDepositShopController.protocol`（`:56`） | jwt |
//!
//! 路由串写**去掉 `/adminapi` 前缀**后的形式，`RouteDoc.path` 写完整契约路径。
//! ⚠️ `logList` / `frozenDetailList` 是**小驼峰**（不是 `log_list` /
//! `frozen_detail_list`），必须逐字符照抄。
//!
//! ## 多租户 / 身份（**本域最关键的语义**）
//!
//! 控制器私有方法 `currentVendorId()`（`:72-78`）：
//!
//! ```java
//! Integer vendorId = HeaderUtils.getVendorId();
//! if (vendorId == null || vendorId <= 0) {
//!     throw new GlobalException("无法获取当前供应商信息", HttpStatus.UNAUTHORIZED);
//! }
//! ```
//!
//! `HeaderUtils.getVendorId()`（`HeaderUtils.java:154-161`）**只**在
//! `getAdminType()` 等于 `"vendor"` 时读取请求头 `x-vendor-id`，
//! 否则返回 `null`；`x-vendor-id` 为空串时也返回 `null`。
//!
//! ⇒ 所有 5 个接口都**必须**带 `x-admin-type: vendor` + 合法 `x-vendor-id`，
//! 否则 401 + `"无法获取当前供应商信息"`。
//! 本模块用 [`resolve_vendor_id`] 复刻该判定（`x-admin-type` 缺失时回退
//! JWT 的 `admin_type`，与 slice008/010/144 同口径），并**不接收**外部传入的
//! `vendorId`，避免越权查询他供应商保证金。
//!
//! ⚠️ `protocol` 在 Java 里**不调用** `currentVendorId()`（协议是平台级配置），
//! 但本实现仍统一做身份校验：`/adminapi` 子树整体已挂 `require_auth`
//! （登录态），而 Java 侧 `x-admin-type: vendor` 是**认证过滤器**要求的合法后台
//! 类型之一（`JwtAuthenticationFilter.java:138-142`）。为保持 5 个接口口径一致、
//! 且不放过「平台管理员误调供应商端接口」，此处同样校验供应商身份。
//! 这一处**相对 Java 更严格**，已列入 `uncertain_points`。
//!
//! ## 返回结构说明
//!
//! Java `logList` / `frozenDetailList` 返回 MyBatis-Plus
//! `Page<T>`（`records`/`total`/`size`/`current`/`pages`），Rust 统一使用项目既有
//! [`PageResult`]（`list`/`total`/`totalPage`/`pageSize`/`page`），
//! 与 slice001/002/005/010/014/017/075/163 一致。
//! 其余 3 个端点返回单个 VO。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::get,
    Extension, Router,
};
use tigshop_core::models::slice044::{
    DepositShopFrozenDetailVO, DepositShopLogQuery, DepositShopLogVO, DepositShopOverviewVO,
    DepositShopProtocolVO, DepositShopWarningVO, HTTP_UNAUTHORIZED, MSG_VENDOR_REQUIRED,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};

use super::super::AdminState;

/// 本域全部 5 条路由（`(HTTP method, 去掉 /adminapi 前缀的路径, 原 Java handler)`）。
///
/// **这是 [`implemented_routes`] 的唯一来源**，与 [`routes`] 里的注册一一对应。
const ROUTE_TABLE: [(&str, &str, &str); 5] = [
    (
        "GET",
        "/deposit/vendor/overview",
        "VendorDepositShopController.overview",
    ),
    (
        "GET",
        "/deposit/vendor/logList",
        "VendorDepositShopController.logList",
    ),
    (
        "GET",
        "/deposit/vendor/frozenDetailList",
        "VendorDepositShopController.frozenDetailList",
    ),
    (
        "GET",
        "/deposit/vendor/warning",
        "VendorDepositShopController.warning",
    ),
    (
        "GET",
        "/deposit/vendor/protocol",
        "VendorDepositShopController.protocol",
    ),
];

/// 供应商端保证金域路由（挂载于 `/adminapi` 下，全局 `require_auth` 已生效）。
///
/// ⚠️ 路径必须写成**字符串字面量**（不能从 [`ROUTE_TABLE`] 循环生成）：
/// `tools/check_api_coverage.py::collect_rust_routes` 静态扫描
/// `\.route\(\s*"(?P<path>...)"`，循环拼接会让覆盖率统计看不见这些路由。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/deposit/vendor/overview", get(overview))
        .route("/deposit/vendor/logList", get(log_list))
        .route("/deposit/vendor/frozenDetailList", get(frozen_detail_list))
        .route("/deposit/vendor/warning", get(warning))
        .route("/deposit/vendor/protocol", get(protocol))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
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

// ——————————————————————————————————————————————
// 请求头 → 供应商标识（替代 Java HeaderUtils.getVendorId）
// ——————————————————————————————————————————————

/// 读取请求头文本值（缺失 / 非 UTF-8 / 空白 → `None`）。
///
/// Java `HeaderUtils.getHeaderValue` 用 `StrUtil.isEmpty`（**只判空串**，
/// 不 trim）。本实现按 `trim` 后判空，与 slice008/010/144 的既有口径一致：
/// `"   "` 视为缺失（Java 会走 `Integer.parseInt("   ")` 抛
/// `NumberFormatException` → HTTP 500，本实现按缺失处理，见 `uncertain_points`）。
fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|v| v.to_str().ok())
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .map(ToString::to_string)
}

/// 解析当前请求的供应商标识
/// （对应 Java `HeaderUtils.getAdminType()` + `getVendorId()`）。
///
/// 判定顺序逐条对齐 Java：
/// 1. `admin_type` 取请求头 `x-admin-type`，缺失时回退 JWT 的 `admin_type`
///    （Rust JWT 已含 `typ`，见 `AuthUser.admin_type`）；
/// 2. 只有 `admin_type == "vendor"` 才读 `x-vendor-id`
///    （Java `getVendorId()` 的 `else { return null; }`）；
/// 3. `x-vendor-id` 缺失 / 非数字 → `0`；
/// 4. 返回值 `<= 0` 由 [`require_vendor_id`] 转 401。
#[must_use]
pub fn resolve_vendor_id(headers: &HeaderMap, auth: &AuthUser) -> i32 {
    let admin_type = header_str(headers, "x-admin-type").or_else(|| auth.admin_type.clone());
    match admin_type.as_deref() {
        Some("vendor") => header_str(headers, "x-vendor-id")
            .and_then(|v| v.parse::<i32>().ok())
            .unwrap_or(0),
        // Java `getVendorId()`：非 vendor 账号一律返回 null（→ 0）
        _ => 0,
    }
}

/// 校验并返回供应商标识。
///
/// 对应 Java `VendorDepositShopController#currentVendorId`：
/// `null` 或 `<= 0` → `GlobalException("无法获取当前供应商信息", 401)`。
/// 本实现用 `Error::biz(401, ...)`，经 `AppError` 映射为 HTTP 401 +
/// `{"code":401,"msg":"无法获取当前供应商信息"}`。
fn require_vendor_id(vendor_id: i32) -> Result<i32, AppError> {
    if vendor_id > 0 {
        Ok(vendor_id)
    } else {
        Err(tigshop_core::Error::biz(HTTP_UNAUTHORIZED, MSG_VENDOR_REQUIRED).into())
    }
}

/// 从请求头 + JWT 解析并校验当前供应商（5 个 handler 的公共前置）。
fn current_vendor(headers: &HeaderMap, auth: &AuthUser) -> Result<i32, AppError> {
    require_vendor_id(resolve_vendor_id(headers, auth))
}

// ——————————————————————————————————————————————
// Handlers
// ——————————————————————————————————————————————

/// `GET /adminapi/deposit/vendor/overview`：保证金总览。
///
/// 对应 Java `VendorDepositShopController.overview()`（`:41`）
/// → `VendorDepositShopServiceImpl#getOverview(vendorId)`。
async fn overview(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
) -> ApiResult<DepositShopOverviewVO> {
    let vendor_id = current_vendor(&headers, &auth)?;
    ok(state.slice044.overview(vendor_id).await?)
}

/// `GET /adminapi/deposit/vendor/logList`：供应商端交易流水（分页）。
///
/// 对应 Java `VendorDepositShopController.logList(DepositShopLogQueryParam param)`（`:47`）
/// → `VendorDepositShopServiceImpl#logList(vendorId, param)`。
///
/// 查询参数按 Java `DepositShopLogQueryParam`（继承 `BasePage`）契约：
/// `type` / `tradeNo` / `startTime` / `endTime` / `page` / `size`
/// （注意是 `size`，不是 `pageSize`）。
async fn log_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Query(q): Query<DepositShopLogQuery>,
) -> ApiResult<PageResult<DepositShopLogVO>> {
    let vendor_id = current_vendor(&headers, &auth)?;
    ok(state.slice044.log_list(vendor_id, &q).await?)
}

/// `GET /adminapi/deposit/vendor/frozenDetailList`：冻结明细（平台扣款记录）。
///
/// 对应 Java
/// `VendorDepositShopController.frozenDetailList(DepositShopLogQueryParam param)`（`:53`）
/// → `VendorDepositShopServiceImpl#frozenDetailList(vendorId, param)`。
///
/// ⚠️ 实现只返回 `type = 4`（平台扣除）的流水，`param.type` 被**忽略**
/// （Java 亦然），详见 `core::models::slice044::DepositShopFrozenDetailVO` 的注释。
async fn frozen_detail_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Query(q): Query<DepositShopLogQuery>,
) -> ApiResult<PageResult<DepositShopFrozenDetailVO>> {
    let vendor_id = current_vendor(&headers, &auth)?;
    ok(state.slice044.frozen_detail_list(vendor_id, &q).await?)
}

/// `GET /adminapi/deposit/vendor/warning`：首页保证金预警横幅。
///
/// 对应 Java `VendorDepositShopController.warning()`（`:59`）
/// → `VendorDepositShopServiceImpl#getWarning(vendorId)`。
async fn warning(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
) -> ApiResult<DepositShopWarningVO> {
    let vendor_id = current_vendor(&headers, &auth)?;
    ok(state.slice044.warning(vendor_id).await?)
}

/// `GET /adminapi/deposit/vendor/protocol`：保证金协议。
///
/// 对应 Java `VendorDepositShopController.protocol()`（`:65`）
/// → `VendorDepositShopServiceImpl#getProtocol()`（**与 vendorId 无关**，
/// 读平台级配置 `deposit_protocol_enable` / `deposit_protocol_content`）。
///
/// ⚠️ 本实现仍校验供应商身份（见模块头「多租户 / 身份」的说明）。
async fn protocol(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
) -> ApiResult<DepositShopProtocolVO> {
    let _vendor_id = current_vendor(&headers, &auth)?;
    ok(state.slice044.protocol().await?)
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::http::{HeaderName, HeaderValue};

    fn headers(pairs: &[(&str, &str)]) -> HeaderMap {
        let mut h = HeaderMap::new();
        for (k, v) in pairs {
            h.insert(
                HeaderName::from_bytes(k.as_bytes()).expect("合法 header 名"),
                HeaderValue::from_str(v).expect("合法 header 值"),
            );
        }
        h
    }

    fn auth_with_type(t: Option<&str>) -> AuthUser {
        AuthUser {
            id: 1,
            username: "tester".into(),
            role_id: Some(1),
            admin_type: t.map(ToString::to_string),
        }
    }

    // ————————————— 路由契约（逐字符） —————————————

    /// 5 条路由必须与契约逐字符一致（含 `/adminapi` 前缀与小驼峰 `logList`）。
    #[test]
    fn implemented_routes_match_contract_paths() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 5, "本域应有 5 条路由");

        let keys: Vec<String> = docs.iter().map(|d| d.key()).collect();
        let expected = [
            "GET /adminapi/deposit/vendor/overview",
            "GET /adminapi/deposit/vendor/logList",
            "GET /adminapi/deposit/vendor/frozenDetailList",
            "GET /adminapi/deposit/vendor/warning",
            "GET /adminapi/deposit/vendor/protocol",
        ];
        for e in expected {
            assert!(keys.contains(&e.to_string()), "缺少路由 {e}；实际={keys:?}");
        }
        // 逐字符比对（防止 `log_list` 之类的顺手简化）
        assert_eq!(docs[1].path, "/adminapi/deposit/vendor/logList");
        assert_eq!(docs[2].path, "/adminapi/deposit/vendor/frozenDetailList");
    }

    /// `implemented_routes` 里的 Java 引用必须指向 `VendorDepositShopController.*`。
    #[test]
    fn implemented_routes_reference_java_handlers() {
        for d in implemented_routes() {
            assert!(
                d.java_ref.starts_with("VendorDepositShopController."),
                "java_ref 异常: {}",
                d.java_ref
            );
            assert_eq!(d.method, "GET");
        }
    }

    /// 路由表长度与接口数一致，且本模块路径不带 `/adminapi` 前缀。
    #[test]
    fn route_table_covers_all_and_has_no_adminapi_prefix() {
        assert_eq!(ROUTE_TABLE.len(), 5);
        for (_, path, _) in ROUTE_TABLE {
            assert!(!path.contains("/adminapi"), "本地路径不得带前缀: {path}");
            assert!(path.starts_with('/'), "本地路径须以 / 开头: {path}");
        }
    }

    // ————————————— 供应商标识解析（对照 HeaderUtils.getVendorId） —————————————

    #[test]
    fn vendor_header_is_used_when_admin_type_is_vendor() {
        let id = resolve_vendor_id(
            &headers(&[("x-admin-type", "vendor"), ("x-vendor-id", "12")]),
            &auth_with_type(None),
        );
        assert_eq!(id, 12);
    }

    #[test]
    fn non_vendor_admin_type_never_yields_vendor_id() {
        // Java getVendorId() 只在 adminType == "vendor" 时读请求头
        for t in ["admin", "shop", "pos", "store", "pickup"] {
            let id = resolve_vendor_id(
                &headers(&[("x-admin-type", t), ("x-vendor-id", "12")]),
                &auth_with_type(None),
            );
            assert_eq!(id, 0, "admin_type={t} 不应解析出 vendorId");
        }
        // 完全缺失 admin_type
        let id = resolve_vendor_id(&headers(&[("x-vendor-id", "12")]), &auth_with_type(None));
        assert_eq!(id, 0);
    }

    #[test]
    fn missing_or_dirty_vendor_header_falls_back_to_zero() {
        for raw in ["", "   ", "abc", "0", "-3"] {
            let id = resolve_vendor_id(
                &headers(&[("x-admin-type", "vendor"), ("x-vendor-id", raw)]),
                &auth_with_type(None),
            );
            assert_eq!(id, 0, "x-vendor-id={raw:?} 应归一为 0");
        }
        // 头缺失
        let id = resolve_vendor_id(&headers(&[("x-admin-type", "vendor")]), &auth_with_type(None));
        assert_eq!(id, 0);
    }

    #[test]
    fn jwt_admin_type_is_fallback_when_header_missing() {
        let id = resolve_vendor_id(
            &headers(&[("x-vendor-id", "9")]),
            &auth_with_type(Some("vendor")),
        );
        assert_eq!(id, 9);

        // 请求头优先于 JWT
        let id = resolve_vendor_id(
            &headers(&[("x-admin-type", "admin"), ("x-vendor-id", "9")]),
            &auth_with_type(Some("vendor")),
        );
        assert_eq!(id, 0);
    }

    /// 非法供应商标识必须映射为 401 + Java 原文案。
    #[test]
    fn require_vendor_id_rejects_non_positive_with_401() {
        for bad in [0, -1, i32::MIN] {
            let err = require_vendor_id(bad).expect_err("非正数应被拒绝");
            assert_eq!(err.0.business_code(), 401);
            assert_eq!(err.0.to_string(), format!("业务错误 [401]: {MSG_VENDOR_REQUIRED}"));
        }
        assert_eq!(require_vendor_id(7).expect("正数应通过"), 7);
    }

    /// 完整的「请求头 + JWT → 供应商」链路。
    #[test]
    fn current_vendor_end_to_end() {
        let ok_id = current_vendor(
            &headers(&[("x-admin-type", "vendor"), ("x-vendor-id", "5")]),
            &auth_with_type(None),
        )
        .expect("合法供应商请求应通过");
        assert_eq!(ok_id, 5);

        let err = current_vendor(&headers(&[]), &auth_with_type(None)).expect_err("缺头应 401");
        assert_eq!(err.0.business_code(), 401);
    }

    // ————————————— 查询参数契约 —————————————

    /// 查询参数按 Java `DepositShopLogQueryParam` 命名（`type` / `tradeNo` /
    /// `startTime` / `endTime` / `page` / `size`）。
    #[test]
    fn log_query_accepts_java_param_names() {
        let q: DepositShopLogQuery = serde_json::from_str(
            r#"{"type":2,"tradeNo":"123","startTime":1,"endTime":2,"page":3,"size":4}"#,
        )
        .expect("应能解析 Java 参数名");
        assert_eq!(q.r#type, Some(2));
        assert_eq!(q.trade_no.as_deref(), Some("123"));
        assert_eq!(q.start_time, Some(1));
        assert_eq!(q.end_time, Some(2));
        assert_eq!(q.page, Some(3));
        assert_eq!(q.size, Some(4));

        // 空查询串（`GET .../logList` 不带参数）必须可用
        let q: DepositShopLogQuery =
            serde_json::from_str("{}").expect("空对象应可解析（全字段可选）");
        assert!(q.r#type.is_none());
        assert!(q.trade_no.is_none());
        assert!(q.start_time.is_none());
        assert!(q.end_time.is_none());
    }

    /// `type=2/3` 走「提现/退还合并」分支，其余走等值。
    #[test]
    fn log_query_type_filter_merges_withdraw_and_refund() {
        let mk = |t: i32| DepositShopLogQuery {
            r#type: Some(t),
            ..Default::default()
        };
        assert_eq!(mk(2).type_filter(), vec![2, 3]);
        assert_eq!(mk(3).type_filter(), vec![2, 3]);
        assert_eq!(mk(1).type_filter(), vec![1]);
        assert_eq!(mk(4).type_filter(), vec![4]);
        assert_eq!(mk(5).type_filter(), vec![5]);
        assert!(DepositShopLogQuery::default().type_filter().is_empty());
    }
}

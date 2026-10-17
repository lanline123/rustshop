//! 管理端订单管理路由（trade::order_admin）。
//! 对应原 Java `com.tigshop.adminapi.controller.order.OrderController`
//! （`@RequestMapping("/adminapi/order/order")`）。
//!
//! | 本模块路由 | 原 Java | 权限 |
//! |-----------|--------|------|
//! | `GET  /adminapi/order/order/list` | `OrderController.list` | 登录 |
//! | `GET  /adminapi/order/order/detail` | `OrderController.detail` | 登录 |
//! | `POST /adminapi/order/order/deliver` | `OrderController.deliver` | `orderDeliverManage` |
//! | `POST /adminapi/order/order/cancelOrderByAdmin` | `OrderController.cancelOrderByAdmin` | 登录 |
//!
//! > **权限说明**：Java 用 `@PreAuthorize("@pms.hasPermission(...)")` 在方法级校验；
//! > Rust 侧 `require_auth` 目前只做登录态校验，权限矩阵尚未实现
//! > （与 seckill.rs / brand.rs 的已知缺口一致）。
//! >
//! > **多租户说明**：Java 通过 `HeaderUtils.getShopId()` / `getAdminType()`
//! > （`x-shop-id` / `x-admin-type` 请求头）区分平台、店铺、供应商、门店账号；
//! > Rust 侧 JWT 尚未签发这些字段，故列表按查询参数
//! > （`shopId` / `vendorId`）处理，平台/店铺视角差异见
//! > `repo::tigshop_core::models::trade::order_admin::build_filter` 注释。
//! >
//! > **⚠️ 合并注意**：这 4 条路径在 `crates/api/admin/src/lib.rs` 中已存在一份
//! > **引导阶段的内联实现**（`/order/order/list` 等 + `implemented_routes()` 的
//! > 4 条 base 记录）。合并本模块时必须**同时删除**那 4 条内联路由与 4 条 base
//! > 记录，否则 axum 的 `Router::merge` 会因同一路径重复注册在启动时 panic。
//!
//! > **返回结构说明**：Java `list` 返回 MyBatis-Plus 的 `Page<OrderVO>`
//! > （`records/total/size/current/pages`），Rust 统一使用项目既有的
//! > `PageResult`（`list/total/totalPage/pageSize/page`）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::trade::order_admin::{
    AftersalesApplyAdminParam, DeliverOrderParam, OrderListQuery, OrderVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 订单管理域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/order/order/list", get(order_list))
        .route("/order/order/detail", get(order_detail))
        .route("/order/order/deliver", post(order_deliver))
        .route("/order/order/cancelOrderByAdmin", post(order_cancel_by_admin))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/order/order/list", "OrderController.list"),
        ("GET", "/adminapi/order/order/detail", "OrderController.detail"),
        ("POST", "/adminapi/order/order/deliver", "OrderController.deliver"),
        (
            "POST",
            "/adminapi/order/order/cancelOrderByAdmin",
            "OrderController.cancelOrderByAdmin",
        ),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/order/order/list`：订单分页。
///
/// 对应 Java `OrderController.list(OrderListPageQuery)`。Java 由请求头
/// `x-shop-id` 决定平台/店铺视角（影响是否排除未支付的拆单子单），
/// Rust 侧 JWT 无 shop_id，改按 `admin_type` 判定：`admin` 或缺失视为平台视角。
async fn order_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<OrderListQuery>,
) -> ApiResult<PageResult<OrderVO>> {
    let platform_view = auth
        .admin_type
        .as_deref()
        .is_none_or(|t| t.is_empty() || t == "admin");
    ok(state.trade_order_admin.list(&q, platform_view).await?)
}

/// `GET /adminapi/order/order/detail?id=`：订单详情。
///
/// 对应 Java `OrderController.detail(@RequestParam Integer id)`。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

async fn order_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<OrderVO> {
    ok(state.trade_order_admin.detail(q.id).await?)
}

/// `POST /adminapi/order/order/deliver`：发货。
///
/// 对应 Java `OrderController.deliver(@RequestBody @Validated DeliverOrderParam)`。
/// Java 返回 `void`，此处返回提示文案（项目内既有约定）。
async fn order_deliver(
    State(state): State<AdminState>,
    Json(param): Json<DeliverOrderParam>,
) -> ApiResult<&'static str> {
    state.trade_order_admin.deliver(&param).await?;
    ok("发货成功")
}

/// `POST /adminapi/order/order/cancelOrderByAdmin`：后台取消订单。
///
/// 对应 Java `OrderController.cancelOrderByAdmin(@RequestBody @Validated
/// AftersalesApplyAdminParam)`：按整单创建「仅退款」售后申请，
/// 由售后审核流程完成退款/关单（原接口返回 `void`）。
///
/// 这里额外回传新建的 `aftersaleId` 便于运维排查（`data` 由 `null` 变为对象）。
async fn order_cancel_by_admin(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Json(param): Json<AftersalesApplyAdminParam>,
) -> ApiResult<serde_json::Value> {
    let aftersale_id = state.trade_order_admin
        .cancel_order_by_admin(&param, Some(auth.username.as_str()))
        .await?;
    ok(serde_json::json!({ "aftersaleId": aftersale_id }))
}
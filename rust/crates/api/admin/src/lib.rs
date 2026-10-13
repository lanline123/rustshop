//! 管理后台 API（对应原 `tigshop-adminapi` 模块）。
//!
//! 路由前缀 `/adminapi`，与原 Java 控制器保持一致：
//! | 路由 | 原 Java 控制器 |
//! |------|---------------|
//! | `POST /adminapi/login` | Spring Security `LoginFilter` |
//! | `GET /adminapi/authority/adminUser/list` | `AdminUserController.list` |
//! | `GET /adminapi/product/list` | `ProductController.list` |
//! | `GET /adminapi/order/list` | `OrderController.list` |
//! | `POST /adminapi/order/ship` | `OrderController.ship` |

use axum::{
    extract::{Query, State},
    http::StatusCode,
    middleware,
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use std::sync::Arc;
use tigshop_core::error::ApiResponse;
use tigshop_core::models::LoginDTO;
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_openapi as openapi;
use tigshop_security::web::{ok, require_auth, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;
use tigshop_services::auth::AuthService;
use tigshop_services::catalog::CatalogService;
use tigshop_services::content::ContentService;
use tigshop_services::coupon::CouponService;
use tigshop_services::logistics::LogisticsService;
use tigshop_services::member::MemberService;
use tigshop_services::order::OrderService;
use tigshop_services::paylog::PaylogService;
use tigshop_services::product::ProductService;
use tigshop_services::refund_apply::RefundApplyService;
use tigshop_services::seckill::SeckillService;
use tigshop_services::time_discount::TimeDiscountService;

pub mod routes;

/// 本 crate 实际注册的路由（用于计算与原 Java 契约的覆盖率）。
///
/// 由 `python tools/check_api_coverage.py` 静态校验；此处显式声明以便
/// 运行时 `/openapi/coverage` 给出同样的数字。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let base = [
        ("GET", "/adminapi/authority/adminUser/list", "AdminUserController.list"),
        ("GET", "/adminapi/authority/adminUser/mineDetail", "AdminUserController.mineDetail"),
        ("GET", "/adminapi/product/product/list", "ProductController.list"),
        ("GET", "/adminapi/product/product/detail", "ProductController.detail"),
        ("GET", "/adminapi/order/order/list", "OrderController.list"),
        ("GET", "/adminapi/order/order/detail", "OrderController.detail"),
        ("POST", "/adminapi/order/order/deliver", "OrderController.deliver"),
        ("POST", "/adminapi/order/order/cancelOrderByAdmin", "OrderController.cancelOrderByAdmin"),
        ("POST", "/adminapi/login/signin", "LoginFilter.attemptAuthentication"),
    ];
    let mut out: Vec<tigshop_openapi::RouteDoc> = base
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
        .collect();
    out.extend(routes::implemented_routes());
    out
}

/// 构建契约/覆盖率文档端点状态。
fn openapi_state() -> tigshop_openapi::OpenApiState {
    tigshop_openapi::OpenApiState::new(
        tigshop_openapi::Contract::embedded(),
        implemented_routes(),
    )
}

/// 管理端应用状态。
#[derive(Clone)]
pub struct AdminState {
    pub auth: Arc<AuthService>,
    pub product: Arc<ProductService>,
    pub order: Arc<OrderService>,
    pub coupon: Arc<CouponService>,
    pub content: Arc<ContentService>,
    pub catalog: Arc<CatalogService>,
    pub logistics: Arc<LogisticsService>,
    pub member: Arc<MemberService>,
    pub seckill: Arc<SeckillService>,
    pub time_discount: Arc<TimeDiscountService>,
    pub paylog: Arc<PaylogService>,
    pub refund_apply: Arc<RefundApplyService>,
    pub jwt: JwtService,
}

/// 构建管理后台路由。
///
/// 路径严格对齐原 Java 控制器的 `@RequestMapping` 组合结果，例如订单域是
/// `/adminapi/order/order/list`（控制器前缀 `/adminapi/order/order`），
/// 而不是想当然的 `/adminapi/order/list`。可用
/// `python tools/check_api_coverage.py` 校验。
pub fn router(state: AdminState) -> Router {
    let protected = Router::new()
        // —— 权限管理（AdminUserController）——
        .route("/authority/adminUser/list", get(admin_user_list))
        .route("/authority/adminUser/mineDetail", get(mine_detail))
        // —— 商品（ProductController @ /adminapi/product/product）——
        .route("/product/product/list", get(product_list))
        .route("/product/product/detail", get(product_detail))
        // —— 订单（OrderController @ /adminapi/order/order）——
        .route("/order/order/list", get(order_list))
        .route("/order/order/detail", get(order_detail))
        .route("/order/order/deliver", post(order_ship))
        .route("/order/order/cancelOrderByAdmin", post(order_cancel))
        // —— 系统（自加端点，不在原契约内）——
        .route("/system/profile/password", post(change_password))
        // —— 各业务域（routes/ 子模块，按域并行开发）——
        .merge(routes::merge_all(&state))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            require_auth,
        ));

    Router::new()
        // —— 登录（无需认证）——
        // 原系统登录由 Spring Security 的 LoginFilter 注册：
        // `setFilterProcessesUrl("/adminapi/login/signin")`
        .route("/login/signin", post(login))
        // 刷新令牌为 Rust 版新增（原系统靠 Redis 会话续期）
        .route("/login/refresh", post(refresh))
        .route("/health", get(health))
        .nest("/adminapi", protected)
        .with_state(state)
        // —— 契约与覆盖率（重写期间对照用）——
        // 该子路由自带状态，须在 `with_state` 之后合并。
        .merge(openapi::routes(openapi_state()))
}

/// 登录请求（兼容原前端字段）。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RefreshDTO {
    refresh_token: String,
}

/// `POST /adminapi/login`：后台登录。
async fn login(
    State(state): State<AdminState>,
    headers: axum::http::HeaderMap,
    Json(dto): Json<LoginDTO>,
) -> Result<Json<ApiResponse<tigshop_core::models::LoginVO>>, AppError> {
    let ip = headers
        .get("X-Forwarded-For")
        .and_then(|v| v.to_str().ok())
        .and_then(|s| s.split(',').next())
        .unwrap_or("unknown")
        .to_string();
    let vo = state.auth.login(&dto, &ip).await?;
    Ok(Json(ApiResponse::success(vo)))
}

/// `POST /adminapi/login/refresh`：刷新令牌。
async fn refresh(
    State(state): State<AdminState>,
    Json(dto): Json<RefreshDTO>,
) -> Result<Json<ApiResponse<tigshop_core::models::LoginVO>>, AppError> {
    let vo = state.auth.refresh(&dto.refresh_token)?;
    Ok(Json(ApiResponse::success(vo)))
}

/// `GET /adminapi/health`：健康检查。
async fn health() -> Json<ApiResponse<&'static str>> {
    Json(ApiResponse::success("UP"))
}

/// `GET /adminapi/authority/adminUser/list`：后台用户分页。
async fn admin_user_list(
    State(state): State<AdminState>,
    Extension(_auth): Extension<AuthUser>,
    Query(q): Query<AdminUserQuery>,
) -> ApiResult<PageResult<serde_json::Value>> {
    let page = q.page_query();
    let (total, users) = state
        .auth
        // 复用仓储分页
        .list_admin_users(q.keyword.as_deref(), q.admin_type.as_deref(), &page)
        .await?;
    let list: Vec<serde_json::Value> = users
        .iter()
        .map(|u| {
            serde_json::json!({
                "adminId": u.admin_id,
                "username": u.username,
                "adminType": u.admin_type,
                "mobile": u.mobile,
                "email": u.email,
                "roleId": u.role_id,
                "isUsing": u.is_using,
                "addTime": u.add_time,
            })
        })
        .collect();
    ok(PageResult::new(total, list, &page))
}

#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct AdminUserQuery {
    keyword: Option<String>,
    admin_type: Option<String>,
    page: Option<u64>,
    page_size: Option<u64>,
}

impl AdminUserQuery {
    fn page_query(&self) -> PageQuery {
        PageQuery {
            page: self.page.unwrap_or(1),
            page_size: self.page_size.unwrap_or(20),
        }
    }
}

/// `GET /adminapi/authority/adminUser/mineDetail`：当前登录信息。
async fn mine_detail(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
) -> ApiResult<serde_json::Value> {
    let _ = &state;
    ok(serde_json::json!({
        "adminId": auth.id,
        "username": auth.username,
        "roleId": auth.role_id,
        "adminType": auth.admin_type,
    }))
}

/// `GET /adminapi/product/list`：商品分页。
async fn product_list(
    State(state): State<AdminState>,
    Query(query): Query<tigshop_core::models::ProductListQuery>,
) -> ApiResult<PageResult<tigshop_core::models::ProductVO>> {
    let page = state.product.list(&query).await?;
    ok(page)
}

/// `GET /adminapi/product/detail?id=`：商品详情。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

async fn product_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<tigshop_core::models::ProductVO> {
    ok(state.product.detail(q.id).await?)
}

/// `GET /adminapi/order/list`：订单分页。
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct OrderQuery {
    order_status: Option<i32>,
    keyword: Option<String>,
    page: Option<u64>,
    page_size: Option<u64>,
}

impl OrderQuery {
    fn page_query(&self) -> PageQuery {
        PageQuery {
            page: self.page.unwrap_or(1),
            page_size: self.page_size.unwrap_or(20),
        }
    }
}

async fn order_list(
    State(state): State<AdminState>,
    Query(q): Query<OrderQuery>,
) -> ApiResult<PageResult<tigshop_core::models::OrderVO>> {
    let page = state
        .order
        .list(None, q.order_status, q.keyword.as_deref(), &q.page_query())
        .await?;
    ok(page)
}

/// `GET /adminapi/order/detail?id=`：订单详情。
async fn order_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<serde_json::Value> {
    let (vo, items) = state.order.detail(q.id, None).await?;
    ok(serde_json::json!({ "order": vo, "items": items }))
}

/// `POST /adminapi/order/ship`：发货。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ShipDTO {
    order_id: i32,
    tracking_no: String,
    logistics_name: String,
}

async fn order_ship(
    State(state): State<AdminState>,
    Json(dto): Json<ShipDTO>,
) -> ApiResult<&'static str> {
    state
        .order
        .ship(dto.order_id, &dto.tracking_no, &dto.logistics_name)
        .await?;
    ok("发货成功")
}

/// `POST /adminapi/order/cancel`：后台取消订单。
#[derive(Debug, Deserialize)]
struct OrderIdDTO {
    order_id: i32,
}

async fn order_cancel(
    State(state): State<AdminState>,
    Json(dto): Json<OrderIdDTO>,
) -> ApiResult<&'static str> {
    state.order.cancel(dto.order_id, None).await?;
    ok("订单已取消")
}

/// `POST /adminapi/system/profile/password`：修改密码。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChangePasswordDTO {
    old_password: String,
    new_password: String,
}

async fn change_password(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<ChangePasswordDTO>,
) -> ApiResult<&'static str> {
    state
        .auth
        .change_password(auth.id, &dto.old_password, &dto.new_password)
        .await?;
    ok("密码修改成功")
}

/// 路由 404 兜底。
pub async fn not_found() -> (StatusCode, Json<ApiResponse<()>>) {
    (
        StatusCode::NOT_FOUND,
        Json(ApiResponse::fail(404, "接口不存在")),
    )
}

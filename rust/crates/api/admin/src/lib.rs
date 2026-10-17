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
use tigshop_services::salesman::SalesmanService;
use tigshop_services::seckill::SeckillService;
use tigshop_services::shop::ShopService;
use tigshop_services::slice044::Slice044Service;
use tigshop_services::slice039::Slice039Service;
use tigshop_services::slice210::Slice210Service;
use tigshop_services::slice163::Slice163Service;
use tigshop_services::slice157::Slice157Service;
use tigshop_services::slice137::Slice137Service;
use tigshop_services::slice108::Slice108Service;
use tigshop_services::slice085::Slice085Service;
use tigshop_services::slice038::Slice038Service;
use tigshop_services::slice037::Slice037Service;
use tigshop_services::slice220::Slice220Service;
use tigshop_services::slice209::Slice209Service;
use tigshop_services::slice201::Slice201Service;
use tigshop_services::slice185::Slice185Service;
use tigshop_services::slice092::Slice092Service;
use tigshop_services::slice081::Slice081Service;
use tigshop_services::slice166::Slice166Service;
use tigshop_services::slice144::Slice144Service;
use tigshop_services::slice139::Slice139Service;
use tigshop_services::slice075::Slice075Service;
use tigshop_services::slice217::Slice217Service;
use tigshop_services::slice205::Slice205Service;
use tigshop_services::slice192::Slice192Service;
use tigshop_services::slice186::Slice186Service;
use tigshop_services::slice120::Slice120Service;
use tigshop_services::slice093::Slice093Service;
use tigshop_services::slice056::Slice056Service;
use tigshop_services::slice216::Slice216Service;
use tigshop_services::slice208::Slice208Service;
use tigshop_services::slice203::Slice203Service;
use tigshop_services::slice199::Slice199Service;
use tigshop_services::slice197::Slice197Service;
use tigshop_services::slice193::Slice193Service;
use tigshop_services::slice191::Slice191Service;
use tigshop_services::slice190::Slice190Service;
use tigshop_services::slice189::Slice189Service;
use tigshop_services::slice181::Slice181Service;
use tigshop_services::slice176::Slice176Service;
use tigshop_services::slice174::Slice174Service;
use tigshop_services::slice171::Slice171Service;
use tigshop_services::slice136::Slice136Service;
use tigshop_services::slice135::Slice135Service;
use tigshop_services::slice133::Slice133Service;
use tigshop_services::slice124::Slice124Service;
use tigshop_services::slice116::Slice116Service;
use tigshop_services::slice114::Slice114Service;
use tigshop_services::slice113::Slice113Service;
use tigshop_services::slice102::Slice102Service;
use tigshop_services::slice098::Slice098Service;
use tigshop_services::slice064::Slice064Service;
use tigshop_services::slice063::Slice063Service;
use tigshop_services::slice051::Slice051Service;
use tigshop_services::slice047::Slice047Service;
use tigshop_services::slice036::Slice036Service;
use tigshop_services::slice034::Slice034Service;
use tigshop_services::slice164::Slice164Service;
use tigshop_services::slice147::Slice147Service;
use tigshop_services::slice132::Slice132Service;
use tigshop_services::slice118::Slice118Service;
use tigshop_services::slice111::Slice111Service;
use tigshop_services::slice094::Slice094Service;
use tigshop_services::slice053::Slice053Service;
use tigshop_services::slice100::Slice100Service;
use tigshop_services::slice042::Slice042Service;
use tigshop_services::slice032::Slice032Service;
use tigshop_services::slice031::Slice031Service;
use tigshop_services::slice030::Slice030Service;
use tigshop_services::slice029::Slice029Service;
use tigshop_services::slice028::Slice028Service;
use tigshop_services::slice027::Slice027Service;
use tigshop_services::slice188::Slice188Service;
use tigshop_services::slice187::Slice187Service;
use tigshop_services::slice026::Slice026Service;
use tigshop_services::slice140::Slice140Service;
use tigshop_services::slice134::Slice134Service;
use tigshop_services::slice097::Slice097Service;
use tigshop_services::slice095::Slice095Service;
use tigshop_services::slice025::Slice025Service;
use tigshop_services::slice024::Slice024Service;
use tigshop_services::slice023::Slice023Service;
use tigshop_services::slice021::Slice021Service;
use tigshop_services::slice020::Slice020Service;
use tigshop_services::slice019::Slice019Service;
use tigshop_services::slice018::Slice018Service;
use tigshop_services::slice017::Slice017Service;
use tigshop_services::slice001::Slice001Service;
use tigshop_services::slice014::Slice014Service;
use tigshop_services::slice012::Slice012Service;
use tigshop_services::slice011::Slice011Service;
use tigshop_services::slice010::Slice010Service;
use tigshop_services::slice008::Slice008Service;
use tigshop_services::slice006::Slice006Service;
use tigshop_services::slice004::Slice004Service;
use tigshop_services::slice003::Slice003Service;
use tigshop_services::slice002::Slice002Service;
use tigshop_services::slice005::Slice005Service;
use tigshop_services::decorate::DecorateService;
use tigshop_services::time_discount::TimeDiscountService;

pub mod routes;
pub mod root;

/// 本 crate 实际注册的路由（用于计算与原 Java 契约的覆盖率）。
///
/// 由 `python tools/check_api_coverage.py` 静态校验；此处显式声明以便
/// 运行时 `/openapi/coverage` 给出同样的数字。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let base = [
        ("POST", "/adminapi/login/signin", "LoginFilter.attemptAuthentication"),
    ];
    let mut out: Vec<tigshop_openapi::RouteDoc> = base
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
        .collect();
    out.extend(routes::implemented_routes());
    // 根级通道（`/im/**`、`/img/**` 等不带 `/adminapi` 前缀的控制器）同样要计入
    // 运行时覆盖率口径，否则 `/openapi/coverage` 会比静态扫描少报。
    out.extend(root::implemented_routes());
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
    pub slice001: Arc<Slice001Service>,
    pub slice002: Arc<Slice002Service>,
    pub slice003: Arc<Slice003Service>,
    pub slice004: Arc<Slice004Service>,
    pub slice005: Arc<Slice005Service>,
    pub slice006: Arc<Slice006Service>,
    pub slice008: Arc<Slice008Service>,
    pub slice010: Arc<Slice010Service>,
    pub slice011: Arc<Slice011Service>,
    pub slice012: Arc<Slice012Service>,
    pub slice014: Arc<Slice014Service>,
    pub shop: Arc<ShopService>,
    pub decorate: Arc<DecorateService>,
    pub salesman: Arc<SalesmanService>,
    pub slice095: Arc<Slice095Service>,
    pub slice097: Arc<Slice097Service>,
    pub slice134: Arc<Slice134Service>,
    pub slice140: Arc<Slice140Service>,
    pub jwt: JwtService,
    pub slice017: Arc<Slice017Service>,
    pub slice018: Arc<Slice018Service>,
    pub slice019: Arc<Slice019Service>,
    pub slice020: Arc<Slice020Service>,
    pub slice021: Arc<Slice021Service>,
    pub slice023: Arc<Slice023Service>,
    pub slice024: Arc<Slice024Service>,
    pub slice025: Arc<Slice025Service>,
    pub slice026: Arc<Slice026Service>,
    pub slice187: Arc<Slice187Service>,
    pub slice188: Arc<Slice188Service>,
    pub slice027: Arc<Slice027Service>,
    pub slice028: Arc<Slice028Service>,
    pub slice029: Arc<Slice029Service>,
    pub slice030: Arc<Slice030Service>,
    pub slice031: Arc<Slice031Service>,
    pub slice032: Arc<Slice032Service>,
    pub slice042: Arc<Slice042Service>,
    pub slice100: Arc<Slice100Service>,
    pub slice053: Arc<Slice053Service>,
    pub slice094: Arc<Slice094Service>,
    pub slice111: Arc<Slice111Service>,
    pub slice118: Arc<Slice118Service>,
    pub slice132: Arc<Slice132Service>,
    pub slice147: Arc<Slice147Service>,
    pub slice164: Arc<Slice164Service>,
    pub slice034: Arc<Slice034Service>,
    pub slice036: Arc<Slice036Service>,
    pub slice047: Arc<Slice047Service>,
    pub slice051: Arc<Slice051Service>,
    pub slice063: Arc<Slice063Service>,
    pub slice064: Arc<Slice064Service>,
    pub slice098: Arc<Slice098Service>,
    pub slice102: Arc<Slice102Service>,
    pub slice113: Arc<Slice113Service>,
    pub slice114: Arc<Slice114Service>,
    pub slice116: Arc<Slice116Service>,
    pub slice124: Arc<Slice124Service>,
    pub slice133: Arc<Slice133Service>,
    pub slice135: Arc<Slice135Service>,
    pub slice136: Arc<Slice136Service>,
    pub slice171: Arc<Slice171Service>,
    pub slice174: Arc<Slice174Service>,
    pub slice176: Arc<Slice176Service>,
    pub slice181: Arc<Slice181Service>,
    pub slice189: Arc<Slice189Service>,
    pub slice190: Arc<Slice190Service>,
    pub slice191: Arc<Slice191Service>,
    pub slice193: Arc<Slice193Service>,
    pub slice197: Arc<Slice197Service>,
    pub slice199: Arc<Slice199Service>,
    pub slice203: Arc<Slice203Service>,
    pub slice208: Arc<Slice208Service>,
    pub slice216: Arc<Slice216Service>,
    pub slice056: Arc<Slice056Service>,
    pub slice093: Arc<Slice093Service>,
    pub slice120: Arc<Slice120Service>,
    pub slice186: Arc<Slice186Service>,
    pub slice192: Arc<Slice192Service>,
    pub slice205: Arc<Slice205Service>,
    pub slice217: Arc<Slice217Service>,
    pub slice075: Arc<Slice075Service>,
    pub slice139: Arc<Slice139Service>,
    pub slice144: Arc<Slice144Service>,
    pub slice166: Arc<Slice166Service>,
    pub slice081: Arc<Slice081Service>,
    pub slice092: Arc<Slice092Service>,
    pub slice185: Arc<Slice185Service>,
    pub slice201: Arc<Slice201Service>,
    pub slice209: Arc<Slice209Service>,
    pub slice220: Arc<Slice220Service>,
    pub slice037: Arc<Slice037Service>,
    pub slice038: Arc<Slice038Service>,
    pub slice085: Arc<Slice085Service>,
    pub slice108: Arc<Slice108Service>,
    pub slice137: Arc<Slice137Service>,
    pub slice157: Arc<Slice157Service>,
    pub slice163: Arc<Slice163Service>,
    pub slice210: Arc<Slice210Service>,
    pub slice039: Arc<Slice039Service>,
    pub slice044: Arc<Slice044Service>,
}

/// 构建管理后台路由。
///
/// 路径严格对齐原 Java 控制器的 `@RequestMapping` 组合结果，例如订单域是
/// `/adminapi/order/order/list`（控制器前缀 `/adminapi/order/order`），
/// 而不是想当然的 `/adminapi/order/list`。可用
/// `python tools/check_api_coverage.py` 校验。
pub fn router(state: AdminState) -> Router {
    // —— 根级路由（无 `/adminapi` 前缀：/im/** 需登录，/img|/video|/app 免登录）——
    let root_protected = root::merge_protected(&state)
        .layer(middleware::from_fn_with_state(state.jwt.clone(), require_auth));
    let root_public = root::merge_public(&state);

    let protected = Router::new()
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
        .merge(root_public)
        .merge(root_protected)
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

/// `GET /adminapi/product/list`：商品分页。
async fn product_list(
    State(state): State<AdminState>,
    Query(query): Query<tigshop_core::models::ProductListQuery>,
) -> ApiResult<PageResult<tigshop_core::models::ProductVO>> {
    let page = state.product.list(&query).await?;
    ok(page)
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

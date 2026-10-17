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
use tigshop_services::identity::auth::AuthService;
use tigshop_services::catalog::category_all::CatalogService;
use tigshop_services::content::article::ContentService;
use tigshop_services::marketing::coupon::CouponService;
use tigshop_services::logistics::shipping_list::LogisticsService;
use tigshop_services::member::member_lists::MemberService;
use tigshop_services::trade::order::OrderService;
use tigshop_services::payment::paylog::PaylogService;
use tigshop_services::catalog::product::ProductService;
use tigshop_services::payment::refund_apply::RefundApplyService;
use tigshop_services::marketing::salesman::SalesmanService;
use tigshop_services::marketing::seckill::SeckillService;
use tigshop_services::merchant::shop::ShopService;
use tigshop_services::analytics::statistics::StatisticsService;
use tigshop_services::member::user_rank::UserRankService;
use tigshop_services::member::feedback::FeedbackService;
use tigshop_services::merchant::vendor_deposit_shop::VendorDepositShopService;
use tigshop_services::member::member_tag_group::MemberTagGroupService;
use tigshop_services::logistics::shipping_type::ShippingTypeService;
use tigshop_services::payment::tax_rate::TaxRateService;
use tigshop_services::system::example::ExampleService;
use tigshop_services::openapi::workbuddy::WorkbuddyService;
use tigshop_services::merchant::shop_product_category::ShopProductCategoryService;
use tigshop_services::marketing::product_style_admin::ProductStyleAdminService;
use tigshop_services::member::member_tag::MemberTagService;
use tigshop_services::member::user_company::UserCompanyService;
use tigshop_services::content::mobile_cat_nav::MobileCatNavService;
use tigshop_services::logistics::logistics_company::LogisticsCompanyService;
use tigshop_services::logistics::shipping_tpl::ShippingTplService;
use tigshop_services::merchant::order_config::OrderConfigService;
use tigshop_services::system::region::RegionService;
use tigshop_services::catalog::attributes_tpl::AttributesTplService;
use tigshop_services::marketing::salesman_material_category::SalesmanMaterialCategoryService;
use tigshop_services::logistics::logistics_company_config::LogisticsCompanyConfigService;
use tigshop_services::system::message_type::MessageTypeService;
use tigshop_services::system::area_code::AreaCodeService;
use tigshop_services::system::mail_templates::MailTemplatesService;
use tigshop_services::logistics::print_config::PrintConfigService;
use tigshop_services::system::cache_manage::CacheManageService;
use tigshop_services::identity::admin_log::AdminLogService;
use tigshop_services::payment::user_balance_log::UserBalanceLogService;
use tigshop_services::member::user_points_log::UserPointsLogService;
use tigshop_services::trade::proxy_shopping_order_check::ProxyShoppingOrderCheckService;
use tigshop_services::trade::proxy_shopping_pickup_contact::ProxyShoppingPickupContactService;
use tigshop_services::trade::proxy_shopping_user_address::ProxyShoppingUserAddressService;
use tigshop_services::trade::proxy_shopping_product::ProxyShoppingProductService;
use tigshop_services::trade::proxy_shopping_user_coupon::ProxyShoppingUserCouponService;
use tigshop_services::trade::proxy_shopping_user::ProxyShoppingUserService;
use tigshop_services::payment::paylog_batch::PaylogBatchService;
use tigshop_services::im::im_message::ImMessageService;
use tigshop_services::system::files::FilesService;
use tigshop_services::im::im_servant::ImServantService;
use tigshop_services::marketing::buyer_show_comment::BuyerShowCommentService;
use tigshop_services::marketing::buyer_show::BuyerShowService;
use tigshop_services::marketing::buyer_show_group::BuyerShowGroupService;
use tigshop_services::content::seo::SeoService;
use tigshop_services::im::im_config::ImConfigService;
use tigshop_services::system::app_version_admin::AppVersionAdminService;
use tigshop_services::content::pc_navigation::PcNavigationService;
use tigshop_services::content::pc_cat_floor::PcCatFloorService;
use tigshop_services::catalog::product_manage::ProductManageService;
use tigshop_services::merchant::shop_manage::ShopManageService;
use tigshop_services::member::user_manage::UserManageService;
use tigshop_services::identity::admin_user::AdminUserService;
use tigshop_services::identity::authority::AuthorityService;
use tigshop_services::trade::aftersales::AftersalesService;
use tigshop_services::catalog::stock_warning::StockWarningService;
use tigshop_services::catalog::category::CategoryService;
use tigshop_services::catalog::product_stock::ProductStockService;
use tigshop_services::trade::order_admin::OrderAdminService;
use tigshop_services::catalog::brand::BrandService;
use tigshop_services::content::decorate::DecorateService;
use tigshop_services::marketing::time_discount::TimeDiscountService;

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
    pub identity: std::sync::Arc<tigshop_services::identity::Services>,
    pub merchant: std::sync::Arc<tigshop_services::merchant::Services>,
    pub catalog: std::sync::Arc<tigshop_services::catalog::Services>,
    pub trade: std::sync::Arc<tigshop_services::trade::Services>,
    pub payment: std::sync::Arc<tigshop_services::payment::Services>,
    pub marketing: std::sync::Arc<tigshop_services::marketing::Services>,
    pub member: std::sync::Arc<tigshop_services::member::Services>,
    pub logistics: std::sync::Arc<tigshop_services::logistics::Services>,
    pub content: std::sync::Arc<tigshop_services::content::Services>,
    pub system: std::sync::Arc<tigshop_services::system::Services>,
    pub im: std::sync::Arc<tigshop_services::im::Services>,
    pub analytics: std::sync::Arc<tigshop_services::analytics::Services>,
    pub openapi: std::sync::Arc<tigshop_services::openapi::Services>,
    pub jwt: JwtService,
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
    let vo = state.identity.auth.login(&dto, &ip).await?;
    Ok(Json(ApiResponse::success(vo)))
}

/// `POST /adminapi/login/refresh`：刷新令牌。
async fn refresh(
    State(state): State<AdminState>,
    Json(dto): Json<RefreshDTO>,
) -> Result<Json<ApiResponse<tigshop_core::models::LoginVO>>, AppError> {
    let vo = state.identity.auth.refresh(&dto.refresh_token)?;
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
    let page = state.catalog.product.list(&query).await?;
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
    state.identity.auth
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
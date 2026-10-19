//! 商城前台 API（对应原 `tigshop-api` 模块）。
//!
//! 路由前缀 `/api`，路径严格对齐原 Java 控制器的 `@RequestMapping`
//! 组合结果（注意「控制器名重复一次」的形式）：
//! | 路由 | 原 Java 控制器 |
//! |------|---------------|
//! | `GET  /api/product/product/list` | `ProductController.list` |
//! | `GET  /api/product/product/detail` | `ProductController.detail` |
//! | `POST /api/user/login/signin` | Spring Security `ClientLoginFilter` |
//! | `POST /api/user/regist/registAct` | `RegisterController.register` |
//! | `GET  /api/cart/cart/list` | `CartController.list` |
//! | `POST /api/cart/cart/addToCart` | `CartController.addProductToCart` |
//! | `POST /api/order/check/submit` | `CheckController.submit` |
//! | `GET  /api/user/order/list` | `UserOrderController.list` |

use tower_http::services::ServeDir;

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
use tigshop_core::models::{LoginDTO, LoginVO, OrderCreateDTO, OrderCreateVO, ProductListQuery};
use tigshop_core::types::PageResult;
use tigshop_openapi as openapi;
use tigshop_security::web::{ok, require_auth, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;
use tigshop_services::trade::cart::CartService;
use tigshop_services::catalog::category_all::CatalogService;
use tigshop_services::content::article::ContentService;
use tigshop_services::marketing::coupon::CouponService;
use tigshop_services::content::decorate::DecorateService;
use tigshop_services::logistics::shipping_list::LogisticsService;
use tigshop_services::member::member_lists::MemberService;
use tigshop_services::trade::order::OrderService;
use tigshop_services::catalog::product::ProductService;
use tigshop_services::marketing::salesman::SalesmanService;
use tigshop_services::marketing::seckill::SeckillService;
use tigshop_services::catalog::short_url::ShortUrlService;
use tigshop_services::im::im_message::ImMessageService;
use tigshop_services::merchant::shop::ShopService;
use tigshop_services::merchant::merchant_client::MerchantClientService;
use tigshop_services::system::config_client::ConfigClientService;
use tigshop_services::content::share::ShareService;
use tigshop_services::system::region_client::RegionClientService;
use tigshop_services::im::web_socket::WebSocketService;
use tigshop_services::content::wechat_live_sync_callback::WechatLiveSyncCallbackService;
use tigshop_services::merchant::shop_client_category::ShopClientCategoryService;
use tigshop_services::logistics::sunmi_print_callback::SunmiPrintCallbackService;
use tigshop_services::marketing::buyer_show_client::BuyerShowClientService;
use tigshop_services::content::seo::SeoService;
use tigshop_services::system::pc::PcService;
use tigshop_services::wholesale::wholesale_client::WholesaleClientService;
use tigshop_services::catalog::product_manage::ProductManageService;
use tigshop_services::merchant::shop_client::ShopClientService;
use tigshop_services::trade::aftersales_client::AftersalesClientService;
use tigshop_services::member::user_center::UserCenterService;
use tigshop_services::catalog::category::CategoryService;
use tigshop_services::marketing::time_discount::TimeDiscountService;
use tigshop_services::payment::paylog::PaylogService;
use tigshop_services::member::user::UserService;

pub mod routes;
pub mod root;

/// 商城前台应用状态。
#[derive(Clone)]
pub struct PublicState {
    pub merchant: std::sync::Arc<tigshop_services::merchant::Services>,
    pub wholesale: std::sync::Arc<tigshop_services::wholesale::Services>,
    pub catalog: std::sync::Arc<tigshop_services::catalog::Services>,
    pub trade: std::sync::Arc<tigshop_services::trade::Services>,
    pub payment: std::sync::Arc<tigshop_services::payment::Services>,
    pub marketing: std::sync::Arc<tigshop_services::marketing::Services>,
    pub member: std::sync::Arc<tigshop_services::member::Services>,
    pub logistics: std::sync::Arc<tigshop_services::logistics::Services>,
    pub content: std::sync::Arc<tigshop_services::content::Services>,
    pub system: std::sync::Arc<tigshop_services::system::Services>,
    pub im: std::sync::Arc<tigshop_services::im::Services>,
    pub jwt: JwtService,
}

/// 构建商城前台路由。
///
/// 路径严格对齐原 Java 控制器的 `@RequestMapping` 组合结果。注意许多域是
/// 「控制器名重复一次」的形式，例如购物车是 `/api/cart/cart/list`、
/// 会员订单是 `/api/user/order/list`，而不是想当然的 `/api/cart/list`。
/// 可用 `python tools/check_api_coverage.py` 校验。
pub fn router(state: PublicState) -> Router {
    // —— 根级路由（无 `/api` 前缀：/im/** 需登录，/s/{code} 免登录）——
    let root_protected = root::merge_protected(&state)
        .layer(middleware::from_fn_with_state(state.jwt.clone(), require_auth));
    let root_public = root::merge_public(&state);

    // 需要登录的接口
    let authorized = Router::new()
        // —— 购物车（CartController @ /api/cart/cart）——
        .route("/cart/cart/list", get(cart_list))
        .route("/cart/cart/addToCart", post(cart_add))
        .route("/cart/cart/updateItem", post(cart_update))
        .route("/cart/cart/updateCheck", post(cart_check))
        .route("/cart/cart/removeItem", post(cart_remove))
        .route("/cart/cart/clear", post(cart_clear))
        // —— 订单（CheckController @ /api/order/check）——
        .route("/order/check/submit", post(order_create))
        // —— 会员订单（UserOrderController @ /api/user/order）——
        .route("/user/order/list", get(order_list))
        .route("/user/order/detail", get(order_detail))
        .route("/user/order/cancelOrder", post(order_cancel))
        // —— 会员资料（UserController @ /api/user/user）——
        .route("/user/user/detail", get(profile))
        // —— 各业务域需登录接口（routes/ 子模块，按域并行开发）——
        .merge(routes::merge_authorized(&state))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            require_auth,
        ));

    // 公开域路由（无需登录），与需登录路由组同挂载于 `/api` 下
    let public_api = routes::merge_public(&state);

    // 契约内 `/api` 前缀的公开路由（含登录/注册/商城首页商品列表）
    let public_root = Router::new()
        .route("/product/product/list", get(product_list))
        // 原系统登录由 Spring Security 的 ClientLoginFilter 注册：
        // `setFilterProcessesUrl("/api/user/login/signin")`
        .route("/user/login/signin", post(user_login))
        .route("/user/regist/registAct", post(user_register))
        .route("/health", get(health));

    Router::new()
        // 裸根健康检查（负载均衡探活用，不属于契约）
        .route("/health", get(health))
        // WebSocket：`/ws?token=...&platform=...`（对齐 Java API 模块 `/ws`）
        .route("/ws", get(ws_handler))
        .nest("/api", authorized.merge(public_api).merge(public_root))
        .merge(root_public)
        .merge(root_protected)
        // 本地对象存储（上传文件）；nginx 需把 /static 反代到本服务
        .nest_service(
            "/static",
            ServeDir::new(
                std::env::var("TIG_FILE__UPLOAD_DIR").unwrap_or_else(|_| "./uploads".to_string()),
            ),
        )
        .with_state(state)
        // —— 契约与覆盖率（重写期间对照用）——
        .merge(openapi::routes(openapi_state()))
}

/// 本 crate 实际注册的路由（用于计算与原 Java 契约的覆盖率）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let base = [
        ("GET", "/api/cart/cart/list", "CartController.list"),
        ("POST", "/api/cart/cart/addToCart", "CartController.addProductToCart"),
        ("POST", "/api/cart/cart/updateItem", "CartController.updateItem"),
        ("POST", "/api/cart/cart/updateCheck", "CartController.updateCheck"),
        ("POST", "/api/cart/cart/removeItem", "CartController.removeItem"),
        ("POST", "/api/cart/cart/clear", "CartController.clear"),
        ("POST", "/api/order/check/submit", "CheckController.submit"),
        ("GET", "/api/user/order/list", "UserOrderController.list"),
        ("GET", "/api/user/order/detail", "UserOrderController.detail"),
        ("POST", "/api/user/order/cancelOrder", "UserOrderController.cancelOrder"),
        ("GET", "/api/user/user/detail", "UserController.detail"),
        ("GET", "/api/product/product/list", "ProductController.list"),
        ("POST", "/api/user/login/signin", "ClientLoginFilter.attemptAuthentication"),
        ("POST", "/api/user/regist/registAct", "RegisterController.register"),
    ];
    let mut out: Vec<tigshop_openapi::RouteDoc> = base
        .iter()
        .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
        .collect();
    out.extend(routes::implemented_routes());
    // 根级通道（`/im/**`、`/s/{code}` 等不带 `/api` 前缀的控制器）同样计入
    // 运行时覆盖率口径。
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

/// `GET /api/health`。
async fn health() -> Json<ApiResponse<&'static str>> {
    Json(ApiResponse::success("UP"))
}

// ————————————————————— 商品 —————————————————————

/// `GET /api/product/list`：商品分页。
async fn product_list(
    State(state): State<PublicState>,
    Query(mut query): Query<ProductListQuery>,
) -> ApiResult<PageResult<tigshop_core::models::ProductVO>> {
    // 前台只看上架商品
    if query.product_status.is_none() {
        query.product_status = Some(1);
    }
    let page = state.catalog.product.list(&query).await?;
    ok(page)
}

/// `GET /api/product/detail?id=`：商品详情。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

// ————————————————————— 用户 —————————————————————

/// 注册请求。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RegisterDTO {
    mobile: String,
    password: String,
    nickname: Option<String>,
}

/// `POST /api/user/register`。
async fn user_register(
    State(state): State<PublicState>,
    Json(dto): Json<RegisterDTO>,
) -> ApiResult<i32> {
    let id = state.member.user
        .register(&dto.mobile, &dto.password, dto.nickname.as_deref())
        .await?;
    ok(id)
}

/// `POST /api/user/login`：会员登录（返回访问+刷新令牌）。
async fn user_login(
    State(state): State<PublicState>,
    Json(dto): Json<LoginDTO>,
) -> Result<Json<ApiResponse<LoginVO>>, AppError> {
    // C 端登录与 Java `ClientLoginFilter` 一致：失败次数达到阈值后要求滑块验证码。
    tigshop_services::system::captcha::validate_login_captcha(
        &dto.username,
        dto.captcha_verification.as_deref(),
    )?;
    let user = match state.member.user.login(&dto.username, &dto.password).await {
        Ok(user) => {
            tigshop_services::system::captcha::reset_login_error(&dto.username);
            user
        }
        Err(e) => {
            tigshop_services::system::captcha::increment_login_error(&dto.username);
            return Err(AppError(e));
        }
    };
    let user_id = user.user_id;
    let username = user.display_name();
    let (access, refresh, expires_in) =
        state
            .jwt
            .issue_pair(user_id.to_string(), username.clone(), None, Some("member".into()))?;
    Ok(Json(ApiResponse::success(LoginVO {
        token: access.clone(),
        admin_type: None,
        access_token: access,
        refresh_token: refresh,
        token_type: "Bearer".into(),
        expires_in,
        admin_id: user_id,
        username,
    })))
}

/// `GET /api/user/profile`：个人资料。
async fn profile(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
) -> ApiResult<Option<serde_json::Value>> {
    let user = state.member.user.find(auth.id).await?.map(|u| {
        serde_json::json!({
            "userId": u.user_id,
            "username": u.display_name(),
            "mobile": u.mobile,
            "avatar": u.avatar,
            "balance": u.available_balance(),
            "points": u.points,
            "rankId": u.rank_id,
        })
    });
    ok(user)
}

// ————————————————————— 购物车 —————————————————————

/// `GET /api/cart/list`：购物车汇总。
async fn cart_list(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
) -> ApiResult<tigshop_services::trade::cart::CartSummaryVO> {
    ok(state.trade.cart.summary(auth.id).await?)
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct CartAddDTO {
    product_id: i32,
    sku_id: Option<i32>,
    #[serde(default = "default_one")]
    quantity: i32,
    /// 拼团上下文（加购自拼团页时携带）
    #[serde(default)]
    groupon_data: Option<serde_json::Value>,
}

fn default_one() -> i32 {
    1
}

/// `POST /api/cart/add`：加入购物车。
async fn cart_add(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<CartAddDTO>,
) -> ApiResult<i32> {
    let groupon = dto
        .groupon_data
        .as_ref()
        .and_then(|v| serde_json::from_value::<
            tigshop_core::models::trade::check::GrouponCartData,
        >(v.clone()).ok())
        .filter(|g| g.is_valid());
    let cart_id = state
        .trade
        .cart
        .add(auth.id, dto.product_id, dto.sku_id, dto.quantity, groupon.as_ref())
        .await?;
    ok(cart_id)
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct CartUpdateDTO {
    cart_id: i32,
    quantity: i32,
}

/// `PUT /api/cart/update`：修改数量。
async fn cart_update(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<CartUpdateDTO>,
) -> ApiResult<&'static str> {
    state.trade.cart
        .update_quantity(auth.id, dto.cart_id, dto.quantity)
        .await?;
    ok("已更新")
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct CartCheckDTO {
    cart_id: i32,
    checked: bool,
}

/// `POST /api/cart/check`：勾选/取消。
async fn cart_check(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<CartCheckDTO>,
) -> ApiResult<&'static str> {
    state.trade.cart
        .set_checked(auth.id, dto.cart_id, dto.checked)
        .await?;
    ok("已更新")
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct CartRemoveDTO {
    cart_ids: Vec<i32>,
}

/// `POST /api/cart/remove`：删除指定行。
async fn cart_remove(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<CartRemoveDTO>,
) -> ApiResult<&'static str> {
    state.trade.cart.remove(auth.id, &dto.cart_ids).await?;
    ok("已删除")
}

/// `POST /api/cart/clear`：清空购物车。
async fn cart_clear(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
) -> ApiResult<&'static str> {
    state.trade.cart.clear(auth.id).await?;
    ok("已清空")
}

// ————————————————————— 订单 —————————————————————

/// `POST /api/order/create`：提交订单。
async fn order_create(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<OrderCreateDTO>,
) -> ApiResult<OrderCreateVO> {
    let vo = state.trade.order.create(auth.id, &dto).await?;
    ok(vo)
}

/// `GET /api/order/list`：我的订单。
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct MyOrderQuery {
    order_status: Option<i32>,
    page: Option<u64>,
    page_size: Option<u64>,
}

async fn order_list(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<MyOrderQuery>,
) -> ApiResult<PageResult<tigshop_core::models::OrderVO>> {
    let page = tigshop_core::types::PageQuery {
        page: q.page.unwrap_or(1),
        page_size: q.page_size.unwrap_or(20),
    };
    let vo = state.trade.order
        .list(Some(auth.id), q.order_status, None, &page)
        .await?;
    ok(vo)
}

/// `GET /api/order/detail?id=`：订单详情。
async fn order_detail(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<IdQuery>,
) -> ApiResult<serde_json::Value> {
    let (vo, items) = state.trade.order.detail(q.id, Some(auth.id)).await?;
    ok(serde_json::json!({ "order": vo, "items": items }))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderIdDTO {
    order_id: i32,
}

/// `POST /api/order/cancel`：取消订单（回滚库存）。
async fn order_cancel(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<OrderIdDTO>,
) -> ApiResult<&'static str> {
    state.trade.order.cancel(dto.order_id, Some(auth.id)).await?;
    ok("订单已取消")
}

/// `/ws`：WebSocket 握手（token 走 query，对齐 Java `WebSocketServerHandler`）。
async fn ws_handler(
    ws: axum::extract::ws::WebSocketUpgrade,
    State(state): State<PublicState>,
    Query(q): Query<tigshop_security::ws::WsQuery>,
) -> axum::response::Response {
    tigshop_security::ws::handle_upgrade(ws, state.jwt.clone(), q).await
}

/// 路由 404 兜底。
pub async fn not_found() -> (StatusCode, Json<ApiResponse<()>>) {
    (
        StatusCode::NOT_FOUND,
        Json(ApiResponse::fail(404, "接口不存在")),
    )
}
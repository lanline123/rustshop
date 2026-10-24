//! o2o 前台 —— 移动端本地生活路由（o2o_client）。
//!
//! b2b2b 5.8.57 移动端新增（**无 Java 控制器对应**），按表 + 前端消费实现：
//!
//! | 本模块路由 | 说明 |
//! |-----------|------|
//! | `GET /o2o/userPickup/getUserPickups` | 当前会员自提人列表 |
//! | `GET /o2o/userPickup/detail?id=` | 自提人详情 |
//! | `POST /o2o/userPickup/create` | 新增自提人 |
//! | `POST /o2o/userPickup/update` | 更新自提人 |
//! | `POST /o2o/userPickup/del` | 删除自提人 |
//! | `GET /o2o/orderPickup/getShopPickupTpl?shopId=` | 门店自提模板 |
//! | `GET /o2o/orderPickup/getOrderPickupShipment?orderId=` | 订单自提单 |
//! | `GET /o2o/orderPickup/getDeliveryOptions?orderId=` | 配送方式（自提单存在=自提） |
//! | `GET /o2o/OrderReservation/getStockCalendar?productId=&shopId=&days=` | 预约库存日历 |
//! | `GET /o2o/storeSearch/getStoreProduct` | 门店商品搜索 |
//!
//! 自提人接口需登录；模板/配送/日历/门店商品匿名。

use axum::{
    extract::{Query, Request, State},
    http::header,
    middleware::{self, Next},
    response::Response,
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use serde_json::Value;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AuthUser};
use tigshop_services::o2o::pickup_client::StockDayVO;

use crate::PublicState;

/// 本域路由（挂载于 `/api` 下）。
const ROUTE_TABLE: [(&str, &str, &str); 10] = [
    ("GET", "/o2o/userPickup/getUserPickups", "UserPickupController.getUserPickups"),
    ("GET", "/o2o/userPickup/detail", "UserPickupController.detail"),
    ("POST", "/o2o/userPickup/create", "UserPickupController.create"),
    ("POST", "/o2o/userPickup/update", "UserPickupController.update"),
    ("POST", "/o2o/userPickup/del", "UserPickupController.del"),
    ("GET", "/o2o/orderPickup/getShopPickupTpl", "OrderPickupController.getShopPickupTpl"),
    ("GET", "/o2o/orderPickup/getOrderPickupShipment", "OrderPickupController.getOrderPickupShipment"),
    ("GET", "/o2o/orderPickup/getDeliveryOptions", "OrderPickupController.getDeliveryOptions"),
    ("GET", "/o2o/OrderReservation/getStockCalendar", "OrderReservationController.getStockCalendar"),
    ("GET", "/o2o/storeSearch/getStoreProduct", "StoreSearchController.getStoreProduct"),
];

/// o2o 前台路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    let pickup = Router::new()
        .route("/o2o/userPickup/getUserPickups", get(get_user_pickups))
        .route("/o2o/userPickup/detail", get(pickup_detail))
        .route("/o2o/userPickup/create", post(pickup_create))
        .route("/o2o/userPickup/update", post(pickup_update))
        .route("/o2o/userPickup/del", post(pickup_del))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            optional_auth,
        ));
    Router::new()
        .merge(pickup)
        .route(
            "/o2o/orderPickup/getShopPickupTpl",
            get(shop_pickup_tpl),
        )
        .route(
            "/o2o/orderPickup/getOrderPickupShipment",
            get(order_pickup_shipment),
        )
        .route("/o2o/orderPickup/getDeliveryOptions", get(delivery_options))
        .route(
            "/o2o/OrderReservation/getStockCalendar",
            get(stock_calendar),
        )
        .route("/o2o/storeSearch/getStoreProduct", get(store_product))
}

/// 已实现路由（契约完整路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/api{path}"), handler))
        .collect()
}

// ————————————————————— 自提人 —————————————————————

async fn get_user_pickups(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
) -> ApiResult<Vec<tigshop_infrastructure::db::repo::o2o::pickup_client::UserPickupVO>> {
    ok(state.o2o.pickup_client.list_pickups(current_user_id(auth)?).await?)
}

#[derive(Debug, Default, Deserialize)]
pub struct PickupIdQuery {
    pub id: Option<i32>,
}

async fn pickup_detail(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Query(q): Query<PickupIdQuery>,
) -> ApiResult<Value> {
    let vo = state
        .o2o
        .pickup_client
        .pickup_detail(current_user_id(auth)?, q.id.unwrap_or(0))
        .await?;
    ok(serde_json::to_value(vo).unwrap_or(Value::Null))
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct PickupSaveDTO {
    pub user_pickup_id: Option<i32>,
    pub consignee: Option<String>,
    pub mobile: Option<String>,
    pub mobile_area_code: Option<String>,
    pub is_default: Option<bool>,
}

async fn pickup_create(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Json(dto): Json<PickupSaveDTO>,
) -> ApiResult<i32> {
    let id = state
        .o2o
        .pickup_client
        .create_pickup(
            current_user_id(auth)?,
            dto.consignee.as_deref().unwrap_or(""),
            dto.mobile.as_deref().unwrap_or(""),
            dto.mobile_area_code.as_deref().unwrap_or(""),
            dto.is_default.unwrap_or(false),
        )
        .await?;
    ok(id)
}

async fn pickup_update(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Json(dto): Json<PickupSaveDTO>,
) -> ApiResult<()> {
    state
        .o2o
        .pickup_client
        .update_pickup(
            current_user_id(auth)?,
            dto.user_pickup_id.unwrap_or(0),
            dto.consignee.as_deref(),
            dto.mobile.as_deref(),
            dto.mobile_area_code.as_deref(),
            dto.is_default,
        )
        .await?;
    ok(())
}

async fn pickup_del(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Json(dto): Json<PickupIdQuery>,
) -> ApiResult<()> {
    state
        .o2o
        .pickup_client
        .delete_pickup(current_user_id(auth)?, dto.id.unwrap_or(0))
        .await?;
    ok(())
}

/// 当前会员 id（未登录 → 401「请先登录」，与 cart_summary 同口径）。
fn current_user_id(auth: Option<Extension<AuthUser>>) -> std::result::Result<i32, tigshop_security::web::AppError> {
    auth.map(|Extension(user)| user.id)
        .filter(|id| *id > 0)
        .ok_or_else(|| tigshop_security::web::AppError(tigshop_core::Error::Unauthorized("请先登录".into())))
}

/// 可选登录态中间件（cart_summary.rs 同款）。
async fn optional_auth(
    State(jwt): State<tigshop_security::JwtService>,
    mut req: Request,
    next: Next,
) -> Result<Response, tigshop_security::web::AppError> {
    let token = req
        .headers()
        .get(header::AUTHORIZATION)
        .and_then(|v| v.to_str().ok())
        .filter(|t| !t.trim().eq_ignore_ascii_case("bearer null") && !t.trim().is_empty())
        .map(str::to_string);
    if let Some(token) = token.as_deref() {
        if let Ok(claims) = jwt.from_authorization(Some(token)) {
            if let Ok(id) = claims.sub.parse::<i32>() {
                req.extensions_mut().insert(AuthUser {
                    id,
                    username: claims.usr,
                    role_id: claims.role,
                    admin_type: claims.typ,
                    shop_id: claims.shop_id,
                });
            }
        }
    }
    Ok(next.run(req).await)
}

// ————————————————————— 自提模板 / 配送单 —————————————————————

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct PickupTplQuery {
    pub shop_id: Option<i64>,
}

async fn shop_pickup_tpl(
    State(state): State<PublicState>,
    Query(q): Query<PickupTplQuery>,
) -> ApiResult<Value> {
    let vo = state
        .o2o
        .pickup_client
        .shop_pickup_tpl(q.shop_id.unwrap_or(0))
        .await?;
    ok(serde_json::to_value(vo).unwrap_or(Value::Null))
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderPickupQuery {
    pub order_id: Option<i32>,
}

async fn order_pickup_shipment(
    State(state): State<PublicState>,
    Query(q): Query<OrderPickupQuery>,
) -> ApiResult<Value> {
    let vo = state
        .o2o
        .pickup_client
        .order_shipment(q.order_id.unwrap_or(0))
        .await?;
    ok(serde_json::to_value(vo).unwrap_or(Value::Null))
}

/// 配送方式：存在自提单 → 自提（pickup），否则为空列表（物流引擎未迁移）。
async fn delivery_options(
    State(state): State<PublicState>,
    Query(q): Query<OrderPickupQuery>,
) -> ApiResult<Value> {
    let shipment = state
        .o2o
        .pickup_client
        .order_shipment(q.order_id.unwrap_or(0))
        .await?;
    let options = match shipment {
        Some(_) => serde_json::json!([{
            "type": "pickup",
            "name": "到店自提",
        }]),
        None => serde_json::json!([]),
    };
    ok(options)
}

// ————————————————————— 库存日历 / 门店商品 —————————————————————

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct StockCalendarQuery {
    pub product_id: Option<i32>,
    pub shop_id: Option<i64>,
    pub days: Option<u32>,
}

async fn stock_calendar(
    State(state): State<PublicState>,
    Query(q): Query<StockCalendarQuery>,
) -> ApiResult<Vec<StockDayVO>> {
    ok(state
        .o2o
        .pickup_client
        .stock_calendar(
            q.product_id.unwrap_or(0),
            q.shop_id,
            q.days.unwrap_or(14).clamp(1, 31),
        )
        .await?)
}

/// 门店商品搜索：按 shop_id 过滤上架商品（名称前缀/模糊匹配），分页。
#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct StoreProductQuery {
    pub shop_id: Option<i64>,
    pub keyword: Option<String>,
    pub page: Option<u64>,
    pub size: Option<u64>,
}

async fn store_product(
    State(state): State<PublicState>,
    Query(q): Query<StoreProductQuery>,
) -> ApiResult<Value> {
    let page = q.page.unwrap_or(1).max(1);
    let size = q.size.unwrap_or(10).clamp(1, 50);
    let (total, rows) = state
        .o2o
        .pickup_client
        .store_product_search(
            q.shop_id.unwrap_or(0),
            q.keyword.as_deref().unwrap_or_default(),
            page,
            size,
        )
        .await?;
    ok(serde_json::json!({ "records": rows, "total": total, "page": page, "size": size }))
}

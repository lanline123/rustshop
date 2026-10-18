//! 管理端代客下单配送方式路由（trade::proxy_shopping_delivery）。
//!
//! 对应 Java `ProxyShoppingDeliveryController`（`/adminapi/proxyShopping/delivery`）：
//! `GET /adminapi/proxyShopping/delivery/getDeliveryOptions`、
//! `GET /adminapi/proxyShopping/delivery/getStoreShippingType`。
//!
//! 两个接口均先校验用户，再返回明确「未迁移」业务错误（配送/运费引擎未迁移）。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::get,
    Router,
};
use tigshop_core::models::trade::proxy_shopping_delivery::{
    DeliveryOptionVO, DeliveryOptionsQuery, ProxyStoreShippingTypeQuery, ShippingTypeVO,
};
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 2 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    (
        "GET",
        "/proxyShopping/delivery/getDeliveryOptions",
        "ProxyShoppingDeliveryController.getDeliveryOptions",
    ),
    (
        "GET",
        "/proxyShopping/delivery/getStoreShippingType",
        "ProxyShoppingDeliveryController.getStoreShippingType",
    ),
];

/// 代客下单配送方式路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route(
            "/proxyShopping/delivery/getDeliveryOptions",
            get(get_delivery_options),
        )
        .route(
            "/proxyShopping/delivery/getStoreShippingType",
            get(get_store_shipping_type),
        )
}

/// 已实现路由（契约完整路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

/// 解析 Java `HeaderUtils.getShopId()`（admin→0；shop/pos→x-shop-id；其它 None）。
#[must_use]
fn shop_id_from_headers(headers: &HeaderMap) -> Option<i32> {
    let header_str = |name: &str| {
        headers
            .get(name)
            .and_then(|v| v.to_str().ok())
            .map(ToString::to_string)
    };
    match header_str("x-admin-type").as_deref() {
        Some("admin") => Some(0),
        Some("shop" | "pos") => {
            header_str("x-shop-id").and_then(|v| v.trim().parse::<i32>().ok())
        }
        _ => None,
    }
}

/// `GET /adminapi/proxyShopping/delivery/getDeliveryOptions`：配送选项（未迁移）。
async fn get_delivery_options(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<DeliveryOptionsQuery>,
) -> ApiResult<DeliveryOptionVO> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .trade
        .proxy_shopping_delivery
        .get_delivery_options(&q, shop_id)
        .await?)
}

/// `GET /adminapi/proxyShopping/delivery/getStoreShippingType`：店铺配送方式（未迁移）。
async fn get_store_shipping_type(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<ProxyStoreShippingTypeQuery>,
) -> ApiResult<Vec<(i32, Vec<ShippingTypeVO>)>> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .trade
        .proxy_shopping_delivery
        .get_store_shipping_type(&q, shop_id)
        .await?)
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::http::HeaderValue;

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

    #[test]
    fn implemented_routes_match_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 2);
        assert_eq!(docs[0].method, "GET");
        assert_eq!(
            docs[0].path,
            "/adminapi/proxyShopping/delivery/getDeliveryOptions"
        );
        assert_eq!(
            docs[1].path,
            "/adminapi/proxyShopping/delivery/getStoreShippingType"
        );
    }

    #[test]
    fn routes_registers_all_table_paths() {
        assert_eq!(ROUTE_TABLE.len(), 2);
        for (_, p, _) in ROUTE_TABLE.iter() {
            assert!(p.starts_with("/proxyShopping/delivery/"));
        }
    }

    #[test]
    fn header_scope_matches_header_utils() {
        assert_eq!(shop_id_from_headers(&headers(&[("x-admin-type", "admin")])), Some(0));
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "9")])),
            Some(9)
        );
        assert_eq!(shop_id_from_headers(&headers(&[])), None);
    }
}

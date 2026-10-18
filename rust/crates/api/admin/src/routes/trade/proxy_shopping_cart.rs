//! 管理端代客下单购物车路由（trade::proxy_shopping_cart）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.proxyshopping.ProxyShoppingCartController`
//! （`@RequestMapping("/adminapi/proxyShopping/cart")`，7 个端点，类级
//! `@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")`）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java 方法 |
//! |----------------------------------|--------------|
//! | `POST /proxyShopping/cart/list` | `list` |
//! | `POST /proxyShopping/cart/addToCart` | `addProductToCart` |
//! | `POST /proxyShopping/cart/clearCart` | `clearCart` |
//! | `POST /proxyShopping/cart/removeItem` | `removeItem` |
//! | `POST /proxyShopping/cart/updateItem` | `updateItem` |
//! | `POST /proxyShopping/cart/updateCheck` | `updateCheck` |
//! | `GET  /proxyShopping/cart/getCount` | `getCount` |
//!
//! > 门店 id 取 Java `HeaderUtils.getShopId()`（`x-admin-type` / `x-shop-id`），
//! > 见 [`shop_id_from_headers`]。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::trade::proxy_shopping_cart::{
    CartListByStoreVO, ExchangeProductToCartVO, ProxyAddToCartDTO, ProxyCartCountQuery,
    ProxyCartListDTO, ProxyClearCartDTO, ProxyRemoveItemDTO, ProxyUpdateCheckDTO,
    ProxyUpdateItemDTO,
};
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域全部 7 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 7] = [
    (
        "POST",
        "/proxyShopping/cart/list",
        "ProxyShoppingCartController.list",
    ),
    (
        "POST",
        "/proxyShopping/cart/addToCart",
        "ProxyShoppingCartController.addProductToCart",
    ),
    (
        "POST",
        "/proxyShopping/cart/clearCart",
        "ProxyShoppingCartController.clearCart",
    ),
    (
        "POST",
        "/proxyShopping/cart/removeItem",
        "ProxyShoppingCartController.removeItem",
    ),
    (
        "POST",
        "/proxyShopping/cart/updateItem",
        "ProxyShoppingCartController.updateItem",
    ),
    (
        "POST",
        "/proxyShopping/cart/updateCheck",
        "ProxyShoppingCartController.updateCheck",
    ),
    (
        "GET",
        "/proxyShopping/cart/getCount",
        "ProxyShoppingCartController.getCount",
    ),
];

/// 代客下单购物车路由（挂载于 `/adminapi` 下，外层统一套 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/proxyShopping/cart/list", post(list))
        .route("/proxyShopping/cart/addToCart", post(add_to_cart))
        .route("/proxyShopping/cart/clearCart", post(clear_cart))
        .route("/proxyShopping/cart/removeItem", post(remove_item))
        .route("/proxyShopping/cart/updateItem", post(update_item))
        .route("/proxyShopping/cart/updateCheck", post(update_check))
        .route("/proxyShopping/cart/getCount", get(get_count))
}

/// 本模块已实现路由（用于 `/openapi/coverage` 覆盖率统计，完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

/// 解析 Java `HeaderUtils.getShopId()`：`admin` ⇒ `0`；`shop|pos` ⇒ `x-shop-id`；
/// 其它/缺失 ⇒ `0`（代客下单购物车按门店隔离，缺失按平台 0 处理）。
#[must_use]
fn shop_id_from_headers(headers: &HeaderMap) -> i32 {
    let header_str = |name: &str| {
        headers
            .get(name)
            .and_then(|v| v.to_str().ok())
            .map(ToString::to_string)
    };
    match header_str("x-admin-type").as_deref() {
        Some("admin") => 0,
        Some("shop" | "pos") => header_str("x-shop-id")
            .and_then(|v| v.trim().parse::<i32>().ok())
            .unwrap_or(0),
        _ => 0,
    }
}

/// `POST /adminapi/proxyShopping/cart/list`：购物车列表。
async fn list(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<ProxyCartListDTO>,
) -> ApiResult<CartListByStoreVO> {
    let shop_id = Some(shop_id_from_headers(&headers));
    ok(state
        .trade
        .proxy_shopping_cart
        .list(&dto, shop_id)
        .await?)
}

/// `POST /adminapi/proxyShopping/cart/addToCart`：加入购物车。
async fn add_to_cart(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<ProxyAddToCartDTO>,
) -> ApiResult<ExchangeProductToCartVO> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state
        .trade
        .proxy_shopping_cart
        .add_to_cart(&dto, shop_id)
        .await?)
}

/// `POST /adminapi/proxyShopping/cart/clearCart`：清空购物车。
async fn clear_cart(
    State(state): State<AdminState>,
    Json(dto): Json<ProxyClearCartDTO>,
) -> ApiResult<()> {
    state.trade.proxy_shopping_cart.clear_cart(&dto).await?;
    ok(())
}

/// `POST /adminapi/proxyShopping/cart/removeItem`：删除购物车商品。
async fn remove_item(
    State(state): State<AdminState>,
    Json(dto): Json<ProxyRemoveItemDTO>,
) -> ApiResult<()> {
    state.trade.proxy_shopping_cart.remove_item(&dto).await?;
    ok(())
}

/// `POST /adminapi/proxyShopping/cart/updateItem`：更新购物车数量。
async fn update_item(
    State(state): State<AdminState>,
    Json(dto): Json<ProxyUpdateItemDTO>,
) -> ApiResult<()> {
    state.trade.proxy_shopping_cart.update_item(&dto).await?;
    ok(())
}

/// `POST /adminapi/proxyShopping/cart/updateCheck`：更新选中状态。
async fn update_check(
    State(state): State<AdminState>,
    Json(dto): Json<ProxyUpdateCheckDTO>,
) -> ApiResult<()> {
    state.trade.proxy_shopping_cart.update_check(&dto).await?;
    ok(())
}

/// `GET /adminapi/proxyShopping/cart/getCount`：购物车商品数量。
async fn get_count(
    State(state): State<AdminState>,
    Query(q): Query<ProxyCartCountQuery>,
) -> ApiResult<i32> {
    ok(state.trade.proxy_shopping_cart.get_count(&q).await?)
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

    /// `implemented_routes()` 必须与 Java 契约逐字符一致（7 条）。
    #[test]
    fn implemented_routes_match_java_contract() {
        let expected = [
            ("POST", "/adminapi/proxyShopping/cart/list"),
            ("POST", "/adminapi/proxyShopping/cart/addToCart"),
            ("POST", "/adminapi/proxyShopping/cart/clearCart"),
            ("POST", "/adminapi/proxyShopping/cart/removeItem"),
            ("POST", "/adminapi/proxyShopping/cart/updateItem"),
            ("POST", "/adminapi/proxyShopping/cart/updateCheck"),
            ("GET", "/adminapi/proxyShopping/cart/getCount"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), expected.len(), "契约中本域共 7 条接口");
        for (doc, (method, path)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *method, "方法不匹配: {path}");
            assert_eq!(doc.path, *path, "路径不匹配");
            assert_eq!(doc.key(), format!("{method} {path}"));
        }
        let mut unique: Vec<String> = docs.iter().map(RouteDoc::key).collect();
        unique.sort();
        unique.dedup();
        assert_eq!(unique.len(), docs.len(), "implemented_routes() 存在重复项");
    }

    /// `routes()` 注册路径必须与 [`ROUTE_TABLE`] 完全一致（7/7）。
    #[test]
    fn routes_registers_all_table_paths() {
        let registered = [
            "/proxyShopping/cart/list",
            "/proxyShopping/cart/addToCart",
            "/proxyShopping/cart/clearCart",
            "/proxyShopping/cart/removeItem",
            "/proxyShopping/cart/updateItem",
            "/proxyShopping/cart/updateCheck",
            "/proxyShopping/cart/getCount",
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

    /// 门店 header 解析（Java `HeaderUtils.getShopId()` 的 admin/shop/pos 三态）。
    #[test]
    fn shop_id_resolution_matches_header_utils() {
        assert_eq!(shop_id_from_headers(&headers(&[("x-admin-type", "admin")])), 0);
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "9")])),
            9
        );
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "pos"), ("x-shop-id", "8")])),
            8
        );
        assert_eq!(shop_id_from_headers(&headers(&[])), 0);
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "bad")])),
            0
        );
    }

    /// 请求体/查询参数按 Java 名（camelCase）解析。
    #[test]
    fn params_accept_java_names() {
        let dto: ProxyCartListDTO =
            serde_json::from_str(r#"{"userId":3,"shopId":1,"isChecked":1,"productId":5}"#).unwrap();
        assert_eq!(dto.user_id, Some(3));
        assert_eq!(dto.product_id, Some(5));
        let dto: ProxyRemoveItemDTO = serde_json::from_str(r#"{"cartIds":[1,2]}"#).unwrap();
        assert_eq!(dto.cart_ids, Some(vec![1, 2]));
        let dto: ProxyUpdateItemDTO =
            serde_json::from_str(r#"{"cartId":1,"data":{"quantity":3}}"#).unwrap();
        assert_eq!(dto.data.unwrap().quantity, Some(3));
        let q: ProxyCartCountQuery = serde_json::from_str(r#"{"shopId":1,"userId":2}"#).unwrap();
        assert_eq!(q.shop_id, Some(1));
    }
}

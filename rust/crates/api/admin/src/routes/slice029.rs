//! 代客下单商品管理路由（slice-029）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.proxyshopping.ProxyShoppingProductController`
//! （`@RequestMapping("/adminapi/proxyShopping/product")`，
//! 类级 `@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")`）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java | 权限 |
//! |--------------------------------|---------|------|
//! | `GET  /proxyShopping/product/productDetail` | `ProxyShoppingProductController.detail` | `proxyShoppingManage` |
//! | `GET  /proxyShopping/product/getProductAvailability` | `ProxyShoppingProductController.getProductAvailability` | `proxyShoppingManage` |
//! | `POST /proxyShopping/product/getProduct` | `ProxyShoppingProductController.list` | `proxyShoppingManage` |
//!
//! 路径中 `proxyShopping` 的 **S 大写**、`productDetail` / `getProductAvailability` /
//! `getProduct` 的大小写均逐字符对齐 Java `@RequestMapping` + `@GetMapping/@PostMapping`
//! 的组合结果（控制器前缀 `/adminapi/proxyShopping/product` 已由 `/adminapi` 挂载点
//! + 本文件路径补齐，**不存在**「前缀重复」的情形）。
//!
//! > **`productDetail` 的一个细微差异**：Java 第一步是
//! > `lambdaQuery().eq(Product::getProductTsn, productTsn).last("limit 1").one()`，
//! > 即**只按 `product_tsn` 精确匹配**且**不过滤 `is_delete`**；
//! > 本实现复用 slice-001 的 `find_product_by_sn`（`product_sn = ? OR product_tsn = ?`
//! > **且 `is_delete = 0`**）。两者在「`product_tsn` 恰好等于另一商品的
//! > `product_sn`」或「该商品已被软删」时结果不同（本实现会漏查软删商品）。
//! > 复用该方法是刻意的：它对「另一个控制器」的行为与 Java **完全一致**，
//! > 且 `repo::slice001` 属他人独占文件不得修改。见返回值的 `uncertain_points`。
//!
//! > **权限说明**：Java 用类级
//! > `@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")` 做方法级校验；
//! > Rust 侧 `require_auth`（由 `api/admin/src/lib.rs::router` 统一套在
//! > `/adminapi` 子树外层）只做登录态校验，权限矩阵尚未实现
//! > （与 seckill / slice001/002/005/010/014/018/026 的已知缺口一致）。
//! > 上表「权限」列仅作对照，当前**任何已登录管理员**都能调用本域全部接口 ——
//! > 即「代客下单选品」目前**无权限门禁**。
//!
//! > **多租户说明**：Java `list` 里执行 `DTO.setShopId(HeaderUtils.getShopId())`，
//! > 即按请求头 `x-admin-type` / `x-shop-id` 判定当前组织
//! > （`admin` → 0 平台；`shop` / `pos` → `x-shop-id`；其它 → `null`）。
//! > 本模块按**同一规则**解析请求头（见 [`shop_id_from_headers`]），
//! > 并把结果**覆盖**到请求体里的 `shopId`（与 Java 一致）。
//!
//! > **未迁移依赖**：促销（秒杀 / 限时折扣 / 拼团）、会员等级折扣、积分兑换、
//! > 预售定金、批发隐藏活动、O2O 门店覆盖价与距离 —— 详见
//! > `crates/services/src/slice029.rs` 模块头逐条说明。这些能力**不伪造**。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::slice001::{
    AdminProductListQuery, ClientProductDetailVO, ProductListItemVO,
};
use tigshop_core::models::slice029::{
    ProductAvailabilityVO, ProxyProductAvailabilityQuery, ProxyProductDetailQuery,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 本域 3 条路由（去 `/adminapi` 前缀的路径 + 原 Java handler）。
///
/// [`implemented_routes`] 的唯一来源；顺序与原 Java `ProxyShoppingProductController`
/// 的声明顺序一致（`detail` → `getProductAvailability` → `list`）。
const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    // (HTTP method, 本模块路径, 原 Java handler)
    (
        "GET",
        "/proxyShopping/product/productDetail",
        "ProxyShoppingProductController.detail",
    ),
    (
        "GET",
        "/proxyShopping/product/getProductAvailability",
        "ProxyShoppingProductController.getProductAvailability",
    ),
    (
        "POST",
        "/proxyShopping/product/getProduct",
        "ProxyShoppingProductController.list",
    ),
];

/// 代客下单商品管理域路由（挂载于 `/adminapi` 下）。
///
/// 路径逐字符对齐 Java：`proxyShopping`（S 大写）、`productDetail`（D 大写）、
/// `getProductAvailability`（A 大写）、`getProduct`。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route(
            "/proxyShopping/product/productDetail",
            get(proxy_product_detail),
        )
        .route(
            "/proxyShopping/product/getProductAvailability",
            get(proxy_get_product_availability),
        )
        .route("/proxyShopping/product/getProduct", post(proxy_product_list))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

/// 解析当前组织 ID（等价 Java `HeaderUtils.getShopId()`）。
///
/// - `x-admin-type: admin` → `Some(0)`（平台，**先于** `x-shop-id` 判定）；
/// - `x-admin-type: shop` / `pos` → `x-shop-id`（缺失 / 非法 → `None`）；
/// - 其它（含请求头缺失）→ `None`。
///
/// ⚠️ 与 `routes/slice003.rs::shop_id_from_headers`（返回 `i32`、缺失回退 0）
/// 的差别：Java `HeaderUtils.getShopId()` 在**最后两个分支返回 `null`**，
/// 而 `list` 是 `DTO.setShopId(null)` → 查询**不加** `shop_id` 条件；
/// 若回退成 0 会错误地把结果限制在平台自营商品上，因此这里保留 `Option`。
/// （该三态语义与 slice-020 / slice-023 的 `resolve_scope` 一致。）
#[must_use]
pub fn shop_id_from_headers(headers: &HeaderMap) -> Option<i32> {
    let admin_type = header_str(headers, "x-admin-type");
    match admin_type.as_deref() {
        Some("admin") => Some(0),
        Some("shop" | "pos") => header_str(headers, "x-shop-id")
            .and_then(|v| v.trim().parse::<i32>().ok()),
        _ => None,
    }
}

fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|v| v.to_str().ok())
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .map(ToString::to_string)
}

/// `GET /adminapi/proxyShopping/product/productDetail`：商品详情。
///
/// 对应 Java `ProxyShoppingProductController.detail(productTsn, productSn, shopId, nearestShopId)`
/// （`ProxyShoppingProductController.java:41-52`）→ `ProductServiceImpl#clientDetail`。
/// 返回结构与前台 `/api/product/product/detail` 一致（复用 slice-001 的实现）。
async fn proxy_product_detail(
    State(state): State<AdminState>,
    Query(q): Query<ProxyProductDetailQuery>,
) -> ApiResult<ClientProductDetailVO> {
    ok(state.slice029.product_detail(&q).await?)
}

/// `GET /adminapi/proxyShopping/product/getProductAvailability`：商品可用数据。
///
/// 对应 Java `ProxyShoppingProductController.getProductAvailability(id, skuId, extraAttrIds, shopId, addressId, userId)`
/// （`ProxyShoppingProductController.java:54-63`）→
/// `ProductServiceImpl#getProductSkuDetail(productId, skuId, 0, extraAttrIds, shopId, addressId, userId)`。
///
/// ⚠️ 促销 / 会员折扣 / 积分兑换 / 预售定金 / O2O 距离未迁移，
/// 返回的 `price` 与 `stock` 在生产活动场景下会与 Java 不同
/// （详见 `crates/services/src/slice029.rs` 模块头第 2 条）。
/// 另：商品不存在且未传 `extraAttrIds` 时下发 `price: null`（与 Java 一致）；
/// 传了 `extraAttrIds` 时 Java 抛 NPE，本实现改为返回 400。
async fn proxy_get_product_availability(
    State(state): State<AdminState>,
    Query(q): Query<ProxyProductAvailabilityQuery>,
) -> ApiResult<ProductAvailabilityVO> {
    ok(state.slice029.get_product_availability(&q).await?)
}

/// `POST /adminapi/proxyShopping/product/getProduct`：商品列表。
///
/// 对应 Java `ProxyShoppingProductController.list(@RequestBody ProductListDTO DTO)`
/// （`ProxyShoppingProductController.java:65-75`）→ `ProductServiceImpl#list`。
///
/// Java 在服务调用前**覆盖** `productTypes`（1/2/3/4/7/8）、`isDelete=0`、
/// `productStatus=1`、`shopId`（请求头）、`saleMode=0`；本 handler 负责解析请求头
/// 里的组织 ID，其余覆盖在服务层完成（见 `Slice029Service::product_list`）。
async fn proxy_product_list(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<AdminProductListQuery>,
) -> ApiResult<PageResult<ProductListItemVO>> {
    let shop_id = shop_id_from_headers(&headers);
    ok(state.slice029.product_list(&dto, shop_id).await?)
}

#[cfg(test)]
mod tests {
    use super::*;
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

    #[test]
    fn route_paths_are_verbatim_java_paths() {
        // 逐字符锁死 3 条路径（写错 cargo check 全绿、前端 404）
        assert_eq!(
            ROUTE_TABLE.map(|(m, p, _)| (m, p)),
            [
                ("GET", "/proxyShopping/product/productDetail"),
                ("GET", "/proxyShopping/product/getProductAvailability"),
                ("POST", "/proxyShopping/product/getProduct"),
            ]
        );
        // `proxyShopping` 的 S 必须大写（Java 请求映射原文）
        for (_, path, _) in ROUTE_TABLE {
            assert!(
                path.starts_with("/proxyShopping/product/"),
                "路径前缀必须逐字对齐 Java：{path}"
            );
        }
    }

    #[test]
    fn implemented_routes_prefixes_adminapi() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 3);
        let paths: Vec<&str> = docs.iter().map(|d| d.path.as_str()).collect();
        assert!(paths.contains(&"/adminapi/proxyShopping/product/productDetail"));
        assert!(paths.contains(&"/adminapi/proxyShopping/product/getProductAvailability"));
        assert!(paths.contains(&"/adminapi/proxyShopping/product/getProduct"));
    }

    #[test]
    fn shop_id_follows_java_header_utils() {
        // 平台账号强制 0（且优先于 x-shop-id）
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "admin"), ("x-shop-id", "9")])),
            Some(0)
        );
        // 店铺 / 收银台账号取 x-shop-id
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "9")])),
            Some(9)
        );
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "pos"), ("x-shop-id", "3")])),
            Some(3)
        );
        // 店铺账号缺 x-shop-id / 非法值 → Java 返回 null（不是 0）
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop")])),
            None
        );
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "abc")])),
            None
        );
        // 请求头缺失 / 未知类型 → null（查询不加 shop_id 条件）
        assert_eq!(shop_id_from_headers(&headers(&[])), None);
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "vendor")])),
            None
        );
        // 空白值按缺失处理（Java `StrUtil.isEmpty`）
        assert_eq!(
            shop_id_from_headers(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "   ")])),
            None
        );
    }
}

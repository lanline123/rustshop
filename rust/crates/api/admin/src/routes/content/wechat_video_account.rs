//! 管理端视频号账号路由（content::wechat_video_account）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.content.WechatVideoAccountController`
//! （`@RequestMapping("/adminapi/content/wechatVideoAccount")`，5 个端点）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java |
//! |----------------------------------|---------|
//! | `GET  /content/wechatVideoAccount/list`   | `list` |
//! | `GET  /content/wechatVideoAccount/detail` | `detail` |
//! | `POST /content/wechatVideoAccount/create` | `create` |
//! | `POST /content/wechatVideoAccount/update` | `update` |
//! | `POST /content/wechatVideoAccount/delete` | `delete` |
//!
//! 店铺范围：Java `getShopId()`；`admin` 无过滤，店铺/门店账号按 `x-shop-id`
//! 过滤（仅 `> 0` 生效）。合并注意：并入 `crates/api/admin/src/routes/content/mod.rs`
//! 的 `routes()` 与 `implemented_routes()`。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::content::wechat_video_account::{
    OperateDTO, WechatVideoAccountCreateDTO, WechatVideoAccountListQuery,
    WechatVideoAccountUpdateDTO, WechatVideoAccountVO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域路由清单（去掉 `/adminapi` 前缀 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 5] = [
    (
        "GET",
        "/content/wechatVideoAccount/list",
        "WechatVideoAccountController.list",
    ),
    (
        "GET",
        "/content/wechatVideoAccount/detail",
        "WechatVideoAccountController.detail",
    ),
    (
        "POST",
        "/content/wechatVideoAccount/create",
        "WechatVideoAccountController.create",
    ),
    (
        "POST",
        "/content/wechatVideoAccount/update",
        "WechatVideoAccountController.update",
    ),
    (
        "POST",
        "/content/wechatVideoAccount/delete",
        "WechatVideoAccountController.delete",
    ),
];

/// 视频号账号路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/content/wechatVideoAccount/list", get(list))
        .route("/content/wechatVideoAccount/detail", get(detail))
        .route("/content/wechatVideoAccount/create", post(create))
        .route("/content/wechatVideoAccount/update", post(update))
        .route("/content/wechatVideoAccount/delete", post(delete))
}

/// 本域已实现路由（覆盖率统计用）。
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

/// `GET .../detail?id=` 的查询参数。
#[derive(Debug, Deserialize)]
struct IdQuery {
    id: Option<i32>,
}

/// `GET /adminapi/content/wechatVideoAccount/list`：视频号账号列表。
async fn list(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Query(q): Query<WechatVideoAccountListQuery>,
) -> ApiResult<PageResult<WechatVideoAccountVO>> {
    let shop = current_shop_id(&headers);
    ok(state.content.wechat_video_account.list(shop, &q).await?)
}

/// `GET /adminapi/content/wechatVideoAccount/detail?id=`：视频号账号详情。
async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<WechatVideoAccountVO> {
    ok(state.content.wechat_video_account.detail(q.id).await?)
}

/// `POST /adminapi/content/wechatVideoAccount/create`：新增视频号账号。
async fn create(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<WechatVideoAccountCreateDTO>,
) -> ApiResult<i32> {
    let shop = current_shop_id(&headers);
    ok(state.content.wechat_video_account.create(shop, &dto).await?)
}

/// `POST /adminapi/content/wechatVideoAccount/update`：更新视频号账号。
async fn update(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Json(dto): Json<WechatVideoAccountUpdateDTO>,
) -> ApiResult<()> {
    let shop = current_shop_id(&headers);
    state.content.wechat_video_account.update(shop, &dto).await?;
    ok(())
}

/// `POST /adminapi/content/wechatVideoAccount/delete`：删除视频号账号。
async fn delete(
    State(state): State<AdminState>,
    Json(dto): Json<OperateDTO>,
) -> ApiResult<()> {
    state.content.wechat_video_account.delete(&dto).await?;
    ok(())
}

fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(axum::http::HeaderName::from_bytes(name.as_bytes()).ok()?)
        .and_then(|v| v.to_str().ok())
        .map(ToString::to_string)
}

/// 当前店铺 ID（Java `HeaderUtils.getShopId()`）：`admin` → `None`；
/// `shop` / `pos` → 解析 `x-shop-id`（缺失/非法 → `None`）；其它 → `None`。
#[must_use]
pub fn current_shop_id(headers: &HeaderMap) -> Option<i32> {
    match header_str(headers, "x-admin-type").as_deref() {
        Some("shop" | "pos") => header_str(headers, "x-shop-id")
            .and_then(|v| v.trim().parse::<i32>().ok()),
        _ => None,
    }
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
    fn implemented_routes_match_java_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 5);
        let expected = [
            ("GET", "/adminapi/content/wechatVideoAccount/list"),
            ("GET", "/adminapi/content/wechatVideoAccount/detail"),
            ("POST", "/adminapi/content/wechatVideoAccount/create"),
            ("POST", "/adminapi/content/wechatVideoAccount/update"),
            ("POST", "/adminapi/content/wechatVideoAccount/delete"),
        ];
        for (method, path) in expected {
            assert!(
                docs.iter().any(|d| d.method == method && d.path == path),
                "缺少 {method} {path}"
            );
        }
        let mut unique: Vec<(String, String)> =
            docs.iter().map(|d| (d.method.clone(), d.path.clone())).collect();
        unique.sort();
        unique.dedup();
        assert_eq!(unique.len(), docs.len(), "implemented_routes() 存在重复项");
    }

    #[test]
    fn route_table_is_unique_and_prefixed() {
        assert_eq!(ROUTE_TABLE.len(), 5);
        for (i, (_, p, _)) in ROUTE_TABLE.iter().enumerate() {
            assert!(
                p.starts_with("/content/wechatVideoAccount/"),
                "路径前缀错误: {p}"
            );
            for (_, p2, _) in ROUTE_TABLE.iter().skip(i + 1) {
                assert_ne!(p, p2, "重复路由声明会导致 merge panic");
            }
        }
    }

    #[test]
    fn shop_scope_matches_java_header_utils() {
        assert_eq!(current_shop_id(&HeaderMap::new()), None);
        assert_eq!(
            current_shop_id(&headers(&[("x-admin-type", "admin"), ("x-shop-id", "9")])),
            None
        );
        assert_eq!(
            current_shop_id(&headers(&[("x-admin-type", "shop"), ("x-shop-id", "9")])),
            Some(9)
        );
        assert_eq!(
            current_shop_id(&headers(&[("x-admin-type", "pos"), ("x-shop-id", "3")])),
            Some(3)
        );
        assert_eq!(current_shop_id(&headers(&[("x-admin-type", "shop")])), None);
    }
}

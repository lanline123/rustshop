//! 商城前台商品路由（切片 catalog::product_manage）。
//!
//! 对应原 Java `com.tigshop.api.controller.product.ProductController`
//! （`@RequestMapping("/api/product/product")`）。
//!
//! | 本模块路由（挂载于 `/api`） | 原 Java |
//! |----------------------------|---------|
//! | `GET /product/product/detail` | `ProductController.detail`（`sn` 必填） |
//! | `GET /product/product/detail` | `ProductController.detailById`（`id` 必填、`sn` 不允许出现） |
//!
//! 注意：原 Java 用 `@GetMapping(value = "/detail", params = {"id", "!sn"})`
//! 让**同一个路径**按参数名分流到两个方法，HTTP 路由层无法区分，
//! 因此本模块用一个 handler 同时兼容 `?sn=` 与 `?id=`（与 Java 行为等价）。
//!
//! 权限：原契约 `auth = optional`（游客可访问，登录后返回会员相关字段），
//! 因此挂在**公开**路由组（与 `crates/api/public/src/lib.rs` 中同样公开的
//! `/product/product/list` 一致）。
//!
//! **未迁移**：评论/咨询总数、秒杀、拼团、积分兑换、批发阶梯价、
//! 收藏状态、门店（O2O）图片与库存覆盖等字段，见
//! [`tigshop_services::catalog::product_manage::ProductManageService::client_detail`]。

use axum::{
    body::Body,
    extract::{Extension, Query, Request, State},
    http::{header, HeaderMap, StatusCode},
    middleware::{self, Next},
    response::Response,
    routing::get,
    Router,
};
use serde::Deserialize;
use std::io::{Cursor, Write};
use tigshop_core::models::catalog::product_manage::ClientProductDetailVO;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;
use zip::write::SimpleFileOptions;

use crate::PublicState;

/// 商品域前台路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .route("/product/product/detail", get(product_detail))
        .route("/product/product/downloadRawImages", get(download_raw_images))
        .layer(middleware::from_fn_with_state(state.jwt.clone(), optional_auth))
}

async fn optional_auth(
    State(jwt): State<JwtService>,
    mut req: Request,
    next: Next,
) -> Result<Response, AppError> {
    let token = req.headers().get(header::AUTHORIZATION).and_then(|v| v.to_str().ok());
    if token.is_some() {
        let Ok(claims) = jwt.from_authorization(token) else { return Ok(next.run(req).await); };
        let Ok(id) = claims.sub.parse::<i32>() else { return Ok(next.run(req).await); };
        req.extensions_mut().insert(AuthUser {
            id,
            username: claims.usr,
            role_id: claims.role,
            admin_type: claims.typ,
            shop_id: claims.shop_id,
        });
    }
    Ok(next.run(req).await)
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
///
/// 原契约中 `ProductController.detail`（`sn`）与 `detailById`（`id`）
/// 是同一路径的两个条目，这里按一个实现上报两次，避免覆盖率漏记。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/api/product/product/detail", "ProductController.detail"),
        ("GET", "/api/product/product/detail", "ProductController.detailById"),
        ("GET", "/api/product/product/downloadRawImages", "ProductController.downloadRawImages"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// 商品详情查询参数。
///
/// - `sn`：商品货号或条形码（原 `detail` 方法）
/// - `id`：商品 ID（原 `detailById` 方法，Java 侧先查货号再走同一逻辑）
/// - `shopId` / `nearestShopId`：O2O 门店上下文（本切片未迁移门店覆盖逻辑）
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct DetailQuery {
    sn: Option<String>,
    id: Option<i32>,
    shop_id: Option<i32>,
    nearest_shop_id: Option<i32>,
}

/// `GET /api/product/product/detail?sn=`（或 `?id=`）：前台商品详情。
///
/// 对应 Java `ProductController.detail` / `detailById`。
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct RawQuery {
    id: Option<i32>,
    shop_id: Option<i32>,
    nearest_shop_id: Option<i32>,
}

/// `GET /api/product/product/downloadRawImages?id=`：登录后下载商品原图。
async fn download_raw_images(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    headers: HeaderMap,
    Query(q): Query<RawQuery>,
) -> Result<Response, AppError> {
    let user_id = auth.as_ref().map(|Extension(a)| a.id).unwrap_or(0);
    if user_id <= 0 {
        return Err(AppError(Error::biz(401, "请先登录后下载商品原图")));
    }
    let product_id = q.id.unwrap_or(0);
    if product_id <= 0 || q.shop_id.is_some_and(|v| v < 0) || q.nearest_shop_id.is_some_and(|v| v < 0) {
        return Err(AppError(Error::biz(400, "商品或门店参数不合法")));
    }
    let product = sqlx::query("SELECT product_name, product_sn, pic_url, pic_thumb, pic_original, pic_large FROM product WHERE product_id=? AND is_delete=0 LIMIT 1")
        .bind(product_id)
        .fetch_optional(&state.repos.pool)
        .await
        .map_err(|e| AppError(Error::Database(e.to_string())))?
        .ok_or_else(|| AppError(Error::biz(400, "商品不存在")))?;
    use sqlx::Row;
    let name = product.try_get::<String, _>("product_name").unwrap_or_default();
    let sn = product.try_get::<Option<String>, _>("product_sn").unwrap_or_default().unwrap_or_default();
    let mut urls: Vec<String> = Vec::new();
    for key in ["pic_url", "pic_original", "pic_large", "pic_thumb"] {
        if let Ok(v) = product.try_get::<Option<String>, _>(key) {
            if let Some(v) = v.filter(|s| !s.trim().is_empty()) {
                urls.push(v);
            }
        }
    }
    if let Ok(rows) = sqlx::query("SELECT pic_url FROM product_gallery WHERE product_id=? ORDER BY sort_order, pic_id")
        .bind(product_id)
        .fetch_all(&state.repos.pool)
        .await
    {
        for row in rows {
            if let Ok(Some(url)) = row.try_get::<Option<String>, _>("pic_url") {
                if !url.trim().is_empty() {
                    urls.push(url);
                }
            }
        }
    }
    let host = headers.get(header::HOST).and_then(|v| v.to_str().ok()).unwrap_or("127.0.0.1");
    let proto = headers
        .get("x-forwarded-proto")
        .and_then(|v| v.to_str().ok())
        .unwrap_or("http");
    let base = format!("{proto}://{host}/");
    urls = urls
        .into_iter()
        .map(|u| {
            if u.starts_with("http://") || u.starts_with("https://") {
                u
            } else {
                format!("{base}{}", u.trim_start_matches('/'))
            }
        })
        .collect();
    urls.sort();
    urls.dedup();
    if urls.is_empty() {
        return Err(AppError(Error::biz(400, "该商品没有可下载的原图")));
    }
    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(25))
        .build()
        .map_err(|e| AppError(Error::Internal(e.to_string())))?;
    let mut zip = zip::ZipWriter::new(Cursor::new(Vec::new()));
    let options = SimpleFileOptions::default().compression_method(zip::CompressionMethod::Deflated);
    let mut written = 0;
    for (index, url) in urls.iter().take(120).enumerate() {
        let response = match client.get(url).send().await {
            Ok(r) if r.status().is_success() => r,
            _ => continue,
        };
        if response.content_length().is_some_and(|n| n > 20 * 1024 * 1024) {
            continue;
        }
        let Ok(bytes) = response.bytes().await else { continue; };
        if bytes.is_empty() || bytes.len() > 20 * 1024 * 1024 {
            continue;
        }
        let ext = url
            .split('?')
            .next()
            .and_then(|u| std::path::Path::new(u).extension())
            .and_then(|e| e.to_str())
            .filter(|e| matches!(e.to_ascii_lowercase().as_str(), "jpg" | "jpeg" | "png" | "webp" | "gif" | "bmp"))
            .unwrap_or("jpg");
        zip.start_file(format!("images/{:03}.{ext}", index + 1), options)
            .map_err(|e| AppError(Error::Internal(e.to_string())))?;
        zip.write_all(&bytes)
            .map_err(|e| AppError(Error::Internal(e.to_string())))?;
        written += 1;
    }
    if written == 0 {
        return Err(AppError(Error::biz(400, "原图下载失败，请稍后重试")));
    }
    let data = zip.finish()
        .map_err(|e| AppError(Error::Internal(e.to_string())))?
        .into_inner();
    let ascii_code = sn.chars().filter(|c| c.is_ascii_alphanumeric() || matches!(c, '.' | '_' | '-')).collect::<String>();
    let encoded = percent_encode(&format!("{name}_{sn}_原图.zip"));
    let disposition = format!(
        "attachment; filename=\"product_{}_raw_images.zip\"; filename*=UTF-8''{}",
        if ascii_code.is_empty() { product_id.to_string() } else { ascii_code },
        encoded
    );
    Response::builder()
        .status(StatusCode::OK)
        .header(header::CONTENT_TYPE, "application/zip")
        .header(header::CONTENT_LENGTH, data.len())
        .header(header::CONTENT_DISPOSITION, disposition)
        .header(header::CACHE_CONTROL, "no-store")
        .body(Body::from(data))
        .map_err(|e| AppError(Error::Internal(e.to_string())))
}

fn percent_encode(input: &str) -> String {
    let mut out = String::new();
    for byte in input.bytes() {
        match byte {
            b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => out.push(byte as char),
            _ => out.push_str(&format!("%{byte:02X}")),
        }
    }
    out
}

async fn product_detail(
    State(state): State<PublicState>,
    Query(q): Query<DetailQuery>,
) -> ApiResult<ClientProductDetailVO> {
    let sn = match q.sn.as_deref().map(str::trim).filter(|s| !s.is_empty()) {
        Some(sn) => sn.to_string(),
        // 对应 Java `getProductSnById`：按 ID 反查货号后走同一逻辑
        None => match q.id {
            Some(id) => state.catalog.product_manage
                .product_sn_by_id(id)
                .await?
                .ok_or_else(|| AppError(Error::biz(400, "商品不存在")))?,
            None => return Err(AppError(Error::biz(400, "商品不存在"))),
        },
    };
    ok(state.catalog.product_manage
        .client_detail(&sn, q.shop_id, q.nearest_shop_id)
        .await?)
}
//! 消费者图片搜索（v5.8.61 `/api/search/image`）。
//!
//! 当前实现配置读取、能力声明和接口契约；阿里云图库检索适配器未接入，
//! 未启用/未配置时返回明确业务错误，避免前端误显示空结果。

use axum::{
    extract::{Multipart, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use serde_json::{json, Value};
use tigshop_core::Error;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AppError};

use crate::PublicState;

const KEYS: [&str; 5] = [
    "imageSearchEnabled",
    "imageSearchH5Enabled",
    "imageSearchPcEnabled",
    "imageSearchAppEnabled",
    "imageSearchShopEnabled",
];

pub fn routes(state: &PublicState) -> Router<PublicState> {
    let _ = state;
    Router::new()
        .route("/search/image/capabilities", get(capabilities))
        .route("/search/image/search", post(search))
        .route("/search/image/results", post(results))
}

#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    [
        ("GET", "/api/search/image/capabilities", "ImageSearchController.capabilities"),
        ("POST", "/api/search/image/search", "ImageSearchController.search"),
        ("POST", "/api/search/image/results", "ImageSearchController.results"),
    ]
    .iter()
    .map(|(m, p, h)| RouteDoc::new(m, p, h))
    .collect()
}

async fn flags(state: &PublicState) -> Result<Vec<(String, String)>, AppError> {
    state
        .system
        .config_basic
        .repo()
        .find_by_codes(&KEYS)
        .await
        .map_err(|e| AppError(e))
}

fn on(rows: &[(String, String)], key: &str) -> bool {
    rows.iter().any(|(k, v)| k == key && v == "1")
}

async fn capabilities(State(state): State<PublicState>) -> ApiResult<Value> {
    let rows = flags(&state).await?;
    let enabled = on(&rows, "imageSearchEnabled");
    ok(json!({
        "enabled": enabled,
        "h5Enabled": enabled && on(&rows, "imageSearchH5Enabled"),
        "pcEnabled": enabled && on(&rows, "imageSearchPcEnabled"),
        "appEnabled": enabled && on(&rows, "imageSearchAppEnabled"),
        "shopEnabled": enabled && on(&rows, "imageSearchShopEnabled"),
        "formats": ["JPEG", "PNG", "WEBP", "BMP", "GIF", "TIFF"]
    }))
}

async fn search(
    State(state): State<PublicState>,
    _multipart: Multipart,
) -> ApiResult<Value> {
    let rows = flags(&state).await?;
    if !on(&rows, "imageSearchEnabled") {
        return Err(AppError(Error::biz(400, "图像搜索未启用")));
    }
    Err(AppError(Error::biz(400, "图像搜索服务暂不可用")))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ResultsQuery {
    search_token: Option<String>,
    cursor: Option<i32>,
    size: Option<i32>,
}

async fn results(
    State(state): State<PublicState>,
    Json(_q): Json<ResultsQuery>,
) -> ApiResult<Value> {
    let rows = flags(&state).await?;
    if !on(&rows, "imageSearchEnabled") {
        return Err(AppError(Error::biz(400, "图像搜索未启用")));
    }
    Err(AppError(Error::biz(400, "搜索会话已过期，请重新上传图片")))
}

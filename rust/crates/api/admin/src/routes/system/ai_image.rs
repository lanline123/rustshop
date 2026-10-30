//! AI 图片与地图设置（v5.8.61 `/adminapi/file/aiImage/*`、`/setting/config/*`）。

use axum::{
    extract::State,
    routing::{get, post},
    Json, Router,
};
use serde_json::{json, Value};
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError};

use crate::AdminState;

const CONFIG_KEYS: [&str; 10] = [
    "aiImageEnabled", "aiImageProvider", "aiImageApiKey", "aiImageSecret",
    "aiImageModel", "aiImageTranslateSource", "amapWebKey", "amapWebSecret",
    "mapProvider", "aiImageTranslateEnabled",
];

pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/file/aiImage/getTranslateConfig", get(get_translate_config))
        .route("/file/aiImage/translate", post(translate))
        .route("/file/aiImage/inpaint", post(inpaint))
        .route("/file/aiImage/localesList", post(locales_list))
        .route("/setting/config/aiImageSettings", get(ai_image_settings))
        .route("/setting/config/saveAiImage", post(save_ai_image))
        .route("/setting/config/amapSettings", get(amap_settings))
        .route("/setting/config/saveAmapSettings", post(save_amap))
}

#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/api/file/aiImage/getTranslateConfig"),
        ("POST", "/api/file/aiImage/translate"),
        ("POST", "/api/file/aiImage/inpaint"),
        ("POST", "/api/file/aiImage/localesList"),
        ("GET", "/api/setting/config/aiImageSettings"),
        ("POST", "/api/setting/config/saveAiImage"),
        ("GET", "/api/setting/config/amapSettings"),
        ("POST", "/api/setting/config/saveAmapSettings"),
    ]
    .iter()
    .map(|(m, p)| tigshop_openapi::RouteDoc::new(m, p, "AiImageController/ConfigController"))
    .collect()
}

async fn load(state: &AdminState) -> Result<Value, AppError> {
    let rows = state
        .system
        .config_basic
        .repo()
        .find_by_codes(&CONFIG_KEYS)
        .await
        .map_err(|e| AppError(e))?;
    let get = |key: &str| {
        rows.iter()
            .find(|(k, _)| k == key)
            .map(|(_, v)| v.clone())
            .unwrap_or_default()
    };
    Ok(json!({
        "enabled": get("aiImageEnabled") == "1",
        "provider": get("aiImageProvider"),
        "apiKey": mask(&get("aiImageApiKey")),
        "secret": mask(&get("aiImageSecret")),
        "model": get("aiImageModel"),
        "translateEnabled": get("aiImageTranslateEnabled") == "1",
        "mapProvider": get("mapProvider"),
        "amapWebKey": mask(&get("amapWebKey")),
        "amapWebSecret": mask(&get("amapWebSecret"))
    }))
}

fn mask(v: &str) -> String {
    if v.is_empty() {
        String::new()
    } else if v.len() <= 4 {
        "****".into()
    } else {
        format!("****{}", &v[v.len() - 4..])
    }
}

async fn get_translate_config(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(load(&state).await?)
}
async fn ai_image_settings(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(load(&state).await?)
}
async fn amap_settings(State(state): State<AdminState>) -> ApiResult<Value> {
    ok(load(&state).await?)
}

async fn save_ai_image(
    State(state): State<AdminState>,
    Json(body): Json<Value>,
) -> ApiResult<()> {
    let pairs = [
        ("aiImageEnabled", "enabled"),
        ("aiImageProvider", "provider"),
        ("aiImageApiKey", "apiKey"),
        ("aiImageSecret", "secret"),
        ("aiImageModel", "model"),
        ("aiImageTranslateEnabled", "translateEnabled"),
    ];
    for (code, field) in pairs {
        if let Some(v) = body.get(field) {
            let value = if let Some(b) = v.as_bool() {
                if b { "1" } else { "0" }.to_string()
            } else {
                v.to_string().trim_matches('"').to_string()
            };
            state.system.config_basic.repo().save_code(code, &value).await?;
        }
    }
    ok(())
}

async fn save_amap(
    State(state): State<AdminState>,
    Json(body): Json<Value>,
) -> ApiResult<()> {
    for (code, field) in [("amapWebKey", "amapWebKey"), ("amapWebSecret", "amapWebSecret"), ("mapProvider", "mapProvider")] {
        if let Some(v) = body.get(field) {
            state.system.config_basic.repo().save_code(code, v.as_str().unwrap_or("")).await?;
        }
    }
    ok(())
}

async fn translate(Json(body): Json<Value>) -> ApiResult<Value> {
    // 未接翻译服务时原样返回源文本，不伪造译文。
    let text = body.get("text").and_then(Value::as_str).unwrap_or("");
    ok(json!({"text": text, "translated": false}))
}

async fn inpaint(Json(_body): Json<Value>) -> ApiResult<Value> {
    Err(AppError(Error::biz(400, "AI 图片修复服务未配置")))
}

async fn locales_list() -> ApiResult<Value> {
    ok(json!([]))
}

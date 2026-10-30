//! 图像搜索管理端（v5.8.61 `/adminapi/setting/imageSearch`）。
//!
//! 配置存 `config` 表 `imageSearch*` 键；任务存 `image_search_sync_job`。
//! 阿里云图库写入/检索适配器尚未接入，构建设置为 PENDING 台账任务。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use serde_json::{json, Value};
use sqlx::Row;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

const CONFIG_KEYS: [&str; 17] = [
    "imageSearchEnabled", "imageSearchH5Enabled", "imageSearchPcEnabled",
    "imageSearchAppEnabled", "imageSearchShopEnabled", "imageSearchProvider",
    "imageSearchIndexName", "imageSearchSimilarityPercent", "imageSearchMaxResults",
    "imageSearchAliyunRegionId", "imageSearchAliyunAccessKeyId", "imageSearchAliyunAccessKeySecret",
    "imageSearchAliyunSecurityToken", "imageSearchAliyunVpc",
    "imageSearchAliyunConnectTimeoutMillis", "imageSearchAliyunReadTimeoutMillis",
    "imageSearchVersion",
];

pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/setting/imageSearch/regions", get(regions))
        .route("/setting/imageSearch/detail", get(detail))
        .route("/setting/imageSearch/save", post(save))
        .route("/setting/imageSearch/check", post(check))
        .route("/setting/imageSearch/build", post(build))
        .route("/setting/imageSearch/syncProduct", post(sync_product))
        .route("/setting/imageSearch/task", get(task))
        .route("/setting/imageSearch/retry", post(retry))
}

#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/api/setting/imageSearch/regions", "ImageSearchAdminController.regions"),
        ("GET", "/api/setting/imageSearch/detail", "ImageSearchAdminController.detail"),
        ("POST", "/api/setting/imageSearch/save", "ImageSearchAdminController.save"),
        ("POST", "/api/setting/imageSearch/check", "ImageSearchAdminController.check"),
        ("POST", "/api/setting/imageSearch/build", "ImageSearchAdminController.build"),
        ("POST", "/api/setting/imageSearch/syncProduct", "ImageSearchAdminController.syncProduct"),
        ("GET", "/api/setting/imageSearch/task", "ImageSearchAdminController.task"),
        ("POST", "/api/setting/imageSearch/retry", "ImageSearchAdminController.retry"),
    ]
    .iter()
    .map(|(m, p, h)| tigshop_openapi::RouteDoc::new(m, p, h))
    .collect()
}

async fn load_config(state: &AdminState) -> Result<Vec<(String, String)>, Error> {
    state
        .system
        .config_basic
        .repo()
        .find_by_codes(&CONFIG_KEYS)
        .await
}

fn cfg_get<'a>(rows: &'a [(String, String)], key: &str) -> &'a str {
    rows.iter()
        .find(|(k, _)| k == key)
        .map(|(_, v)| v.as_str())
        .unwrap_or("")
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RegionsQuery {
    provider: Option<String>,
}

async fn regions(Query(q): Query<RegionsQuery>) -> ApiResult<Value> {
    if q.provider.as_deref().unwrap_or("aliyun") != "aliyun" {
        return Err(tigshop_security::web::AppError(Error::biz(400, "图搜厂商尚未接入")));
    }
    ok(json!([
        {"regionId":"cn-shanghai","regionName":"华东2（上海）"},
        {"regionId":"cn-beijing","regionName":"华北2（北京）"},
        {"regionId":"cn-hangzhou","regionName":"华东1（杭州）"},
        {"regionId":"cn-shenzhen","regionName":"华南1（深圳）"}
    ]))
}

async fn detail(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows = load_config(&state).await?;
    let job = sqlx::query(
        "SELECT * FROM image_search_sync_job ORDER BY id DESC LIMIT 1",
    )
    .fetch_optional(&state.repos.pool)
    .await
    .map_err(|e| tigshop_security::web::AppError(Error::Database(e.to_string())))?;
    let build_job = job.map(|r| job_json(&r)).unwrap_or(Value::Null);
    let number = |v: &str| v.parse::<i64>().unwrap_or(0);
    ok(json!({
        "version": number(cfg_get(&rows, "imageSearchVersion")),
        "settings": {
            "enabled": cfg_get(&rows, "imageSearchEnabled") == "1",
            "h5Enabled": cfg_get(&rows, "imageSearchH5Enabled") == "1",
            "pcEnabled": cfg_get(&rows, "imageSearchPcEnabled") == "1",
            "appEnabled": cfg_get(&rows, "imageSearchAppEnabled") == "1",
            "shopEnabled": cfg_get(&rows, "imageSearchShopEnabled") == "1",
            "provider": cfg_get(&rows, "imageSearchProvider"),
            "indexName": cfg_get(&rows, "imageSearchIndexName"),
            "similarityPercent": number(cfg_get(&rows, "imageSearchSimilarityPercent")),
            "maxResults": number(cfg_get(&rows, "imageSearchMaxResults")),
            "aliyun": {
                "regionId": cfg_get(&rows, "imageSearchAliyunRegionId"),
                "accessKeyId": mask(cfg_get(&rows, "imageSearchAliyunAccessKeyId")),
                "accessKeySecret": mask(cfg_get(&rows, "imageSearchAliyunAccessKeySecret")),
                "securityToken": mask(cfg_get(&rows, "imageSearchAliyunSecurityToken")),
                "vpc": cfg_get(&rows, "imageSearchAliyunVpc") == "1",
                "connectTimeoutMillis": number(cfg_get(&rows, "imageSearchAliyunConnectTimeoutMillis")),
                "readTimeoutMillis": number(cfg_get(&rows, "imageSearchAliyunReadTimeoutMillis"))
            }
        },
        "buildJob": build_job
    }))
}

fn mask(value: &str) -> String {
    if value.is_empty() {
        String::new()
    } else if value.len() <= 4 {
        "****".to_string()
    } else {
        format!("****{}", &value[value.len() - 4..])
    }
}

async fn save(
    State(state): State<AdminState>,
    Json(body): Json<Value>,
) -> ApiResult<()> {
    let old_version = load_config(&state)
        .await?
        .iter()
        .find(|(k, _)| k == "imageSearchVersion")
        .and_then(|(_, v)| v.parse::<i64>().ok())
        .unwrap_or(0);
    let version = body.get("version").and_then(Value::as_i64).unwrap_or(-1);
    if version != old_version {
        return Err(tigshop_security::web::AppError(Error::biz(400, "配置已被修改，请刷新后重试")));
    }
    let settings = body.get("settings").cloned().unwrap_or(Value::Null);
    let map = [
        ("imageSearchEnabled", "enabled"),
        ("imageSearchH5Enabled", "h5Enabled"),
        ("imageSearchPcEnabled", "pcEnabled"),
        ("imageSearchAppEnabled", "appEnabled"),
        ("imageSearchShopEnabled", "shopEnabled"),
        ("imageSearchProvider", "provider"),
        ("imageSearchIndexName", "indexName"),
        ("imageSearchSimilarityPercent", "similarityPercent"),
        ("imageSearchMaxResults", "maxResults"),
    ];
    for (code, field) in map {
        if let Some(v) = settings.get(field) {
            let value = if let Some(b) = v.as_bool() {
                if b { "1" } else { "0" }.to_string()
            } else {
                v.to_string().trim_matches('"').to_string()
            };
            state.system.config_basic.repo().save_code(code, &value).await?;
        }
    }
    if let Some(aliyun) = settings.get("aliyun") {
        let keys = [
            ("imageSearchAliyunRegionId", "regionId"),
            ("imageSearchAliyunAccessKeyId", "accessKeyId"),
            ("imageSearchAliyunAccessKeySecret", "accessKeySecret"),
            ("imageSearchAliyunSecurityToken", "securityToken"),
            ("imageSearchAliyunVpc", "vpc"),
            ("imageSearchAliyunConnectTimeoutMillis", "connectTimeoutMillis"),
            ("imageSearchAliyunReadTimeoutMillis", "readTimeoutMillis"),
        ];
        for (code, field) in keys {
            if let Some(v) = aliyun.get(field) {
                let value = if let Some(b) = v.as_bool() {
                    if b { "1" } else { "0" }.to_string()
                } else {
                    v.to_string().trim_matches('"').to_string()
                };
                state.system.config_basic.repo().save_code(code, &value).await?;
            }
        }
    }
    state
        .system
        .config_basic
        .repo()
        .save_code("imageSearchVersion", &(old_version + 1).to_string())
        .await?;
    ok(())
}

async fn check(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows = load_config(&state).await?;
    let configured = !cfg_get(&rows, "imageSearchAliyunAccessKeyId").is_empty()
        && !cfg_get(&rows, "imageSearchAliyunAccessKeySecret").is_empty();
    ok(json!({"reachable": false, "requestId": Value::Null, "configured": configured}))
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct BuildQuery {
    product_id: Option<i32>,
}

async fn build(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows = load_config(&state).await?;
    let job_key = format!("{}:build", cfg_get(&rows, "imageSearchIndexName"));
    let id = insert_job(&state, &job_key, 0).await?;
    ok(json!({"taskId": id, "state": "PENDING"}))
}

async fn sync_product(
    State(state): State<AdminState>,
    Query(q): Query<BuildQuery>,
) -> ApiResult<Value> {
    let product_id = q.product_id.unwrap_or(0);
    let rows = load_config(&state).await?;
    let job_key = format!("{}:product:{}", cfg_get(&rows, "imageSearchIndexName"), product_id);
    let id = insert_job(&state, &job_key, product_id).await?;
    ok(json!({"taskId": id, "state": "PENDING"}))
}

async fn insert_job(state: &AdminState, job_key: &str, product_id: i32) -> Result<i64, Error> {
    let now = tigshop_common::utils::now_seconds() as i64;
    let r = sqlx::query(
        "INSERT INTO image_search_sync_job(job_key,target_key,provider,index_name,kind,product_id,version,state,cursor_id,max_product_id,scanned,added,skipped,deleted,attempts,next_run_at,last_error,created_at,updated_at) \
         VALUES(?, '', 'aliyun', '', 'build', ?, 1, 'PENDING', 0, 0, 0, 0, 0, 0, 0, 0, '', ?, ?) \
         ON DUPLICATE KEY UPDATE version=version+1, state='PENDING', updated_at=VALUES(updated_at)",
    )
    .bind(job_key)
    .bind(product_id)
    .bind(now)
    .bind(now)
    .execute(&state.repos.pool)
    .await
    .map_err(|e| Error::Database(e.to_string()))?;
    Ok(r.last_insert_id() as i64)
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct TaskQuery {
    task_id: Option<i64>,
}

async fn task(State(state): State<AdminState>, Query(q): Query<TaskQuery>) -> ApiResult<Value> {
    let task_id = q.task_id.unwrap_or(0);
    let row = sqlx::query("SELECT * FROM image_search_sync_job WHERE id = ? LIMIT 1")
        .bind(task_id)
        .fetch_optional(&state.repos.pool)
        .await
        .map_err(|e| tigshop_security::web::AppError(Error::Database(e.to_string())))?;
    ok(row.map(|r| job_json(&r)).unwrap_or(Value::Null))
}

async fn retry(
    State(state): State<AdminState>,
    Query(q): Query<TaskQuery>,
) -> ApiResult<()> {
    let task_id = q.task_id.unwrap_or(0);
    sqlx::query("UPDATE image_search_sync_job SET state='PENDING', attempts=attempts+1, last_error='', updated_at=? WHERE id=?")
        .bind(tigshop_common::utils::now_seconds() as i64)
        .bind(task_id)
        .execute(&state.repos.pool)
        .await
        .map_err(|e| tigshop_security::web::AppError(Error::Database(e.to_string())))?;
    ok(())
}

fn job_json(row: &sqlx::mysql::MySqlRow) -> Value {
    json!({
        "taskId": row.try_get::<i64, _>("id").unwrap_or(0),
        "state": row.try_get::<String, _>("state").unwrap_or_default(),
        "scanned": row.try_get::<i64, _>("scanned").unwrap_or(0),
        "added": row.try_get::<i64, _>("added").unwrap_or(0),
        "skipped": row.try_get::<i64, _>("skipped").unwrap_or(0),
        "deleted": row.try_get::<i64, _>("deleted").unwrap_or(0),
        "lastError": row.try_get::<String, _>("last_error").unwrap_or_default()
    })
}

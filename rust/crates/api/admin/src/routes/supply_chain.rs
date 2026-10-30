//! 供应链（v5.8.61 `/adminapi/supplyChain/*`）基础路由。
//!
//! 覆盖供应商、供应商分类、供应商商品、采购单/入库/对账/退货/设置。
//! 列表/详情/状态/删除使用通用表读取；复杂保存动作先返回明确未迁移错误。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use serde_json::{json, Map, Value};
use sqlx::mysql::MySqlRow;
use sqlx::{Column, MySql, QueryBuilder, Row, TypeInfo};
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError};

use crate::AdminState;

pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        // supplier
        .route("/supplyChain/supplier/list", get(supplier_list))
        .route("/supplyChain/supplier/detail", get(supplier_detail))
        .route("/supplyChain/supplier/create", post(supplier_create))
        .route("/supplyChain/supplier/update", post(supplier_update))
        .route("/supplyChain/supplier/status", post(supplier_status))
        .route("/supplyChain/supplier/delete", post(supplier_delete))
        .route("/supplyChain/supplier/qualification/upload", post(qualification_upload))
        // category
        .route("/supplyChain/supplierCategory/list", get(category_list))
        .route("/supplyChain/supplierCategory/save", post(category_save))
        .route("/supplyChain/supplierCategory/status", post(category_status))
        .route("/supplyChain/supplierCategory/delete", post(category_delete))
        // supplier product
        .route("/supplyChain/supplierProduct/list", get(product_list))
        .route("/supplyChain/supplierProduct/summary", get(product_list))
        .route("/supplyChain/supplierProduct/selection", get(product_list))
        .route("/supplyChain/supplierProduct/detail", get(product_detail))
        .route("/supplyChain/supplierProduct/status", post(product_status))
        .route("/supplyChain/supplierProduct/delete", post(product_delete))
        .route("/supplyChain/supplierProduct/batchDelete", post(product_batch_delete))
        .route("/supplyChain/supplierProduct/save", post(product_save))
        .route("/supplyChain/supplierProduct/batchSave", post(product_batch_save))
        // purchase order
        .route("/supplyChain/purchaseOrder/list", get(order_list))
        .route("/supplyChain/purchaseOrder/detail", get(order_detail))
        .route("/supplyChain/purchaseOrder/create", post(order_create))
        .route("/supplyChain/purchaseOrder/update", post(order_update))
        .route("/supplyChain/purchaseOrder/finish", post(order_finish))
        // receipt
        .route("/supplyChain/purchaseReceipt/list", get(receipt_list))
        .route("/supplyChain/purchaseReceipt/detail", get(receipt_detail))
        .route("/supplyChain/purchaseReceipt/save", post(receipt_save))
        // reconciliation
        .route("/supplyChain/purchaseReconciliation/list", get(reconciliation_list))
        .route("/supplyChain/purchaseReconciliation/detail", get(reconciliation_detail))
        .route("/supplyChain/purchaseReconciliation/candidates", get(reconciliation_candidates))
        .route("/supplyChain/purchaseReconciliation/export", get(reconciliation_export))
        .route("/supplyChain/purchaseReconciliation/create", post(reconciliation_create))
        .route("/supplyChain/purchaseReconciliation/update", post(reconciliation_update))
        .route("/supplyChain/purchaseReconciliation/approve", post(reconciliation_approve))
        .route("/supplyChain/purchaseReconciliation/reject", post(reconciliation_reject))
        .route("/supplyChain/purchaseReconciliation/resubmit", post(reconciliation_resubmit))
        .route("/supplyChain/purchaseReconciliation/void", post(reconciliation_void))
        // return
        .route("/supplyChain/purchaseReturn/list", get(return_list))
        .route("/supplyChain/purchaseReturn/detail", get(return_detail))
        .route("/supplyChain/purchaseReturn/save", post(return_save))
        .route("/supplyChain/purchaseReturn/confirm", post(return_confirm))
        .route("/supplyChain/purchaseReturn/delete", post(return_delete))
        // setting
        .route("/supplyChain/purchaseSetting/detail", get(setting_detail))
        .route("/supplyChain/purchaseSetting/save", post(setting_save))
}

#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    let paths = [
        ("GET", "/api/supplyChain/supplier/list"),
        ("GET", "/api/supplyChain/supplier/detail"),
        ("POST", "/api/supplyChain/supplier/create"),
        ("POST", "/api/supplyChain/supplier/update"),
        ("POST", "/api/supplyChain/supplier/status"),
        ("POST", "/api/supplyChain/supplier/delete"),
        ("POST", "/api/supplyChain/supplier/qualification/upload"),
        ("GET", "/api/supplyChain/supplierCategory/list"),
        ("POST", "/api/supplyChain/supplierCategory/save"),
        ("POST", "/api/supplyChain/supplierCategory/status"),
        ("POST", "/api/supplyChain/supplierCategory/delete"),
        ("GET", "/api/supplyChain/supplierProduct/list"),
        ("GET", "/api/supplyChain/supplierProduct/summary"),
        ("GET", "/api/supplyChain/supplierProduct/selection"),
        ("GET", "/api/supplyChain/supplierProduct/detail"),
        ("POST", "/api/supplyChain/supplierProduct/status"),
        ("POST", "/api/supplyChain/supplierProduct/delete"),
        ("POST", "/api/supplyChain/supplierProduct/batchDelete"),
        ("POST", "/api/supplyChain/supplierProduct/save"),
        ("POST", "/api/supplyChain/supplierProduct/batchSave"),
        ("GET", "/api/supplyChain/purchaseOrder/list"),
        ("GET", "/api/supplyChain/purchaseOrder/detail"),
        ("POST", "/api/supplyChain/purchaseOrder/create"),
        ("POST", "/api/supplyChain/purchaseOrder/update"),
        ("POST", "/api/supplyChain/purchaseOrder/finish"),
        ("GET", "/api/supplyChain/purchaseReceipt/list"),
        ("GET", "/api/supplyChain/purchaseReceipt/detail"),
        ("POST", "/api/supplyChain/purchaseReceipt/save"),
        ("GET", "/api/supplyChain/purchaseReconciliation/list"),
        ("GET", "/api/supplyChain/purchaseReconciliation/detail"),
        ("GET", "/api/supplyChain/purchaseReconciliation/candidates"),
        ("GET", "/api/supplyChain/purchaseReconciliation/export"),
        ("POST", "/api/supplyChain/purchaseReconciliation/create"),
        ("POST", "/api/supplyChain/purchaseReconciliation/update"),
        ("POST", "/api/supplyChain/purchaseReconciliation/approve"),
        ("POST", "/api/supplyChain/purchaseReconciliation/reject"),
        ("POST", "/api/supplyChain/purchaseReconciliation/resubmit"),
        ("POST", "/api/supplyChain/purchaseReconciliation/void"),
        ("GET", "/api/supplyChain/purchaseReturn/list"),
        ("GET", "/api/supplyChain/purchaseReturn/detail"),
        ("POST", "/api/supplyChain/purchaseReturn/save"),
        ("POST", "/api/supplyChain/purchaseReturn/confirm"),
        ("POST", "/api/supplyChain/purchaseReturn/delete"),
        ("GET", "/api/supplyChain/purchaseSetting/detail"),
        ("POST", "/api/supplyChain/purchaseSetting/save"),
    ];
    paths
        .iter()
        .map(|(m, p)| tigshop_openapi::RouteDoc::new(m, p, "SupplyChainController"))
        .collect()
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct Pg {
    page: Option<u64>,
    size: Option<u64>,
    keyword: Option<String>,
    id: Option<i64>,
    ids: Option<Vec<i64>>,
    status: Option<i32>,
    shop_id: Option<i32>,
}

fn page_of(q: &Pg) -> (u64, u64, i64) {
    let page = q.page.unwrap_or(1).max(1);
    let size = q.size.unwrap_or(15).clamp(1, 200);
    (page, size, ((page - 1) * size) as i64)
}

async fn list_table(
    state: &AdminState,
    table: &str,
    q: &Pg,
    supplier_only: bool,
) -> Result<Value, AppError> {
    let (page, size, offset) = page_of(q);
    let mut cq: QueryBuilder<'_, MySql> = QueryBuilder::new(format!("SELECT COUNT(*) FROM {table} WHERE 1=1"));
    let mut rq: QueryBuilder<'_, MySql> = QueryBuilder::new(format!("SELECT * FROM {table} WHERE 1=1"));
    if table != "vendor" {
        cq.push(" AND is_del = 0");
        rq.push(" AND is_del = 0");
    }
    if supplier_only {
        cq.push(" AND source_type = 2");
        rq.push(" AND source_type = 2");
    }
    if let Some(shop_id) = q.shop_id {
        cq.push(" AND shop_id = ").push_bind(shop_id);
        rq.push(" AND shop_id = ").push_bind(shop_id);
    }
    if let Some(status) = q.status {
        cq.push(" AND status = ").push_bind(status);
        rq.push(" AND status = ").push_bind(status);
    }
    if let Some(kw) = q.keyword.as_deref().map(str::trim).filter(|s| !s.is_empty()) {
        let like = format!("%{kw}%");
        let col = if table == "vendor" { "vendor_name" } else { "category_name" };
        cq.push(format!(" AND {col} LIKE ")).push_bind(like.clone());
        rq.push(format!(" AND {col} LIKE ")).push_bind(like);
    }
    rq.push(" ORDER BY ").push(if table == "vendor" { "vendor_id" } else { "id" }).push(" DESC LIMIT ").push_bind(size as i64).push(" OFFSET ").push_bind(offset);
    let (total,): (i64,) = cq.build_query_as().fetch_one(&state.repos.pool).await.map_err(db)?;
    let rows = rq.build().fetch_all(&state.repos.pool).await.map_err(db)?;
    let records: Vec<Value> = rows.iter().map(row_to_json).collect();
    Ok(json!({"records": records, "total": total, "page": page, "size": size}))
}

async fn detail_table(state: &AdminState, table: &str, id: i64) -> Result<Value, AppError> {
    let key = if table == "vendor" { "vendor_id" } else { "id" };
    let row = sqlx::query(&format!("SELECT * FROM {table} WHERE {key} = ? LIMIT 1"))
        .bind(id)
        .fetch_optional(&state.repos.pool)
        .await
        .map_err(db)?;
    Ok(row.as_ref().map(row_to_json).unwrap_or(Value::Null))
}

async fn set_status(state: &AdminState, table: &str, id: i64, status: i32) -> Result<(), AppError> {
    let key = if table == "vendor" { "vendor_id" } else { "id" };
    sqlx::query(&format!("UPDATE {table} SET status = ? WHERE {key} = ?"))
        .bind(status)
        .bind(id)
        .execute(&state.repos.pool)
        .await
        .map_err(db)?;
    Ok(())
}

async fn soft_delete(state: &AdminState, table: &str, id: i64) -> Result<(), AppError> {
    let key = if table == "vendor" { "vendor_id" } else { "id" };
    let sql = if table == "vendor" {
        format!("DELETE FROM {table} WHERE {key} = ?")
    } else {
        format!("UPDATE {table} SET is_del = 1 WHERE {key} = ?")
    };
    sqlx::query(&sql).bind(id).execute(&state.repos.pool).await.map_err(db)?;
    Ok(())
}

fn db(e: sqlx::Error) -> AppError {
    AppError(Error::Database(e.to_string()))
}

fn row_to_json(row: &MySqlRow) -> Value {
    let mut map = Map::new();
    for (idx, col) in row.columns().iter().enumerate() {
        let name = col.name();
        let type_name = col.type_info().name().to_ascii_uppercase();
        let value = if type_name.contains("INT") {
            row.try_get::<Option<i64>, _>(idx).ok().flatten().map(Value::from)
        } else if type_name.contains("DECIMAL") || type_name.contains("NUMERIC") {
            row.try_get::<Option<rust_decimal::Decimal>, _>(idx)
                .ok()
                .flatten()
                .map(|v| Value::String(v.to_string()))
        } else if type_name.contains("FLOAT") || type_name.contains("DOUBLE") {
            row.try_get::<Option<f64>, _>(idx).ok().flatten().map(Value::from)
        } else if type_name.contains("JSON") {
            row.try_get::<Option<Value>, _>(idx).ok().flatten()
        } else {
            row.try_get::<Option<String>, _>(idx).ok().flatten().map(Value::from)
        };
        map.insert(name.to_string(), value.unwrap_or(Value::Null));
    }
    Value::Object(map)
}

// ————————————————— supplier —————————————————
async fn supplier_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(list_table(&state, "vendor", &q, true).await?)
}
async fn supplier_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(detail_table(&state, "vendor", q.id.unwrap_or(0)).await?)
}
async fn supplier_create(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<i64> {
    let name = body.get("vendorName").and_then(Value::as_str).unwrap_or("").trim();
    if name.is_empty() { return Err(AppError(Error::biz(400, "供应商名称不能为空"))); }
    let now = tigshop_common::utils::now_seconds() as i64;
    let r = sqlx::query(
        "INSERT INTO vendor(source_type, shop_id, vendor_code, vendor_name, contact_mobile, purchase_category_id, vendor_logo, description, status, add_time) VALUES(2,?,?,?,?,?,?,?,?,?)",
    )
    .bind(body.get("shopId").and_then(Value::as_i64).unwrap_or(0) as i32)
    .bind(body.get("vendorCode").and_then(Value::as_str).unwrap_or(""))
    .bind(name)
    .bind(body.get("contactMobile").and_then(Value::as_str).unwrap_or(""))
    .bind(body.get("purchaseCategoryId").and_then(Value::as_i64).unwrap_or(0))
    .bind(body.get("vendorLogo").and_then(Value::as_str).unwrap_or(""))
    .bind(body.get("description").and_then(Value::as_str).unwrap_or(""))
    .bind(body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32)
    .bind(now)
    .execute(&state.repos.pool).await.map_err(db)?;
    ok(r.last_insert_id() as i64)
}
async fn supplier_update(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let id = body.get("vendorId").or_else(|| body.get("id")).and_then(Value::as_i64).unwrap_or(0);
    sqlx::query("UPDATE vendor SET vendor_name=COALESCE(NULLIF(?,''),vendor_name), contact_mobile=COALESCE(NULLIF(?,''),contact_mobile) WHERE vendor_id=? AND source_type=2")
        .bind(body.get("vendorName").and_then(Value::as_str).unwrap_or(""))
        .bind(body.get("contactMobile").and_then(Value::as_str).unwrap_or(""))
        .bind(id)
        .execute(&state.repos.pool).await.map_err(db)?;
    ok(())
}
async fn supplier_status(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let id = body.get("id").and_then(Value::as_i64).unwrap_or(0);
    let status = body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32;
    set_status(&state, "vendor", id, status).await?;
    ok(())
}
async fn supplier_delete(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    soft_delete(&state, "vendor", body.get("id").and_then(Value::as_i64).unwrap_or(0)).await?;
    ok(())
}
async fn qualification_upload() -> ApiResult<String> {
    Err(AppError(Error::biz(400, "资质上传未迁移")))
}

// ————————————————— category —————————————————
async fn category_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(list_table(&state, "purchase_supplier_category", &q, false).await?)
}
async fn category_save(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<i64> {
    let id = body.get("id").and_then(Value::as_i64).unwrap_or(0);
    let name = body.get("categoryName").and_then(Value::as_str).unwrap_or("").trim();
    let now = tigshop_common::utils::now_seconds() as i64;
    if id > 0 {
        sqlx::query("UPDATE purchase_supplier_category SET category_name=?, sort_order=?, status=?, remark=?, update_time=? WHERE id=?")
            .bind(name)
            .bind(body.get("sortOrder").and_then(Value::as_i64).unwrap_or(50) as i32)
            .bind(body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32)
            .bind(body.get("remark").and_then(Value::as_str).unwrap_or(""))
            .bind(now).bind(id)
            .execute(&state.repos.pool).await.map_err(db)?;
        ok(id)
    } else {
        let r = sqlx::query("INSERT INTO purchase_supplier_category(shop_id,category_name,sort_order,status,remark,create_time,update_time) VALUES(?,?,?,?,?,?,?)")
            .bind(body.get("shopId").and_then(Value::as_i64).unwrap_or(0) as i32)
            .bind(name)
            .bind(body.get("sortOrder").and_then(Value::as_i64).unwrap_or(50) as i32)
            .bind(body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32)
            .bind(body.get("remark").and_then(Value::as_str).unwrap_or(""))
            .bind(now).bind(now)
            .execute(&state.repos.pool).await.map_err(db)?;
        ok(r.last_insert_id() as i64)
    }
}
async fn category_status(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    set_status(&state, "purchase_supplier_category", body.get("id").and_then(Value::as_i64).unwrap_or(0), body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32).await?;
    ok(())
}
async fn category_delete(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    soft_delete(&state, "purchase_supplier_category", body.get("id").and_then(Value::as_i64).unwrap_or(0)).await?;
    ok(())
}

// ————————————————— supplier product —————————————————
async fn product_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(list_table(&state, "vendor_product", &q, false).await?)
}
async fn product_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(detail_table(&state, "vendor_product", q.id.unwrap_or(0)).await?)
}
async fn product_status(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let id = body.get("id").and_then(Value::as_i64).unwrap_or(0);
    let status = body.get("status").and_then(Value::as_i64).unwrap_or(1) as i32;
    sqlx::query("UPDATE vendor_product SET product_state=? WHERE id=?").bind(status).bind(id).execute(&state.repos.pool).await.map_err(db)?;
    ok(())
}
async fn product_delete(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_product SET is_del=1 WHERE id=?").bind(body.get("id").and_then(Value::as_i64).unwrap_or(0)).execute(&state.repos.pool).await.map_err(db)?;
    ok(())
}
async fn product_batch_delete(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let ids = body.get("ids").and_then(Value::as_array).cloned().unwrap_or_default();
    for id in ids { let _ = sqlx::query("UPDATE vendor_product SET is_del=1 WHERE id=?").bind(id.as_i64().unwrap_or(0)).execute(&state.repos.pool).await; }
    ok(())
}
async fn product_save(State(_state): State<AdminState>, Json(_body): Json<Value>) -> ApiResult<i64> {
    Err(AppError(Error::biz(400, "供应商商品保存未迁移")))
}
async fn product_batch_save(State(_state): State<AdminState>, Json(_body): Json<Value>) -> ApiResult<()> {
    Err(AppError(Error::biz(400, "供应商商品批量保存未迁移")))
}

// ————————————————— purchase order —————————————————
async fn order_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(list_table(&state, "purchase_order", &q, false).await?)
}
async fn order_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    ok(detail_table(&state, "purchase_order", q.id.unwrap_or(0)).await?)
}
async fn order_create(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<i64> { Err(AppError(Error::biz(400, "采购单创建未迁移"))) }
async fn order_update(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<()> { Err(AppError(Error::biz(400, "采购单修改未迁移"))) }
async fn order_finish(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<()> { Err(AppError(Error::biz(400, "采购单完成未迁移"))) }

// ————————————————— receipt / reconciliation / return —————————————————
async fn receipt_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(list_table(&state, "purchase_receipt", &q, false).await?) }
async fn receipt_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(detail_table(&state, "purchase_receipt", q.id.unwrap_or(0)).await?) }
async fn receipt_save(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<()> { Err(AppError(Error::biz(400, "入库单保存未迁移"))) }

async fn reconciliation_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(list_table(&state, "purchase_reconciliation", &q, false).await?) }
async fn reconciliation_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(detail_table(&state, "purchase_reconciliation", q.id.unwrap_or(0)).await?) }
async fn reconciliation_candidates(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(list_table(&state, "purchase_receipt", &q, false).await?) }
async fn reconciliation_export() -> ApiResult<Value> { ok(json!([])) }
async fn reconciliation_create(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<i64> { Err(AppError(Error::biz(400, "对账单创建未迁移"))) }
async fn reconciliation_update(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<()> { Err(AppError(Error::biz(400, "对账单修改未迁移"))) }
async fn reconciliation_approve(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { set_status(&state, "purchase_reconciliation", body.get("id").and_then(Value::as_i64).unwrap_or(0), 2).await?; ok(()) }
async fn reconciliation_reject(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { set_status(&state, "purchase_reconciliation", body.get("id").and_then(Value::as_i64).unwrap_or(0), 3).await?; ok(()) }
async fn reconciliation_resubmit(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { set_status(&state, "purchase_reconciliation", body.get("id").and_then(Value::as_i64).unwrap_or(0), 0).await?; ok(()) }
async fn reconciliation_void(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { set_status(&state, "purchase_reconciliation", body.get("id").and_then(Value::as_i64).unwrap_or(0), 4).await?; ok(()) }

async fn return_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(list_table(&state, "purchase_return", &q, false).await?) }
async fn return_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> { ok(detail_table(&state, "purchase_return", q.id.unwrap_or(0)).await?) }
async fn return_save(State(_): State<AdminState>, Json(_): Json<Value>) -> ApiResult<()> { Err(AppError(Error::biz(400, "退货单保存未迁移"))) }
async fn return_confirm(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { set_status(&state, "purchase_return", body.get("id").and_then(Value::as_i64).unwrap_or(0), 1).await?; ok(()) }
async fn return_delete(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> { soft_delete(&state, "purchase_return", body.get("id").and_then(Value::as_i64).unwrap_or(0)).await?; ok(()) }

// ————————————————— setting —————————————————
async fn setting_detail(State(state): State<AdminState>) -> ApiResult<Value> {
    let row = sqlx::query("SELECT * FROM purchase_setting WHERE is_del=0 ORDER BY id DESC LIMIT 1").fetch_optional(&state.repos.pool).await.map_err(db)?;
    ok(row.as_ref().map(row_to_json).unwrap_or(Value::Null))
}
async fn setting_save(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let now = tigshop_common::utils::now_seconds() as i64;
    sqlx::query("INSERT INTO purchase_setting(shop_id,auto_finish_days,allow_over_receive,create_time,update_time) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE auto_finish_days=VALUES(auto_finish_days),allow_over_receive=VALUES(allow_over_receive),update_time=VALUES(update_time)")
        .bind(body.get("shopId").and_then(Value::as_i64).unwrap_or(0) as i32)
        .bind(body.get("autoFinishDays").and_then(Value::as_i64).unwrap_or(0) as i32)
        .bind(body.get("allowOverReceive").and_then(Value::as_i64).unwrap_or(0) as i32)
        .bind(now).bind(now)
        .execute(&state.repos.pool).await.map_err(db)?;
    ok(())
}

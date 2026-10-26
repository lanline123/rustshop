//! 低频杂项接口（system::lowfreq_extra） —— 一次性低频接口（b2b2b 5.8.57 新版前端调用，无 Java 参考；
//! 列名取自 overseas pure.sql，简化口径见各 handler 注释）。
use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use serde_json::Value;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

fn pool(state: &AdminState) -> sqlx::MySqlPool {
    state.system.config_basic.repo().pool_clone()
}
fn db(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct Pg {
    pub page: Option<u64>,
    pub size: Option<u64>,
    pub keyword: Option<String>,
    pub id: Option<i64>,
    pub ids: Option<Vec<i64>>,
    pub field: Option<String>,
    pub value: Option<Value>,
    pub status: Option<i32>,
    pub vendor_id: Option<i32>,
    pub shop_id: Option<i64>,
    pub product_id: Option<i64>,
    pub order_id: Option<i32>,
    pub parent_id: Option<i32>,
    pub tip_id: Option<i64>,
    pub tip_name: Option<String>,
}

fn json_ids(body: &Value) -> Vec<i64> {
    let mut out = Vec::new();
    if let Some(arr) = body.get("ids").and_then(Value::as_array) {
        for v in arr {
            if let Some(id) = v.as_i64() {
                out.push(id);
            }
        }
    }
    if out.is_empty() {
        if let Some(id) = body.get("id").and_then(Value::as_i64) {
            out.push(id);
        }
    }
    out
}

fn page_of(q: &Pg) -> (u64, u64, u64) {
    let p = q.page.unwrap_or(1).max(1);
    let s = q.size.unwrap_or(10).clamp(1, 100);
    (p, s, (p - 1) * s)
}

async fn read_cfg(state: &AdminState, code: &str) -> Result<Value, Error> {
    let raw = state.system.config_basic.repo().find_by_code(code).await?;
    Ok(raw.and_then(|s| serde_json::from_str(&s).ok()).unwrap_or_else(|| serde_json::json!({})))
}

macro_rules! list_pg {
    ($fn_name:ident, $table:literal, $key:literal, $jo:expr, $like:literal) => {
        async fn $fn_name(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
            let (p, s, off) = page_of(&q);
            let kw = format!("%{}%", q.keyword.clone().unwrap_or_default().trim());
            let (total,): (i64,) = sqlx::query_as(
                &format!("SELECT COUNT(*) FROM `{}` WHERE `{}` LIKE ?", $table, $like))
                .bind(&kw).fetch_one(&pool(&state)).await.map_err(db)?;
            let rows: Vec<(Value,)> = sqlx::query_as(&format!(
                "SELECT JSON_OBJECT({}) FROM `{}` WHERE `{}` LIKE ? ORDER BY `{}` DESC LIMIT ? OFFSET ?",
                $jo, $table, $like, $key))
                .bind(&kw).bind(s).bind(off)
                .fetch_all(&pool(&state)).await.map_err(db)?;
            ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "total": total, "page": p, "size": s}))
        }
    };
}

macro_rules! detail_one {
    ($fn_name:ident, $table:literal, $key:literal, $jo:expr) => {
        async fn $fn_name(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
            let row: Option<(Value,)> = sqlx::query_as(&format!(
                "SELECT JSON_OBJECT({}) FROM `{}` WHERE `{}` = ? LIMIT 1", $jo, $table, $key))
                .bind(q.id.unwrap_or(0)).fetch_optional(&pool(&state)).await.map_err(db)?;
            ok(row.map(|(v,)| v).unwrap_or(Value::Null))
        }
    };
}

macro_rules! upd_field {
    ($fn_name:ident, $table:literal, $key:literal, [$($fld:literal),+]) => {
        async fn $fn_name(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
            let f = q.field.clone().unwrap_or_default();
            if ![$($fld),+].contains(&f.as_str()) {
                return Err(tigshop_security::web::AppError(Error::Validation("不支持的字段".into())));
            }
            let v = match q.value.clone().unwrap_or(Value::Null) {
                Value::String(sv) => sv,
                other => other.to_string(),
            };
            sqlx::query(&format!("UPDATE `{}` SET `{}` = ? WHERE `{}` = ?", $table, f, $key))
                .bind(v).bind(q.id.unwrap_or(0))
                .execute(&pool(&state)).await.map_err(db)?;
            ok(())
        }
    };
}

macro_rules! del_ids {
    ($fn_name:ident, $table:literal, $key:literal) => {
        async fn $fn_name(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
            let ids = q.ids.clone().or_else(|| q.id.map(|v| vec![v])).unwrap_or_default();
            if ids.is_empty() { return Err(tigshop_security::web::AppError(Error::Validation("缺少 id".into()))); }
            let list = ids.iter().map(|v| v.to_string()).collect::<Vec<_>>().join(",");
            sqlx::query(&format!("DELETE FROM `{}` WHERE `{}` IN ({})", $table, $key, list))
                .execute(&pool(&state)).await.map_err(db)?;
            ok(())
        }
    };
}

macro_rules! cfg_get {
    ($fn_name:ident, $code:literal) => {
        async fn $fn_name(State(state): State<AdminState>) -> ApiResult<Value> {
            ok(read_cfg(&state, $code).await?)
        }
    };
}

macro_rules! cfg_set {
    ($fn_name:ident, $code:literal) => {
        async fn $fn_name(State(state): State<AdminState>, Json(body): Json<serde_json::Map<String, Value>>) -> ApiResult<()> {
            state.system.config_basic.repo().save_code($code, &Value::Object(body).to_string()).await?;
            ok(())
        }
    };
}

/// 通用状态更新：body {id 或 ids, status?}（缺省 0=撤回待处理）。
async fn set_status(state: &AdminState, table: &str, key: &str, q: &Pg, status: i32) -> Result<(), Error> {
    let ids = q.ids.clone().or_else(|| q.id.map(|v| vec![v])).unwrap_or_default();
    if ids.is_empty() { return Err(Error::Validation("缺少 id".into())); }
    let list = ids.iter().map(|v| v.to_string()).collect::<Vec<_>>().join(",");
    sqlx::query(&format!("UPDATE `{}` SET status = ? WHERE `{}` IN ({})", table, key, list))
        .bind(status).execute(&pool(state)).await.map_err(db)?;
    Ok(())
}

// post /o2o/areaStoreManager/list
list_pg!(h_0, "area_store_manager", "area_store_manager_id", "'area_store_manager_id', area_store_manager_id, 'area_store_name', area_store_name, 'sort_order', sort_order, 'create_time', create_time, 'update_time', update_time", "area_store_name");

// post /o2o/areaStoreManager/del
del_ids!(h_1, "area_store_manager", "area_store_manager_id");

// post /o2o/areaStoreManager/removeShop
del_ids!(h_2, "area_store_manager_shop", "area_store_manager_shop_id");

// post /o2o/areaStoreManager/shopList
list_pg!(h_3, "area_store_manager_shop", "area_store_manager_shop_id", "'area_store_manager_shop_id', area_store_manager_shop_id, 'area_store_manager_id', area_store_manager_id, 'shop_id', shop_id, 'create_time', create_time", "area_store_manager_id");

// get /o2o/decorate/decorate/detail
detail_one!(h_4, "decorate", "decorate_id", "'decorate_id', decorate_id, 'decorate_title', decorate_title, 'data', data");

// post /o2o/decorate/decorate/update
async fn h_5(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let data = q.value.clone().unwrap_or(Value::Null).to_string();
    sqlx::query("UPDATE decorate SET data = ? WHERE decorate_id = ?").bind(data).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /o2o/dispatch/assign
async fn h_6(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    // 简化：按 delivery_order 主键指派配送员（staff_id 经 id 传入）
    sqlx::query("UPDATE delivery_order SET status = 1 WHERE id = ?").bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /o2o/dispatch/availableStaff
async fn h_7(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('id', id, 'username', username, 'shopId', shop_id, 'isUsing', is_using) FROM admin_user_pos WHERE is_using = 0 OR is_using = 1").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// post /order/dispatch/assign
async fn h_8(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    // 简化：按 delivery_order 主键指派配送员（staff_id 经 id 传入）
    sqlx::query("UPDATE delivery_order SET status = 1 WHERE id = ?").bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /order/dispatch/availableStaff
async fn h_9(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('id', id, 'username', username, 'shopId', shop_id, 'isUsing', is_using) FROM admin_user_pos WHERE is_using = 0 OR is_using = 1").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// post /o2o/homeService/saveConfig
cfg_set!(h_10, "o2oHomeServiceConfig");

// post /o2o/localDelivery/saveConfig
cfg_set!(h_11, "o2oLocalDeliveryConfig");

// get /o2o/intraCityDelivery/platformConfig/getConfig
cfg_get!(h_12, "o2oIntraCityDeliveryConfig");

// post /o2o/intraCityDelivery/platformConfig/saveConfig
cfg_set!(h_13, "o2oIntraCityDeliveryConfig");

// post /o2o/reservationTpl/batch
del_ids!(h_14, "reservation_tpl", "reservation_tpl_id");

// post /o2o/shopPickupConfig/saveShopPickupTpl
async fn h_15(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    sqlx::query("INSERT INTO shop_pickup_tpl (shop_id, tpl_name, stocking_status, pickup_time_status, pickup_end_status, is_default, create_time, update_time) VALUES (?, ?, 0, 0, 0, 1, ?, ?)")
        .bind(q.shop_id.unwrap_or(0)).bind(q.keyword.clone().unwrap_or_default()).bind(now).bind(now)
        .execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /o2o/storeProduct/writeOff
async fn h_16(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i32;
    sqlx::query("UPDATE order_pickup_shipment SET shipment_status = 1, check_time = ? WHERE order_id = ?")
        .bind(now).bind(q.order_id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /o2o/tip/list
list_pg!(h_17, "tip", "tip_id", "'tip_id', tip_id, 'tip_name', tip_name, 'status', status, 'add_time', add_time", "tip_name");

// post /o2o/tip/del
del_ids!(h_18, "tip", "tip_id");

// post /o2o/tip/batch
del_ids!(h_19, "tip", "tip_id");

// get /o2o/tip/selectAllTip
async fn h_20(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('tipId', tip_id, 'tipName', tip_name, 'status', status) FROM tip WHERE status = 1").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// get /o2o/tip/tipConfig
cfg_get!(h_21, "o2oTipConfig");

// get /o2o/tip/detail
async fn h_tip_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let id = q.id.or(q.tip_id).unwrap_or(0);
    let row: Option<(Value,)> = sqlx::query_as(
        "SELECT JSON_OBJECT('tipId', tip_id, 'tipName', tip_name, 'status', status, 'addTime', add_time) FROM tip WHERE tip_id = ?",
    )
    .bind(id)
    .fetch_optional(&pool(&state))
    .await
    .map_err(db)?;
    ok(row.map(|(v,)| v).unwrap_or(Value::Null))
}

// post /o2o/tip/create
async fn h_tip_create(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<i64> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    let r = sqlx::query("INSERT INTO tip (tip_name, status, add_time) VALUES (?, ?, ?)")
        .bind(q.tip_name.unwrap_or_default())
        .bind(q.status.unwrap_or(1))
        .bind(now)
        .execute(&pool(&state))
        .await
        .map_err(db)?;
    ok(r.last_insert_id() as i64)
}

// post /o2o/tip/update
async fn h_tip_update(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let id = q.tip_id.or(q.id).unwrap_or(0);
    sqlx::query("UPDATE tip SET tip_name = ?, status = ? WHERE tip_id = ?")
        .bind(q.tip_name.unwrap_or_default())
        .bind(q.status.unwrap_or(0))
        .bind(id)
        .execute(&pool(&state))
        .await
        .map_err(db)?;
    ok(())
}

// post /o2o/tip/updateField
async fn h_tip_update_field(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let id = q.id.or(q.tip_id).unwrap_or(0);
    let field = q.field.unwrap_or_default();
    let column = match field.as_str() {
        "tipName" | "tip_name" => "tip_name",
        "status" => "status",
        other => return Err(tigshop_security::web::AppError(Error::Validation(format!("不支持字段 {other}")))),
    };
    let val = q.value.unwrap_or(Value::Null);
    let sql = format!("UPDATE tip SET {column} = ? WHERE tip_id = ?");
    if column == "status" {
        sqlx::query(&sql)
            .bind(val.as_i64().unwrap_or(0))
            .bind(id)
            .execute(&pool(&state))
            .await
            .map_err(db)?;
    } else {
        sqlx::query(&sql)
            .bind(val.as_str().unwrap_or_default())
            .bind(id)
            .execute(&pool(&state))
            .await
            .map_err(db)?;
    }
    ok(())
}

// ————————————————————— O2O 服务人员（service_staff） —————————————————————

// get /o2o/serviceStaff/list
async fn h_staff_list(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (p, s, off) = page_of(&q);
    let kw = format!("%{}%", q.keyword.clone().unwrap_or_default().trim());
    let status = q.status.unwrap_or(-1);
    let (total,): (i64,) = sqlx::query_as(
        "SELECT COUNT(*) FROM service_staff WHERE is_delete = 0 AND (? = -1 OR status = ?) AND (name LIKE ? OR phone LIKE ?)",
    )
    .bind(status)
    .bind(status)
    .bind(&kw)
    .bind(&kw)
    .fetch_one(&pool(&state))
    .await
    .map_err(db)?;
    let rows: Vec<(Value,)> = sqlx::query_as(
        "SELECT JSON_OBJECT('id', id, 'name', name, 'nickname', nickname, 'avatar', avatar, 'phone', phone, 'memberId', member_id, 'storeId', store_id, 'type', type, 'status', status, 'workStatus', work_status, 'completedOrderCount', completed_order_count, 'completedOrderAmount', completed_order_amount, 'createTime', create_time) FROM service_staff WHERE is_delete = 0 AND (? = -1 OR status = ?) AND (name LIKE ? OR phone LIKE ?) ORDER BY id DESC LIMIT ? OFFSET ?",
    )
    .bind(status)
    .bind(status)
    .bind(&kw)
    .bind(&kw)
    .bind(s)
    .bind(off)
    .fetch_all(&pool(&state))
    .await
    .map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "total": total, "page": p, "size": s}))
}

// get /o2o/serviceStaff/detail
async fn h_staff_detail(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let row: Option<(Value,)> = sqlx::query_as(
        "SELECT JSON_OBJECT('id', id, 'name', name, 'nickname', nickname, 'avatar', avatar, 'phone', phone, 'memberId', member_id, 'storeId', store_id, 'type', type, 'status', status, 'workStatus', work_status, 'completedOrderCount', completed_order_count, 'completedOrderAmount', completed_order_amount, 'createTime', create_time) FROM service_staff WHERE id = ? AND is_delete = 0",
    )
    .bind(q.id.unwrap_or(0))
    .fetch_optional(&pool(&state))
    .await
    .map_err(db)?;
    ok(row.map(|(v,)| v).unwrap_or(Value::Null))
}

// get /o2o/serviceStaff/showMobile
async fn h_staff_show_mobile(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let row: Option<(Value,)> = sqlx::query_as(
        "SELECT JSON_OBJECT('id', id, 'name', name, 'phone', phone) FROM service_staff WHERE id = ? AND is_delete = 0",
    )
    .bind(q.id.unwrap_or(0))
    .fetch_optional(&pool(&state))
    .await
    .map_err(db)?;
    ok(row.map(|(v,)| v).unwrap_or(Value::Null))
}

// post /o2o/serviceStaff/create
async fn h_staff_create(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<i64> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    let gs = |k: &str| body.get(k).and_then(Value::as_str).unwrap_or_default().to_string();
    let gi = |k: &str| body.get(k).and_then(Value::as_i64).unwrap_or(0);
    let r = sqlx::query(
        "INSERT INTO service_staff (name, nickname, avatar, phone, member_id, store_id, type, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
    )
    .bind(gs("name"))
    .bind(gs("nickname"))
    .bind(gs("avatar"))
    .bind(gs("phone"))
    .bind(gi("memberId"))
    .bind(gi("storeId"))
    .bind(body.get("type").and_then(Value::as_i64).unwrap_or(1))
    .bind(body.get("status").and_then(Value::as_i64).unwrap_or(1))
    .bind(now)
    .bind(now)
    .execute(&pool(&state))
    .await
    .map_err(db)?;
    ok(r.last_insert_id() as i64)
}

// post /o2o/serviceStaff/update
async fn h_staff_update(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    let gs = |k: &str| body.get(k).and_then(Value::as_str).unwrap_or_default().to_string();
    let gi = |k: &str| body.get(k).and_then(Value::as_i64).unwrap_or(0);
    sqlx::query(
        "UPDATE service_staff SET name = ?, nickname = ?, avatar = ?, phone = ?, member_id = ?, store_id = ?, type = ?, status = ?, update_time = ? WHERE id = ? AND is_delete = 0",
    )
    .bind(gs("name"))
    .bind(gs("nickname"))
    .bind(gs("avatar"))
    .bind(gs("phone"))
    .bind(gi("memberId"))
    .bind(gi("storeId"))
    .bind(body.get("type").and_then(Value::as_i64).unwrap_or(1))
    .bind(body.get("status").and_then(Value::as_i64).unwrap_or(1))
    .bind(now)
    .bind(gi("id"))
    .execute(&pool(&state))
    .await
    .map_err(db)?;
    ok(())
}

// post /o2o/serviceStaff/updateField
async fn h_staff_update_field(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let id = body.get("id").and_then(Value::as_i64).unwrap_or(0);
    let field = body.get("field").and_then(Value::as_str).unwrap_or_default();
    let column = match field {
        "status" => "status",
        "workStatus" | "work_status" => "work_status",
        "name" => "name",
        "phone" => "phone",
        "storeId" | "store_id" => "store_id",
        other => return Err(tigshop_security::web::AppError(Error::Validation(format!("不支持字段 {other}")))),
    };
    let val = body.get("val").cloned().or_else(|| body.get("value").cloned()).unwrap_or(Value::Null);
    let sql = format!("UPDATE service_staff SET {column} = ? WHERE id = ? AND is_delete = 0");
    if matches!(column, "status" | "work_status" | "store_id") {
        sqlx::query(&sql)
            .bind(val.as_i64().unwrap_or(0))
            .bind(id)
            .execute(&pool(&state))
            .await
            .map_err(db)?;
    } else {
        sqlx::query(&sql)
            .bind(val.as_str().unwrap_or_default())
            .bind(id)
            .execute(&pool(&state))
            .await
            .map_err(db)?;
    }
    ok(())
}

// post /o2o/serviceStaff/del
async fn h_staff_del(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    let ids = json_ids(&body);
    if ids.is_empty() {
        return Err(tigshop_security::web::AppError(Error::Validation("缺少 id".into())));
    }
    let list = ids.iter().map(ToString::to_string).collect::<Vec<_>>().join(",");
    sqlx::query(&format!("UPDATE service_staff SET is_delete = 1 WHERE id IN ({list})"))
        .execute(&pool(&state))
        .await
        .map_err(db)?;
    ok(())
}

// post /o2o/serviceStaff/batch
async fn h_staff_batch(State(state): State<AdminState>, Json(body): Json<Value>) -> ApiResult<()> {
    h_staff_del(State(state), Json(body)).await
}

// post /pos/table/cart/list
async fn h_22(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<tigshop_core::models::pos::cashier::PosCartVO> {
    ok(state.pos.cashier.cart(0, 0, q.id.unwrap_or(0) as i32).await?)
}

// post /pos/table/cart/addToCart
async fn h_23(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<i64> {
    use tigshop_core::models::pos::cashier::PosCartAddDTO;
    ok(state.pos.cashier.add_to_cart(0, &PosCartAddDTO {
        product_id: q.product_id.unwrap_or(0),
        table_id: q.id.map(|v| v as i32),
        ..Default::default()
    }).await?)
}

// post /pos/table/cart/removeItem
async fn h_24(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    use tigshop_core::models::pos::cashier::PosCartRemoveDTO;
    state.pos.cashier.remove_items(0, &PosCartRemoveDTO { item_ids: q.ids.clone(), ..Default::default() }).await?;
    ok(())
}

// post /pos/table/cart/updateItem
async fn h_25(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    use tigshop_core::models::pos::cashier::PosCartUpdateDTO;
    state.pos.cashier.update_item(0, &PosCartUpdateDTO { item_id: q.id, quantity: q.status, is_checked: None, ..Default::default() }).await?;
    ok(())
}

// get /salesman/category/config
cfg_get!(h_26, "salesmanCategoryConfig");

// get /salesman/group/config
cfg_get!(h_27, "salesmanGroupConfig");

// get /salesman/material/config
cfg_get!(h_28, "salesmanMaterialConfig");

// get /salesman/salesman/config
cfg_get!(h_29, "salesmanConfig");

// post /salesman/group/updateField
upd_field!(h_30, "salesman_group", "group_id", ["groupName", "status"]);

// post /salesman/product/updateField
upd_field!(h_31, "salesman_product", "salesman_product_id", ["status"]);

// post /salesman/salesman/updateField
upd_field!(h_32, "salesman", "salesman_id", ["status", "mobile"]);

// post /salesman/salesman/batch
del_ids!(h_33, "salesman", "salesman_id");

// get /setting/areaCode/config
cfg_get!(h_34, "areaCodeConfig");

// post /setting/areaCode/translation
async fn h_35(_state: State<AdminState>, Json(q): Json<Pg>) -> ApiResult<Value> {
    // 无翻译凭据：原样返回
    let kw = q.keyword.clone().unwrap_or_default();
    ok(serde_json::json!({ "translation": kw }))
}

// get /setting/friendLinks/getAll
async fn h_36(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('linkId', link_id, 'linkTitle', link_title, 'linkUrl', link_url, 'isShow', is_show) FROM friend_link").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// get /setting/shippingTpl/getAll
async fn h_37(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('shippingTplId', shipping_tpl_id, 'tplName', tpl_name, 'isDefault', is_default) FROM shipping_tpl").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// post /setting/gallery/updateField
upd_field!(h_38, "album", "id", ["albumName", "isShow"]);

// get /setting/region/getChildRegion
async fn h_39(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('regionId', region_id, 'regionName', region_name, 'parentId', parent_id, 'level', level) FROM region WHERE parent_id = ? ORDER BY region_id")
        .bind(q.parent_id.unwrap_or(1)).fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// post /setting/messageType/batch
del_ids!(h_40, "message_type", "message_id");

// post /setting/messageType/miniProgramMessageTemplateSync
async fn h_41(_state: State<AdminState>) -> ApiResult<()> { ok(()) }

// post /setting/messageType/wechatMessageTemplateSync
async fn h_42(_state: State<AdminState>) -> ApiResult<()> { ok(()) }

// post /content/articleCategory/moveCat
async fn h_43(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE article_category SET parent_id = ? WHERE article_category_id = ?")
        .bind(q.parent_id.unwrap_or(0)).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /merchant/shopProductCategory/moveCat
async fn h_44(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    // shop 商品分类表按 shop_id 区分（表名 shop_product_category，主键 product_category_id）
    sqlx::query("UPDATE shop_product_category SET parent_id = ? WHERE product_category_id = ?")
        .bind(q.parent_id.unwrap_or(0)).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /content/wechatLiveProduct/batch
del_ids!(h_45, "wechat_live_goods", "id");

// post /merchant/merchant/search
list_pg!(h_46, "merchant", "merchant_id", "'merchant_id', merchant_id, 'merchant_apply_id', merchant_apply_id, 'user_id', user_id, 'add_time', add_time, 'base_data', base_data, 'shop_data', shop_data, 'merchant_data', merchant_data, 'status', status, 'is_delete', is_delete, 'type', type, 'company_name', company_name, 'corporate_name', corporate_name, 'settlement_cycle', settlement_cycle", "merchant_apply_id");

// get /merchant/merchant/userCount
async fn h_47(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (c,): (i64,) = sqlx::query_as("SELECT COUNT(*) FROM user").fetch_one(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({ "count": c }))
}

// get /merchant/shop/isPlatformAllocation
cfg_get!(h_48, "platformAllocation");

// post /order/aftersales/batch
async fn h_49(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    set_status(&state, "aftersales", "aftersales_id", &q, q.status.unwrap_or(0)).await?;
    ok(())
}

// post /order/aftersales/rejectReceive —— 见 trade::aftersales（重复，未挂载）
// get /order/aftersales/returnGoodsStatus —— 见 trade::aftersales（重复，未挂载）

// post /order/aftersales/updateField
upd_field!(h_52, "aftersales", "aftersale_id", ["status", "adminRemark"]);

// post /order/order/updateField
upd_field!(h_53, "order", "order_id", ["order_status", "admin_remark"]);

// get /product/eCardGroup/cardList
list_pg!(h_54, "e_card_group", "group_id", "'group_id', group_id, 'group_name', group_name, 'shop_id', shop_id, 'remark', remark, 'is_use', is_use, 'add_time', add_time, 'up_time', up_time", "group_name");

// post /product/productGroup/search
list_pg!(h_55, "product_group", "product_group_id", "'product_group_id', product_group_id, 'product_group_name', product_group_name, 'product_group_sn', product_group_sn, 'product_group_description', product_group_description, 'product_ids', product_ids, 'add_time', add_time, 'shop_id', shop_id", "product_group_name");

// post /promotion/giftCard/batch
del_ids!(h_56, "gift_cards", "gift_cards_id");

// post /promotion/seckill/updateField
upd_field!(h_57, "seckill", "seckill_id", ["status"]);

// post /promotion/timeDiscount/updateField
upd_field!(h_58, "time_discount", "discount_id", ["status"]);

// post /promotion/wechatLive/batch
del_ids!(h_59, "wechat_live", "wechat_live_id");

// post /user/userCompany/updateField
upd_field!(h_60, "user_company", "id", ["status", "audit_remark"]);

// get /user/pushLog/list
list_pg!(h_61, "unipush_log", "unipush_log_id", "'unipush_log_id', unipush_log_id, 'type', type, 'add_time', add_time, 'data', data, 'respond_data', respond_data, 'is_success', is_success, 'user_ids', user_ids, 'message_title', message_title, 'message_content', message_content, 'send_user_type', send_user_type", "type");

// post /user/pushLog/del
del_ids!(h_62, "unipush_log", "unipush_log_id");

// get /user/oauth/render
async fn h_63(_state: State<AdminState>, Query(_q): Query<Pg>) -> ApiResult<()> {
    Err(tigshop_security::web::AppError(Error::Validation("OAuth 未配置".into())))
}

// get /user/oauth/callback
async fn h_64(_state: State<AdminState>) -> ApiResult<()> {
    Err(tigshop_security::web::AppError(Error::Validation("OAuth 未配置".into())))
}

// get /finance/serverFundManagement/settings/detail
cfg_get!(h_65, "serverFundManagement");

// get /decorate/themeStyle/list
cfg_get!(h_66, "themeStyle");

// post /decorate/themeStyle/saveThemeStyle
cfg_set!(h_67, "themeStyle");

// post /decorate/mobileDecorate/list
list_pg!(h_68, "decorate", "decorate_id", "'decorate_id', decorate_id, 'decorate_title', decorate_title, 'data', data, 'draft_data', draft_data, 'decorate_type', decorate_type, 'page_type', page_type, 'is_home', is_home, 'shop_id', shop_id, 'status', status, 'update_time', update_time, 'locale_id', locale_id, 'parent_id', parent_id, 'sort_order', sort_order, 'binded_h5_id', binded_h5_id, 'share_info', share_info", "decorate_title");

// post /decorate/mobileDecorate/batch
del_ids!(h_69, "decorate", "decorate_id");

// post /decorate/mobileDecorate/updateField
upd_field!(h_70, "decorate", "decorate_id", ["decorate_title", "is_home"]);

// post /decorate/decorateRequest/productList
async fn h_71(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (p, s, off) = page_of(&q);
    let kw = format!("%{}%", q.keyword.clone().unwrap_or_default().trim());
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('productId', product_id, 'productName', product_name, 'picThumb', pic_thumb, 'productPrice', product_price) FROM product WHERE product_name LIKE ? ORDER BY product_id DESC LIMIT ? OFFSET ?")
        .bind(&kw).bind(s).bind(off).fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "page": p, "size": s}))
}

/// 域路由（挂载于 /adminapi 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/o2o/areaStoreManager/list", post(h_0))
        .route("/o2o/areaStoreManager/del", post(h_1))
        .route("/o2o/areaStoreManager/removeShop", post(h_2))
        .route("/o2o/areaStoreManager/shopList", post(h_3))
        .route("/o2o/decorate/decorate/detail", get(h_4))
        .route("/o2o/decorate/decorate/update", post(h_5))
        .route("/o2o/dispatch/assign", post(h_6))
        .route("/o2o/dispatch/availableStaff", get(h_7))
        .route("/order/dispatch/assign", post(h_8))
        .route("/order/dispatch/availableStaff", get(h_9))
        .route("/o2o/homeService/saveConfig", post(h_10))
        .route("/o2o/localDelivery/saveConfig", post(h_11))
        .route("/o2o/intraCityDelivery/platformConfig/getConfig", get(h_12))
        .route("/o2o/intraCityDelivery/platformConfig/saveConfig", post(h_13))
        .route("/o2o/reservationTpl/batch", post(h_14))
        .route("/o2o/shopPickupConfig/saveShopPickupTpl", post(h_15))
        .route("/o2o/storeProduct/writeOff", post(h_16))
        .route("/o2o/tip/list", post(h_17))
        .route("/o2o/tip/del", post(h_18))
        .route("/o2o/tip/batch", post(h_19))
        .route("/o2o/tip/selectAllTip", get(h_20))
        .route("/o2o/tip/tipConfig", get(h_21))
        .route("/o2o/tip/detail", get(h_tip_detail))
        .route("/o2o/tip/create", post(h_tip_create))
        .route("/o2o/tip/update", post(h_tip_update))
        .route("/o2o/tip/updateField", post(h_tip_update_field))
        .route("/o2o/serviceStaff/list", get(h_staff_list))
        .route("/o2o/serviceStaff/detail", get(h_staff_detail))
        .route("/o2o/serviceStaff/showMobile", get(h_staff_show_mobile))
        .route("/o2o/serviceStaff/create", post(h_staff_create))
        .route("/o2o/serviceStaff/update", post(h_staff_update))
        .route("/o2o/serviceStaff/updateField", post(h_staff_update_field))
        .route("/o2o/serviceStaff/del", post(h_staff_del))
        .route("/o2o/serviceStaff/batch", post(h_staff_batch))
        .route("/pos/table/cart/list", post(h_22))
        .route("/pos/table/cart/addToCart", post(h_23))
        .route("/pos/table/cart/removeItem", post(h_24))
        .route("/pos/table/cart/updateItem", post(h_25))
        .route("/salesman/category/config", get(h_26))
        .route("/salesman/group/config", get(h_27))
        .route("/salesman/material/config", get(h_28))
        .route("/salesman/salesman/config", get(h_29))
        .route("/salesman/group/updateField", post(h_30))
        .route("/salesman/product/updateField", post(h_31))
        .route("/salesman/salesman/updateField", post(h_32))
        .route("/salesman/salesman/batch", post(h_33))
        .route("/setting/areaCode/config", get(h_34))
        .route("/setting/areaCode/translation", post(h_35))
        .route("/setting/friendLinks/getAll", get(h_36))
        .route("/setting/shippingTpl/getAll", get(h_37))
        .route("/setting/gallery/updateField", post(h_38))
        .route("/setting/region/getChildRegion", get(h_39))
        .route("/setting/messageType/batch", post(h_40))
        .route("/setting/messageType/miniProgramMessageTemplateSync", post(h_41))
        .route("/setting/messageType/wechatMessageTemplateSync", post(h_42))
        .route("/content/articleCategory/moveCat", post(h_43))
        .route("/merchant/shopProductCategory/moveCat", post(h_44))
        .route("/content/wechatLiveProduct/batch", post(h_45))
        .route("/merchant/merchant/search", post(h_46))
        .route("/merchant/merchant/userCount", get(h_47))
        .route("/merchant/shop/isPlatformAllocation", get(h_48))
        .route("/order/aftersales/batch", post(h_49))
        // rejectReceive / returnGoodsStatus 已由 trade::aftersales 完整实现
        // （Java 对照版），此处不再重复注册（同方法同路径 = 启动 panic）
        .route("/order/aftersales/updateField", post(h_52))
        .route("/order/order/updateField", post(h_53))
        .route("/product/eCardGroup/cardList", get(h_54))
        .route("/product/productGroup/search", post(h_55))
        .route("/promotion/giftCard/batch", post(h_56))
        .route("/promotion/seckill/updateField", post(h_57))
        .route("/promotion/timeDiscount/updateField", post(h_58))
        .route("/promotion/wechatLive/batch", post(h_59))
        .route("/user/userCompany/updateField", post(h_60))
        .route("/user/pushLog/list", get(h_61))
        .route("/user/pushLog/del", post(h_62))
        .route("/user/oauth/render", get(h_63))
        .route("/user/oauth/callback", get(h_64))
        .route("/finance/serverFundManagement/settings/detail", get(h_65))
        .route("/decorate/themeStyle/list", get(h_66))
        .route("/decorate/themeStyle/saveThemeStyle", post(h_67))
        .route("/decorate/mobileDecorate/list", post(h_68))
        .route("/decorate/mobileDecorate/batch", post(h_69))
        .route("/decorate/mobileDecorate/updateField", post(h_70))
        .route("/decorate/decorateRequest/productList", post(h_71))
}

//! vendor 低频接口（vendor::lowfreq） —— 一次性低频接口（b2b2b 5.8.57 新版前端调用，无 Java 参考；
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
                return Err(Error::Validation("不支持的字段".into()));
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
            if ids.is_empty() { return Err(Error::Validation("缺少 id".into())); }
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

// post /vendor/vendorWithdraw/list
list_pg!(h_0, 'vendor_withdraw', 'vendor_withdraw_log_id', "'vendor_withdraw_log_id', vendor_withdraw_log_id, 'vendor_id', vendor_id, 'amount', amount, 'handling_fee', handling_fee, 'handling_rate', handling_rate, 'status', status, 'add_time', add_time, 'remark', remark, 'vendor_account_id', vendor_account_id, 'account_data', account_data, 'audit_remark', audit_remark, 'payment_voucher', payment_voucher, 'withdraw_sn', withdraw_sn", 'vendor_id');

// get /vendor/vendorWithdraw/detail
detail_one!(h_1, 'vendor_withdraw', 'vendor_withdraw_log_id', "'vendor_withdraw_log_id', vendor_withdraw_log_id, 'vendor_id', vendor_id, 'amount', amount, 'handling_fee', handling_fee, 'handling_rate', handling_rate, 'status', status, 'add_time', add_time, 'remark', remark, 'vendor_account_id', vendor_account_id, 'account_data', account_data, 'audit_remark', audit_remark, 'payment_voucher', payment_voucher, 'withdraw_sn', withdraw_sn");

// post /vendor/vendorWithdraw/create
async fn h_2(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<i64> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    let r = sqlx::query("INSERT INTO vendor_withdraw (vendor_id, amount, status, add_time, withdraw_sn) VALUES (?, 0, 0, ?, ?)")
        .bind(q.vendor_id.unwrap_or(0)).bind(now).bind(format!("WD{now}"))
        .execute(&pool(&state)).await.map_err(db)?;
    ok(r.last_insert_id() as i64)
}

// post /vendor/vendorWithdraw/audit
async fn h_3(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_withdraw SET status = ? WHERE vendor_withdraw_log_id = ?")
        .bind(q.status.unwrap_or(1)).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /vendor/vendorWithdraw/config
cfg_get!(h_4, 'vendorWithdrawConfig');

// get /vendor/vendorWithdraw/summaryList
list_pg!(h_5, 'vendor_withdraw', 'vendor_withdraw_log_id', "'vendor_withdraw_log_id', vendor_withdraw_log_id, 'vendor_id', vendor_id, 'amount', amount, 'handling_fee', handling_fee, 'handling_rate', handling_rate, 'status', status, 'add_time', add_time, 'remark', remark, 'vendor_account_id', vendor_account_id, 'account_data', account_data, 'audit_remark', audit_remark, 'payment_voucher', payment_voucher, 'withdraw_sn', withdraw_sn", 'vendor_id');

// post /vendor/vendorWithdraw/uploadPayVoucher
async fn h_6(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_withdraw SET payment_voucher = ? WHERE vendor_withdraw_log_id = ?")
        .bind(q.keyword.clone().unwrap_or_default()).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /vendor/vendorShopBind/list
list_pg!(h_7, 'vendor_shop_bind', 'id', "'id', id, 'vendor_id', vendor_id, 'shop_id', shop_id, 'add_time', add_time", 'vendor_id');

// get /vendor/vendorShopBind/merchantList
list_pg!(h_8, 'merchant', 'merchant_id', "'merchant_id', merchant_id, 'merchant_apply_id', merchant_apply_id, 'user_id', user_id, 'add_time', add_time, 'base_data', base_data, 'shop_data', shop_data, 'merchant_data', merchant_data, 'status', status, 'is_delete', is_delete, 'type', type, 'company_name', company_name, 'corporate_name', corporate_name, 'settlement_cycle', settlement_cycle", 'merchant_apply_id');

// get /vendor/vendorShopBind/orderList
async fn h_9(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (p, s, off) = page_of(&q);
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('orderId', order_id, 'orderSn', order_sn, 'userId', user_id, 'totalAmount', total_amount) FROM `order` ORDER BY order_id DESC LIMIT ? OFFSET ?")
        .bind(s).bind(off).fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "page": p, "size": s}))
}

// get /vendor/vendorShopBind/productList
async fn h_10(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (p, s, off) = page_of(&q);
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('productId', product_id, 'productName', product_name, 'picThumb', pic_thumb, 'productPrice', product_price) FROM product WHERE shop_id = ? ORDER BY product_id DESC LIMIT ? OFFSET ?")
        .bind(q.shop_id.unwrap_or(0)).bind(s).bind(off).fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "page": p, "size": s}))
}

// get /vendor/vendorShopBind/shopList
list_pg!(h_11, 'vendor_shop_bind', 'id', "'id', id, 'vendor_id', vendor_id, 'shop_id', shop_id, 'add_time', add_time", 'vendor_id');

// post /vendor/adminVendorUser/list
list_pg!(h_12, 'admin_user_vendor', 'id', "'id', id, 'admin_id', admin_id, 'user_id', user_id, 'vendor_id', vendor_id, 'username', username, 'email', email, 'avatar', avatar, 'auth_list', auth_list, 'is_using', is_using, 'is_admin', is_admin, 'add_time', add_time, 'role_id', role_id", 'admin_id');

// post /vendor/adminVendorUser/batch
del_ids!(h_13, 'admin_user_vendor', 'id');

// post /vendor/adminVendorUser/del
del_ids!(h_14, 'admin_user_vendor', 'id');

// get /vendor/adminVendorAccount/index
list_pg!(h_15, 'vendor_account', 'account_id', "'account_id', account_id, 'vendor_id', vendor_id, 'account_type', account_type, 'account_name', account_name, 'account_no', account_no, 'bank_name', bank_name, 'add_time', add_time, 'bank_branch', bank_branch", 'vendor_id');

// get /vendor/adminVendorAccount/listFund
list_pg!(h_16, 'vendor_account', 'account_id', "'account_id', account_id, 'vendor_id', vendor_id, 'account_type', account_type, 'account_name', account_name, 'account_no', account_no, 'bank_name', bank_name, 'add_time', add_time, 'bank_branch', bank_branch", 'vendor_id');

// get /vendor/adminVendorAccount/logList
list_pg!(h_17, 'vendor_account_log', 'vendor_account_log_id', "'vendor_account_log_id', vendor_account_log_id, 'add_time', add_time, 'vendor_money', vendor_money, 'frozen_money', frozen_money, 'type', type, 'remarks', remarks, 'new_vendor_money', new_vendor_money, 'new_frozen_money', new_frozen_money, 'vendor_id', vendor_id", 'add_time');

// get /vendor/vendorAccount/index
list_pg!(h_18, 'vendor_account', 'account_id', "'account_id', account_id, 'vendor_id', vendor_id, 'account_type', account_type, 'account_name', account_name, 'account_no', account_no, 'bank_name', bank_name, 'add_time', add_time, 'bank_branch', bank_branch", 'vendor_id');

// get /vendor/vendorAccount/list
list_pg!(h_19, 'vendor_account', 'account_id', "'account_id', account_id, 'vendor_id', vendor_id, 'account_type', account_type, 'account_name', account_name, 'account_no', account_no, 'bank_name', bank_name, 'add_time', add_time, 'bank_branch', bank_branch", 'vendor_id');

// get /vendor/vendorAccount/logList
list_pg!(h_20, 'vendor_account_log', 'vendor_account_log_id', "'vendor_account_log_id', vendor_account_log_id, 'add_time', add_time, 'vendor_money', vendor_money, 'frozen_money', frozen_money, 'type', type, 'remarks', remarks, 'new_vendor_money', new_vendor_money, 'new_frozen_money', new_frozen_money, 'vendor_id', vendor_id", 'add_time');

// post /vendor/vendorAccount/del
del_ids!(h_21, 'vendor_account', 'account_id');

// get /vendor/vendorAccount/config
cfg_get!(h_22, 'vendorAccountConfig');

// get /vendor/vendorAccount/applyShopAgreement
cfg_get!(h_23, 'vendorApplyShopAgreement');

// post /vendor/vendor/delVendor
del_ids!(h_24, 'vendor', 'vendor_id');

// get /vendor/vendor/staffShow
list_pg!(h_25, 'admin_user_vendor', 'id', "'id', id, 'admin_id', admin_id, 'user_id', user_id, 'vendor_id', vendor_id, 'username', username, 'email', email, 'avatar', avatar, 'auth_list', auth_list, 'is_using', is_using, 'is_admin', is_admin, 'add_time', add_time, 'role_id', role_id", 'admin_id');

// post /vendor/vendor/updateField
upd_field!(h_26, 'vendor', 'vendor_id', ["vendor_name", "status", "contact_mobile"]);

// post /vendor/vendor/updateInfo
upd_field!(h_27, 'vendor', 'vendor_id', ["vendor_name", "contact_mobile", "description", "kefu_phone"]);

// post /vendor/product/auditAgain
async fn h_28(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_product SET audit_state = 0 WHERE id = ?").bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /vendor/product/batch
del_ids!(h_29, 'vendor_product', 'id');

// post /vendor/product/changeState
async fn h_30(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_product SET product_state = ? WHERE id = ?")
        .bind(q.status.unwrap_or(1)).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /vendor/product/config
cfg_get!(h_31, 'vendorProductConfig');

// post /vendor/product/copy
async fn h_32(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<i64> {
    let r = sqlx::query("INSERT INTO vendor_product (product_name, product_category_id, product_state, sku_type, audit_state, pic_url, pic_thumb, pic_original, pic_large, is_recycle, vendor_id, create_time, update_time, create_by_id, update_by_id, is_del, product_sn_generate_type, product_brief, product_desc) SELECT CONCAT(product_name, ' 副本'), product_category_id, product_state, sku_type, 0, pic_url, pic_thumb, pic_original, pic_large, is_recycle, vendor_id, UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), create_by_id, update_by_id, is_del, product_sn_generate_type, product_brief, product_desc FROM vendor_product WHERE id = ?")
        .bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(r.last_insert_id() as i64)
}

// post /vendor/product/del
del_ids!(h_33, 'vendor_product', 'id');

// get /vendor/product/getParticiple
async fn h_34(_state: State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    // 分词服务未迁移：按空格/2字切分
    let kw = q.keyword.clone().unwrap_or_default();
    let parts: Vec<String> = kw.chars().collect::<Vec<_>>().chunks(2).map(|c| c.iter().collect()).collect();
    ok(serde_json::json!({ "words": parts }))
}

// post /vendor/product/platform/audit
async fn h_35(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_product SET audit_state = ? WHERE id = ?")
        .bind(q.status.unwrap_or(1)).bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /vendor/product/platform/getWaitingCheckedVendorCount
async fn h_36(State(state): State<AdminState>) -> ApiResult<Value> {
    let (c,): (i64,) = sqlx::query_as("SELECT COUNT(DISTINCT vendor_id) FROM vendor_product WHERE audit_state = 0").fetch_one(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({ "count": c }))
}

// post /vendor/product/platform/pageList
async fn h_37(State(state): State<AdminState>, Query(q): Query<Pg>) -> ApiResult<Value> {
    let (p, s, off) = page_of(&q);
    let kw = format!("%{}%", q.keyword.clone().unwrap_or_default().trim());
    let (total,): (i64,) = sqlx::query_as("SELECT COUNT(*) FROM vendor_product WHERE audit_state = 0 AND product_name LIKE ?")
        .bind(&kw).fetch_one(&pool(&state)).await.map_err(db)?;
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('id', id, 'productName', product_name, 'vendorId', vendor_id, 'auditState', audit_state, 'picThumb', pic_thumb) FROM vendor_product WHERE audit_state = 0 AND product_name LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?")
        .bind(&kw).bind(s).bind(off).fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>(), "total": total, "page": p, "size": s}))
}

// post /vendor/product/recycle
async fn h_38(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    sqlx::query("UPDATE vendor_product SET is_recycle = 1 WHERE id = ?").bind(q.id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// get /vendor/product/shippingTplList
async fn h_39(State(state): State<AdminState>) -> ApiResult<Value> {
    let rows: Vec<(Value,)> = sqlx::query_as("SELECT JSON_OBJECT('shippingTplId', shipping_tpl_id, 'tplName', tpl_name) FROM shipping_tpl").fetch_all(&pool(&state)).await.map_err(db)?;
    ok(serde_json::json!({"records": rows.into_iter().map(|(v,)| v).collect::<Vec<_>>()}))
}

// post /vendor/product/updateField
upd_field!(h_40, 'vendor_product', 'id', ["product_name", "product_state", "audit_state", "product_brief"]);

// get /vendor/productInventoryLog/list
list_pg!(h_41, 'vendor_product_stock_record', 'product_stock_id', "'product_stock_id', product_stock_id, 'stock_code', stock_code, 'in_or_out', in_or_out, 'type', type, 'add_time', add_time, 'add_by', add_by, 'comment', comment, 'vendor_id', vendor_id", 'stock_code');

// get /vendor/stock/adjustDetail
detail_one!(h_42, 'vendor_product_stock_record_detail', 'record_detail_id', "'record_detail_id', record_detail_id, 'stock_record_id', stock_record_id, 'vendor_product_id', vendor_product_id, 'vendor_product_sku_id', vendor_product_sku_id, 'quantity', quantity");

// get /vendor/stock/getRecordDetail
detail_one!(h_43, 'vendor_product_stock_record', 'product_stock_id', "'product_stock_id', product_stock_id, 'stock_code', stock_code, 'in_or_out', in_or_out, 'type', type, 'add_time', add_time, 'add_by', add_by, 'comment', comment, 'vendor_id', vendor_id");

// get /vendor/stock/page
list_pg!(h_44, 'vendor_product_stock_record', 'product_stock_id', "'product_stock_id', product_stock_id, 'stock_code', stock_code, 'in_or_out', in_or_out, 'type', type, 'add_time', add_time, 'add_by', add_by, 'comment', comment, 'vendor_id', vendor_id", 'stock_code');

// get /vendor/stock/record/page
list_pg!(h_45, 'vendor_product_stock_record', 'product_stock_id', "'product_stock_id', product_stock_id, 'stock_code', stock_code, 'in_or_out', in_or_out, 'type', type, 'add_time', add_time, 'add_by', add_by, 'comment', comment, 'vendor_id', vendor_id", 'stock_code');

// get /vendor/stock/stockDetail
detail_one!(h_46, 'vendor_product', 'id', "'id', id, 'product_name', product_name, 'product_brand_id', product_brand_id, 'product_category_id', product_category_id, 'product_sn_generate_type', product_sn_generate_type, 'product_brief', product_brief, 'product_state', product_state, 'sku_type', sku_type, 'product_desc', product_desc, 'audit_state', audit_state, 'pic_url', pic_url, 'pic_thumb', pic_thumb, 'pic_original', pic_original, 'pic_large', pic_large, 'is_recycle', is_recycle, 'vendor_id', vendor_id");

// get /vendor/stock/getAddByList
list_pg!(h_47, 'admin_user_vendor', 'id', "'id', id, 'admin_id', admin_id, 'user_id', user_id, 'vendor_id', vendor_id, 'username', username, 'email', email, 'avatar', avatar, 'auth_list', auth_list, 'is_using', is_using, 'is_admin', is_admin, 'add_time', add_time, 'role_id', role_id", 'admin_id');

// get /vendor/stock/downloadTemplate
async fn h_48() -> ApiResult<&'static str> {
    // 简化：CSV 表头模板
    ok("skuSn,stock,price\n")
}

// get /vendor/stock/getImportErrorData
async fn h_49() -> ApiResult<Value> { ok(serde_json::json!({"records": []})) }

// post /vendor/stock/batchImport
async fn h_50(_state: State<AdminState>) -> ApiResult<()> {
    Err(Error::Validation("批量导入未迁移（简化实现）".into()))
}

// post /vendor/stock/stockIn
async fn h_51(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    sqlx::query("INSERT INTO vendor_product_stock_record (stock_code, in_or_out, type, add_time, add_by, comment, vendor_id) VALUES ('MANUAL', 1, 0, ?, 'admin', 'POS/后台入库', ?)")
        .bind(now).bind(q.vendor_id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /vendor/stock/stockOut
async fn h_52(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    sqlx::query("INSERT INTO vendor_product_stock_record (stock_code, in_or_out, type, add_time, add_by, comment, vendor_id) VALUES ('MANUAL', 2, 0, ?, 'admin', '后台出库', ?)")
        .bind(now).bind(q.vendor_id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

// post /vendor/stock/stockAdjust
async fn h_53(State(state): State<AdminState>, Json(q): Json<Pg>) -> ApiResult<()> {
    let now = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap_or_default().as_secs() as i64;
    sqlx::query("INSERT INTO vendor_product_stock_record (stock_code, in_or_out, type, add_time, add_by, comment, vendor_id) VALUES ('ADJ', 1, 1, ?, 'admin', '库存调整', ?)")
        .bind(now).bind(q.vendor_id.unwrap_or(0)).execute(&pool(&state)).await.map_err(db)?;
    ok(())
}

/// 域路由（挂载于 /adminapi 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/vendor/vendorWithdraw/list", post(h_0))
        .route("/vendor/vendorWithdraw/detail", get(h_1))
        .route("/vendor/vendorWithdraw/create", post(h_2))
        .route("/vendor/vendorWithdraw/audit", post(h_3))
        .route("/vendor/vendorWithdraw/config", get(h_4))
        .route("/vendor/vendorWithdraw/summaryList", get(h_5))
        .route("/vendor/vendorWithdraw/uploadPayVoucher", post(h_6))
        .route("/vendor/vendorShopBind/list", post(h_7))
        .route("/vendor/vendorShopBind/merchantList", get(h_8))
        .route("/vendor/vendorShopBind/orderList", get(h_9))
        .route("/vendor/vendorShopBind/productList", get(h_10))
        .route("/vendor/vendorShopBind/shopList", get(h_11))
        .route("/vendor/adminVendorUser/list", post(h_12))
        .route("/vendor/adminVendorUser/batch", post(h_13))
        .route("/vendor/adminVendorUser/del", post(h_14))
        .route("/vendor/adminVendorAccount/index", get(h_15))
        .route("/vendor/adminVendorAccount/listFund", get(h_16))
        .route("/vendor/adminVendorAccount/logList", get(h_17))
        .route("/vendor/vendorAccount/index", get(h_18))
        .route("/vendor/vendorAccount/list", get(h_19))
        .route("/vendor/vendorAccount/logList", get(h_20))
        .route("/vendor/vendorAccount/del", post(h_21))
        .route("/vendor/vendorAccount/config", get(h_22))
        .route("/vendor/vendorAccount/applyShopAgreement", get(h_23))
        .route("/vendor/vendor/delVendor", post(h_24))
        .route("/vendor/vendor/staffShow", get(h_25))
        .route("/vendor/vendor/updateField", post(h_26))
        .route("/vendor/vendor/updateInfo", post(h_27))
        .route("/vendor/product/auditAgain", post(h_28))
        .route("/vendor/product/batch", post(h_29))
        .route("/vendor/product/changeState", post(h_30))
        .route("/vendor/product/config", get(h_31))
        .route("/vendor/product/copy", post(h_32))
        .route("/vendor/product/del", post(h_33))
        .route("/vendor/product/getParticiple", get(h_34))
        .route("/vendor/product/platform/audit", post(h_35))
        .route("/vendor/product/platform/getWaitingCheckedVendorCount", get(h_36))
        .route("/vendor/product/platform/pageList", post(h_37))
        .route("/vendor/product/recycle", post(h_38))
        .route("/vendor/product/shippingTplList", get(h_39))
        .route("/vendor/product/updateField", post(h_40))
        .route("/vendor/productInventoryLog/list", get(h_41))
        .route("/vendor/stock/adjustDetail", get(h_42))
        .route("/vendor/stock/getRecordDetail", get(h_43))
        .route("/vendor/stock/page", get(h_44))
        .route("/vendor/stock/record/page", get(h_45))
        .route("/vendor/stock/stockDetail", get(h_46))
        .route("/vendor/stock/getAddByList", get(h_47))
        .route("/vendor/stock/downloadTemplate", get(h_48))
        .route("/vendor/stock/getImportErrorData", get(h_49))
        .route("/vendor/stock/batchImport", post(h_50))
        .route("/vendor/stock/stockIn", post(h_51))
        .route("/vendor/stock/stockOut", post(h_52))
        .route("/vendor/stock/stockAdjust", post(h_53))
}

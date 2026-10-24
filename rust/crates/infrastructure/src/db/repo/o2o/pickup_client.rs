//! o2o H5 前台仓储（o2o::pickup_client）。
//!
//! b2b2b 5.8.57 移动端新增接口（无 Java 控制器对应），按表实现：
//! - `user_pickup_info`：会员自提点/自提人信息 CRUD
//! - `shop_pickup_tpl`：门店自提模板读
//! - `order_pickup_shipment`：订单自提配送读
//! - `product_reservation_stock`：到店预约库存日历读（复用 admin 侧仓储逻辑）

use serde::Serialize;
use sqlx::MySqlPool;
use tigshop_core::{Error, Result};

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

/// 自提人信息（Java `UserPickupInfo`）。
#[derive(Debug, Clone, Default, sqlx::FromRow, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct UserPickupVO {
    pub user_pickup_id: i32,
    pub user_id: i32,
    pub consignee: String,
    pub mobile: String,
    pub mobile_area_code: String,
    pub is_default: i32,
    pub is_selected: i32,
}

/// 自提模板（Java `ShopPickupTpl` 子集）。
#[derive(Debug, Clone, Default, sqlx::FromRow, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct ShopPickupTplVO {
    pub shop_pickup_tpl_id: i64,
    pub shop_id: i64,
    pub tpl_name: String,
    pub stocking_status: i32,
    pub stocking_json: Option<String>,
    pub pickup_time_status: i32,
    pub pickup_time_json: Option<String>,
    pub pickup_end_status: i32,
    pub pickup_end_json: Option<String>,
    pub is_default: i32,
}

/// 自提配送单（Java `OrderPickupShipment` 子集）。
#[derive(Debug, Clone, Default, sqlx::FromRow, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderPickupShipmentVO {
    pub shipment_id: i32,
    pub order_id: i32,
    pub pickup_sn: String,
    pub pickup_qr_code: Option<String>,
    pub shipment_status: i32,
    pub check_time: i32,
}

/// 库存日历行（到店预约：日期 × 余量）。
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct StockDayVO {
    pub day: String,
    pub total: i32,
    pub used: i32,
    pub available: i32,
}

#[derive(Clone)]
pub struct PickupClientRepository {
    pool: MySqlPool,
}

impl PickupClientRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 当前会员的自提人列表。
    pub async fn list_by_user(&self, user_id: i32) -> Result<Vec<UserPickupVO>> {
        sqlx::query_as::<_, UserPickupVO>(
            "SELECT user_pickup_id, user_id, consignee, mobile, mobile_area_code, \
             is_default, is_selected FROM user_pickup_info \
             WHERE user_id = ? ORDER BY is_default DESC, user_pickup_id DESC",
        )
        .bind(user_id)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 单条（校验归属）。
    pub async fn find_owned(&self, user_id: i32, id: i32) -> Result<Option<UserPickupVO>> {
        sqlx::query_as::<_, UserPickupVO>(
            "SELECT user_pickup_id, user_id, consignee, mobile, mobile_area_code, \
             is_default, is_selected FROM user_pickup_info \
             WHERE user_id = ? AND user_pickup_id = ?",
        )
        .bind(user_id)
        .bind(id)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 新增；`is_default=1` 时清除其它默认。
    pub async fn create(
        &self,
        user_id: i32,
        consignee: &str,
        mobile: &str,
        area_code: &str,
        is_default: bool,
    ) -> Result<i32> {
        let mut tx = self.pool.begin().await.map_err(map_err)?;
        if is_default {
            sqlx::query("UPDATE user_pickup_info SET is_default = 0 WHERE user_id = ?")
                .bind(user_id)
                .execute(&mut *tx)
                .await
                .map_err(map_err)?;
        }
        let r = sqlx::query(
            "INSERT INTO user_pickup_info (user_id, consignee, mobile, mobile_area_code, \
             is_default, is_selected) VALUES (?, ?, ?, ?, ?, 0)",
        )
        .bind(user_id)
        .bind(consignee)
        .bind(mobile)
        .bind(area_code)
        .bind(i32::from(is_default))
        .execute(&mut *tx)
        .await
        .map_err(map_err)?;
        tx.commit().await.map_err(map_err)?;
        Ok(r.last_insert_id() as i32)
    }

    /// 更新（只更新提供的字段）；`is_default=1` 时清除其它默认。
    pub async fn update(
        &self,
        user_id: i32,
        id: i32,
        consignee: Option<&str>,
        mobile: Option<&str>,
        area_code: Option<&str>,
        is_default: Option<bool>,
    ) -> Result<()> {
        let mut tx = self.pool.begin().await.map_err(map_err)?;
        if is_default == Some(true) {
            sqlx::query("UPDATE user_pickup_info SET is_default = 0 WHERE user_id = ?")
                .bind(user_id)
                .execute(&mut *tx)
                .await
                .map_err(map_err)?;
        }
        if let Some(v) = consignee {
            sqlx::query("UPDATE user_pickup_info SET consignee = ? WHERE user_id = ? AND user_pickup_id = ?")
                .bind(v).bind(user_id).bind(id)
                .execute(&mut *tx).await.map_err(map_err)?;
        }
        if let Some(v) = mobile {
            sqlx::query("UPDATE user_pickup_info SET mobile = ? WHERE user_id = ? AND user_pickup_id = ?")
                .bind(v).bind(user_id).bind(id)
                .execute(&mut *tx).await.map_err(map_err)?;
        }
        if let Some(v) = area_code {
            sqlx::query("UPDATE user_pickup_info SET mobile_area_code = ? WHERE user_id = ? AND user_pickup_id = ?")
                .bind(v).bind(user_id).bind(id)
                .execute(&mut *tx).await.map_err(map_err)?;
        }
        if let Some(v) = is_default {
            sqlx::query("UPDATE user_pickup_info SET is_default = ? WHERE user_id = ? AND user_pickup_id = ?")
                .bind(i32::from(v)).bind(user_id).bind(id)
                .execute(&mut *tx).await.map_err(map_err)?;
        }
        tx.commit().await.map_err(map_err)
    }

    /// 删除（校验归属）。
    pub async fn delete(&self, user_id: i32, id: i32) -> Result<()> {
        sqlx::query("DELETE FROM user_pickup_info WHERE user_id = ? AND user_pickup_id = ?")
            .bind(user_id)
            .bind(id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(())
    }

    /// 门店自提模板：门店级优先，缺省回退平台默认（`shop_id=0`）。
    pub async fn find_pickup_tpl(&self, shop_id: i64) -> Result<Option<ShopPickupTplVO>> {
        let row = sqlx::query_as::<_, ShopPickupTplVO>(
            "SELECT shop_pickup_tpl_id, shop_id, tpl_name, stocking_status, stocking_json, \
             pickup_time_status, pickup_time_json, pickup_end_status, pickup_end_json, is_default \
             FROM shop_pickup_tpl WHERE shop_id = ? ORDER BY is_default DESC LIMIT 1",
        )
        .bind(shop_id)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)?;
        if row.is_some() {
            return Ok(row);
        }
        sqlx::query_as::<_, ShopPickupTplVO>(
            "SELECT shop_pickup_tpl_id, shop_id, tpl_name, stocking_status, stocking_json, \
             pickup_time_status, pickup_time_json, pickup_end_status, pickup_end_json, is_default \
             FROM shop_pickup_tpl WHERE shop_id = 0 ORDER BY is_default DESC LIMIT 1",
        )
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 订单自提配送单。
    pub async fn find_shipment_by_order(
        &self,
        order_id: i32,
    ) -> Result<Option<OrderPickupShipmentVO>> {
        sqlx::query_as::<_, OrderPickupShipmentVO>(
            "SELECT shipment_id, order_id, pickup_sn, pickup_qr_code, shipment_status, check_time \
             FROM order_pickup_shipment WHERE order_id = ? ORDER BY shipment_id DESC LIMIT 1",
        )
        .bind(order_id)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 门店商品搜索（按 shop_id 过滤上架商品，名称模糊）。
    ///
    /// 返回 `(total, records)`；records 为前端消费的 camelCase JSON 行。
    pub async fn store_product_search(
        &self,
        shop_id: i64,
        keyword: &str,
        limit: u64,
        offset: u64,
    ) -> Result<(i64, Vec<serde_json::Value>)> {
        let kw = format!("%{}%", keyword.trim());
        let (total,): (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM product WHERE shop_id = ? AND product_status = 1              AND product_name LIKE ?",
        )
        .bind(shop_id)
        .bind(&kw)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;
        let rows = sqlx::query_as::<_, (serde_json::Value,)>(
            "SELECT JSON_OBJECT('productId', product_id, 'productName', product_name,              'picThumb', pic_thumb, 'productPrice', product_price, 'marketPrice', market_price,              'productStock', product_stock)              FROM product WHERE shop_id = ? AND product_status = 1 AND product_name LIKE ?              ORDER BY product_id DESC LIMIT ? OFFSET ?",
        )
        .bind(shop_id)
        .bind(&kw)
        .bind(limit)
        .bind(offset)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;
        let rows = rows.into_iter().map(|(v,)| v).collect::<Vec<_>>();
        Ok((total.max(0), rows))
    }

    /// 到店预约库存日历：某商品（+可选门店）未来 `days` 天的余量。
    ///
    /// `product_reservation_stock` 行语义：available = total - used（见 admin 侧
    /// StockCalendarRepository 同口径）。
    pub async fn stock_calendar(
        &self,
        product_id: i32,
        shop_id: Option<i64>,
        days: u32,
    ) -> Result<Vec<StockDayVO>> {
        let today = chrono::Utc::now().date_naive();
        let end = today + chrono::Duration::days(i64::from(days.min(31)));
        let rows: Vec<(String, i32, i32)> = match shop_id {
            Some(sid) => sqlx::query_as(
                "SELECT stock_date, total_stock, used_stock FROM product_reservation_stock \
                 WHERE product_id = ? AND shop_id = ? AND stock_date BETWEEN ? AND ? \
                 ORDER BY stock_date",
            )
            .bind(product_id)
            .bind(sid)
            .bind(today.format("%Y-%m-%d").to_string())
            .bind(end.format("%Y-%m-%d").to_string())
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?,
            None => sqlx::query_as(
                "SELECT stock_date, SUM(total_stock), SUM(used_stock) \
                 FROM product_reservation_stock \
                 WHERE product_id = ? AND stock_date BETWEEN ? AND ? \
                 GROUP BY stock_date ORDER BY stock_date",
            )
            .bind(product_id)
            .bind(today.format("%Y-%m-%d").to_string())
            .bind(end.format("%Y-%m-%d").to_string())
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?,
        };
        Ok(rows
            .into_iter()
            .map(|(day, total, used)| StockDayVO {
                available: (total - used).max(0),
                day,
                total,
                used,
            })
            .collect())
    }
}

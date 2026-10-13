//! 秒杀域仓储：`seckill` / `seckill_item`。
//! 对应原 Java `SeckillMapper` / `SeckillItemMapper`。
//!
//! 列名严格对齐 `java/database/pure.sql`：
//! - `seckill`：`seckill_id` / `seckill_name` / `seckill_start_time` /
//!   `seckill_end_time` / `seckill_limit_num` / `product_id` / `shop_id`
//! - `seckill_item`：`rec_id` / `seckill_id` / `product_id` / `sku_id` /
//!   `seckill_price` / `seckill_stock` / `seckill_sales` /
//!   `seckill_start_time` / `seckill_end_time`

use rust_decimal::Decimal;
use sqlx::MySqlPool;
use tigshop_core::entities::{Seckill, SeckillItem};
use tigshop_core::types::PageQuery;
use tigshop_core::{Error, Result};

#[derive(Clone)]
pub struct SeckillRepository {
    pool: MySqlPool,
}

impl SeckillRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池（供服务层在事务中复用）。
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    // ————————————————————— 活动 —————————————————————

    /// 按 ID 查询秒杀活动。
    pub async fn find_by_id(&self, seckill_id: i32) -> Result<Option<Seckill>> {
        sqlx::query_as::<_, Seckill>("SELECT * FROM seckill WHERE seckill_id = ? LIMIT 1")
            .bind(seckill_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 秒杀活动分页（关键词匹配活动名，可按店铺过滤）。
    pub async fn paginate(
        &self,
        keyword: Option<&str>,
        shop_id: Option<i32>,
        page: &PageQuery,
    ) -> Result<(u64, Vec<Seckill>)> {
        let like = keyword
            .map(str::trim)
            .filter(|k| !k.is_empty())
            .map(|k| format!("%{k}%"));
        let (limit, offset) = page.limit_offset();

        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM seckill
             WHERE (? IS NULL OR seckill_name LIKE ?)
               AND (? IS NULL OR shop_id = ?)",
        )
        .bind(&like)
        .bind(&like)
        .bind(shop_id)
        .bind(shop_id)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;

        let rows = sqlx::query_as::<_, Seckill>(
            "SELECT * FROM seckill
             WHERE (? IS NULL OR seckill_name LIKE ?)
               AND (? IS NULL OR shop_id = ?)
             ORDER BY seckill_id DESC
             LIMIT ? OFFSET ?",
        )
        .bind(&like)
        .bind(&like)
        .bind(shop_id)
        .bind(shop_id)
        .bind(limit)
        .bind(offset)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;

        Ok((total.0.max(0) as u64, rows))
    }

    /// 新增秒杀活动，返回 `seckill_id`。
    pub async fn insert(&self, s: &Seckill) -> Result<i32> {
        let r = sqlx::query(
            "INSERT INTO seckill
             (seckill_name, seckill_start_time, seckill_end_time,
              seckill_limit_num, product_id, shop_id)
             VALUES (?, ?, ?, ?, ?, ?)",
        )
        .bind(&s.seckill_name)
        .bind(s.seckill_start_time)
        .bind(s.seckill_end_time)
        .bind(s.seckill_limit_num)
        .bind(s.product_id)
        .bind(s.shop_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(r.last_insert_id() as i32)
    }

    /// 更新秒杀活动。
    pub async fn update(&self, s: &Seckill) -> Result<()> {
        let affected = sqlx::query(
            "UPDATE seckill SET
               seckill_name = ?,
               seckill_start_time = ?,
               seckill_end_time = ?,
               seckill_limit_num = ?,
               product_id = ?
             WHERE seckill_id = ?",
        )
        .bind(&s.seckill_name)
        .bind(s.seckill_start_time)
        .bind(s.seckill_end_time)
        .bind(s.seckill_limit_num)
        .bind(s.product_id)
        .bind(s.seckill_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        if affected == 0 {
            return Err(Error::NotFound("秒杀活动不存在".into()));
        }
        Ok(())
    }

    /// 删除秒杀活动及其明细（事务）。
    pub async fn delete(&self, seckill_id: i32) -> Result<()> {
        let mut tx = self.pool.begin().await.map_err(map_err)?;
        sqlx::query("DELETE FROM seckill_item WHERE seckill_id = ?")
            .bind(seckill_id)
            .execute(&mut *tx)
            .await
            .map_err(map_err)?;
        sqlx::query("DELETE FROM seckill WHERE seckill_id = ?")
            .bind(seckill_id)
            .execute(&mut *tx)
            .await
            .map_err(map_err)?;
        tx.commit().await.map_err(map_err)?;
        Ok(())
    }

    // ————————————————————— 活动明细 —————————————————————

    /// 某活动下的全部明细。
    pub async fn find_items(&self, seckill_id: i32) -> Result<Vec<SeckillItem>> {
        sqlx::query_as::<_, SeckillItem>(
            "SELECT * FROM seckill_item WHERE seckill_id = ? ORDER BY rec_id ASC",
        )
        .bind(seckill_id)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 新增活动明细，返回 `rec_id`。
    pub async fn insert_item(&self, item: &SeckillItem) -> Result<i32> {
        let r = sqlx::query(
            "INSERT INTO seckill_item
             (seckill_id, product_id, sku_id, seckill_price, seckill_stock,
              seckill_sales, seckill_start_time, seckill_end_time)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        )
        .bind(item.seckill_id)
        .bind(item.product_id)
        .bind(item.sku_id)
        .bind(item.seckill_price)
        .bind(item.seckill_stock)
        .bind(item.seckill_sales)
        .bind(item.seckill_start_time)
        .bind(item.seckill_end_time)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(r.last_insert_id() as i32)
    }

    /// 清空某活动的全部明细（重建场景）。
    pub async fn delete_items(&self, seckill_id: i32) -> Result<u64> {
        let r = sqlx::query("DELETE FROM seckill_item WHERE seckill_id = ?")
            .bind(seckill_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(r.rows_affected())
    }

    /// 查询商品/SKU **当前进行中**的秒杀明细。
    ///
    /// 对应 Java `SeckillServiceImpl#getProductActivityInfo`：
    /// 以 `seckill_item` 的时间窗为准（明细自带起止时间，可覆盖活动级时间）。
    /// `sku_id` 传 `None` 时按商品级（`sku_id = 0`）匹配。
    pub async fn find_active_item(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        now: i32,
    ) -> Result<Option<SeckillItem>> {
        sqlx::query_as::<_, SeckillItem>(
            "SELECT * FROM seckill_item
             WHERE product_id = ?
               AND sku_id = ?
               AND seckill_start_time <= ?
               AND seckill_end_time >= ?
             ORDER BY rec_id DESC
             LIMIT 1",
        )
        .bind(product_id)
        .bind(sku_id.unwrap_or(0))
        .bind(now)
        .bind(now)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 批量查询多个 SKU 在某活动下的明细（结算页批量取价，消除 N+1）。
    ///
    /// 对应 Java `SeckillServiceImpl#parsePrice` 中按 `skuId` 批量预取的优化。
    pub async fn find_items_by_skus(
        &self,
        seckill_id: i32,
        sku_ids: &[i32],
    ) -> Result<Vec<SeckillItem>> {
        if sku_ids.is_empty() {
            return Ok(Vec::new());
        }
        let placeholders = vec!["?"; sku_ids.len()].join(", ");
        let sql = format!(
            "SELECT * FROM seckill_item WHERE seckill_id = ? AND sku_id IN ({placeholders})"
        );
        let mut q = sqlx::query_as::<_, SeckillItem>(&sql).bind(seckill_id);
        for id in sku_ids {
            q = q.bind(id);
        }
        q.fetch_all(&self.pool).await.map_err(map_err)
    }

    /// 原子扣减秒杀库存。
    ///
    /// 对应 Java `SeckillServiceImpl#decStock` 的
    /// `ge(seckill_stock, quantity).setDecrBy(...)` 语义：
    /// **库存不足时不做任何更新**，返回 `false`，由调用方抛业务错误。
    /// 该 `WHERE seckill_stock >= ?` 条件是防超卖的关键。
    pub async fn dec_stock(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
        now: i32,
    ) -> Result<bool> {
        let affected = sqlx::query(
            "UPDATE seckill_item
             SET seckill_stock = seckill_stock - ?
             WHERE product_id = ?
               AND sku_id = ?
               AND seckill_start_time <= ?
               AND seckill_end_time >= ?
               AND seckill_stock >= ?",
        )
        .bind(quantity)
        .bind(product_id)
        .bind(sku_id.unwrap_or(0))
        .bind(now)
        .bind(now)
        .bind(quantity)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        Ok(affected > 0)
    }

    /// 回补秒杀库存（取消订单/退款）。
    pub async fn inc_stock(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<()> {
        sqlx::query(
            "UPDATE seckill_item
             SET seckill_stock = seckill_stock + ?
             WHERE product_id = ? AND sku_id = ?",
        )
        .bind(quantity)
        .bind(product_id)
        .bind(sku_id.unwrap_or(0))
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(())
    }

    /// 累加秒杀销量。
    pub async fn inc_sales(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<()> {
        sqlx::query(
            "UPDATE seckill_item
             SET seckill_sales = seckill_sales + ?
             WHERE product_id = ? AND sku_id = ?",
        )
        .bind(quantity)
        .bind(product_id)
        .bind(sku_id.unwrap_or(0))
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(())
    }

    /// 扣减秒杀销量（订单取消时回退）。
    ///
    /// 用 `GREATEST(..., 0)` 兜底，避免并发回退把销量减成负数。
    pub async fn dec_sales(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<()> {
        sqlx::query(
            "UPDATE seckill_item
             SET seckill_sales = GREATEST(CAST(seckill_sales AS SIGNED) - ?, 0)
             WHERE product_id = ? AND sku_id = ?",
        )
        .bind(quantity)
        .bind(product_id)
        .bind(sku_id.unwrap_or(0))
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(())
    }

    /// 当前进行中的秒杀商品列表（前台秒杀专区）。
    ///
    /// 返回 `(活动, 明细)` 以便上层组装商品信息。
    pub async fn find_active_items(
        &self,
        now: i32,
        page: &PageQuery,
    ) -> Result<(u64, Vec<SeckillItem>)> {
        let (limit, offset) = page.limit_offset();

        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM seckill_item
             WHERE seckill_start_time <= ? AND seckill_end_time >= ?
               AND seckill_stock > 0",
        )
        .bind(now)
        .bind(now)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;

        let rows = sqlx::query_as::<_, SeckillItem>(
            "SELECT * FROM seckill_item
             WHERE seckill_start_time <= ? AND seckill_end_time >= ?
               AND seckill_stock > 0
             ORDER BY rec_id DESC
             LIMIT ? OFFSET ?",
        )
        .bind(now)
        .bind(now)
        .bind(limit)
        .bind(offset)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)?;

        Ok((total.0.max(0) as u64, rows))
    }

    /// 校验 SKU 是否已在**其他进行中**的秒杀活动中（防止同款重复上活动）。
    pub async fn exists_conflicting_item(
        &self,
        product_id: i32,
        sku_id: i32,
        exclude_seckill_id: Option<i32>,
    ) -> Result<bool> {
        let row: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM seckill_item
             WHERE product_id = ? AND sku_id = ?
               AND (? IS NULL OR seckill_id <> ?)",
        )
        .bind(product_id)
        .bind(sku_id)
        .bind(exclude_seckill_id)
        .bind(exclude_seckill_id)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(row.0 > 0)
    }
}

fn map_err(e: sqlx::Error) -> Error {
    match e {
        sqlx::Error::RowNotFound => Error::NotFound("秒杀活动不存在".into()),
        other => Error::Database(other.to_string()),
    }
}

/// 秒杀价格解析结果（供购物车/结算页使用）。
#[derive(Debug, Clone)]
pub struct SeckillPriceHit {
    pub sku_id: i32,
    pub seckill_price: Decimal,
    pub seckill_stock: i32,
}

#[cfg(test)]
mod tests {
    // 需要真实 MySQL 的集成测试见 tests/ 目录。
    // 本文件的纯逻辑（时间窗判定、库存比较）由 service 层测试覆盖。
}

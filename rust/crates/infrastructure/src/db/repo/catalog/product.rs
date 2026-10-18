//! `product` / `product_sku` 仓储，替代 `ProductMapper` / `ProductSkuMapper`。

use rust_decimal::Decimal;
use sqlx::MySqlPool;
use tigshop_core::entities::{Product, ProductSku};
use tigshop_core::models::ProductListQuery;
use tigshop_core::{Error, Result};

#[derive(Clone)]
pub struct ProductRepository {
    pool: MySqlPool,
}

impl ProductRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 按 ID 查询商品。
    pub async fn find_by_id(&self, product_id: i32) -> Result<Option<Product>> {
        sqlx::query_as::<_, Product>("SELECT * FROM product WHERE product_id = ? LIMIT 1")
            .bind(product_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 商品分页查询（关键词/分类/品牌/状态/价格过滤 + 排序）。
    pub async fn paginate(
        &self,
        q: &ProductListQuery,
    ) -> Result<(u64, Vec<Product>)> {
        let page = q.page.unwrap_or(1).max(1);
        let page_size = q.page_size.unwrap_or(20).clamp(1, 200);
        let offset = (page - 1) * page_size;

        let keyword = q.keyword.as_ref().map(|k| format!("%{k}%"));
        let (price_min, price_max) = (q.price_min, q.price_max);

        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM product
             WHERE is_delete = 0
               AND (? IS NULL OR product_name LIKE ? OR product_sn LIKE ?)
               AND (? IS NULL OR category_id = ?)
               AND (? IS NULL OR brand_id = ?)
               AND (? IS NULL OR shop_id = ?)
               AND (? IS NULL OR product_status = ?)
               AND (? IS NULL OR product_price >= ?)
               AND (? IS NULL OR product_price <= ?)",
        )
        .bind(&keyword).bind(&keyword).bind(&keyword)
        .bind(q.category_id).bind(q.category_id)
        .bind(q.brand_id).bind(q.brand_id)
        .bind(q.shop_id).bind(q.shop_id)
        .bind(q.product_status).bind(q.product_status)
        .bind(price_min).bind(price_min)
        .bind(price_max).bind(price_max)
        .fetch_one(&self.pool)
        .await
        .map_err(map_err)?;

        // 排序白名单，防止 SQL 注入
        let order_by = match q.sort_by.as_deref() {
            Some("price") => "product_price",
            Some("sales") => "(virtual_sales + click_count)",
            Some("add_time") | None => "add_time",
            Some(_) => "add_time",
        };
        let dir = if q.sort_order.as_deref() == Some("asc") {
            "ASC"
        } else {
            "DESC"
        };
        let sql = format!(
            "SELECT * FROM product
             WHERE is_delete = 0
               AND (? IS NULL OR product_name LIKE ? OR product_sn LIKE ?)
               AND (? IS NULL OR category_id = ?)
               AND (? IS NULL OR brand_id = ?)
               AND (? IS NULL OR shop_id = ?)
               AND (? IS NULL OR product_status = ?)
               AND (? IS NULL OR product_price >= ?)
               AND (? IS NULL OR product_price <= ?)
             ORDER BY {order_by} {dir}, product_id DESC
             LIMIT ? OFFSET ?"
        );

        let rows = sqlx::query_as::<_, Product>(&sql)
            .bind(&keyword).bind(&keyword).bind(&keyword)
            .bind(q.category_id).bind(q.category_id)
            .bind(q.brand_id).bind(q.brand_id)
            .bind(q.shop_id).bind(q.shop_id)
            .bind(q.product_status).bind(q.product_status)
            .bind(price_min).bind(price_min)
            .bind(price_max).bind(price_max)
            .bind(page_size)
            .bind(offset)
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?;

        Ok((total.0 as u64, rows))
    }

    /// 按主键批量查询商品（结算页/导入等场景的 N+1 优化）。
    ///
    /// # Errors
    /// 数据库错误。
    pub async fn find_by_ids(&self, ids: &[i32]) -> Result<Vec<Product>> {
        if ids.is_empty() {
            return Ok(Vec::new());
        }
        let placeholders = vec!["?"; ids.len()].join(", ");
        let sql = format!("SELECT * FROM product WHERE product_id IN ({placeholders})");
        let mut q = sqlx::query_as::<_, Product>(&sql);
        for id in ids {
            q = q.bind(id);
        }
        q.fetch_all(&self.pool).await.map_err(map_err)
    }

    /// 查询商品 SKU 列表。
    pub async fn find_skus(&self, product_id: i32) -> Result<Vec<ProductSku>> {
        sqlx::query_as::<_, ProductSku>(
            "SELECT * FROM product_sku WHERE product_id = ? ORDER BY sku_id ASC",
        )
        .bind(product_id)
        .fetch_all(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 扣减库存（乐观并发安全：WHERE 保证库存充足才更新）。
    ///
    /// 返回实际扣减数量；库存不足时返回 0，调用方转 `Error::InsufficientStock`。
    pub async fn deduct_stock(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<bool> {
        let affected = match sku_id {
            Some(sku) => {
                let r = sqlx::query(
                    "UPDATE product_sku SET sku_stock = sku_stock - ?
                     WHERE sku_id = ? AND sku_stock >= ? AND is_enabled = 1",
                )
                .bind(quantity)
                .bind(sku)
                .bind(quantity)
                .execute(&self.pool)
                .await
                .map_err(map_err)?;
                r.rows_affected()
            }
            None => {
                let r = sqlx::query(
                    "UPDATE product SET product_stock = product_stock - ?
                     WHERE product_id = ? AND product_stock >= ?",
                )
                .bind(quantity)
                .bind(product_id)
                .bind(quantity)
                .execute(&self.pool)
                .await
                .map_err(map_err)?;
                r.rows_affected()
            }
        };
        Ok(affected > 0)
    }

    /// 回滚库存（取消/退款场景）。
    pub async fn restore_stock(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<()> {
        match sku_id {
            Some(sku) => {
                sqlx::query("UPDATE product_sku SET sku_stock = sku_stock + ? WHERE sku_id = ?")
                    .bind(quantity)
                    .bind(sku)
                    .execute(&self.pool)
                    .await
                    .map_err(map_err)?;
            }
            None => {
                sqlx::query("UPDATE product SET product_stock = product_stock + ? WHERE product_id = ?")
                    .bind(quantity)
                    .bind(product_id)
                    .execute(&self.pool)
                    .await
                    .map_err(map_err)?;
            }
        }
        Ok(())
    }

    /// 商品是否上架且库存充足。
    pub async fn check_purchasable(
        &self,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<bool> {
        if let Some(p) = self.find_by_id(product_id).await? {
            if !p.is_on_sale() {
                return Ok(false);
            }
            if let Some(sku) = sku_id {
                let skus = self.find_skus(product_id).await?;
                let found = skus.iter().find(|s| s.sku_id == sku);                return Ok(match found {
                    Some(s) => s.available_stock() >= i64::from(quantity),
                    None => false,
                });
            }
            return Ok(i64::from(p.product_stock) >= i64::from(quantity));
        }
        Ok(false)
    }

    /// 查询商品当前售价（促销价优先）。
    pub async fn current_price(&self, product_id: i32) -> Result<Option<Decimal>> {
        let p = self.find_by_id(product_id).await?;
        Ok(p.map(|p| p.current_price()))
    }

    /// 批量查询商品售价（消除下单循环内 N+1 查询）。
    ///
    /// 返回 `product_id -> product_price`；库中不存在的 ID 不出现在结果里，
    /// 调用方需自行回退（如取购物车行原价）。空列表直接返回空 map，不查库。
    pub async fn current_prices(
        &self,
        product_ids: &[i32],
    ) -> Result<std::collections::HashMap<i32, Option<Decimal>>> {
        let mut map = std::collections::HashMap::with_capacity(product_ids.len());
        if product_ids.is_empty() {
            return Ok(map);
        }

        let placeholders = vec!["?"; product_ids.len()].join(", ");
        let sql = format!(
            "SELECT product_id, product_price FROM product WHERE product_id IN ({placeholders})"
        );
        let mut query = sqlx::query_as::<_, (i32, Option<Decimal>)>(&sql);
        for pid in product_ids {
            query = query.bind(pid);
        }
        for (pid, price) in query.fetch_all(&self.pool).await.map_err(map_err)? {
            map.insert(pid, price);
        }
        Ok(map)
    }

    /// 事务内扣减库存（供下单使用，随外层事务一起提交或回滚）。
    pub async fn deduct_stock_in_tx(
        &self,
        tx: &mut sqlx::Transaction<'_, sqlx::MySql>,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<bool> {
        let affected = match sku_id {
            Some(sku) => {
                sqlx::query(
                    "UPDATE product_sku SET sku_stock = sku_stock - ?
                     WHERE sku_id = ? AND sku_stock >= ? AND is_enabled = 1",
                )
                .bind(quantity)
                .bind(sku)
                .bind(quantity)
                .execute(&mut **tx)
                .await
                .map_err(map_err)?
                .rows_affected()
            }
            None => {
                sqlx::query(
                    "UPDATE product SET product_stock = product_stock - ?
                     WHERE product_id = ? AND product_stock >= ?",
                )
                .bind(quantity)
                .bind(product_id)
                .bind(quantity)
                .execute(&mut **tx)
                .await
                .map_err(map_err)?
                .rows_affected()
            }
        };
        Ok(affected > 0)
    }

    /// 事务内回滚库存（下单失败时补偿）。
    pub async fn restore_stock_in_tx(
        &self,
        tx: &mut sqlx::Transaction<'_, sqlx::MySql>,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
    ) -> Result<()> {
        match sku_id {
            Some(sku) => {
                sqlx::query("UPDATE product_sku SET sku_stock = sku_stock + ? WHERE sku_id = ?")
                    .bind(quantity)
                    .bind(sku)
                    .execute(&mut **tx)
                    .await
                    .map_err(map_err)?;
            }
            None => {
                sqlx::query("UPDATE product SET product_stock = product_stock + ? WHERE product_id = ?")
                    .bind(quantity)
                    .bind(product_id)
                    .execute(&mut **tx)
                    .await
                    .map_err(map_err)?;
            }
        }
        Ok(())
    }
}

fn map_err(e: sqlx::Error) -> Error {
    match e {
        sqlx::Error::RowNotFound => Error::NotFound("商品不存在".into()),
        other => Error::Database(other.to_string()),
    }
}
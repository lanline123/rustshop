//! 代客下单配送方式域仓储（trade::proxy_shopping_delivery）。
//!
//! 表：`cart`；查询使用运行时 `sqlx::query_as`。配送计算本身未迁移，
//! 仓储仅提供购物车存在性/数量校验。

use rust_decimal::Decimal;
use sqlx::MySqlPool;

use tigshop_core::models::trade::proxy_shopping_delivery::CART_TYPE_ADMIN_SHOPPING;
use tigshop_core::{Error, Result};

/// 代客下单配送方式仓储。
#[derive(Clone)]
pub struct ProxyShoppingDeliveryRepository {
    pool: MySqlPool,
}

impl ProxyShoppingDeliveryRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池。
    #[must_use]
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    /// 代客下单购物车商品数量（校验用户是否有可配送商品）。
    pub async fn sum_cart_quantity(&self, user_id: i32, shop_id: Option<i32>) -> Result<i64> {
        let row: Option<(Option<Decimal>,)> = sqlx::query_as(
            "SELECT SUM(quantity) FROM cart
             WHERE user_id = ? AND type = ? AND (? IS NULL OR shop_id = ?)",
        )
        .bind(user_id)
        .bind(CART_TYPE_ADMIN_SHOPPING)
        .bind(shop_id)
        .bind(shop_id)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(row
            .and_then(|(sum,)| sum)
            .map(|sum| i64::try_from(sum).unwrap_or(0))
            .unwrap_or(0))
    }
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn repository_type_exists() {
        assert!(std::any::type_name::<ProxyShoppingDeliveryRepository>()
            .contains("ProxyShoppingDelivery"));
    }
}


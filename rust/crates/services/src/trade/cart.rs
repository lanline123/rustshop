//! 购物车服务：加购、改量、勾选、删除、价格汇总。
//! 对应原 Java `CartServiceImpl`。

use std::sync::Arc;

use rust_decimal::Decimal;
use serde::Serialize;
use tigshop_core::entities::Cart;
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

use crate::catalog::product::ProductService;

/// 购物车行 VO。
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct CartItemVO {
    pub cart_id: i32,
    pub product_id: i32,
    pub product_name: String,
    pub pic_thumb: String,
    pub sku_id: Option<i32>,
    pub sku_data: Option<String>,
    pub price: Decimal,
    pub quantity: i32,
    pub subtotal: Decimal,
    pub checked: bool,
    pub shop_id: i32,
    /// 库存是否仍充足（下单前提示）
    pub stock_enough: bool,
}

/// 购物车汇总。
#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct CartSummaryVO {
    pub items: Vec<CartItemVO>,
    /// 勾选商品总数
    pub checked_quantity: i32,
    /// 勾选商品总额
    pub checked_amount: Decimal,
    pub total_quantity: i32,
}

#[derive(Clone)]
pub struct CartService {
    repos: Arc<Repositories>,
    product: ProductService,
}

impl CartService {
    pub fn new(repos: Arc<Repositories>, product: ProductService) -> Self {
        Self { repos, product }
    }

    /// 加入购物车（同款累加数量；拼团上下文以独立行保存）。
    ///
    /// `is_quick = true` 表示“立即购买”：先取消该会员其它购物车行的勾选，
    /// 保证结算页只带当前这一件商品。
    pub async fn add(
        &self,
        user_id: i32,
        product_id: i32,
        sku_id: Option<i32>,
        quantity: i32,
        groupon_data: Option<&tigshop_core::models::trade::check::GrouponCartData>,
        is_quick: bool,
    ) -> Result<i32> {
        if quantity <= 0 {
            return Err(Error::Validation("数量必须大于0".into()));
        }
        // 校验商品可购
        self.product.check_purchasable(product_id, sku_id, quantity).await?;

        if is_quick {
            self.repos.trade.cart.uncheck_all(user_id).await?;
        }

        // 已存在同款则累加（拼团商品不与普通购物车行合并）
        if groupon_data.is_none() {
            if let Some(existing) = self
                .repos.trade.cart
                .find_line(user_id, product_id, sku_id)
                .await?
            {
                let new_qty = existing.quantity + quantity;
                self.product.check_purchasable(product_id, sku_id, new_qty).await?;
                let cart_id = existing.cart_id;
                self.repos.trade.cart.update_quantity(cart_id, user_id, new_qty).await?;
                if is_quick {
                    self.repos.trade.cart.update_checked(cart_id, user_id, true).await?;
                }
                return Ok(cart_id);
            }
        }

        let price = self
            .product
            .detail(product_id)
            .await
            .map(|v| v.product_price)
            .unwrap_or_default();

        let cart = Cart {
            user_id,
            product_id,
            sku_id,
            quantity,
            original_price: price,
            market_price: Some(price),
            is_checked: 1,
            r#type: 0,
            extra_data: groupon_data
                .and_then(|g| serde_json::to_string(g).ok()),
            ..Default::default()
        };
        self.repos.trade.cart.insert(&cart).await
    }

    /// 修改数量。
    pub async fn update_quantity(
        &self,
        user_id: i32,
        cart_id: i32,
        quantity: i32,
    ) -> Result<()> {
        if quantity <= 0 {
            return Err(Error::Validation("数量必须大于0".into()));
        }
        let cart = self
            .repos.trade.cart
            .find_owned(cart_id, user_id)
            .await?
            .ok_or_else(|| Error::NotFound("购物车记录不存在".into()))?;
        self.product
            .check_purchasable(cart.product_id, cart.sku_id, quantity)
            .await?;
        self.repos.trade.cart.update_quantity(cart_id, user_id, quantity).await
    }

    /// 勾选/取消勾选。
    pub async fn set_checked(&self, user_id: i32, cart_id: i32, checked: bool) -> Result<()> {
        self.repos.trade.cart.update_checked(cart_id, user_id, checked).await
    }

    /// 删除指定行。
    pub async fn remove(&self, user_id: i32, cart_ids: &[i32]) -> Result<()> {
        self.repos.trade.cart.delete(cart_ids, user_id).await?;
        Ok(())
    }

    /// 清空。
    pub async fn clear(&self, user_id: i32) -> Result<()> {
        self.repos.trade.cart.clear(user_id).await?;
        Ok(())
    }

    /// 购物车汇总（实时校验库存/价格）。
    pub async fn summary(&self, user_id: i32) -> Result<CartSummaryVO> {
        let carts = self.repos.trade.cart.find_by_user(user_id, None).await?;
        let mut items = Vec::with_capacity(carts.len());
        let (mut checked_qty, mut checked_amount, mut total_qty) = (0, Decimal::ZERO, 0);

        for c in carts {
            let pid = c.product_id;
            let qty = c.quantity;
            // original_price 为 NOT NULL；market_price 可空，仅作兜底
            let price = if c.original_price > Decimal::ZERO {
                c.original_price
            } else {
                c.market_price.unwrap_or_default()
            };
            let subtotal = price * Decimal::from(qty);
            let checked = c.is_checked();
            let stock_enough = self
                .product
                .check_purchasable(pid, c.sku_id, qty.max(1))
                .await
                .is_ok();

            if checked {
                checked_qty += qty;
                checked_amount += subtotal;
            }
            total_qty += qty;

            items.push(CartItemVO {
                cart_id: c.cart_id,
                product_id: pid,
                product_name: String::new(), // 需要时通过商品详情补充
                pic_thumb: c.pic_thumb.unwrap_or_default(),
                sku_id: c.sku_id,
                sku_data: c.sku_data,
                price,
                quantity: qty,
                subtotal,
                checked,
                shop_id: c.shop_id,
                stock_enough,
            });
        }

        Ok(CartSummaryVO {
            items,
            checked_quantity: checked_qty,
            checked_amount,
            total_quantity: total_qty,
        })
    }

    /// 获取已勾选且可结算的购物车行（下单用）。
    pub async fn checked_lines(&self, user_id: i32, cart_ids: &[i32]) -> Result<Vec<Cart>> {
        let carts = self.repos.trade.cart.find_by_user(user_id, None).await?;
        Ok(carts
            .into_iter()
            .filter(|c| c.is_checked() && (cart_ids.is_empty() || cart_ids.contains(&c.cart_id)))
            .collect())
    }
}
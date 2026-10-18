//! 代客下单购物车域服务（trade::proxy_shopping_cart）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.proxyshopping.ProxyShoppingCartController`
//! + `CartService` 的代客下单子集（`type = 15` 代客下单购物车）。
//!
//! # 逐端点口径
//!
//! | 端点 | 实现 | 未迁移部分 |
//! |------|------|------------|
//! | `list` | 用户 + 门店维度读 DB 购物车行，按门店分组，返回快照价小计 | 优惠/活动/改价/运费计算字段（返回 null） |
//! | `addToCart` | 用户/商品/类型/上架/规格/库存/单买冲突校验 + 合并或插入 | 秒杀/拼团/抽奖/礼品卡库存等特殊活动校验 |
//! | `clearCart` | 删除用户 `type = 15` 全部行 | 改价缓存清理（CartGlobalChangePrice 未迁移） |
//! | `removeItem` | 按购物车 id 批量删除 | — |
//! | `updateItem` | 数量校验 + 库存校验 + 更新数量 | 价格重算/限购校验（限购引擎未迁移） |
//! | `updateCheck` | 逐行校验归属与选中状态后批量更新 | 勾选时的商品可用性引擎 |
//! | `getCount` | 用户 + 可选门店维度 `SUM(quantity)` | — |
//!
//! 未迁移的计算类子步骤一律返回明确业务错误或 null，不编造数值。

use std::collections::HashMap;
use std::sync::Arc;

use rust_decimal::Decimal;
use tigshop_common::utils::now_seconds;
use tigshop_core::models::trade::proxy_shopping_cart::{
    cart_type_from_product_type, is_single_buy, sum_quantity, validate_checked,
    validate_product_type, validate_user_id, CartByStoreVO, CartListByStoreVO, CartTotalVO,
    ExchangeProductToCartVO, ProxyAddToCartDTO, ProxyCartCountQuery, ProxyCartListDTO,
    ProxyClearCartDTO, ProxyRemoveItemDTO, ProxyUpdateCheckDTO, ProxyUpdateItemDTO,
    CART_TYPE_ADMIN_SHOPPING, MSG_CART_CONFLICT, MSG_CART_ID_REQUIRED, MSG_CART_ITEM_NOT_EXIST,
    MSG_OFF_SALE, MSG_PRODUCT_NOT_EXIST, MSG_QUANTITY_INVALID, MSG_QUANTITY_REQUIRED,
    MSG_SKU_DISABLED, MSG_STOCK_INSUFFICIENT, PRODUCT_STATUS_ON_SALE,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::proxy_shopping_cart::ProxyShoppingCartRepository;
use tigshop_infrastructure::db::Repositories;

/// 代客下单购物车域服务。
pub struct ProxyShoppingCartService {
    repos: Arc<Repositories>,
}

impl ProxyShoppingCartService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED 访问：`self.repos.trade.proxy_shopping_cart`）。
    #[must_use]
    pub fn repo(&self) -> &ProxyShoppingCartRepository {
        &self.repos.trade.proxy_shopping_cart
    }

    /// 购物车列表（按门店分组；不含优惠/活动计算）。
    pub async fn list(
        &self,
        dto: &ProxyCartListDTO,
        header_shop_id: Option<i32>,
    ) -> Result<CartListByStoreVO> {
        let user_id = validate_user_id(dto.user_id).map_err(Error::Validation)?;
        let shop_id = header_shop_id.or(dto.shop_id);
        let rows = self
            .repo()
            .list_carts(user_id, shop_id, dto.product_id, dto.is_checked)
            .await?;

        let mut product_amount = Decimal::ZERO;
        let mut total_count = 0_i64;
        let mut checked_count = 0_i64;
        let mut groups: Vec<CartByStoreVO> = Vec::new();
        let mut index: HashMap<i32, usize> = HashMap::new();
        for row in &rows {
            product_amount += row.original_price * Decimal::from(row.quantity);
            total_count += i64::from(row.quantity);
            if row.is_checked == 1 {
                checked_count += i64::from(row.quantity);
            }
            let pos = match index.get(&row.shop_id) {
                Some(pos) => *pos,
                None => {
                    groups.push(CartByStoreVO {
                        shop_id: row.shop_id,
                        shop_title: row.shop_title.clone(),
                        carts: Vec::new(),
                    });
                    let pos = groups.len() - 1;
                    index.insert(row.shop_id, pos);
                    pos
                }
            };
            groups[pos].carts.push(row.to_vo());
        }
        Ok(CartListByStoreVO {
            total: CartTotalVO {
                product_amount,
                total_count,
                checked_count,
                // 以下优惠/活动/改价/服务费字段依赖未迁移的价格引擎 → null（不编造）
                ..Default::default()
            },
            has_change_price: false,
            cart_list: groups,
            is_wholesale_buyer: false,
        })
    }

    /// 加入购物车（Java `addProductToCart` + 控制器校验）。
    pub async fn add_to_cart(
        &self,
        dto: &ProxyAddToCartDTO,
        header_shop_id: i32,
    ) -> Result<ExchangeProductToCartVO> {
        let user_id = validate_user_id(dto.user_id).map_err(Error::Validation)?;
        let repo = self.repo();
        let product_id = dto
            .id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::biz_msg(MSG_PRODUCT_NOT_EXIST))?;
        let Some(product) = repo.product_snapshot(product_id).await? else {
            return Err(Error::biz_msg(MSG_PRODUCT_NOT_EXIST));
        };
        if product.is_delete != 0 {
            return Err(Error::biz_msg(MSG_PRODUCT_NOT_EXIST));
        }
        validate_product_type(Some(product.product_type)).map_err(Error::biz_msg)?;
        if product.product_status != PRODUCT_STATUS_ON_SALE {
            return Err(Error::biz_msg(MSG_OFF_SALE));
        }
        let number = dto.number.unwrap_or(1);
        if number <= 0 {
            return Err(Error::biz_msg(MSG_QUANTITY_INVALID));
        }
        let sku_id = dto.sku_id.unwrap_or(0).max(0);
        let sku = repo.sku_snapshot(product.product_id, sku_id).await?;
        if sku_id > 0 {
            match &sku {
                Some(sku) if sku.is_enabled == 1 => {}
                _ => return Err(Error::biz_msg(MSG_SKU_DISABLED)),
            }
        }
        // 库存校验：SKU 优先，否则商品库存（Java getCartStock）
        let stock = if sku_id > 0 {
            sku.as_ref().and_then(|s| s.sku_stock)
        } else {
            Some(product.product_stock)
        };
        let cart_type = cart_type_from_product_type(product.product_type);
        let existing = repo
            .find_same_cart(user_id, product.product_id, sku_id, cart_type)
            .await?;
        let want = number + existing.as_ref().map(|row| row.quantity).unwrap_or(0);
        if let Some(stock) = stock {
            if want > stock {
                return Err(Error::biz_msg(MSG_STOCK_INSUFFICIENT));
            }
        }
        // 单买商品冲突校验（Java isCanAddToCart）
        let carts = repo.list_existing_carts(user_id, header_shop_id).await?;
        if let Some(first) = carts.iter().find(|row| is_single_buy(row.product_type)) {
            let same_product = first.product_id == product.product_id;
            let same_sku = first.sku_id.max(0) == sku_id;
            if !same_product || !same_sku {
                return Err(Error::biz_msg(MSG_CART_CONFLICT));
            }
        } else if !carts.is_empty() && is_single_buy(product.product_type) {
            return Err(Error::biz_msg(MSG_CART_CONFLICT));
        }
        let now = now_i32();
        match existing {
            Some(row) => {
                repo.add_cart_quantity(row.cart_id, number, now).await?;
            }
            None => {
                repo.insert_cart(
                    user_id,
                    &product,
                    sku_id,
                    sku.as_ref(),
                    number,
                    cart_type,
                    now,
                )
                .await?;
            }
        }
        tracing::info!("代客下单加购成功: user_id={user_id}, product_id={}", product.product_id);
        Ok(ExchangeProductToCartVO {
            item: true,
            flow_type: CART_TYPE_ADMIN_SHOPPING,
        })
    }

    /// 清空购物车（Java `clearCart`）。
    pub async fn clear_cart(&self, dto: &ProxyClearCartDTO) -> Result<()> {
        let user_id = validate_user_id(dto.user_id).map_err(Error::Validation)?;
        self.repo()
            .clear_carts(user_id, CART_TYPE_ADMIN_SHOPPING)
            .await?;
        Ok(())
    }

    /// 删除购物车商品（Java `removeBatchByIds`；空列表为无操作）。
    pub async fn remove_item(&self, dto: &ProxyRemoveItemDTO) -> Result<()> {
        let Some(ids) = dto.cart_ids.as_deref().filter(|ids| !ids.is_empty()) else {
            return Ok(());
        };
        self.repo().delete_by_ids(ids).await?;
        Ok(())
    }

    /// 更新购物车数量（Java `updateCartItem`）。
    pub async fn update_item(&self, dto: &ProxyUpdateItemDTO) -> Result<()> {
        let quantity = dto
            .data
            .as_ref()
            .and_then(|data| data.quantity)
            .ok_or_else(|| Error::Validation(MSG_QUANTITY_REQUIRED.into()))?;
        if quantity <= 0 {
            return Err(Error::biz_msg(MSG_QUANTITY_INVALID));
        }
        let cart_id = dto
            .cart_id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::Validation(MSG_CART_ID_REQUIRED.into()))?;
        let repo = self.repo();
        let rows = repo.find_carts_by_ids(&[cart_id]).await?;
        let Some(cart) = rows.first() else {
            return Err(Error::biz_msg(MSG_CART_ITEM_NOT_EXIST));
        };
        // 库存校验（Java assertCartCanChangeQuantity）
        let sku_id = cart.sku_id.unwrap_or(0);
        let stock = if sku_id > 0 {
            repo.sku_snapshot(cart.product_id, sku_id)
                .await?
                .and_then(|sku| sku.sku_stock)
        } else {
            cart.product_stock
        };
        if let Some(stock) = stock {
            if quantity > stock {
                return Err(Error::biz_msg(MSG_STOCK_INSUFFICIENT));
            }
        }
        repo.update_quantity(cart_id, quantity, now_i32()).await?;
        Ok(())
    }

    /// 更新选中状态（Java `updateCheck`；空 data 无操作）。
    pub async fn update_check(&self, dto: &ProxyUpdateCheckDTO) -> Result<()> {
        let user_id = validate_user_id(dto.user_id).map_err(Error::Validation)?;
        let Some(items) = dto.data.as_deref() else {
            return Ok(());
        };
        if items.is_empty() {
            return Ok(());
        }
        let repo = self.repo();
        for item in items {
            let cart_id = item
                .cart_id
                .filter(|id| *id > 0)
                .ok_or_else(|| Error::Validation(MSG_CART_ID_REQUIRED.into()))?;
            let checked = validate_checked(item.is_checked).map_err(Error::biz_msg)?;
            let rows = repo.find_carts_by_ids(&[cart_id]).await?;
            match rows.first() {
                Some(cart) if cart.user_id == user_id => {
                    repo.update_checked(cart_id, checked).await?;
                }
                _ => return Err(Error::biz_msg(MSG_CART_ITEM_NOT_EXIST)),
            }
        }
        Ok(())
    }

    /// 购物车商品数量（Java `getCount`）。
    pub async fn get_count(&self, q: &ProxyCartCountQuery) -> Result<i32> {
        let user_id = validate_user_id(q.user_id).map_err(Error::Validation)?;
        let total = self.repo().sum_quantity(user_id, q.shop_id).await?;
        Ok(i32::try_from(total).unwrap_or(i32::MAX))
    }
}

fn now_i32() -> i32 {
    i32::try_from(now_seconds()).unwrap_or(i32::MAX)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn quantity_and_count_bounds() {
        assert_eq!(sum_quantity([1, 2, 3]), 6);
        assert_eq!(i32::try_from(7_i64).unwrap_or(i32::MAX), 7);
    }

    #[test]
    fn cart_type_mapping_available() {
        assert_eq!(cart_type_from_product_type(2), 6);
        assert_eq!(CART_TYPE_ADMIN_SHOPPING, 15);
    }
}

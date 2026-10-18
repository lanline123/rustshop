//! 订单结算域服务（trade::check）。
//!
//! 对应原 Java `com.tigshop.service.order.impl.OrderCheckServiceImpl` 的
//! `getAvailablePaymentType` / `getLastInvoice` / `getOrderCheckData` /
//! `getStoreShippingType`。
//!
//! # 实现口径
//!
//! - 结算页 `index` / `update` / `updateCoupon`：勾选购物车行 → 商品金额 →
//!   可用优惠券（选中券按 `CouponService::calc_discount` 抵扣）→ 余额/积分展示 →
//!   支付方式/配送方式回显；运费模板、秒杀/拼团/满减活动暂未接入（运费 0、活动折扣 0）。
//! - `getStoreShippingType`：按店铺返回 `shipping_type` 列表（`shop_id=0` 视为全局）。
//! - `available_payment_types` / `last_invoice`：完整 DB/config 实现。

use std::collections::{BTreeMap, HashMap};
use std::sync::Arc;

use rust_decimal::Decimal;

use tigshop_core::entities::{Cart, Product};
use tigshop_core::models::trade::check::{
    build_payment_types, config_enabled, parse_sku_data, CartStoreTotalVO, CheckCartByStoreVO,
    CheckCartVO, CheckCouponVO, CheckItemVO, CheckTotalVO, CouponListVO, OrderCheckParam,
    OrderCheckVO, OrderInvoiceVO, PaymentTypeVO, ShippingTypeVO, StoreShippingTypeQuery,
    CONFIG_USE_OFFLINE, CONFIG_USE_SURPLUS, MSG_CART_EMPTY, PAYMENT_TYPE_ONLINE,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::check::CheckRepository;
use tigshop_infrastructure::db::Repositories;

use crate::marketing::coupon::CouponService;

/// 订单结算域服务。
pub struct CheckService {
    repos: Arc<Repositories>,
}

impl CheckService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED 访问：`self.repos.trade.check`）。
    #[must_use]
    pub fn repo(&self) -> &CheckRepository {
        &self.repos.trade.check
    }

    /// 可用支付方式（Java `getAvailablePaymentType`）。
    pub async fn available_payment_types(
        &self,
        flow_type: Option<i32>,
    ) -> Result<Vec<PaymentTypeVO>> {
        let repo = self.repo();
        let use_offline =
            config_enabled(repo.find_config_value(CONFIG_USE_OFFLINE).await?.as_deref());
        let use_surplus =
            config_enabled(repo.find_config_value(CONFIG_USE_SURPLUS).await?.as_deref());
        Ok(build_payment_types(
            use_offline,
            use_surplus,
            false,
            flow_type,
        ))
    }

    /// 最近一条订单发票（Java `getLastInvoice`）。
    pub async fn last_invoice(
        &self,
        user_id: i32,
        invoice_type: Option<i32>,
        title_type: Option<i32>,
    ) -> Result<Option<OrderInvoiceVO>> {
        self.repo()
            .find_last_invoice(user_id, invoice_type, title_type)
            .await
    }

    /// 结算页 index。
    pub async fn order_check_index(
        &self,
        user_id: i32,
        param: &OrderCheckParam,
    ) -> Result<OrderCheckVO> {
        self.build_check(user_id, param).await
    }

    /// 结算页 update（地址/配送/优惠券变化后重算）。
    pub async fn order_check_update(
        &self,
        user_id: i32,
        param: &OrderCheckParam,
    ) -> Result<OrderCheckVO> {
        self.build_check(user_id, param).await
    }

    /// 结算页 updateCoupon（只关注优惠券选择，重算金额）。
    pub async fn order_check_update_coupon(
        &self,
        user_id: i32,
        param: &OrderCheckParam,
    ) -> Result<OrderCheckVO> {
        self.build_check(user_id, param).await
    }

    /// 店铺配送方式（Java `getStoreShippingType`）。
    ///
    /// 返回 `{shopId: [ShippingType...]}`；`shipping_type.shop_id = 0` 作为全局方式
    /// 出现在每个店铺列表里。当前没有勾选商品时，按配送方式表自身的店铺分组返回。
    pub async fn store_shipping_types(
        &self,
        query: &StoreShippingTypeQuery,
    ) -> Result<HashMap<i32, Vec<ShippingTypeVO>>> {
        let user_id = query.user_id.unwrap_or(0);
        let mut shop_ids: Vec<i32> = Vec::new();
        if user_id > 0 {
            let carts = self.repos.trade.cart.find_by_user(user_id, None).await?;
            for cart in carts.iter().filter(|c| c.is_checked()) {
                if !shop_ids.contains(&cart.shop_id) {
                    shop_ids.push(cart.shop_id);
                }
            }
        }
        let all = self.repos.logistics.shipping_type.list_all().await?;
        if shop_ids.is_empty() {
            let mut grouped: HashMap<i32, Vec<ShippingTypeVO>> = HashMap::new();
            for item in &all {
                grouped
                    .entry(item.shop_id)
                    .or_default()
                    .push(to_shipping_vo(item));
            }
            if grouped.is_empty() {
                grouped.insert(shop_ids.first().copied().unwrap_or(0), default_shipping_types(0));
            }
            return Ok(grouped);
        }
        let mut grouped: HashMap<i32, Vec<ShippingTypeVO>> = HashMap::new();
        for shop_id in shop_ids {
            let list: Vec<ShippingTypeVO> = all
                .iter()
                .filter(|s| s.shop_id == shop_id || s.shop_id == 0)
                .map(to_shipping_vo)
                .collect();
            if list.is_empty() {
                grouped.insert(shop_id, default_shipping_types(shop_id));
            } else {
                grouped.insert(shop_id, list);
            }
        }
        Ok(grouped)
    }

    /// 结算页数据装配。
    async fn build_check(&self, user_id: i32, param: &OrderCheckParam) -> Result<OrderCheckVO> {
        let carts: Vec<Cart> = self
            .repos
            .trade
            .cart
            .find_by_user(user_id, None)
            .await?
            .into_iter()
            .filter(Cart::is_checked)
            .collect();
        if carts.is_empty() {
            return Err(Error::biz(1001, MSG_CART_EMPTY));
        }

        let mut product_ids: Vec<i32> = Vec::new();
        for cart in &carts {
            if !product_ids.contains(&cart.product_id) {
                product_ids.push(cart.product_id);
            }
        }
        let products = self.repos.catalog.product.find_by_ids(&product_ids).await?;
        let product_map: HashMap<i32, &Product> =
            products.iter().map(|p| (p.product_id, p)).collect();

        // 商品金额 + 按店铺分组
        let mut shop_groups: BTreeMap<i32, Vec<&Cart>> = BTreeMap::new();
        let mut product_amount = Decimal::ZERO;
        let mut total_quantity: i32 = 0;
        for cart in &carts {
            let price = product_map
                .get(&cart.product_id)
                .map(|p| p.product_price)
                .unwrap_or(cart.original_price);
            product_amount += price * Decimal::from(cart.quantity.max(1));
            total_quantity += cart.quantity.max(1);
            shop_groups.entry(cart.shop_id).or_default().push(cart);
        }

        // 优惠券：可用券列表 + 选中券抵扣
        let coupon_service = CouponService::new(self.repos.clone());
        let usable = coupon_service.my_usable_coupons(user_id).await?;
        let selected: Vec<i32> = param
            .use_coupon_ids
            .clone()
            .or_else(|| param.select_user_coupon_ids.clone())
            .unwrap_or_default();
        let coupon_amount = if selected.is_empty() {
            Decimal::ZERO
        } else {
            match coupon_service
                .calc_discount(user_id, &selected, product_amount)
                .await
            {
                Ok(amount) => amount,
                Err(e) => {
                    tracing::warn!("结算页优惠券试算失败（按不使用券处理）: {e}");
                    Decimal::ZERO
                }
            }
        };

        let user = self.repos.member.user.find_by_id(user_id).await?;
        let balance = user.as_ref().map(|u| u.balance).unwrap_or_default();
        let available_points = user.as_ref().map(|u| u.points).unwrap_or(0);
        let default_address_id = user.as_ref().map(|u| u.address_id).unwrap_or(0);
        let address_id = param
            .address_id
            .filter(|id| *id > 0)
            .unwrap_or(default_address_id);

        let payments = self.available_payment_types(param.flow_type).await?;
        let pay_type_id = param
            .pay_type_id
            .filter(|id| *id > 0)
            .or_else(|| payments.first().map(|p| p.type_id))
            .unwrap_or(PAYMENT_TYPE_ONLINE);
        let flow_type = param.flow_type.unwrap_or(1);

        let all_shipping = self.repos.logistics.shipping_type.list_all().await?;
        let mut cart_list: Vec<CheckCartByStoreVO> = Vec::new();
        for (shop_id, lines) in &shop_groups {
            let mut carts_vo: Vec<CheckCartVO> = Vec::new();
            for cart in lines {
                let product = product_map.get(&cart.product_id);
                let price = product
                    .map(|p| p.product_price)
                    .unwrap_or(cart.original_price);
                let qty = cart.quantity.max(1);
                carts_vo.push(CheckCartVO {
                    cart_id: cart.cart_id,
                    user_id: cart.user_id,
                    product_id: cart.product_id,
                    product_sn: cart.product_sn.clone(),
                    seo_link: product.and_then(|p| p.seo_identification.clone()),
                    pic_thumb: cart
                        .pic_thumb
                        .clone()
                        .or_else(|| product.map(|p| p.pic_thumb.clone()))
                        .unwrap_or_default(),
                    market_price: cart
                        .market_price
                        .or_else(|| product.map(|p| p.market_price)),
                    original_price: cart.original_price,
                    quantity: qty,
                    sku_id: cart.sku_id.unwrap_or(0),
                    sku_data: parse_sku_data(cart.sku_data.as_deref()),
                    extra_sku_data: parse_sku_data(cart.extra_sku_data.as_deref()),
                    product_type: product
                        .and_then(|p| p.product_type)
                        .unwrap_or(cart.product_type),
                    is_checked: cart.is_checked(),
                    shop_id: cart.shop_id,
                    r#type: cart.r#type,
                    update_time: cart.update_time,
                    product_name: product.map(|p| p.product_name.clone()).unwrap_or_default(),
                    product_price: price,
                    category_id: product.map(|p| p.category_id).unwrap_or(0),
                    brand_id: product.map(|p| p.brand_id).unwrap_or(0),
                    product_stock: product.map(|p| p.product_stock).unwrap_or(0),
                    price,
                    stock: product.map(|p| p.product_stock).unwrap_or(0),
                    has_sku: cart.sku_id.unwrap_or(0) > 0,
                    subtotal: price * Decimal::from(qty),
                    origin_price: cart.original_price,
                    is_disabled: product.map(|p| p.product_status != 1).unwrap_or(false),
                });
            }
            let shop_title = product_map
                .get(&carts_vo.first().map(|c| c.product_id).unwrap_or_default())
                .map(|p| p.product_name.clone())
                .unwrap_or_default();
            cart_list.push(CheckCartByStoreVO {
                shop_id: *shop_id,
                shop_title: if *shop_id == 0 {
                    "平台自营".to_string()
                } else {
                    shop_title
                },
                no_shipping: 0,
                min_order_amount_diff: "0".to_string(),
                is_not_reach_min_order_amount: false,
                carts: carts_vo,
                total: CartStoreTotalVO::default(),
                used_promotions: Vec::new(),
                enable_use_promotion: Vec::new(),
                gift: Vec::new(),
            });
        }

        let mut store_shipping_fee: HashMap<i32, Decimal> = HashMap::new();
        for shop_id in shop_groups.keys() {
            store_shipping_fee.insert(*shop_id, Decimal::ZERO);
        }

        let discount_after = (product_amount - coupon_amount).max(Decimal::ZERO);
        let total_amount = (product_amount - coupon_amount).max(Decimal::ZERO);
        let total = CheckTotalVO {
            product_amount,
            checked_count: total_quantity,
            discounts: coupon_amount,
            discount_after: Some(discount_after.to_string()),
            total_count: total_quantity,
            discount_coupon_amount: coupon_amount,
            coupon_amount,
            discount_amount: coupon_amount,
            shipping_fee: Decimal::ZERO,
            store_shipping_fee,
            total_amount,
            balance,
            unpaid_amount: total_amount,
            ..Default::default()
        };

        let enable_coupons: Vec<CheckCouponVO> = usable
            .iter()
            .map(|uc| CheckCouponVO {
                id: uc.id,
                coupon_id: uc.coupon_id,
                coupon_name: uc.coupon_name.clone(),
                coupon_type: uc.coupon_type,
                coupon_money: uc.coupon_money,
                min_order_amount: uc.min_order_amount,
                coupon_desc: uc.coupon_desc.clone(),
                is_global: uc.send_range == 0,
                coupon_discount: uc.coupon_discount,
                shop_id: 0,
                end_date: format_epoch(uc.end_date),
                user_coupon_id: uc.id,
                selected: selected.contains(&uc.id),
            })
            .collect();

        let shipping_type_value = param
            .shipping_type
            .clone()
            .unwrap_or_else(|| serde_json::json!({}));

        Ok(OrderCheckVO {
            cart_list,
            total,
            has_change_price: false,
            change_price_type: 0,
            price_change_type: 0,
            cart_change_price_type: 0,
            available_points,
            coupon_list: CouponListVO {
                enable_coupons,
                disable_coupons: Vec::new(),
            },
            item: CheckItemVO {
                address_id,
                shipping_type: shipping_type_value,
                pay_type_id,
                use_point: param.use_point.unwrap_or(0),
                use_balance: param.use_balance.unwrap_or_default(),
                use_credit: param.use_credit.unwrap_or(0),
                use_coupon_ids: Some(selected.clone()),
                select_user_coupon_ids: Some(selected),
                flow_type,
                delivery_type: param.delivery_type,
            },
            use_coupon_ids: param.use_coupon_ids.clone(),
            select_user_coupon_ids: param.select_user_coupon_ids.clone(),
            tmpl_ids: all_shipping
                .iter()
                .map(|s| s.shipping_type_id.to_string())
                .collect(),
            flow_type,
            message: None,
            delivery_option: None,
            product_presale: None,
            credit_payment_info: None,
            coupon_exclude_amount: "0".to_string(),
            points_exclude_amount: "0".to_string(),
        })
    }
}

fn to_shipping_vo(item: &tigshop_core::entities::ShippingType) -> ShippingTypeVO {
    ShippingTypeVO {
        shipping_type_id: item.shipping_type_id,
        shipping_type_name: item.shipping_type_name.clone(),
        origin_shipping_type_name: item.shipping_type_name.clone(),
        shipping_default_id: Some(item.shipping_default_id),
        shipping_type_desc: Some(item.shipping_type_desc.clone()),
        shipping_time_desc: Some(item.shipping_time_desc.clone()),
        is_support_cod: Some(item.is_support_cod),
        sort_order: item.sort_order,
        shop_id: item.shop_id,
    }
}

fn default_shipping_types(shop_id: i32) -> Vec<ShippingTypeVO> {
    vec![ShippingTypeVO {
        shipping_type_id: 1,
        shipping_type_name: "快递发货".to_string(),
        origin_shipping_type_name: "快递发货".to_string(),
        shipping_default_id: Some(0),
        shipping_type_desc: Some("快递发货".to_string()),
        shipping_time_desc: None,
        is_support_cod: Some(0),
        sort_order: 0,
        shop_id,
    }]
}

fn format_epoch(ts: i32) -> String {
    if ts <= 0 {
        return String::new();
    }
    chrono::DateTime::from_timestamp(i64::from(ts), 0)
        .map(|dt| dt.format("%Y-%m-%d %H:%M:%S").to_string())
        .unwrap_or_default()
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::trade::check::MSG_LOGIN_REQUIRED;

    #[test]
    fn empty_cart_message_is_explicit() {
        assert_eq!(MSG_CART_EMPTY, "请先选择要结算的商品");
        assert_eq!(MSG_LOGIN_REQUIRED, "请先登录");
    }

    #[test]
    fn shipping_fallback_has_default_type() {
        let list = default_shipping_types(7);
        assert_eq!(list.len(), 1);
        assert_eq!(list[0].shop_id, 7);
        assert_eq!(list[0].shipping_type_name, "快递发货");
    }

    #[test]
    fn epoch_formatting() {
        assert_eq!(format_epoch(0), "");
        assert_eq!(format_epoch(-1), "");
        assert!(format_epoch(1_700_000_000).starts_with("2023-"));
    }
}

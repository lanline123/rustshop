//! 订单服务：下单、状态流转、取消（回滚库存）、支付回调。
//! 对应原 Java `OrderServiceImpl`。

use std::sync::Arc;

use rust_decimal::Decimal;
use tigshop_common::utils;
use tigshop_core::entities::{Order, OrderItem};
use tigshop_core::models::{OrderCreateDTO, OrderCreateVO, OrderVO};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

use crate::trade::cart::CartService;
use crate::catalog::product::ProductService;

/// 订单状态常量（严格对齐 `java/database/pure.sql` 中 `order.order_status`
/// 的列注释：`0 待确认/待支付 1 已确定/待发货 2 处理中/已发货 3 已取消
/// 4 无效 5 已完成`）。
///
/// ⚠️ 历史缺陷提示：本模块曾使用 `10/20/30/40/50` 一套自造状态值，与真实库
/// 口径不符——`mark_paid` 会把 `order_status` 写成 `10`，管理端与前台
/// 按 `1`（待发货）过滤时**永远查不到该订单**。已于本次修正为库口径；
/// 任何新增代码请勿再引入非 `0..=5` 的订单状态字面量。
pub mod status {
    /// 待确认 / 待支付
    pub const UNPAID: i32 = 0;
    /// 已确定 / 待发货（已付款）
    pub const PAID: i32 = 1;
    /// 处理中 / 已发货
    pub const SHIPPED: i32 = 2;
    /// 已取消
    pub const CANCELED: i32 = 3;
    /// 无效
    pub const INVALID: i32 = 4;
    /// 已完成
    pub const FINISHED: i32 = 5;
}

#[derive(Clone)]
pub struct OrderService {
    repos: Arc<Repositories>,
    product: ProductService,
    cart: CartService,
}

impl OrderService {
    pub fn new(repos: Arc<Repositories>, product: ProductService, cart: CartService) -> Self {
        Self { repos, product, cart }
    }

    /// 创建订单：
    /// 1. 读取勾选购物车行
    /// 2. 校验并扣减库存（不足则整体失败）
    /// 3. 生成订单号并写入 order + order_item
    /// 4. 清理已下单的购物车行
    pub async fn create(&self, user_id: i32, dto: &OrderCreateDTO) -> Result<OrderCreateVO> {
        let lines = self.cart.checked_lines(user_id, &dto.cart_ids).await?;
        if lines.is_empty() {
            return Err(Error::Validation("未选择任何商品".into()));
        }

        // 金额计算
        let mut product_amount = Decimal::ZERO;
        let mut items: Vec<OrderItem> = Vec::with_capacity(lines.len());
        let mut order_sn = utils::generate_order_sn();

        // 订单号防重
        let mut retries = 0;
        while self.repos.trade.order.sn_exists(&order_sn).await? && retries < 5 {
            order_sn = utils::generate_order_sn();
            retries += 1;
        }

        // 商品 + 价格（与结算页同口径：商品现价；SKU 价格由 current_prices 提供）
        let product_ids: Vec<i32> = lines.iter().map(|l| l.product_id).collect();
        let products = crate::trade::check::load_products(&self.repos, &product_ids).await?;
        let prices = self.repos.catalog.product.current_prices(&product_ids).await?;
        let mut store_amounts: std::collections::BTreeMap<i32, Decimal> =
            std::collections::BTreeMap::new();

        // 秒杀活动价（与结算页同一映射）
        let now_activity = utils::now_seconds() as i32;
        let seckill = crate::trade::check::load_seckill_prices(&self.repos, &lines, now_activity)
            .await?;
        // 拼团活动价（购物车 extra_data 携带 grouponData）
        let groupon_prices = crate::trade::check::load_groupon_prices(
            &self.repos,
            &lines,
            i64::from(now_activity),
        )
        .await?;
        let mut seckill_discount = Decimal::ZERO;
        // (rec_id, 数量, 商品ID, 活动开始, 活动结束, 活动ID)
        let mut seckill_lines: Vec<(i32, i32, i32, i32, i32, i32)> = Vec::new();
        // 拼团明细：(商品名, 拼团价信息)
        let mut groupon_lines: Vec<(String, crate::trade::check::GrouponPrice)> = Vec::new();

        // 预计算活动单价（秒杀/拼团），再应用满减/满折得到最终结算价
        let mut line_prices: std::collections::HashMap<i32, Decimal> =
            std::collections::HashMap::new();
        for line in &lines {
            let pid = line.product_id;
            let sku_id = line.sku_id.unwrap_or(0);
            let base_price = prices
                .get(&pid)
                .and_then(|p| *p)
                .unwrap_or(line.original_price);
            let price = if let Some(g) = groupon_prices.get(&line.cart_id) {
                g.price.min(base_price)
            } else {
                seckill
                    .get(&(pid, sku_id))
                    .or_else(|| seckill.get(&(pid, 0)))
                    .and_then(|item| item.seckill_price)
                    .map(|p| p.min(base_price))
                    .unwrap_or(base_price)
            };
            line_prices.insert(line.cart_id, price);
        }
        crate::trade::check::apply_full_reductions(
            &self.repos,
            &lines,
            &products,
            &mut line_prices,
            now_activity,
        )
        .await?;

        for line in &lines {
            let pid = line.product_id;
            let sku_id = line.sku_id.unwrap_or(0);
            let qty = line.quantity.max(1);
            let base_price = prices
                .get(&pid)
                .and_then(|p| *p)
                .unwrap_or(line.original_price);
            let seckill_item = seckill
                .get(&(pid, sku_id))
                .or_else(|| seckill.get(&(pid, 0)));
            let groupon_line = groupon_prices.get(&line.cart_id);
            let (activity_price, is_pin) = if let Some(g) = groupon_line {
                (g.price.min(base_price), true)
            } else {
                (
                    seckill_item
                        .and_then(|item| item.seckill_price)
                        .map(|p| p.min(base_price))
                        .unwrap_or(base_price),
                    false,
                )
            };
            // 满减/满折后的最终单价
            let price = line_prices
                .get(&line.cart_id)
                .copied()
                .unwrap_or(activity_price);
            let subtotal = price * Decimal::from(qty);
            product_amount += subtotal;
            seckill_discount +=
                (base_price - price).max(Decimal::ZERO) * Decimal::from(qty);
            *store_amounts.entry(line.shop_id).or_default() += subtotal;
            if let Some(g) = groupon_line {
                groupon_lines.push((
                    products
                        .get(&pid)
                        .map(|p| p.product_name.clone())
                        .unwrap_or_default(),
                    g.clone(),
                ));
            } else if let Some(item) = seckill_item {
                if activity_price < base_price {
                    if let (Some(rec_id), Some(start), Some(end)) =
                        (Some(item.rec_id), item.seckill_start_time, item.seckill_end_time)
                    {
                        seckill_lines.push((
                            rec_id,
                            qty,
                            pid,
                            start,
                            end,
                            item.seckill_id.unwrap_or(0),
                        ));
                    }
                }
            }

            let promotion_data = groupon_line
                .and_then(|g| serde_json::to_string(&g.data).ok());
            items.push(OrderItem {
                order_sn: order_sn.clone(),
                user_id,
                price,
                quantity: qty,
                product_id: pid,
                sku_id,
                sku_data: line.sku_data.clone().unwrap_or_default(),
                origin_price: Some(base_price),
                shop_id: line.shop_id,
                is_pin: i32::from(is_pin),
                promotion_data,
                ..Default::default()
            });
        }

        // 秒杀限购校验（按活动窗口内该用户已购数量）
        for (_, qty, pid, start, end, seckill_id) in &seckill_lines {
            if *seckill_id <= 0 {
                continue;
            }
            if let Some(record) = self.repos.marketing.seckill.find_by_id(*seckill_id).await? {
                if record.seckill_limit_num.unwrap_or(0) > 0 {
                    let bought = self
                        .repos
                        .marketing
                        .seckill
                        .count_user_purchases(user_id, *pid, *start, *end)
                        .await?;
                    if bought + i64::from(*qty) > i64::from(record.seckill_limit_num.unwrap_or(0)) {
                        return Err(Error::biz(1001, "超过秒杀商品限购数量".to_string()));
                    }
                }
            }
        }

        // —— 收货地址快照（同时取地区用于运费模板匹配）——
        let addr = match dto.address_id.filter(|id| *id > 0) {
            Some(address_id) => {
                self.repos
                    .member
                    .user_address_client
                    .find_by_id(address_id)
                    .await?
            }
            None => None,
        };
        let (consignee, mobile, email, address, region_ids, region_names, region_id_list) =
            match addr {
                Some(a) => {
                    let ids: Vec<i32> =
                        serde_json::from_str(&a.region_ids).unwrap_or_default();
                    (
                        a.consignee,
                        a.mobile,
                        a.email.unwrap_or_default(),
                        a.address,
                        a.region_ids,
                        a.region_names,
                        ids,
                    )
                }
                None => (
                    String::new(),
                    String::new(),
                    String::new(),
                    String::new(),
                    "[]".to_string(),
                    "[]".to_string(),
                    Vec::new(),
                ),
            };

        // —— 优惠券抵扣（店铺范围 + 门槛校验，与结算页同口径）——
        // 前端结算表单提交 `useCouponIds`；兼容旧字段 `couponId`。
        let coupon_used: Vec<i32> = dto
            .use_coupon_ids
            .clone()
            .filter(|ids| !ids.is_empty())
            .or_else(|| dto.coupon_id.filter(|id| *id > 0).map(|id| vec![id]))
            .unwrap_or_default();
        let mut coupon_amount = Decimal::ZERO;
        let mut valid_coupons: Vec<i32> = Vec::new();
        if !coupon_used.is_empty() {
            let coupons = self
                .repos
                .marketing
                .coupon
                .find_by_ids(&coupon_used)
                .await?;
            let by_id: std::collections::HashMap<i32, tigshop_core::entities::Coupon> = coupons
                .into_iter()
                .map(|c| (c.coupon_id, c))
                .collect();
            for coupon_id in &coupon_used {
                let coupon = by_id.get(coupon_id).ok_or_else(|| {
                    Error::biz(1001, format!("优惠券不存在: {coupon_id}"))
                })?;
                let shop_id = coupon.shop_id.unwrap_or(0);
                let base = if shop_id > 0 {
                    store_amounts.get(&shop_id).copied().unwrap_or(Decimal::ZERO)
                } else {
                    product_amount
                };
                if base <= Decimal::ZERO {
                    return Err(Error::biz(1001, "优惠券不适用于当前店铺商品".to_string()));
                }
                if base < coupon.min_order_amount {
                    return Err(Error::biz(1001, "优惠券未达到使用门槛".to_string()));
                }
                coupon_amount += tigshop_core::models::trade::check::coupon_rule_discount(
                    coupon.coupon_type,
                    coupon.coupon_money,
                    coupon.coupon_discount,
                    coupon.max_order_amount,
                    base,
                );
                valid_coupons.push(*coupon_id);
            }
        }

        // —— 运费模板（与结算页同一函数）——
        let shipping = crate::trade::check::calculate_shipping(
            &self.repos,
            &lines,
            &products,
            &region_id_list,
            if dto.shipping_method == Some(2) {
                Some(2)
            } else {
                None
            },
        )
        .await?;
        let shipping_fee = shipping.total;

        // —— 积分抵扣（与结算页同一函数）——
        let user = self
            .repos
            .member
            .user
            .find_by_id(user_id)
            .await?
            .ok_or_else(|| Error::biz_msg("用户不存在"))?;
        let cfg = self
            .repos
            .trade
            .check
            .find_config_values(&[
                "integralScale",
                "integralPercent",
                "integralPercentBaseAmount",
                "usePoints",
            ])
            .await?;
        let config_enabled = |key: &str| {
            cfg.get(key).map(|v| v.trim() == "1").unwrap_or(false)
        };
        let decimal_cfg = |key: &str, default: Decimal| {
            cfg.get(key)
                .and_then(|v| v.trim().parse::<Decimal>().ok())
                .unwrap_or(default)
        };
        let points = if config_enabled("usePoints") {
            tigshop_core::models::trade::check::calc_points(
                dto.use_points.unwrap_or(0),
                user.points,
                (product_amount - coupon_amount).max(Decimal::ZERO),
                decimal_cfg("integralScale", Decimal::ZERO),
                decimal_cfg("integralPercent", Decimal::ZERO),
                decimal_cfg("integralPercentBaseAmount", Decimal::ONE_HUNDRED),
            )
        } else {
            Default::default()
        };
        let points_amount = points.points_amount;

        // 应付金额 = 商品 - 优惠券 + 运费 - 积分抵扣（与结算页 totalAmount 一致）
        let total =
            (product_amount - coupon_amount + shipping_fee - points_amount).max(Decimal::ZERO);

        // —— 定金预售：单商品 + product_presale.sale_mode = 2 ——
        let now_ts = utils::now_seconds() as i32;
        let mut presale_row: Option<tigshop_core::entities::OrderPresale> = None;
        let mut deposit_now = total;
        if lines.len() == 1 {
            let product_id = lines[0].product_id;
            if let Some(product) = products.get(&product_id) {
                if product.sale_mode == 2 {
                    if let Some(ps) = self
                        .repos
                        .catalog
                        .product_manage
                        .find_presale(product_id)
                        .await?
                    {
                        let deposit = tigshop_core::models::trade::check::calc_deposit(
                            total,
                            ps.deposit_type,
                            ps.deposit_ratio,
                            ps.deposit_fixed_amount,
                            ps.is_round_down,
                        );
                        if deposit > Decimal::ZERO {
                            deposit_now = deposit;
                            presale_row = Some(tigshop_core::entities::OrderPresale {
                                presale_order_id: 0,
                                order_id: 0,
                                order_sn: order_sn.clone(),
                                product_id,
                                sale_mode: 2,
                                deposit_amount: deposit,
                                final_payment_amount: (total - deposit).max(Decimal::ZERO),
                                deposit_paid_time: None,
                                final_payment_start_time: None,
                                final_payment_paid_time: None,
                                presale_status: 0,
                                delivery_days: if ps.delivery_type == 1 {
                                    Some(ps.delivery_relative_days.unwrap_or(0))
                                } else {
                                    None
                                },
                                delivery_date: if ps.delivery_type == 2 {
                                    ps.delivery_date
                                } else {
                                    None
                                },
                                create_time: now_ts,
                                update_time: now_ts,
                            });
                        }
                    }
                }
            }
        }

        let order = Order {
            is_presale: if presale_row.is_some() { 1 } else { 0 },
            presale_type: presale_row.as_ref().map(|_| 2),
            order_type: if groupon_lines.is_empty() { Some(0) } else { Some(2) },
            order_sn: order_sn.clone(),
            user_id,
            order_status: status::UNPAID,
            shipping_status: 0,
            pay_status: 1, // 1=未支付
            add_time: utils::now_seconds() as i32,
            buyer_note: dto.buyer_note.clone().unwrap_or_default(),
            shipping_method: dto.shipping_method.unwrap_or(1),
            pay_type_id: dto.pay_type_id.unwrap_or(0),
            consignee,
            mobile,
            email,
            address,
            region_ids,
            region_names,
            use_points: points.used_points,
            points_amount,
            is_valid: 1,
            is_del: 0,
            total_amount: total,
            paid_amount: Decimal::ZERO,
            unpaid_amount: deposit_now,
            product_amount,
            coupon_amount,
            shipping_fee,
            discount_amount: seckill_discount,
            order_source: "web".into(),
            ..Default::default()
        };

        // 「扣库存 + 写订单」同一事务：任一行库存不足或订单写入失败，
        // 事务整体回滚，不残留已扣库存（原 Java 由 @Transactional 保证）。
        let mut groupon_record_id: Option<i64> = None;

        let order_id = {
            let mut tx = self
                .repos.trade.order
                .begin()
                .await
                .map_err(|e| Error::Database(e.to_string()))?;

            let mut deducted: Vec<(i32, Option<i32>, i32)> = Vec::with_capacity(items.len());
            let mut result: Result<i32> = Ok(0);

            for (idx, item) in items.iter().enumerate() {
                let pid = item.product_id;
                let qty = item.quantity;
                // 无 SKU 商品（sku_id 缺省）必须按 product_stock 扣减：
                // 传 Some(0) 会 UPDATE product_sku WHERE sku_id=0 → 0 行 → 误报库存不足。
                let sku = lines.get(idx).and_then(|l| l.sku_id);
                match self
                    .repos.catalog.product
                    .deduct_stock_in_tx(&mut tx, pid, sku, qty)
                    .await
                {
                    Ok(true) => deducted.push((pid, sku, qty)),
                    Ok(false) => {
                        result = Err(Error::InsufficientStock {
                            product_id: pid.to_string(),
                            stock: 0,
                        });
                        break;
                    }
                    Err(e) => {
                        result = Err(e);
                        break;
                    }
                }
            }

            if result.is_ok() {
                result = self
                    .repos.trade.order
                    .insert_order_in_tx(&mut tx, &order, &items)
                    .await;
            }

            // 拼团记录（与订单同事务；开团/参团状态机）
            if result.is_ok() && !groupon_lines.is_empty() {
                if let Some(order_id) = result.as_ref().ok().copied() {
                    for (product_name, line) in &groupon_lines {
                        match crate::trade::groupon::create_record_in_tx(
                            &mut tx,
                            &self.repos,
                            user_id,
                            &user.username,
                            order_id,
                            &order_sn,
                            product_name,
                            line,
                        )
                        .await
                        {
                            Ok(id) => {
                                if groupon_record_id.is_none() {
                                    groupon_record_id = Some(id);
                                }
                            }
                            Err(e) => {
                                result = Err(e);
                                break;
                            }
                        }
                    }
                }
            }

            // 秒杀库存扣减（与订单同事务，失败整体回滚）
            if result.is_ok() {
                for (rec_id, qty, _, _, _, _) in &seckill_lines {
                    match self
                        .repos
                        .marketing
                        .seckill
                        .dec_stock_inc_sales_in_tx(&mut tx, *rec_id, *qty)
                        .await
                    {
                        Ok(true) => {}
                        Ok(false) => {
                            result = Err(Error::biz(1001, "秒杀库存不足".to_string()));
                            break;
                        }
                        Err(e) => {
                            result = Err(e);
                            break;
                        }
                    }
                }
            }

            // 预售订单信息与订单同事务写入，避免「订单在、预售信息丢」
            if result.is_ok() {
                if let Some(mut row) = presale_row.clone() {
                    if let Some(order_id) = result.as_ref().ok() {
                        row.order_id = *order_id;
                        result = self
                            .repos
                            .payment
                            .pay
                            .insert_presale_in_tx(&mut tx, &row)
                            .await
                            .map(|_| row.order_id);
                    }
                }
            }

            match result {
                Ok(id) => {
                    tx.commit().await.map_err(|e| Error::Database(e.to_string()))?;
                    id
                }
                Err(e) => {
                    // 事务回滚即撤消本事务内已扣的库存，无需手工补偿
                    let _ = tx.rollback().await;
                    // 库存被回滚，需失效详情缓存避免读到脏数据
                    for (pid, _, _) in &deducted {
                        self.product.invalidate_detail_cache(*pid).await;
                    }
                    return Err(e);
                }
            }
        };

        for item in items.iter_mut() {
            item.order_id = order_id;
        }

        // 清理已下单购物车行
        let cart_ids: Vec<i32> = lines.iter().map(|l| l.cart_id).collect();
        if let Err(e) = self.cart.remove(user_id, &cart_ids).await {
            tracing::warn!("清理购物车失败: {e}");
        }

        // 占用优惠券（尽力而为；失败仅告警，避免订单已建但接口失败）
        if !valid_coupons.is_empty() {
            if let Err(e) = crate::marketing::coupon::CouponService::new(self.repos.clone())
                .use_coupons(user_id, &valid_coupons, order_id)
                .await
            {
                tracing::warn!("订单 {order_id} 优惠券占用失败: {e}");
            }
        }

        // 扣减积分（与结算页口径一致；失败仅告警，避免已建订单接口失败）
        if points.used_points > 0
            && !self
                .repos
                .member
                .user
                .try_debit_points(user_id, points.used_points)
                .await?
        {
            tracing::warn!(
                "订单 {order_id} 积分扣减失败: user={user_id}, points={}",
                points.used_points
            );
        }

        // 扣减成功，失效商品详情缓存
        for line in &lines {
            self.product.invalidate_detail_cache(line.product_id).await;
        }

        tracing::info!("创建订单成功: order_id={order_id}, sn={order_sn}, user={user_id}");

        // —— 余额抵扣（Java `OrderCheckServiceImpl#calcBalanceDeduction`）——
        // 定金预售：本次只支付定金，余额抵扣留给尾款阶段（避免把定金当全款结算）
        let mut online_amount = if presale_row.is_some() { deposit_now } else { total };
        let mut return_type = 1_i32;
        if presale_row.is_none() {
        if let Some(requested) = dto.use_balance {
            if requested > Decimal::ZERO {
                let available = self
                    .repos
                    .member
                    .user
                    .find_by_id(user_id)
                    .await?
                    .map(|u| u.available_balance())
                    .unwrap_or(Decimal::ZERO);
                let mut balance_used = requested.min(total).min(available);
                if balance_used > Decimal::ZERO {
                    let debited = self
                        .repos
                        .member
                        .user
                        .try_debit_balance(user_id, balance_used)
                        .await?;
                    if !debited {
                        balance_used = Decimal::ZERO;
                    }
                }
                if balance_used > Decimal::ZERO {
                    let _ = self
                        .repos
                        .trade
                        .order
                        .set_balance_amount(order_id, balance_used)
                        .await;
                    let now = utils::now_seconds() as i32;
                    if balance_used >= total {
                        if self
                            .repos
                            .trade
                            .order
                            .mark_paid(order_id, now, total)
                            .await?
                        {
                            online_amount = Decimal::ZERO;
                            return_type = 2;
                            self.write_balance_paylog(order_id, &order_sn, total, balance_used, now)
                                .await;
                        }
                    } else {
                        let _ = self
                            .repos
                            .trade
                            .order
                            .apply_partial_payment(order_id, balance_used)
                            .await;
                        online_amount = total - balance_used;
                        self.write_balance_paylog(order_id, &order_sn, total, balance_used, now)
                            .await;
                    }
                }
            }
        }

        }

        let out_trade_no = format!("PAY{order_sn}");
        Ok(OrderCreateVO {
            order_id,
            order_sn,
            total_amount: total,
            online_amount,
            out_trade_no,
            return_type,
            groupon_promotion_record_id: groupon_record_id.and_then(|v| i32::try_from(v).ok()),
        })
    }

    /// 余额抵扣流水（对应 Java `PaylogService#creatPayLog(payCode="balance")`）。
    async fn write_balance_paylog(
        &self,
        order_id: i32,
        order_sn: &str,
        order_amount: Decimal,
        pay_amount: Decimal,
        now: i32,
    ) {
        use tigshop_core::entities::Paylog;
        use tigshop_core::models::payment::paylog::{generate_pay_sn, PayOrderType};
        use tigshop_infrastructure::db::repo::payment::paylog::PAYLOG_STATUS_PAID;
        let paylog = Paylog {
            paylog_id: 0,
            pay_sn: generate_pay_sn(),
            pay_name: format!("余额支付-订单{order_sn}"),
            order_id,
            order_sn: order_sn.to_string(),
            order_amount,
            order_type: PayOrderType::Product as i32,
            pay_amount,
            pay_status: PAYLOG_STATUS_PAID,
            pay_code: "balance".to_string(),
            profit_sharing_channel: None,
            add_time: Some(now),
            transaction_id: format!("BAL{}", utils::now_millis()),
            notify_data: String::new(),
            refund_amount: Decimal::ZERO,
            token_code: String::new(),
            appid: None,
        };
        if let Err(e) = self.repos.payment.paylog.insert(&paylog).await {
            tracing::warn!("余额支付流水写入失败: order_id={order_id} {e}");
        }
    }

    /// 订单详情。
    pub async fn detail(&self, order_id: i32, user_id: Option<i32>) -> Result<(OrderVO, Vec<OrderItem>)> {
        let order = self
            .repos.trade.order
            .find_by_id(order_id)
            .await?
            .ok_or_else(|| Error::NotFound("订单不存在".into()))?;

        // 归属校验（非本人不可查）
        if let Some(uid) = user_id {
            if order.user_id != uid {
                return Err(Error::Forbidden("无权查看该订单".into()));
            }
        }

        let items = self.repos.trade.order.find_items(order_id).await?;
        Ok((to_vo(&order, items.len() as i32), items))
    }

    /// 订单分页。
    pub async fn list(
        &self,
        user_id: Option<i32>,
        order_status: Option<i32>,
        keyword: Option<&str>,
        page: &PageQuery,
    ) -> Result<PageResult<OrderVO>> {
        let (total, orders) = self
            .repos.trade.order
            .paginate(user_id, order_status, keyword, page.page, page.page_size)
            .await?;
        let list = orders
            .iter()
            .map(|o| to_vo(o, 0))
            .collect();
        Ok(PageResult::new(total, list, page))
    }

    /// 取消订单（仅待付款可取消，取消后回滚库存）。
    pub async fn cancel(&self, order_id: i32, user_id: Option<i32>) -> Result<()> {
        let order = self
            .repos.trade.order
            .find_by_id(order_id)
            .await?
            .ok_or_else(|| Error::NotFound("订单不存在".into()))?;
        if let Some(uid) = user_id {
            if order.user_id != uid {
                return Err(Error::Forbidden("无权操作该订单".into()));
            }
        }
        if !order.is_cancellable() {
            return Err(Error::Conflict("当前订单状态不可取消".into()));
        }

        // 原子状态流转：0 → 40
        let ok = self
            .repos.trade.order
            .transition_status(order_id, &[status::UNPAID], status::CANCELED)
            .await?;
        if !ok {
            return Err(Error::Conflict("订单状态已变更，请刷新后重试".into()));
        }

        // 回滚库存
        let items = self.repos.trade.order.find_items(order_id).await?;
        for item in items {
            let pid = item.product_id;
            let qty = item.quantity;
            if let Err(e) = self.product.restore_stock(pid, Some(item.sku_id), qty).await {
                tracing::error!("取消订单回滚库存失败 order={order_id} product={pid}: {e}");
            }
        }
        tracing::info!("订单已取消: {order_id}");
        Ok(())
    }

    /// 支付成功回调（由支付服务调用，幂等）。
    pub async fn on_payment_success(
        &self,
        order_id: i32,
        paid_amount: Decimal,
        out_trade_no: Option<String>,
    ) -> Result<()> {
        let order = self
            .repos.trade.order
            .find_by_id(order_id)
            .await?
            .ok_or_else(|| Error::NotFound("订单不存在".into()))?;

        if order.is_paid() {
            tracing::info!("订单已支付，跳过重复回调: {order_id}");
            return Ok(()); // 幂等
        }

        self.repos.trade.order
            .mark_paid(order_id, utils::now_seconds() as i32, paid_amount)
            .await?;
        if let Some(no) = out_trade_no {
            tracing::info!("订单支付成功: {order_id}, trade_no={no}");
        }
        Ok(())
    }

    /// 发货（后台操作）。
    pub async fn ship(&self, order_id: i32, tracking_no: &str, logistics_name: &str) -> Result<()> {
        let ok = self
            .repos.trade.order
            .transition_status(order_id, &[status::PAID], status::SHIPPED)
            .await?;
        if !ok {
            return Err(Error::Conflict("订单当前状态不可发货".into()));
        }
        self.repos.trade.order
            .update_shipping(order_id, tracking_no, logistics_name, utils::now_seconds() as i32)
            .await?;
        tracing::info!("订单已发货: {order_id}, 运单号: {tracking_no}");
        Ok(())
    }
}

fn to_vo(o: &Order, item_count: i32) -> OrderVO {
    OrderVO {
        order_id: o.order_id,
        order_sn: o.order_sn.clone(),
        order_status: o.order_status,
        status_label: o.status_label().to_string(),
        pay_status: o.pay_status,
        shipping_status: o.shipping_status,
        total_amount: o.total_amount,
        paid_amount: o.paid_amount,
        consignee: o.consignee.clone(),
        mobile: o.mobile.clone(),
        add_time: o.add_time,
        item_count,
    }
}
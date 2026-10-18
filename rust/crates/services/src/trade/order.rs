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

        // 批量取价：一次查询替代循环内逐条 current_price（消除 N+1）。
        let product_ids: Vec<i32> = lines.iter().map(|l| l.product_id).collect();
        let prices = self.repos.catalog.product.current_prices(&product_ids).await?;

        for line in &lines {
            let pid = line.product_id;
            let sku_id = line.sku_id;
            let qty = line.quantity.max(1);
            let price = prices
                .get(&pid)
                .and_then(|p| *p)
                .unwrap_or(line.original_price);
            let subtotal = price * Decimal::from(qty);
            product_amount += subtotal;

            items.push(OrderItem {
                order_sn: order_sn.clone(),
                user_id,
                price,
                quantity: qty,
                product_id: pid,
                sku_id: sku_id.unwrap_or(0),
                sku_data: line.sku_data.clone().unwrap_or_default(),
                origin_price: Some(line.original_price),
                shop_id: line.shop_id,
                ..Default::default()
            });
        }

        // —— 优惠券抵扣（与结算页一致：按用户券 + 商品金额试算）——
        // 前端结算表单提交 `useCouponIds`；兼容旧字段 `couponId`。
        let coupon_used: Vec<i32> = dto
            .use_coupon_ids
            .clone()
            .filter(|ids| !ids.is_empty())
            .or_else(|| dto.coupon_id.filter(|id| *id > 0).map(|id| vec![id]))
            .unwrap_or_default();
        let coupon_amount = if coupon_used.is_empty() {
            Decimal::ZERO
        } else {
            crate::marketing::coupon::CouponService::new(self.repos.clone())
                .calc_discount(user_id, &coupon_used, product_amount)
                .await?
        };
        // 运费：运费模板引擎未接入，暂取 0（与结算页一致）
        let shipping_fee = Decimal::ZERO;
        let total = (product_amount - coupon_amount).max(Decimal::ZERO) + shipping_fee;

        // —— 收货地址快照 ——
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
        let (consignee, mobile, email, address, region_ids, region_names) = match addr {
            Some(a) => (
                a.consignee,
                a.mobile,
                a.email.unwrap_or_default(),
                a.address,
                a.region_ids,
                a.region_names,
            ),
            None => (
                String::new(),
                String::new(),
                String::new(),
                String::new(),
                "[]".to_string(),
                "[]".to_string(),
            ),
        };

        let order = Order {
            order_type: Some(0),
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
            use_points: dto.use_points.unwrap_or(0),
            is_valid: 1,
            is_del: 0,
            total_amount: total,
            paid_amount: Decimal::ZERO,
            unpaid_amount: total,
            product_amount,
            coupon_amount,
            shipping_fee,
            order_source: "web".into(),
            ..Default::default()
        };

        // 「扣库存 + 写订单」同一事务：任一行库存不足或订单写入失败，
        // 事务整体回滚，不残留已扣库存（原 Java 由 @Transactional 保证）。
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
        if !coupon_used.is_empty() {
            if let Err(e) = crate::marketing::coupon::CouponService::new(self.repos.clone())
                .use_coupons(user_id, &coupon_used, order_id)
                .await
            {
                tracing::warn!("订单 {order_id} 优惠券占用失败: {e}");
            }
        }

        // 扣减成功，失效商品详情缓存
        for line in &lines {
            self.product.invalidate_detail_cache(line.product_id).await;
        }

        tracing::info!("创建订单成功: order_id={order_id}, sn={order_sn}, user={user_id}");

        // —— 余额抵扣（Java `OrderCheckServiceImpl#calcBalanceDeduction`）——
        let mut online_amount = total;
        let mut return_type = 1_i32;
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

        let out_trade_no = format!("PAY{order_sn}");
        Ok(OrderCreateVO {
            order_id,
            order_sn,
            total_amount: total,
            online_amount,
            out_trade_no,
            return_type,
            groupon_promotion_record_id: None,
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
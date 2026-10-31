//! 订单支付域服务（payment::pay）。
//!
//! 对应原 Java `com.tigshop.api.controller.order.PayController` 的 6 个
//! `optional` 端点 + `OrderPayServiceImpl` 的 DB/config 部分。
//!
//! # 已实现（纯 DB / config）
//!
//! - `paymentList`：`getAvailablePayment("checkout")`（支付开关 + 客户端过滤 + 排序）。
//! - `index`：订单归属校验 + 订单摘要 + 支付方式（按 `pay_type_id` 过滤式）+
//!   线下支付说明；无 id 时等同下单前收银台（不含 offline 展示项）。
//! - `getPayLog`：拆单子单兜底、最后一笔成功 paylog、实付金额/支付名称、
//!   预售定金/尾款口径、预约信息。
//! - `checkStatus`：订单 `pay_status` 优先 + 最后一笔 paylog 口径。
//!
//! # 未迁移（渠道 SDK）
//!
//! - `create` / `createDepositPayment`：完成用户/订单归属/已支付校验后返回
//!   [`MSG_CHANNEL_UNMIGRATED`]，**不创建 paylog、不伪造支付参数**。
//! - `checkStatus` 的微信付款码轮询分支（`PayConstants.CODE_PAY_CODE`）依赖微信
//!   支付查询 SDK：未迁移，直接返回本地口径。
//!
//! # 偏差
//!
//! 1. `index.order` 为订单摘要（见 [`PayOrderVO`] 注释），非 Java `OrderVO` 全量。
//! 2. `index` 的「已支付」判定用 `pay_status == 2` 近似 Java 的
//!    `availableActions.toPay == false`。
//! 3. i18n 文案未迁移，使用固定中文（与 Java 默认文案一致）。
//! 4. `checkStatus.paylogId` 在 Java 仅当 id 为空时使用（控制器 id 必填，实际不可达）。

use std::sync::Arc;

use tigshop_core::entities::Paylog;
use tigshop_core::models::payment::pay::{
    available_payments, is_reservation_order, offline_payment_list, pay_method_name, CheckStatusQuery,
    OrderReservationVO, PayCreateQuery, PayCreateVO, PayIndexQuery, PayIndexVO, PayLogQuery, PayOrderVO,
    PaylogVO, MSG_CHANNEL_UNMIGRATED, MSG_NOT_LOGGED_IN, MSG_ORDER_ALREADY_PAID,
    MSG_ORDER_NOT_FOUND, MSG_PARAM_MISSING, PAY_STATUS_PAID,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::payment::pay::{PayOrderRow, PayRepository};
use tigshop_infrastructure::db::Repositories;

/// 本域涉及的全部配置键（一次查询）。
const CONFIG_KEYS: [&str; 32] = [
    tigshop_core::models::payment::pay::CFG_USE_WECHAT,
    tigshop_core::models::payment::pay::CFG_USE_ALIPAY,
    tigshop_core::models::payment::pay::CFG_USE_PAYPAL,
    tigshop_core::models::payment::pay::CFG_USE_APPLE_PAY,
    tigshop_core::models::payment::pay::CFG_USE_LAKALA,
    tigshop_core::models::payment::pay::CFG_USE_YABANPAY,
    tigshop_core::models::payment::pay::CFG_USE_YABANPAY_WECHAT,
    tigshop_core::models::payment::pay::CFG_USE_YABANPAY_ALIPAY,
    tigshop_core::models::payment::pay::CFG_USE_YUNPAY,
    tigshop_core::models::payment::pay::CFG_USE_HUIFU,
    tigshop_core::models::payment::pay::CFG_USE_YEEPAY,
    tigshop_core::models::payment::pay::CFG_USE_OFFLINE,
    tigshop_core::models::payment::pay::CFG_PAYMENT_ORDER,
    tigshop_core::models::payment::pay::CFG_OFFLINE_PAY_BANK_ENABLE,
    tigshop_core::models::payment::pay::CFG_OFFLINE_PAY_BANK,
    tigshop_core::models::payment::pay::CFG_OFFLINE_PAY_COMPANY_ENABLE,
    tigshop_core::models::payment::pay::CFG_OFFLINE_PAY_COMPANY,
    tigshop_core::models::payment::pay::CFG_THIRD_PARTY_TRANSFER_ENABLE,
    tigshop_core::models::payment::pay::CFG_THIRD_PARTY_TRANSFER,
    tigshop_core::models::payment::pay::CFG_OTHER_REMITTANCE_ENABLE,
    tigshop_core::models::payment::pay::CFG_OTHER_REMITTANCE,
    tigshop_core::models::payment::pay::CFG_APPLE_PROVIDER,
    tigshop_core::models::payment::pay::CFG_APPLE_PROCESSING_KEY,
    tigshop_core::models::payment::pay::CFG_APPLE_MERCHANT_ID,
    tigshop_core::models::payment::pay::CFG_APPLE_DISPLAY_NAME,
    tigshop_core::models::payment::pay::CFG_APPLE_MERCHANT_CERT,
    tigshop_core::models::payment::pay::CFG_APPLE_MERCHANT_KEY,
    tigshop_core::models::payment::pay::CFG_APPLE_GATEWAY,
    tigshop_core::models::payment::pay::CFG_APPLE_MERCHANT_NO,
    tigshop_core::models::payment::pay::CFG_APPLE_ACQUIRER_KEY,
    tigshop_core::models::payment::pay::CFG_USE_OFFLINE,
    tigshop_core::models::payment::pay::CFG_PAYMENT_ORDER,
];

/// 订单支付域服务。
pub struct PayService {
    repos: Arc<Repositories>,
}

impl PayService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    fn repo(&self) -> &PayRepository {
        &self.repos.payment.pay
    }

    async fn load_configs(&self) -> Result<std::collections::HashMap<String, String>> {
        self.repo().config_values(&CONFIG_KEYS).await
    }

    // ————————————————————— paymentList —————————————————————

    /// 支付方式列表（Java `getPaymentSelection`）。
    pub async fn payment_list(
        &self,
        user_id: Option<i32>,
        client_type: &str,
    ) -> Result<PayIndexVO> {
        require_user(user_id)?;
        let configs = self.load_configs().await?;
        Ok(PayIndexVO {
            order: None,
            payment_list: available_payments(&configs, client_type, "checkout"),
            offline_payment_list: std::collections::BTreeMap::new(),
        })
    }

    // ————————————————————— index —————————————————————

    /// 订单支付页（Java `getPayOrderInfo`）。
    pub async fn index(
        &self,
        q: &PayIndexQuery,
        user_id: Option<i32>,
        client_type: &str,
    ) -> Result<PayIndexVO> {
        let user_id = require_user(user_id)?;
        let configs = self.load_configs().await?;
        let mut payment_list = available_payments(
            &configs,
            client_type,
            if q.id.is_none() { "checkout" } else { "order" },
        );
        let mut order_vo: Option<PayOrderVO> = None;
        let mut order_sn = String::new();
        match q.id {
            None => {}
            Some(id) => {
                let order = self
                    .repo()
                    .find_order_by_id(id)
                    .await?
                    .ok_or_else(|| Error::biz(500, MSG_ORDER_NOT_FOUND))?;
                if order.user_id != user_id {
                    return Err(Error::biz(500, MSG_ORDER_NOT_FOUND));
                }
                if order.pay_status == PAY_STATUS_PAID {
                    return Err(Error::biz(500, MSG_ORDER_ALREADY_PAID));
                }
                if order.pay_type_id == 1 {
                    payment_list.retain(|m| m != "offline");
                } else if order.pay_type_id == 3 {
                    payment_list.retain(|m| m == "offline");
                }
                order_sn = order.order_sn.clone();
                let mut vo = to_order_vo(&order);
                if let Some(user) = self.repos.member.user.find_by_id(user_id).await? {
                    vo.balance_amount = user.available_balance();
                }
                order_vo = Some(vo);
            }
        }
        let offline = offline_payment_list(&configs, &payment_list, &order_sn);
        Ok(PayIndexVO {
            order: order_vo,
            payment_list,
            offline_payment_list: offline,
        })
    }

    // ————————————————————— getPayLog —————————————————————

    /// 检测订单支付状态（Java `getPayLog` → `PaylogVO`）。
    pub async fn paylog(
        &self,
        q: &PayLogQuery,
        user_id: Option<i32>,
    ) -> Result<PaylogVO> {
        let user_id = require_user(user_id)?;
        let order_id = q
            .id
            .filter(|id| *id != 0)
            .ok_or_else(|| Error::biz(500, MSG_PARAM_MISSING))?;
        // 兼容拆单后父订单被物理删除：按 parent_order_id 子订单兜底
        let (order, split_children): (PayOrderRow, Option<Vec<PayOrderRow>>) =
            match self.repo().find_order_by_id(order_id).await? {
                Some(order) if order.user_id == user_id => (order, None),
                Some(_) => return Err(Error::biz(500, MSG_ORDER_NOT_FOUND)),
                None => {
                    let list = self
                        .repo()
                        .find_children_by_parent(order_id, user_id)
                        .await?;
                    if list.is_empty() {
                        return Err(Error::biz(500, MSG_ORDER_NOT_FOUND));
                    }
                    let first = list[0].clone();
                    (first, Some(list))
                }
            };
        let effective_paid_amount = match &split_children {
            None => order.paid_amount,
            Some(list) => list.iter().map(|o| o.paid_amount).sum(),
        };
        let last = self
            .repo()
            .find_last_success_paylog(order_id, 0)
            .await?;
        let mut target = PaylogVO::default();
        // 与 Java 一致：`balance == 1` 视为「全部在线/线下支付」
        if order.balance != rust_decimal::Decimal::ONE {
            if order.is_presale == 1 && order.presale_type == Some(2) {
                if let Some(presale) = self.repo().find_presale_by_order_id(order.order_id).await? {
                    if presale.presale_status == 1 {
                        target.pay_amount = Some(effective_paid_amount);
                    } else if presale.presale_status == 2 {
                        copy_paylog(&mut target, last.as_ref());
                    }
                }
            } else {
                target.pay_amount = Some(effective_paid_amount);
            }
        } else {
            copy_paylog(&mut target, last.as_ref());
        }
        if target
            .pay_name
            .as_deref()
            .map(|v| v.trim().is_empty())
            .unwrap_or(true)
        {
            target.pay_name = Some(pay_method_name(target.pay_code.as_deref().unwrap_or("")));
        }
        target.order_type = order.order_type;
        if is_reservation_order(order.order_type) {
            if let Some(reservation) = self
                .repo()
                .find_reservation_by_order_id(order.order_id)
                .await?
            {
                target.order_reservation = Some(OrderReservationVO {
                    order_reservations_id: reservation.order_reservations_id,
                    order_id: reservation.order_id,
                    product_reservation_time_id: reservation.product_reservation_time_id,
                    sku_id: reservation.sku_id,
                    reservation_date: reservation.reservation_date,
                    start_time: reservation.start_time,
                    end_time: reservation.end_time,
                    time_slot_description: reservation.time_slot_description,
                    add_time: reservation.add_time.to_string(),
                });
            }
        }
        Ok(target)
    }

    // ————————————————————— checkStatus —————————————————————

    /// 检测订单支付状态（Java `getCheckStatus`）：1 已支付 / 0 待支付。
    pub async fn check_status(
        &self,
        q: &CheckStatusQuery,
        user_id: Option<i32>,
    ) -> Result<i32> {
        let user_id = require_user(user_id)?;
        let order_id = q
            .id
            .ok_or_else(|| Error::biz(500, MSG_PARAM_MISSING))?;
        let order = self
            .repo()
            .find_order_by_id(order_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_ORDER_NOT_FOUND))?;
        if order.user_id != user_id {
            return Err(Error::biz(500, MSG_ORDER_NOT_FOUND));
        }
        let pay_status = order.pay_status;
        if pay_status == PAY_STATUS_PAID {
            return Ok(1);
        }
        let last = self
            .repo()
            .find_last_success_paylog(order_id, 0)
            .await?;
        let Some(paylog) = last else {
            return Ok(pay_status);
        };
        if paylog.pay_code != "wechat" {
            return Ok(pay_status);
        }
        // 微信付款码轮询分支（渠道查询 SDK）未迁移：返回本地口径
        Ok(if pay_status > 0 { 1 } else { 0 })
    }

    // ————————————————————— create / createDepositPayment —————————————————————

    /// 支付订单创建（Java `create`）：本地校验后返回渠道未迁移错误。
    pub async fn create(
        &self,
        q: &PayCreateQuery,
        user_id: Option<i32>,
    ) -> Result<PayCreateVO> {
        self.validate_create_target(q, user_id).await?;
        let user_id = require_user(user_id)?;
        let pay_type = q.r#type.as_deref().unwrap_or_default();

        // —— 线下汇款：创建待支付 paylog，订单保持待付款，后台确认收款后置已付 ——
        if pay_type == "offline" {
            let order_id = q.id.unwrap_or(0);
            let order = self
                .repo()
                .find_order_by_id(order_id)
                .await?
                .ok_or_else(|| Error::biz(500, MSG_ORDER_NOT_FOUND))?;
            let pay_sn = self.create_pending_offline_paylog(&order).await?;
            return Ok(PayCreateVO {
                order_id: Some(order.order_id),
                order_sn: Some(order.order_sn.clone()),
                order_amount: Some(order.unpaid_amount),
                pay_info: Some(serde_json::json!({ "paySn": pay_sn, "payType": "offline" })),
                code: None,
            });
        }

        // —— 余额支付：直接扣减并标记订单（与 /api/balance/pay 同一实现）——
        if pay_type == "balance" {
            let balance_pay =
                crate::payment::balance_pay::BalancePayService::new(self.repos.clone());
            let result = balance_pay
                .pay_order(user_id, q.id.unwrap_or(0), None)
                .await?;
            return Ok(PayCreateVO {
                order_id: q.id,
                order_sn: None,
                order_amount: Some(result.deducted),
                pay_info: Some(serde_json::json!({
                    "paySn": result.pay_sn,
                    "payType": "balance",
                    "deducted": result.deducted.to_string(),
                    "orderFullyPaid": result.order_fully_paid,
                })),
                code: None,
            });
        }

        // —— 在线渠道：协议骨架已就绪，配置缺失时给出可操作错误 ——
        let configs = crate::payment::channels::load_channel_configs(&self.repos).await?;
        match pay_type {
            "wechat" | "wechatH5" | "wechatApp" | "wechatMini" => {
                if !configs.wechat.is_configured() {
                    return Err(Error::biz(
                        1001,
                        format!(
                            "微信支付未配置：缺少 {}（设置-支付设置）",
                            configs.wechat.missing().join("/")
                        ),
                    ));
                }
                let order_id = q.id.unwrap_or(0);
                let order = self.repo().find_order_by_id(order_id).await?
                    .ok_or_else(|| Error::biz(500, MSG_ORDER_NOT_FOUND))?;
                let client_type = q.code.as_deref().unwrap_or("pc");
                let pay_sn = self.create_pending_paylog(
                    &order,
                    "wechat",
                    &format!("微信支付-订单{}", order.order_sn),
                ).await?;
                let pay_info = self.wechat_create(&order, &configs.wechat, client_type, user_id, &pay_sn).await?;
                return Ok(PayCreateVO {
                    order_id: Some(order.order_id),
                    order_sn: Some(order.order_sn.clone()),
                    order_amount: Some(order.unpaid_amount),
                    pay_info: Some(pay_info),
                    code: None,
                });
            }
            "alipay" | "ali" => {
                if !configs.alipay.is_configured() {
                    return Err(Error::biz(
                        1001,
                        format!(
                            "支付宝未配置：缺少 {}（设置-支付设置）",
                            configs.alipay.missing().join("/")
                        ),
                    ));
                }
                Err(Error::biz(
                    1001,
                    "支付宝协议骨架已就绪（RSA2 签名 / 异步通知验签），下单接口待沙箱联调",
                ))
            }
            "paypal" => {
                if !configs.paypal.is_configured() {
                    return Err(Error::biz(
                        1001,
                        format!(
                            "PayPal 未配置：缺少 {}（设置-支付设置）",
                            configs.paypal.missing().join("/")
                        ),
                    ));
                }
                Err(Error::biz(
                    1001,
                    "PayPal 配置与网关已就绪（sandbox/production），OAuth2 下单待沙箱联调",
                ))
            }
            _ => Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED)),
        }
    }

    /// 读取微信/支付宝/PayPal 渠道配置（供回调验签与下单使用）。
    ///
    /// # Errors
    /// 数据库读取失败。
    pub async fn channel_configs(
        &self,
    ) -> Result<crate::payment::channels::ChannelConfigs> {
        crate::payment::channels::load_channel_configs(&self.repos).await
    }

    /// 读取 `config` 表单项（Stripe/Xendit/Antom 等新增渠道使用）。
    pub async fn config_value(&self, code: &str) -> Result<Option<String>> {
        self.repos.system.config_basic.find_by_code(code).await
    }

    /// 创建线下汇款待支付流水（Java `PaylogService#creatPayLog` 离线分支）。
    async fn create_pending_offline_paylog(&self, order: &PayOrderRow) -> Result<String> {
        use tigshop_core::entities::Paylog;
        use tigshop_core::models::payment::paylog::{generate_pay_sn, PayOrderType};
        use tigshop_infrastructure::db::repo::payment::paylog::PAYLOG_STATUS_UNPAID;
        let pay_sn = generate_pay_sn();
        let paylog = Paylog {
            paylog_id: 0,
            pay_sn: pay_sn.clone(),
            pay_name: format!("线下汇款-订单{}", order.order_sn),
            order_id: order.order_id,
            order_sn: order.order_sn.clone(),
            order_amount: order.total_amount,
            order_type: PayOrderType::Product as i32,
            pay_amount: order.unpaid_amount,
            pay_status: PAYLOG_STATUS_UNPAID,
            pay_code: "offline".to_string(),
            profit_sharing_channel: None,
            add_time: Some(tigshop_common::utils::now_seconds() as i32),
            transaction_id: String::new(),
            notify_data: String::new(),
            refund_amount: rust_decimal::Decimal::ZERO,
            token_code: String::new(),
            appid: None,
        };
        self.repos.payment.paylog.insert(&paylog).await?;
        Ok(pay_sn)
    }

    /// 微信支付 v3 直连下单（native/h5/jsapi/app）。
    async fn wechat_create(
        &self,
        order: &PayOrderRow,
        cfg: &crate::payment::channels::WechatPayV3Config,
        client_type: &str,
        user_id: i32,
        pay_sn: &str,
    ) -> Result<serde_json::Value> {
        use crate::payment::channels::{wechat_authorization, wechat_sign, wechat_sign_message};
        let pc_domain = self.config_value("pcDomain").await?.unwrap_or_default();
        if pc_domain.trim().is_empty() {
            return Err(Error::biz(1001, "pcDomain 未配置，无法生成微信支付回调地址"));
        }
        let notify_url = format!("{}/api/order/pay/notify/wechat", pc_domain.trim_end_matches('/'));
        let amount = (order.unpaid_amount * rust_decimal::Decimal::from(100)).round().to_string();
        let (path, body) = match client_type {
            "h5" => ("/v3/pay/transactions/h5", serde_json::json!({
                "appid": cfg.app_id,
                "mchid": cfg.mchid,
                "description": "订单支付",
                "out_trade_no": pay_sn,
                "notify_url": notify_url,
                "amount": {"total": amount, "currency": "CNY"},
                "scene_info": {"payer_client_ip": "127.0.0.1", "h5_info": {"type": "Wap"}}
            })),
            "wechat" | "wechatMini" | "miniProgram" => {
                use sqlx::Row;
                let auth_type = if matches!(client_type, "wechatMini" | "miniProgram") { 2 } else { 1 };
                let row = sqlx::query("SELECT open_id FROM user_authorize WHERE user_id=? AND authorize_type=? ORDER BY authorize_id DESC LIMIT 1")
                    .bind(user_id).bind(auth_type)
                    .fetch_optional(&self.repos.pool).await.map_err(|e| Error::Database(e.to_string()))?;
                let openid = row.and_then(|r| r.try_get::<String, _>("open_id").ok()).unwrap_or_default();
                if openid.is_empty() {
                    return Err(Error::biz(1001, "微信支付需要用户 openid，请先授权登录"));
                }
                ("/v3/pay/transactions/jsapi", serde_json::json!({
                    "appid": cfg.app_id,
                    "mchid": cfg.mchid,
                    "description": "订单支付",
                    "out_trade_no": pay_sn,
                    "notify_url": notify_url,
                    "amount": {"total": amount, "currency": "CNY"},
                    "payer": {"openid": openid}
                }))
            }
            "app" | "wechatApp" => ("/v3/pay/transactions/app", serde_json::json!({
                "appid": cfg.app_id,
                "mchid": cfg.mchid,
                "description": "订单支付",
                "out_trade_no": pay_sn,
                "notify_url": notify_url,
                "amount": {"total": amount, "currency": "CNY"}
            })),
            _ => ("/v3/pay/transactions/native", serde_json::json!({
                "appid": cfg.app_id,
                "mchid": cfg.mchid,
                "description": "订单支付",
                "out_trade_no": pay_sn,
                "notify_url": notify_url,
                "amount": {"total": amount, "currency": "CNY"}
            })),
        };
        let body_text = body.to_string();
        let timestamp = tigshop_common::utils::now_seconds().to_string();
        let nonce = uuid::Uuid::new_v4().simple().to_string();
        let message = wechat_sign_message("POST", path, &timestamp, &nonce, &body_text);
        let private_key = cfg.private_key_pem.as_deref().unwrap_or_default();
        let signature = wechat_sign(private_key, &message)?;
        let auth = wechat_authorization(&cfg.mchid, &cfg.serial_no, &nonce, &timestamp, &signature);
        let client = reqwest::Client::builder()
            .timeout(std::time::Duration::from_secs(20))
            .build()
            .map_err(|e| Error::Internal(e.to_string()))?;
        let resp = client.post(format!("https://api.mch.weixin.qq.com{path}"))
            .header("Authorization", auth)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .body(body_text)
            .send().await
            .map_err(|e| Error::biz(1001, format!("微信支付下单网络异常: {e}")))?;
        let status = resp.status();
        let text = resp.text().await.unwrap_or_default();
        let v: serde_json::Value = serde_json::from_str(&text).unwrap_or(serde_json::Value::Null);
        if !status.is_success() {
            let msg = v.get("message").and_then(|m| m.as_str()).unwrap_or("微信支付下单失败");
            return Err(Error::biz(1001, format!("微信支付下单失败: {msg}")));
        }
        match client_type {
            "h5" => {
                let url = v.get("h5_url").and_then(|x| x.as_str()).unwrap_or_default();
                if url.is_empty() {
                    return Err(Error::biz(1001, "微信支付响应缺少 h5_url"));
                }
                Ok(serde_json::json!({"paySn": pay_sn, "payType": "wechat", "h5Url": url}))
            }
            "wechat" | "wechatMini" | "miniProgram" => {
                let prepay = v.get("prepay_id").and_then(|x| x.as_str()).unwrap_or_default();
                if prepay.is_empty() {
                    return Err(Error::biz(1001, "微信支付响应缺少 prepay_id"));
                }
                let ts = tigshop_common::utils::now_seconds().to_string();
                let n = uuid::Uuid::new_v4().simple().to_string();
                let package = format!("prepay_id={prepay}");
                let sign_msg = format!("{}\n{}\n{}\n{}\n", cfg.app_id, ts, n, package);
                let pay_sign = wechat_sign(private_key, &sign_msg)?;
                Ok(serde_json::json!({
                    "paySn": pay_sn, "payType": "wechat", "appId": cfg.app_id,
                    "timeStamp": ts, "nonceStr": n, "package": package,
                    "signType": "RSA", "paySign": pay_sign
                }))
            }
            "app" | "wechatApp" => {
                let prepay = v.get("prepay_id").and_then(|x| x.as_str()).unwrap_or_default();
                if prepay.is_empty() {
                    return Err(Error::biz(1001, "微信支付响应缺少 prepay_id"));
                }
                let ts = tigshop_common::utils::now_seconds().to_string();
                let n = uuid::Uuid::new_v4().simple().to_string();
                let sign_msg = format!("{}\n{}\n{}\n{}\n", cfg.app_id, ts, n, prepay);
                let pay_sign = wechat_sign(private_key, &sign_msg)?;
                Ok(serde_json::json!({
                    "paySn": pay_sn, "payType": "wechat", "appId": cfg.app_id,
                    "partnerId": cfg.mchid, "prepayId": prepay, "packageValue": "Sign=WXPay",
                    "nonceStr": n, "timeStamp": ts, "sign": pay_sign
                }))
            }
            _ => {
                let url = v.get("code_url").and_then(|x| x.as_str()).unwrap_or_default();
                if url.is_empty() {
                    return Err(Error::biz(1001, "微信支付响应缺少 code_url"));
                }
                Ok(serde_json::json!({"paySn": pay_sn, "payType": "wechat", "codeUrl": url}))
            }
        }
    }

    /// 创建通用待支付 paylog（渠道下单前落库）。
    async fn create_pending_paylog(
        &self,
        order: &PayOrderRow,
        pay_code: &str,
        pay_name: &str,
    ) -> Result<String> {
        use tigshop_core::models::payment::paylog::{generate_pay_sn, PayOrderType};
        use tigshop_infrastructure::db::repo::payment::paylog::PAYLOG_STATUS_UNPAID;
        let pay_sn = generate_pay_sn();
        let paylog = Paylog {
            paylog_id: 0,
            pay_sn: pay_sn.clone(),
            pay_name: pay_name.to_string(),
            order_id: order.order_id,
            order_sn: order.order_sn.clone(),
            order_amount: order.total_amount,
            order_type: PayOrderType::Product as i32,
            pay_amount: order.unpaid_amount,
            pay_status: PAYLOG_STATUS_UNPAID,
            pay_code: pay_code.to_string(),
            profit_sharing_channel: None,
            add_time: Some(tigshop_common::utils::now_seconds() as i32),
            transaction_id: String::new(),
            notify_data: String::new(),
            refund_amount: rust_decimal::Decimal::ZERO,
            token_code: String::new(),
            appid: None,
        };
        self.repos.payment.paylog.insert(&paylog).await?;
        Ok(pay_sn)
    }

    /// 预售定金支付创建（Java `createDepositPayment`）：本地校验后返回未迁移错误。
    pub async fn create_deposit_payment(
        &self,
        q: &PayCreateQuery,
        user_id: Option<i32>,
    ) -> Result<PayCreateVO> {
        self.validate_create_target(q, user_id).await?;
        Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED))
    }

    /// 两条创建端点的共同本地校验：登录 + id + 订单归属 + 未支付。
    async fn validate_create_target(
        &self,
        q: &PayCreateQuery,
        user_id: Option<i32>,
    ) -> Result<()> {
        let user_id = require_user(user_id)?;
        let order_id = q
            .id
            .ok_or_else(|| Error::biz(500, MSG_PARAM_MISSING))?;
        let order = self
            .repo()
            .find_order_by_id(order_id)
            .await?
            .ok_or_else(|| Error::biz(500, MSG_ORDER_NOT_FOUND))?;
        if order.user_id != user_id {
            return Err(Error::biz(500, MSG_ORDER_NOT_FOUND));
        }
        if order.pay_status == PAY_STATUS_PAID {
            return Err(Error::biz(500, MSG_ORDER_ALREADY_PAID));
        }
        let _ = (&q.r#type, &q.code);
        Ok(())
    }
}

/// 登录态校验（Java `requireUserId`）。
fn require_user(user_id: Option<i32>) -> Result<i32> {
    match user_id {
        Some(id) if id != 0 => Ok(id),
        _ => Err(Error::biz(500, MSG_NOT_LOGGED_IN)),
    }
}

/// 订单行 → 订单摘要 VO。
fn to_order_vo(order: &PayOrderRow) -> PayOrderVO {
    PayOrderVO {
        order_id: order.order_id,
        order_sn: order.order_sn.clone(),
        user_id: order.user_id,
        total_amount: order.total_amount,
        paid_amount: order.paid_amount,
        unpaid_amount: order.unpaid_amount,
        balance: order.balance,
        balance_amount: rust_decimal::Decimal::ZERO,
        pay_status: order.pay_status,
        order_status: order.order_status,
        is_presale: order.is_presale,
        presale_type: order.presale_type,
        pay_type_id: order.pay_type_id,
        add_time: format_cst(order.add_time),
        shipping_type_name: Some(order.shipping_type_name.clone()).filter(|s| !s.is_empty()),
    }
}

/// 秒级时间戳 → `YYYY-MM-DD HH:mm:ss`（UTC+8，Java `TigUtils.handelTime` 同口径）。
fn format_cst(sec: i32) -> String {
    chrono::DateTime::<chrono::Utc>::from_timestamp(i64::from(sec), 0)
        .map(|dt| (dt.naive_utc() + chrono::Duration::hours(8)).format("%Y-%m-%d %H:%M:%S").to_string())
        .unwrap_or_default()
}

/// 复制 paylog 到 VO（Java `BeanUtils.copyProperties(one, target)`，`None` 时 no-op）。
fn copy_paylog(target: &mut PaylogVO, paylog: Option<&Paylog>) {
    let Some(source) = paylog else {
        return;
    };
    target.paylog_id = Some(source.paylog_id);
    target.pay_sn = Some(source.pay_sn.clone());
    target.pay_name = Some(source.pay_name.clone());
    target.order_id = Some(source.order_id);
    target.order_sn = Some(source.order_sn.clone());
    target.order_amount = Some(source.order_amount);
    target.order_type = Some(source.order_type);
    target.pay_amount = Some(source.pay_amount);
    target.pay_status = Some(source.pay_status);
    target.pay_code = Some(source.pay_code.clone());
    target.add_time = source.add_time.map(i64::from);
    target.transaction_id = Some(source.transaction_id.clone());
    target.notify_data = Some(source.notify_data.clone());
    target.refund_amount = Some(source.refund_amount);
    target.token_code = Some(source.token_code.clone());
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn format_cst_matches_java_handel_time() {
        // 2026-10-25 10:23:26 +08:00 == 1792899806
        assert_eq!(format_cst(1792899806), "2026-10-25 11:43:26");
        assert_eq!(format_cst(0), "1970-01-01 08:00:00");
    }


    #[test]
    fn require_user_rejects_guest() {
        assert_eq!(require_user(Some(7)).unwrap(), 7);
        assert_eq!(
            require_user(None).unwrap_err().to_string(),
            format!("业务错误 [500]: {MSG_NOT_LOGGED_IN}")
        );
        assert_eq!(
            require_user(Some(0)).unwrap_err().to_string(),
            format!("业务错误 [500]: {MSG_NOT_LOGGED_IN}")
        );
    }

    #[test]
    fn order_vo_maps_all_fields() {
        let row = PayOrderRow {
            order_id: 3,
            order_sn: "SN3".into(),
            user_id: 7,
            total_amount: rust_decimal::Decimal::new(10000, 2),
            paid_amount: rust_decimal::Decimal::ZERO,
            unpaid_amount: rust_decimal::Decimal::new(10000, 2),
            balance: rust_decimal::Decimal::ZERO,
            pay_status: 0,
            order_status: 1,
            pay_type_id: 1,
            is_presale: 0,
            presale_type: None,
            order_type: Some(1),
            parent_order_id: None,
            add_time: 1792899806,
            shipping_type_name: String::new(),
        };
        let vo = to_order_vo(&row);
        assert_eq!(vo.add_time, "2026-10-25 11:43:26");
        assert_eq!(vo.shipping_type_name, None);
        assert_eq!(vo.order_id, 3);
        assert_eq!(vo.order_sn, "SN3");
        assert_eq!(vo.total_amount, rust_decimal::Decimal::new(10000, 2));
        assert_eq!(vo.unpaid_amount, rust_decimal::Decimal::new(10000, 2));
    }

    #[test]
    fn copy_paylog_none_is_noop_and_some_fills() {
        let mut target = PaylogVO::default();
        copy_paylog(&mut target, None);
        assert_eq!(target.paylog_id, None);
        let source = Paylog {
            paylog_id: 9,
            pay_sn: "P9".into(),
            pay_name: "".into(),
            order_id: 3,
            order_sn: "SN3".into(),
            order_amount: rust_decimal::Decimal::new(10000, 2),
            order_type: 0,
            pay_amount: rust_decimal::Decimal::new(10000, 2),
            pay_status: 1,
            pay_code: "wechat".into(),
            profit_sharing_channel: None,
            add_time: Some(1_700_000_000),
            transaction_id: "T".into(),
            notify_data: "{}".into(),
            refund_amount: rust_decimal::Decimal::ZERO,
            token_code: "tk".into(),
            appid: None,
        };
        copy_paylog(&mut target, Some(&source));
        assert_eq!(target.paylog_id, Some(9));
        assert_eq!(target.add_time, Some(1_700_000_000));
        assert_eq!(target.pay_code.as_deref(), Some("wechat"));
    }
}

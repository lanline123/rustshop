//! Apple Pay 前台服务（payment::apple_pay，4 个 `optional` 端点）。
//!
//! 对应原 Java `com.tigshop.api.controller.order.ApplePayController`：
//! `validateMerchant` / `confirm` / `confirmByPaymentMethod` / `notify/ums`。
//!
//! # 已实现（本地校验部分）
//! - 未登录 →「用户未登录」；
//! - 请求体缺少 `paySn` →「参数缺失」；
//! - `paylog` 不存在 →「找不到原支付记录」；
//! - 归属校验：`paylog.order_type = 1`（充值单）走 `user_recharge_order.user_id`，
//!   否则走 `order.user_id`；不匹配/不存在 →「订单不存在」。
//!
//! # 未迁移
//! 三个 Apple Pay 端点与 `notify/ums` 的验签/商户校验/支付确认/异步通知处理
//! 依赖银联商务、Stripe、Adyen 等 PSP SDK（`ApplePayService`），Rust 侧未迁移：
//! 本地校验通过后统一返回 [`MSG_CHANNEL_UNMIGRATED`]。

use std::sync::Arc;

use serde_json::Value;

use tigshop_core::models::payment::apple_pay::{
    body_is_empty, pay_sn_from_body, MSG_CHANNEL_UNMIGRATED, MSG_ORDER_NOT_FOUND,
    MSG_PARAM_MISSING, MSG_PAYLOG_NOT_FOUND, MSG_USER_NOT_LOGIN,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

pub struct ApplePayService {
    repos: Arc<Repositories>,
}

impl ApplePayService {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// `POST /api/order/pay/applepay/validateMerchant`：商户校验（PSP 未迁移）。
    pub async fn validate_merchant(&self, user_id: Option<i32>, body: &Value) -> Result<()> {
        self.verify_ownership(user_id, body).await?;
        Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED))
    }

    /// `POST /api/order/pay/applepay/confirm`：支付确认（PSP 未迁移）。
    pub async fn confirm(&self, user_id: Option<i32>, body: &Value) -> Result<()> {
        self.verify_ownership(user_id, body).await?;
        Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED))
    }

    /// `POST /api/order/pay/applepay/confirmByPaymentMethod`：按支付方式确认（PSP 未迁移）。
    pub async fn confirm_by_payment_method(&self, user_id: Option<i32>, body: &Value) -> Result<()> {
        self.verify_ownership(user_id, body).await?;
        Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED))
    }

    /// `POST /api/order/pay/notify/ums`：银联商务异步通知（无法验签）。
    pub async fn notify_ums(&self, body: &Value) -> Result<()> {
        if body_is_empty(body) {
            return Err(Error::biz_msg(MSG_PARAM_MISSING));
        }
        Err(Error::biz_msg(MSG_CHANNEL_UNMIGRATED))
    }

    /// 本地支付记录归属校验（对应 Java ApplePayService 前置逻辑）。
    async fn verify_ownership(&self, user_id: Option<i32>, body: &Value) -> Result<()> {
        let user_id = user_id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::biz_msg(MSG_USER_NOT_LOGIN))?;
        let pay_sn =
            pay_sn_from_body(body).ok_or_else(|| Error::biz_msg(MSG_PARAM_MISSING))?;
        let paylog = self
            .repos
            .payment
            .apple_pay
            .find_paylog_by_pay_sn(&pay_sn)
            .await?
            .ok_or_else(|| Error::biz_msg(MSG_PAYLOG_NOT_FOUND))?;
        let owner = if paylog.order_type == 1 {
            self.repos
                .payment
                .apple_pay
                .recharge_owner(paylog.order_id)
                .await?
        } else {
            self.repos
                .payment
                .apple_pay
                .order_owner(paylog.order_id)
                .await?
        };
        match owner {
            Some(id) if id == user_id => Ok(()),
            _ => Err(Error::biz_msg(MSG_ORDER_NOT_FOUND)),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::payment::apple_pay::pay_sn_from_body;

    #[test]
    fn ownership_preconditions_map_to_verbatim_messages() {
        // 未登录
        let err = tigshop_core::models::payment::apple_pay::MSG_USER_NOT_LOGIN;
        assert_eq!(err, "用户未登录");
        // 缺 paySn
        assert!(pay_sn_from_body(&serde_json::json!({})).is_none());
    }

    #[test]
    fn channel_unmigrated_message_is_explicit() {
        assert!(MSG_CHANNEL_UNMIGRATED.contains("未迁移"));
        assert!(MSG_CHANNEL_UNMIGRATED.contains("Apple Pay"));
    }
}

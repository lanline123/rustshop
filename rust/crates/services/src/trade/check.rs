//! 订单结算域服务（trade::check）。
//!
//! 对应原 Java `com.tigshop.service.order.impl.OrderCheckServiceImpl` 的
//! `getAvailablePaymentType` / `getLastInvoice` / `getOrderCheckData` /
//! `getStoreShippingType`。
//!
//! # 逐端点实现口径
//!
//! - `available_payment_types`：完整实现（读 `useOffline` / `useSurplus` 配置，
//!   礼品卡 flowType=12 特判；「在线支付」额外依赖支付渠道表——支付域未迁移，
//!   当前 `has_channels = false`，见偏差）。
//! - `last_invoice`：完整 DB 实现（`order_invoice` 最近一条）。
//! - `order_check_index` / `order_check_update` / `order_check_update_coupon`：
//!   保留端点与入参，**返回明确「未迁移」业务错误**（优惠券/促销/运费/积分/结算
//!   计算引擎未迁移），不编造金额。
//! - `store_shipping_types`：保留端点与入参，**返回明确「未迁移」业务错误**
//!   （依赖购物车价格计算与运费模板引擎）。

use std::sync::Arc;

use tigshop_core::models::trade::check::{
    build_payment_types, config_enabled, OrderCheckParam, OrderCheckVO, OrderInvoiceVO,
    PaymentTypeVO, StoreShippingTypeQuery, CONFIG_USE_OFFLINE, CONFIG_USE_SURPLUS,
    MSG_CHECK_NOT_MIGRATED, MSG_COUPON_NOT_MIGRATED, MSG_STORE_SHIPPING_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::check::CheckRepository;
use tigshop_infrastructure::db::Repositories;

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
    ///
    /// ⚠️ 「在线支付」的 `paymentService.getAvailablePayment("")` 渠道检查属于支付域，
    /// 当前未迁移 → `has_channels = false`（偏差已记录；余额支付开启时仍会显示在线支付）。
    pub async fn available_payment_types(
        &self,
        flow_type: Option<i32>,
    ) -> Result<Vec<PaymentTypeVO>> {
        let repo = self.repo();
        let use_offline = config_enabled(
            repo.find_config_value(CONFIG_USE_OFFLINE).await?.as_deref(),
        );
        let use_surplus = config_enabled(
            repo.find_config_value(CONFIG_USE_SURPLUS).await?.as_deref(),
        );
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

    /// 结算页 index（未迁移：返回明确业务错误）。
    pub async fn order_check_index(&self, _param: &OrderCheckParam) -> Result<OrderCheckVO> {
        Err(Error::biz_msg(MSG_CHECK_NOT_MIGRATED))
    }

    /// 结算页 update（未迁移：返回明确业务错误）。
    pub async fn order_check_update(&self, _param: &OrderCheckParam) -> Result<OrderCheckVO> {
        Err(Error::biz_msg(MSG_CHECK_NOT_MIGRATED))
    }

    /// 结算页 updateCoupon（未迁移：返回明确业务错误）。
    pub async fn order_check_update_coupon(
        &self,
        _param: &OrderCheckParam,
    ) -> Result<OrderCheckVO> {
        Err(Error::biz_msg(MSG_COUPON_NOT_MIGRATED))
    }

    /// 店铺配送方式（未迁移：返回明确业务错误）。
    pub async fn store_shipping_types(
        &self,
        _query: &StoreShippingTypeQuery,
    ) -> Result<Vec<(i32, Vec<serde_json::Value>)>> {
        Err(Error::biz_msg(MSG_STORE_SHIPPING_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn not_migrated_errors_carry_verbatim_messages() {
        let err = Error::biz_msg(MSG_CHECK_NOT_MIGRATED);
        assert!(err.to_string().contains("尚未迁移"));
        assert!(MSG_COUPON_NOT_MIGRATED.contains("尚未迁移"));
        assert!(MSG_STORE_SHIPPING_NOT_MIGRATED.contains("尚未迁移"));
    }

    #[test]
    fn payment_builder_reused_from_model() {
        let types = build_payment_types(true, true, false, None);
        assert_eq!(types.len(), 2);
    }
}

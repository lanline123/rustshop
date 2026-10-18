//! 订单结算域（trade::check）DTO / VO / 纯函数。
//!
//! 对应原 Java `com.tigshop.api.controller.order.CheckController`
//! （`@RequestMapping("/api/order/check")`，6 个端点）。
//!
//! # 迁移口径（逐端点）
//!
//! | 端点 | 实现 |
//! |------|------|
//! | `GET  getAvailablePaymentType` | 完整 DB/config 实现（`useOffline` / `useSurplus` 开关 + 礼品卡 flowType=12） |
//! | `GET  getInvoice` | 完整 DB 实现（`order_invoice` 最近一条，invoiceType/titleType 缺省 0） |
//! | `POST index` / `update` / `updateCoupon` | 保留端点与入参；结算价格/优惠券/促销/运费/积分计算引擎未迁移 → **明确「未迁移」业务错误**，不编造金额 |
//! | `GET  getStoreShippingType` | 保留端点与入参；依赖购物车价格/运费模板计算引擎未迁移 → **明确「未迁移」业务错误** |
//!
//! 金额 `rust_decimal::Decimal`；时间 i32 秒。

use rust_decimal::Decimal;
use serde::{Deserialize, Serialize};

// ——————————————————————————————————————————————
// 常量（文案逐字对齐 Java / i18n）
// ——————————————————————————————————————————————

/// `请先登录`（Java `getCurrentUserId()` 无登录态时不可用）。
pub const MSG_LOGIN_REQUIRED: &str = "请先登录";
/// 结算页 index / update 未迁移（价格、优惠券、促销、运费、积分、结算计算引擎）。
pub const MSG_CHECK_NOT_MIGRATED: &str =
    "订单结算页依赖优惠券/促销/运费/积分/结算计算引擎，当前 Rust 侧尚未迁移，本接口暂不可用";
/// 更新优惠券未迁移。
pub const MSG_COUPON_NOT_MIGRATED: &str =
    "订单结算的优惠券试算依赖优惠券/促销计算引擎，当前 Rust 侧尚未迁移，本接口暂不可用";
/// 店铺配送方式未迁移。
pub const MSG_STORE_SHIPPING_NOT_MIGRATED: &str =
    "获取店铺配送方式依赖购物车价格计算与运费模板引擎，当前 Rust 侧尚未迁移，本接口暂不可用";

/// 支付类型：在线支付（Java `PaymentType.ONLINE`）。
pub const PAYMENT_TYPE_ONLINE: i32 = 1;
/// 支付类型：线下支付（Java `PaymentType.OFFLINE`）。
pub const PAYMENT_TYPE_OFFLINE: i32 = 3;
/// 礼品卡支付 flowType（Java `getAvailablePaymentType` 特判 12）。
pub const FLOW_TYPE_GIFT_CARD: i32 = 12;
/// 支付类型名称（i18n `payment.type.*`）。
pub const PAYMENT_NAME_ONLINE: &str = "在线支付";
pub const PAYMENT_NAME_OFFLINE: &str = "线下支付";
pub const PAYMENT_NAME_GIFT_CARD: &str = "礼品卡兑换";
/// 配置键：线下支付开关（Java `SettingsEnum.USE_OFFLINE`）。
pub const CONFIG_USE_OFFLINE: &str = "useOffline";
/// 配置键：余额支付开关（Java `SettingsEnum.USE_SURPLUS`）。
pub const CONFIG_USE_SURPLUS: &str = "useSurplus";

// ——————————————————————————————————————————————
// 纯函数
// ——————————————————————————————————————————————

/// 配置值是否开启（Java `"1".equals(bizVal)`；缺失/异常 → false）。
#[must_use]
pub fn config_enabled(raw: Option<&str>) -> bool {
    raw.map(str::trim) == Some("1")
}

/// 构造支付类型列表（Java `OrderCheckServiceImpl#getAvailablePaymentType`）：
/// - `flowType == 12`（礼品卡）→ 仅「礼品卡兑换」（在线，未禁用，显示）；
/// - 否则：余额支付开启**或**有可用支付渠道 → 加入「在线支付」；
///   始终追加「线下支付」，`isShow = 线下支付开关`。
///
/// ⚠️ 支付渠道表（`paymentService.getAvailablePayment`）在 Rust 侧未迁移，
/// `has_payment_channels` 由调用方传入（当前恒 false，并记录偏差）。
#[must_use]
pub fn build_payment_types(
    use_offline: bool,
    use_surplus: bool,
    has_payment_channels: bool,
    flow_type: Option<i32>,
) -> Vec<PaymentTypeVO> {
    let mut result = Vec::new();
    if flow_type == Some(FLOW_TYPE_GIFT_CARD) {
        result.push(PaymentTypeVO {
            type_id: PAYMENT_TYPE_ONLINE,
            type_name: PAYMENT_NAME_GIFT_CARD.to_string(),
            disabled: false,
            disabled_desc: String::new(),
            is_show: true,
        });
        return result;
    }
    if use_surplus || has_payment_channels {
        result.push(PaymentTypeVO {
            type_id: PAYMENT_TYPE_ONLINE,
            type_name: PAYMENT_NAME_ONLINE.to_string(),
            disabled: false,
            disabled_desc: String::new(),
            is_show: true,
        });
    }
    result.push(PaymentTypeVO {
        type_id: PAYMENT_TYPE_OFFLINE,
        type_name: PAYMENT_NAME_OFFLINE.to_string(),
        disabled: false,
        disabled_desc: String::new(),
        is_show: use_offline,
    });
    result
}

/// 发票查询参数归一（Java `getLastInvoice`：null → 0）。
#[must_use]
pub fn invoice_filter(invoice_type: Option<i32>, title_type: Option<i32>) -> (i32, i32) {
    (invoice_type.unwrap_or(0), title_type.unwrap_or(0))
}

// ——————————————————————————————————————————————
// 入参
// ——————————————————————————————————————————————

/// 结算参数（Java `OrderCheckParam` 的常用字段子集；计算引擎未迁移，仅用于入参兼容）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderCheckParam {
    pub user_id: Option<i32>,
    pub flow_type: Option<i32>,
    pub shop_id: Option<i32>,
    pub delivery_type: Option<i32>,
    pub address_id: Option<i32>,
    pub pay_type_id: Option<i32>,
    pub use_point: Option<i32>,
    pub use_balance: Option<Decimal>,
    pub use_coupon_ids: Option<Vec<i32>>,
    pub select_user_coupon_ids: Option<Vec<i32>>,
    pub use_default_coupon_ids: Option<i32>,
    pub buyer_note: Option<String>,
    pub select_from: Option<i32>,
    pub enable_change_price: Option<bool>,
}

/// 店铺配送方式查询（Java `StoreShippingTypeQuery`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct StoreShippingTypeQuery {
    pub shop_id: Option<i32>,
    pub flow_type: Option<i32>,
    pub user_id: Option<i32>,
    pub address_id: Option<i32>,
    pub pay_type_id: Option<i32>,
    pub use_point: Option<i32>,
    pub use_balance: Option<Decimal>,
    pub use_coupon_ids: Option<Vec<i32>>,
    pub exclude_quick_product: Option<bool>,
}

/// 支付方式查询（Java `@RequestParam flowType`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct PaymentTypeQuery {
    pub flow_type: Option<i32>,
}

/// 发票查询（Java `@RequestParam invoiceType/titleType`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct InvoiceQuery {
    pub invoice_type: Option<i32>,
    pub title_type: Option<i32>,
}

// ——————————————————————————————————————————————
// 出参
// ——————————————————————————————————————————————

/// 支付方式（Java `OrderCheckVO.PaymentType`）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct PaymentTypeVO {
    pub type_id: i32,
    pub type_name: String,
    pub disabled: bool,
    pub disabled_desc: String,
    pub is_show: bool,
}

/// 订单发票（Java `OrderInvoice` 实体字段）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderInvoiceVO {
    pub id: i32,
    pub user_id: i32,
    pub order_id: i32,
    pub invoice_type: i32,
    pub status: i32,
    pub apply_reply: String,
    pub title_type: i32,
    pub company_code: String,
    pub company_name: String,
    pub company_address: String,
    pub company_phone: String,
    pub company_bank: String,
    pub company_account: String,
    pub invoice_content: String,
    pub invoice_no: String,
    pub amount: Decimal,
    pub mobile: String,
    pub email: String,
    pub add_time: i32,
    pub invoice_attachment: Option<String>,
    pub invoicing_time: i32,
}

/// 结算页返回（Java `OrderCheckVO`）。
///
/// **未迁移**：本域 `index` / `update` / `updateCoupon` 始终返回业务错误，
/// 该结构不会被实际下发（保留类型以对齐契约返回类型）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderCheckVO {
    /// 未迁移标记（恒 true）
    pub calculation_not_migrated: bool,
}

/// 配送方式（Java `OrderCheckVO.ShippingType` 常用字段）；
/// `getStoreShippingType` 未迁移，本结构不会被实际下发。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct ShippingTypeVO {
    pub shipping_type_id: i32,
    pub shipping_type_name: String,
    pub origin_shipping_type_name: String,
    pub shipping_default_id: Option<i32>,
    pub shipping_type_desc: Option<String>,
    pub shipping_time_desc: Option<String>,
    pub is_support_cod: Option<i32>,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn payment_types_match_java_logic() {
        // flowType == 12：仅礼品卡兑换
        let types = build_payment_types(true, true, true, Some(12));
        assert_eq!(types.len(), 1);
        assert_eq!(types[0].type_id, PAYMENT_TYPE_ONLINE);
        assert_eq!(types[0].type_name, "礼品卡兑换");
        assert!(types[0].is_show);

        // 余额开关关闭且无渠道：仅线下支付，isShow 跟随 useOffline
        let types = build_payment_types(false, false, false, None);
        assert_eq!(types.len(), 1);
        assert_eq!(types[0].type_id, PAYMENT_TYPE_OFFLINE);
        assert_eq!(types[0].type_name, "线下支付");
        assert!(!types[0].is_show);

        // 余额开启：在线 + 线下
        let types = build_payment_types(true, true, false, None);
        assert_eq!(types.len(), 2);
        assert_eq!(types[0].type_id, PAYMENT_TYPE_ONLINE);
        assert_eq!(types[0].type_name, "在线支付");
        assert!(types[0].is_show);
        assert_eq!(types[1].type_id, PAYMENT_TYPE_OFFLINE);
        assert!(types[1].is_show);

        // 支付渠道可用（即使余额关闭）也显示在线支付
        let types = build_payment_types(false, false, true, None);
        assert_eq!(types.len(), 2);
        assert_eq!(types[0].type_id, PAYMENT_TYPE_ONLINE);
    }

    #[test]
    fn config_and_invoice_defaults() {
        assert!(config_enabled(Some("1")));
        assert!(!config_enabled(Some("0")));
        assert!(!config_enabled(None));
        assert!(!config_enabled(Some("true")));
        assert_eq!(invoice_filter(None, None), (0, 0));
        assert_eq!(invoice_filter(Some(1), Some(2)), (1, 2));
    }

    #[test]
    fn params_accept_java_names_and_vo_camel_case() {
        let param: OrderCheckParam = serde_json::from_str(
            r#"{"flowType":15,"addressId":3,"useCouponIds":[1,2],"useBalance":10.5,"selectFrom":1}"#,
        )
        .unwrap();
        assert_eq!(param.flow_type, Some(15));
        assert_eq!(param.use_coupon_ids, Some(vec![1, 2]));
        let q: InvoiceQuery = serde_json::from_str(r#"{"invoiceType":1,"titleType":2}"#).unwrap();
        assert_eq!(q.title_type, Some(2));
        let vo = OrderInvoiceVO {
            id: 1,
            amount: Decimal::new(1050, 2),
            ..Default::default()
        };
        let value = serde_json::to_value(&vo).unwrap();
        assert_eq!(value["invoiceType"], 0);
        assert_eq!(value["amount"], "10.50");
        let payment = PaymentTypeVO {
            type_id: 1,
            type_name: "在线支付".into(),
            ..Default::default()
        };
        let value = serde_json::to_value(&payment).unwrap();
        assert_eq!(value["typeId"], 1);
        assert_eq!(value["typeName"], "在线支付");
    }

    #[test]
    fn not_migrated_messages_are_explicit() {
        for msg in [
            MSG_CHECK_NOT_MIGRATED,
            MSG_COUPON_NOT_MIGRATED,
            MSG_STORE_SHIPPING_NOT_MIGRATED,
        ] {
            assert!(msg.contains("尚未迁移"), "文案需明确未迁移: {msg}");
        }
    }
}

//! 订单 DTO，对应原 Java `dto/order`、`query/order`、`vo/order`。

use rust_decimal::Decimal;
use serde::{Deserialize, Serialize};

/// 创建订单请求（提交结算页数据）。
#[derive(Debug, Clone, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderCreateDTO {
    /// 购物车行ID列表
    pub cart_ids: Vec<i32>,
    /// 收货地址ID；自提时可空
    pub address_id: Option<i32>,
    /// 配送方式：1-快递 2-自提 3-同城配送
    pub shipping_method: Option<i32>,
    /// 使用优惠券ID
    pub coupon_id: Option<i32>,
    /// 使用积分
    pub use_points: Option<i32>,
    /// 买家备注
    pub buyer_note: Option<String>,
    /// 支付方式：对应 pay_type_id
    pub pay_type_id: Option<i32>,
    /// 是否使用余额支付
    pub use_balance: Option<bool>,
}

impl Default for OrderCreateDTO {
    fn default() -> Self {
        Self {
            cart_ids: Vec::new(),
            address_id: None,
            shipping_method: Some(1),
            coupon_id: None,
            use_points: None,
            buyer_note: None,
            pay_type_id: None,
            use_balance: None,
        }
    }
}

/// 创建订单结果。
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct OrderCreateVO {
    pub order_id: i32,
    pub order_sn: String,
    /// 应付总额
    pub total_amount: Decimal,
    /// 需在线支付金额
    pub online_amount: Decimal,
    /// 支付流水号（预支付用）
    pub out_trade_no: String,
}

/// 订单列表项 VO。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct OrderVO {
    pub order_id: i32,
    pub order_sn: String,
    pub order_status: i32,
    pub status_label: String,
    pub pay_status: i32,
    pub shipping_status: i32,
    pub total_amount: Decimal,
    pub paid_amount: Decimal,
    pub consignee: String,
    pub mobile: String,
    pub add_time: i32,
    pub item_count: i32,
}
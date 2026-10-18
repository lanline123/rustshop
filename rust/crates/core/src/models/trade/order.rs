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
    /// 使用积分（前端结算表单字段名为 `usePoint`）
    #[serde(default, alias = "usePoint")]
    pub use_points: Option<i32>,
    /// 买家备注
    pub buyer_note: Option<String>,
    /// 支付方式：对应 pay_type_id
    pub pay_type_id: Option<i32>,
    /// 使用余额金额（0/缺省 = 不使用；`true` 兼容旧前端 = 尽可能全额抵扣）
    #[serde(default, deserialize_with = "de_use_balance")]
    pub use_balance: Option<Decimal>,
    /// 前端结算表单里的用户优惠券 ID 列表（`useCouponIds`）
    #[serde(default)]
    pub use_coupon_ids: Option<Vec<i32>>,
}

/// 兼容前端三种 `useBalance` 入参：数字（余额抵扣金额）、布尔、字符串数字。
fn de_use_balance<'de, D>(deserializer: D) -> std::result::Result<Option<Decimal>, D::Error>
where
    D: serde::Deserializer<'de>,
{
    use serde::Deserialize;
    let value = Option::<serde_json::Value>::deserialize(deserializer)?;
    let Some(value) = value else {
        return Ok(None);
    };
    match value {
        serde_json::Value::Null => Ok(None),
        serde_json::Value::Bool(true) => Ok(Some(Decimal::MAX)),
        serde_json::Value::Bool(false) => Ok(None),
        serde_json::Value::Number(n) => Ok(n.to_string().parse::<Decimal>().ok()),
        serde_json::Value::String(s) => Ok(s.trim().parse::<Decimal>().ok()),
        _ => Ok(None),
    }
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
            use_coupon_ids: None,
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
    /// 1=需继续支付（跳支付页）；2=无需支付（余额已全额抵扣，前端直接跳订单列表）
    #[serde(default = "default_return_type")]
    pub return_type: i32,
    /// 拼团记录 ID（下单成功时回填）
    #[serde(default)]
    pub groupon_promotion_record_id: Option<i32>,
}

fn default_return_type() -> i32 {
    1
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
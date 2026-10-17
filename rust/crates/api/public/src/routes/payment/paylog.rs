//! 商城前台支付路由。
//!
//! 回调路径对齐原 Java `PayController`（`/api/order/pay`）：
//!
//! | 本模块路由 | 原 Java |
//! |-----------|---------|
//! | `POST /api/order/pay/notify/{payType}` | `PayController.notify` |
//! | `POST /api/order/pay/refundNotify/{payType}` | `PayController.refundNotify` |
//! | `POST /api/order/pay/notify/lakala` | `PayController.lakalaNotify` |
//! | `POST /api/pay/prepare` | Rust 版新增（原系统由 `/api/order/pay/create` 承担） |
//! | `GET  /api/pay/refunds` | Rust 版新增（原系统无退款记录查询端点） |
//!
//! **安全说明**：`notify` 系列接口虽为公开，但必须由渠道签名校验
//! 通过后才可信。当前实现仅落库与状态流转，**签名校验待各渠道
//! 实现接入**（见 `services/payment.rs` 的 `PayChannel` trait）。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use rust_decimal::Decimal;
use serde::Deserialize;
use tigshop_core::models::payment::paylog::{PayOrderType, PaylogRefundVO, RefundStatus};
use tigshop_security::web::{ok, ApiResult, AuthUser};
use tigshop_services::payment::paylog::PaylogOrderRef;

use crate::PublicState;

/// 支付域**需登录**路由（外层已套 `require_auth`）。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
        .route("/pay/prepare", post(prepare))
        .route("/pay/refunds", get(refunds))
}

/// 支付域**公开**路由（渠道回调，靠签名而非登录态鉴权）。
///
/// 路径参数名沿用原 Java 的 `payType`（camelCase），以便与契约逐字符对齐。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
        // 拉卡拉回调路径更具体，需先于 `{payType}` 注册
        .route("/order/pay/notify/lakala", post(notify))
        .route("/order/pay/notify/{payType}", post(notify))
        .route("/order/pay/refundNotify/{payType}", post(refund_notify))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
///
/// 仅列出与原契约路径一致的端点；`/api/pay/prepare` 与 `/api/pay/refunds`
/// 属 Rust 新增，会被覆盖率报告归入 `extra_routes`。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("POST", "/api/order/pay/notify/lakala", "PayController.lakalaNotify"),
        ("POST", "/api/order/pay/notify/{payType}", "PayController.notify"),
        ("POST", "/api/order/pay/refundNotify/{payType}", "PayController.refundNotify"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// 发起支付请求。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct PrepareDTO {
    order_id: i32,
    order_sn: String,
    /// 订单总额
    total_amount: Decimal,
    /// 应付金额；缺省取总额
    unpaid_amount: Option<Decimal>,
    /// 支付方式编码：wechat / alipay / balance ...
    pay_code: String,
    /// 业务类型，缺省为商品订单
    order_type: Option<i32>,
    /// 分账渠道（可选）
    profit_sharing_channel: Option<String>,
    /// 预售类型：2 = 定金预售（不参与支付单复用）
    presale_type: Option<i32>,
}

/// `POST /api/pay/prepare`：发起支付，返回支付单信息。
///
/// 对应 Java `PayService#pay` 的支付单准备阶段：
/// 创建或复用 `paylog`，返回 `paySn` 供前端调起渠道。
/// **渠道支付参数**需各渠道实现接入后填充。
async fn prepare(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<PrepareDTO>,
) -> ApiResult<serde_json::Value> {
    let order_type = match dto.order_type {
        Some(t) => PayOrderType::try_from(t)
            .map_err(|_| tigshop_core::Error::Validation("业务类型非法（应为 0/1/2）".into()))?,
        None => PayOrderType::Product,
    };

    let order = PaylogOrderRef {
        order_id: dto.order_id,
        order_sn: dto.order_sn.clone(),
        total_amount: dto.total_amount,
        unpaid_amount: dto.unpaid_amount.unwrap_or(dto.total_amount),
        presale_type: dto.presale_type,
    };

    let paylog = state.payment_paylog
        .create_or_reuse(
            None,
            &order,
            &dto.pay_code,
            order_type,
            dto.unpaid_amount,
            dto.profit_sharing_channel.as_deref(),
        )
        .await?;

    tracing::info!(
        "用户 {} 发起支付: pay_sn={} order_sn={} amount={}",
        auth.id,
        paylog.pay_sn,
        dto.order_sn,
        paylog.pay_amount
    );

    ok(serde_json::json!({
        "paySn": paylog.pay_sn,
        "payCode": paylog.pay_code,
        "payAmount": paylog.pay_amount,
        "payStatus": paylog.pay_status,
        // 渠道支付参数由具体渠道实现填充（微信 JSAPI / 支付宝表单 / 跳转 URL）
        "payParams": serde_json::Value::Null,
    }))
}

/// 支付回调载荷（各渠道字段不同，此处取通用字段）。
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct NotifyDTO {
    /// 商户支付单号
    out_trade_no: Option<String>,
    /// 渠道交易号
    transaction_id: Option<String>,
    /// 实付金额
    pay_amount: Option<Decimal>,
    /// 原始报文（留痕）
    raw: Option<String>,
}

/// `POST /api/pay/notify`：渠道支付回调。
///
/// 按商户支付单号标记支付成功（幂等：已支付的行不会被重复更新）。
///
/// **注意**：当前**未做渠道签名校验** —— 生产环境必须在各渠道实现
/// 中验签后再调用，否则可被伪造回调。参见 `services/payment.rs`。
async fn notify(
    State(state): State<PublicState>,
    Json(dto): Json<NotifyDTO>,
) -> ApiResult<serde_json::Value> {
    let pay_sn = dto
        .out_trade_no
        .as_deref()
        .filter(|s| !s.trim().is_empty())
        .ok_or_else(|| tigshop_core::Error::Validation("缺少商户支付单号".into()))?;

    let transaction_id = dto.transaction_id.as_deref().unwrap_or_default();
    let raw = dto.raw.as_deref().unwrap_or_default();

    let affected = state.payment_paylog
        .mark_paid_by_pay_sn(pay_sn, transaction_id, raw)
        .await?;

    ok(serde_json::json!({
        "paySn": pay_sn,
        "updatedRows": affected,
        // 渠道要求返回的确认标识（微信/支付宝约定为 SUCCESS）
        "reply": "SUCCESS",
    }))
}

/// 退款回调载荷。
#[derive(Debug, Deserialize, Default)]
#[serde(rename_all = "camelCase", default)]
struct RefundNotifyDTO {
    /// 退款单号（对应 `paylog_refund.refund_sn`）
    refund_sn: Option<String>,
    /// 退款状态：1 成功 2 失败
    status: Option<i32>,
    /// 支付日志ID（若渠道回传）
    paylog_refund_id: Option<i32>,
}

/// `POST /api/pay/refundNotify`：渠道退款回调。
async fn refund_notify(
    State(state): State<PublicState>,
    Json(dto): Json<RefundNotifyDTO>,
) -> ApiResult<serde_json::Value> {
    let refund_id = dto
        .paylog_refund_id
        .ok_or_else(|| tigshop_core::Error::Validation("缺少退款日志ID".into()))?;
    let status = RefundStatus::try_from(dto.status.unwrap_or(1))
        .map_err(|_| tigshop_core::Error::Validation("退款状态非法（应为 0/1/2）".into()))?;

    state.payment_paylog.update_refund_status(refund_id, status).await?;

    ok(serde_json::json!({
        "refundSn": dto.refund_sn,
        "status": status as i32,
        "reply": "SUCCESS",
    }))
}

/// 退款记录查询参数。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct RefundQuery {
    order_id: i32,
}

/// `GET /api/pay/refunds?orderId=`：订单退款记录。
async fn refunds(
    State(state): State<PublicState>,
    Extension(_auth): Extension<AuthUser>,
    Query(q): Query<RefundQuery>,
) -> ApiResult<Vec<PaylogRefundVO>> {
    ok(state.payment_paylog.refunds_of_order(q.order_id).await?)
}
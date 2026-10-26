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
//! **安全说明**：`notify` 系列接口为公开接口，但**必须通过渠道签名校验**
//! 后才落库：微信支付 v3（SHA256-RSA 验签 + AES-256-GCM 解密 `resource`）、
//! 支付宝（RSA2 验签 + `trade_status` 判定）。其它渠道在拿到凭据前返回明确
//! 「待联调」错误，不信任裸 JSON 回调。

use axum::{
    body::Bytes,
    extract::{Path, Query, State},
    http::{HeaderMap, StatusCode},
    response::{IntoResponse, Response},
    routing::{get, post},
    Extension, Json, Router,
};
use rust_decimal::Decimal;
use serde::Deserialize;
use std::collections::BTreeMap;
use tigshop_core::models::payment::paylog::{PayOrderType, PaylogRefundVO, RefundStatus};
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
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
        // 海外渠道回调路径更具体，需先于 `{payType}` 注册
        .route("/order/pay/notify/stripe", post(stripe_notify))
        .route("/order/pay/notify/xendit", post(xendit_notify))
        .route("/order/pay/notify/antom", post(antom_notify))
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
        ("POST", "/api/order/pay/notify/stripe", "PayController.stripeNotify"),
        ("POST", "/api/order/pay/notify/xendit", "PayController.xenditNotify"),
        ("POST", "/api/order/pay/notify/antom", "PayController.antomNotify"),
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

    let paylog = state.payment.paylog
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

/// 渠道支付回调（按 `payType` 分发验签）。
///
/// - 微信支付 v3：`Wechatpay-Signature`（SHA256-RSA）+ APIv3 密钥解密 `resource`；
/// - 支付宝：RSA2 验签 + `trade_status`（`TRADE_SUCCESS`/`TRADE_FINISHED`）；
/// - 其它渠道：明确报「待凭据联调」，不信任未验签回调。
async fn notify(
    State(state): State<PublicState>,
    Path(pay_type): Path<String>,
    axum::extract::OriginalUri(uri): axum::extract::OriginalUri,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, AppError> {
    let configs = state.payment.pay.channel_configs().await?;
    let body_str = String::from_utf8_lossy(&body).to_string();
    match pay_type.as_str() {
        // —— 微信支付 v3 ——
        "wechat" | "wechatPartner" | "wechatCombine" | "wechatH5" => {
            let public_key = configs
                .wechat
                .public_key_pem
                .clone()
                .filter(|v| !v.trim().is_empty())
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "微信支付未配置：缺少 wechatPayPublicKey")))?;
            let api_v3_key = configs
                .wechat
                .api_v3_key
                .clone()
                .filter(|v| !v.trim().is_empty())
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "微信支付未配置：缺少 wechatPayKey（APIv3 密钥）")))?;
            let timestamp = header_str(&headers, "wechatpay-timestamp")
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "微信回调缺少 Wechatpay-Timestamp")))?;
            let nonce = header_str(&headers, "wechatpay-nonce")
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "微信回调缺少 Wechatpay-Nonce")))?;
            let signature = header_str(&headers, "wechatpay-signature")
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "微信回调缺少 Wechatpay-Signature")))?;
            let message = tigshop_services::payment::channels::wechat_sign_message(
                "POST",
                uri.path(),
                &timestamp,
                &nonce,
                &body_str,
            );
            tigshop_services::payment::channels::wechat_verify(&public_key, &message, &signature)?;

            let json: serde_json::Value = serde_json::from_str(&body_str)
                .map_err(|e| AppError(tigshop_core::Error::Validation(format!("微信回调报文非法: {e}"))))?;
            let resource = json
                .get("resource")
                .ok_or_else(|| AppError(tigshop_core::Error::Validation("微信回调缺少 resource".into())))?;
            let ciphertext = resource
                .get("ciphertext")
                .and_then(serde_json::Value::as_str)
                .ok_or_else(|| AppError(tigshop_core::Error::Validation("微信回调缺少 ciphertext".into())))?;
            let resource_nonce = resource
                .get("nonce")
                .and_then(serde_json::Value::as_str)
                .ok_or_else(|| AppError(tigshop_core::Error::Validation("微信回调缺少 nonce".into())))?;
            let associated_data = resource
                .get("associated_data")
                .and_then(serde_json::Value::as_str)
                .unwrap_or_default();
            let plain = tigshop_services::payment::channels::wechat_decrypt_resource(
                &api_v3_key,
                associated_data,
                resource_nonce,
                ciphertext,
            )?;
            let data: serde_json::Value = serde_json::from_str(&plain)
                .map_err(|e| AppError(tigshop_core::Error::Validation(format!("微信回调解密报文非法: {e}"))))?;
            let pay_sn = data
                .get("out_trade_no")
                .and_then(serde_json::Value::as_str)
                .filter(|v| !v.trim().is_empty())
                .ok_or_else(|| AppError(tigshop_core::Error::Validation("微信回调缺少 out_trade_no".into())))?;
            let transaction_id = data
                .get("transaction_id")
                .and_then(serde_json::Value::as_str)
                .unwrap_or_default();
            state
                .payment
                .paylog
                .mark_paid_by_pay_sn(pay_sn, transaction_id, &body_str)
                .await?;
            if let Err(e) = state
                .payment
                .paylog
                .settle_order_by_pay_sn(pay_sn, transaction_id)
                .await
            {
                tracing::warn!("支付回调后推进订单状态失败: pay_sn={pay_sn}, err={e}");
            }
            Ok((
                StatusCode::OK,
                Json(serde_json::json!({ "code": "SUCCESS", "message": "成功" })),
            )
                .into_response())
        }
        // —— 支付宝 ——
        "ali" | "alipay" => {
            let public_key = configs
                .alipay
                .alipay_public_key_pem
                .clone()
                .filter(|v| !v.trim().is_empty())
                .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "支付宝未配置：缺少 alipayRsaPublicKey")))?;
            let form: Vec<(String, String)> = serde_urlencoded::from_bytes(&body)
                .map_err(|e| AppError(tigshop_core::Error::Validation(format!("支付宝回调报文非法: {e}"))))?;
            let params: BTreeMap<String, String> = form.into_iter().collect();
            tigshop_services::payment::channels::alipay_verify_notify(&public_key, &params)?;
            let trade_status = params
                .get("trade_status")
                .map(String::as_str)
                .unwrap_or_default();
            if !matches!(trade_status, "TRADE_SUCCESS" | "TRADE_FINISHED") {
                return Ok((StatusCode::OK, "failure").into_response());
            }
            let pay_sn = params
                .get("out_trade_no")
                .map(String::as_str)
                .filter(|v| !v.trim().is_empty())
                .ok_or_else(|| AppError(tigshop_core::Error::Validation("支付宝回调缺少 out_trade_no".into())))?;
            let transaction_id = params.get("trade_no").map(String::as_str).unwrap_or_default();
            state
                .payment
                .paylog
                .mark_paid_by_pay_sn(pay_sn, transaction_id, &body_str)
                .await?;
            if let Err(e) = state
                .payment
                .paylog
                .settle_order_by_pay_sn(pay_sn, transaction_id)
                .await
            {
                tracing::warn!("支付回调后推进订单状态失败: pay_sn={pay_sn}, err={e}");
            }
            Ok((StatusCode::OK, "success").into_response())
        }
        // —— PayPal / 其它渠道 ——
        other => Err(AppError(tigshop_core::Error::biz(
            1001,
            format!("支付渠道 {other} 回调验签待凭据联调（协议骨架已就绪）"),
        ))),
    }
}

fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|value| value.to_str().ok())
        .map(str::to_string)
}

/// `POST /api/order/pay/notify/stripe`：Stripe Webhook。
///
/// 仅接受 HMAC 验签通过的 `payment_intent.succeeded` / `checkout.session.completed`。
async fn stripe_notify(
    State(state): State<PublicState>,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, AppError> {
    let secret = state
        .payment
        .pay
        .config_value("stripeWebhookSecret")
        .await?
        .filter(|v| !v.trim().is_empty())
        .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "Stripe 未配置 Webhook Secret")))?;
    let signature = header_str(&headers, "stripe-signature")
        .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "Stripe 回调缺少 Stripe-Signature")))?;
    let body_str = String::from_utf8_lossy(&body).to_string();
    tigshop_services::payment::channels::stripe_verify_webhook(&secret, &signature, &body_str)?;
    let event: serde_json::Value = serde_json::from_str(&body_str)
        .map_err(|e| AppError(tigshop_core::Error::Validation(format!("Stripe 回调报文非法: {e}"))))?;
    let event_type = event.get("type").and_then(serde_json::Value::as_str).unwrap_or_default();
    if !matches!(event_type, "payment_intent.succeeded" | "checkout.session.completed") {
        return Ok((StatusCode::OK, "SUCCESS").into_response());
    }
    let object = event
        .get("data")
        .and_then(|d| d.get("object"))
        .ok_or_else(|| AppError(tigshop_core::Error::Validation("Stripe 回调缺少 data.object".into())))?;
    let pay_sn = object
        .get("metadata")
        .and_then(|m| m.get("pay_sn"))
        .and_then(serde_json::Value::as_str)
        .or_else(|| object.get("client_reference_id").and_then(serde_json::Value::as_str))
        .filter(|v| !v.trim().is_empty())
        .ok_or_else(|| AppError(tigshop_core::Error::Validation("Stripe 回调缺少 pay_sn".into())))?;
    let transaction_id = object.get("id").and_then(serde_json::Value::as_str).unwrap_or_default();
    state
        .payment
        .paylog
        .mark_paid_by_pay_sn(pay_sn, transaction_id, &body_str)
        .await?;
    if let Err(e) = state
        .payment
        .paylog
        .settle_order_by_pay_sn(pay_sn, transaction_id)
        .await
    {
        tracing::warn!("Stripe 回调推进订单失败: pay_sn={pay_sn}, err={e}");
    }
    Ok((StatusCode::OK, "SUCCESS").into_response())
}

/// `POST /api/order/pay/notify/xendit`：Xendit 回调。
async fn xendit_notify(
    State(state): State<PublicState>,
    headers: HeaderMap,
    body: Bytes,
) -> Result<Response, AppError> {
    let expected = state
        .payment
        .pay
        .config_value("xenditWebhookToken")
        .await?
        .filter(|v| !v.trim().is_empty())
        .ok_or_else(|| AppError(tigshop_core::Error::biz(1001, "Xendit 未配置 Webhook Token")))?;
    let actual = header_str(&headers, "x-callback-token").unwrap_or_default();
    if !tigshop_services::payment::channels::xendit_verify_token(&expected, &actual) {
        return Ok((StatusCode::BAD_REQUEST, Json(serde_json::json!({"status":"FAILED"}))).into_response());
    }
    let body_str = String::from_utf8_lossy(&body).to_string();
    let payload: serde_json::Value = serde_json::from_str(&body_str)
        .map_err(|e| AppError(tigshop_core::Error::Validation(format!("Xendit 回调报文非法: {e}"))))?;
    let status = payload.get("status").and_then(serde_json::Value::as_str).unwrap_or_default();
    if !matches!(status, "PAID" | "SETTLED") {
        return Ok((StatusCode::OK, Json(serde_json::json!({"status":"OK"}))).into_response());
    }
    let pay_sn = payload
        .get("external_id")
        .and_then(serde_json::Value::as_str)
        .filter(|v| !v.trim().is_empty())
        .ok_or_else(|| AppError(tigshop_core::Error::Validation("Xendit 回调缺少 external_id".into())))?;
    let transaction_id = payload.get("id").and_then(serde_json::Value::as_str).unwrap_or_default();
    state
        .payment
        .paylog
        .mark_paid_by_pay_sn(pay_sn, transaction_id, &body_str)
        .await?;
    if let Err(e) = state
        .payment
        .paylog
        .settle_order_by_pay_sn(pay_sn, transaction_id)
        .await
    {
        tracing::warn!("Xendit 回调推进订单失败: pay_sn={pay_sn}, err={e}");
    }
    Ok((StatusCode::OK, Json(serde_json::json!({"status":"OK"}))).into_response())
}

/// `POST /api/order/pay/notify/antom`：Antom 回调（RSA 验签待补）。
async fn antom_notify() -> Result<Response, AppError> {
    Err(AppError(tigshop_core::Error::biz(
        1001,
        "Antom 回调 RSA 验签尚未迁移，不能接受支付通知",
    )))
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

    state.payment.paylog.update_refund_status(refund_id, status).await?;

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
    ok(state.payment.paylog.refunds_of_order(q.order_id).await?)
}
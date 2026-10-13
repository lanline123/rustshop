package com.tigshop.service.pay.applepay;

import java.math.BigDecimal;

/**
 * 收单通道客户端抽象（真正完成扣款/退款/查询的机构）。
 *
 * <p>两种集成模式：</p>
 * <ul>
 *   <li>银行/银联系收单（如银联商务）：{@link #requiresDecryption()} 返回 true，商户自解密后提交卡凭据。</li>
 *   <li>聚合支付/PSP（Stripe、Adyen）：返回 false，直接透传 Apple Pay 原始 token，由 PSP 解密扣款。</li>
 * </ul>
 *
 * @author Tigshop团队
 */
public interface AcquirerClient {

    /**
     * 本收单机构是否需要商户自解密后再提交（true=银行收单自解密；false=PSP 透传原始 token）。
     */
    boolean requiresDecryption();

    /**
     * 是否为异步结算模型（如银联商务：submit 仅"受理/下单"，最终成功以异步通知为准）。
     * 为 true 时，confirm 不得在 submit 后立即 paySuccess，须等 {@link #parseNotify} 驱动入账。
     */
    default boolean isAsyncSettlement() {
        return false;
    }

    /**
     * 提交收单扣款。
     *
     * @param ctx 扣款上下文（paySn/金额/原始 token/解密数据/配置）
     * @return 收单通道交易号（transactionId）
     */
    String submit(ApplePayAcquirerContext ctx);

    /**
     * 收单退款。
     */
    boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config);

    /**
     * 查询收单交易状态（对账/补单）。
     */
    String query(String transactionId, ApplePayAcquirerConfig config);

    /**
     * 是否支持"按 paymentMethodId 确认"（PSP 前端驱动模式，如 Stripe：前端 Stripe.js 出 paymentMethodId 后端确认）。
     */
    default boolean supportsPaymentMethodConfirm() {
        return false;
    }

    /**
     * 按 paymentMethodId 创建并确认扣款（前端已用 PSP SDK 完成 Apple Pay 授权拿到 paymentMethodId）。
     *
     * @param paySn           支付流水号
     * @param amount          金额（服务端为准）
     * @param paymentMethodId 前端返回的 PSP paymentMethodId/token
     * @param config          收单配置
     * @return 交易号（成功时）
     */
    default String submitByPaymentMethod(String paySn, java.math.BigDecimal amount,
                                         String paymentMethodId, ApplePayAcquirerConfig config) {
        throw new UnsupportedOperationException("当前收单机构不支持 paymentMethod 确认");
    }

    /**
     * 解析并验签收单异步通知（如银联商务支付结果通知）。默认不支持。
     *
     * @param params 通知报文参数
     * @param config 收单配置（含验签密钥）
     * @return 解析结果（是否验签通过/是否支付成功/paySn/交易号/应答体）
     */
    default AcquirerNotifyResult parseNotify(java.util.Map<String, Object> params, ApplePayAcquirerConfig config) {
        return AcquirerNotifyResult.unsupported();
    }
}

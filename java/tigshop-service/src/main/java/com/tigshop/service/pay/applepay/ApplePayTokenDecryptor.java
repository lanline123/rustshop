package com.tigshop.service.pay.applepay;

/**
 * Apple Pay 支付 Token 解密抽象。
 *
 * <p>用「支付处理证书私钥」解密 {@code ApplePayPayment.token.paymentData}，得到卡网络令牌/密文用于收单扣款。
 * 安全铁律（BR-07）：解密后的卡凭据仅在内存中使用，禁止落库、禁止写入日志。</p>
 *
 * @author Tigshop团队
 */
public interface ApplePayTokenDecryptor {

    /**
     * 解密 Apple Pay paymentToken。
     *
     * @param paymentTokenJson 前端透传的 ApplePayPayment.token 原始 JSON（含加密 paymentData）
     * @return 解密后的支付数据（仅内存传递给收单，禁止落库/日志）
     */
    String decrypt(String paymentTokenJson);
}

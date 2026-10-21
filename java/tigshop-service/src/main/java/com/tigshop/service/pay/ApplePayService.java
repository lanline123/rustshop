package com.tigshop.service.pay;

import com.tigshop.bean.vo.order.pay.ApplePayConfirmVO;

/**
 * Apple Pay 支付服务（在通用 PayService 基础上扩展商户校验与支付确认两步交互）。
 *
 * @author Tigshop团队
 */
public interface ApplePayService extends PayService {

    /**
     * 商户校验（merchant validation）。
     *
     * @param paySn         支付流水号（校验存在且未支付）
     * @param validationUrl Apple 面板回调给前端的 validationURL（须为 *.apple.com 域）
     * @param displayDomain 前端 window.location.hostname
     * @return Apple 返回的 merchantSession 原始 JSON
     */
    String validateMerchant(String paySn, String validationUrl, String displayDomain);

    /**
     * 支付确认（解密 Token → 收单扣款 → paySuccess）。
     *
     * @param paySn            支付流水号；金额、币种一律以服务端 paylog 为准
     * @param paymentTokenJson ApplePayPayment.token 原始 JSON
     * @return 支付确认结果
     */
    ApplePayConfirmVO confirm(String paySn, String paymentTokenJson);

    /**
     * 按 paymentMethodId 确认（PSP 前端驱动模式，如 Stripe：前端 SDK 完成 Apple Pay 授权拿到 paymentMethodId）。
     *
     * @param paySn           支付流水号；金额以服务端 paylog 为准
     * @param paymentMethodId 前端 PSP SDK 返回的 paymentMethodId
     * @return 支付确认结果
     */
    ApplePayConfirmVO confirmByPaymentMethod(String paySn, String paymentMethodId);
}

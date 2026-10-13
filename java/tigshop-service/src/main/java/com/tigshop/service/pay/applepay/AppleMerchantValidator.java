package com.tigshop.service.pay.applepay;

/**
 * Apple Pay 商户校验（merchant validation）抽象。
 *
 * <p>前端 {@code onvalidatemerchant} 拿到 Apple 面板回调的 validationUrl 后转交后端，
 * 后端用「商户身份证书 + 私钥」与 Apple 服务器建立双向 TLS 会话，换取 merchantSession 原样透传回前端。</p>
 *
 * @author Tigshop团队
 */
public interface AppleMerchantValidator {

    /**
     * 请求 Apple 商户校验会话。
     *
     * @param validationUrl Apple 面板回调给前端的 validationURL（必须为 *.apple.com 域，防 SSRF）
     * @param displayDomain 前端 window.location.hostname
     * @return Apple 返回的 merchantSession 原始 JSON
     */
    String validateMerchant(String validationUrl, String displayDomain);
}

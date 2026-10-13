package com.tigshop.service.pay.applepay;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收单扣款上下文。
 *
 * <p>{@code decryptedPaymentData} 仅在需要自解密的收单机构下有值（禁止落库/日志 BR-07）；
 * {@code rawTokenJson} 为 Apple Pay 原始 token，供 PSP 模式透传。</p>
 *
 * @author Tigshop团队
 */
@Data
@AllArgsConstructor
public class ApplePayAcquirerContext {

    /** 支付流水号 */
    private String paySn;

    /** 扣款金额（服务端为准） */
    private BigDecimal amount;

    /** Apple Pay 原始 token JSON（PSP 模式透传） */
    private String rawTokenJson;

    /** 自解密后的支付凭据（银行收单模式；PSP 模式为 null） */
    private String decryptedPaymentData;

    /** 收单通道配置 */
    private ApplePayAcquirerConfig config;
}

package com.tigshop.service.pay.applepay;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Apple Pay 收单通道配置（从 config 表读取后传入收单客户端）。
 *
 * @author Tigshop团队
 */
@Data
@AllArgsConstructor
public class ApplePayAcquirerConfig {

    /** 收单网关地址 */
    private String gateway;

    /** 收单商户号 */
    private String merchantNo;

    /** 收单密钥 */
    private String key;

    /** 结算币种（ISO 4217） */
    private String currency;
}

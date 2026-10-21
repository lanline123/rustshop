package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 回调通知载荷
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class NotifyPayload {

    /** 序列号 */
    private String serialNumber;

    /** 随机串 */
    private String nonce;

    /** 时间戳 */
    private String timestamp;

    /** 签名 */
    private String signature;

    /** 原始Body内容 */
    private String body;
}

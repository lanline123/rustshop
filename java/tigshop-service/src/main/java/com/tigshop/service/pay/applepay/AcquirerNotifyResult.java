package com.tigshop.service.pay.applepay;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收单异步通知解析结果。
 *
 * @author Tigshop团队
 */
@Data
public class AcquirerNotifyResult {

    /** 验签是否通过 */
    private boolean verified;

    /** 是否支付成功 */
    private boolean paid;

    /** 支付流水号（merOrderId） */
    private String paySn;

    /** 收单交易号 */
    private String transactionId;

    /** 通知报文中的支付金额（元，用于与本地 paylog 交叉校验；为空则跳过校验） */
    private BigDecimal amount;

    /** 回给收单机构的应答体（如银联要求返回 SUCCESS） */
    private String ackResponse;

    public static AcquirerNotifyResult unsupported() {
        return new AcquirerNotifyResult();
    }

    public static AcquirerNotifyResult fail() {
        return new AcquirerNotifyResult();
    }

    public static AcquirerNotifyResult success(String paySn, String transactionId, BigDecimal amount,
                                               String ackResponse) {
        AcquirerNotifyResult r = new AcquirerNotifyResult();
        r.setVerified(true);
        r.setPaid(true);
        r.setPaySn(paySn);
        r.setTransactionId(transactionId);
        r.setAmount(amount);
        r.setAckResponse(ackResponse);
        return r;
    }
}

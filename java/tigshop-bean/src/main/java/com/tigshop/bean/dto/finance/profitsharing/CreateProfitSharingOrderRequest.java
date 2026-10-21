package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 创建分账单请求
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class CreateProfitSharingOrderRequest {

    /** 子商户号 */
    private String subMchId;

    /** 微信支付交易号 */
    private String transactionId;

    /**
     * 原交易请求日期（yyyyMMdd）。
     * <p>汇付延时交易确认分账需按「原支付日」定位原交易，跨天分账时不能用当天。
     * 由业务层从 paylog.addTime 推导填入；微信渠道忽略此字段。</p>
     */
    private String orgReqDate;

    /** 分账金额-元 */
    private BigDecimal splitAmount;

    /** 交易总金额-元（汇付延时确认时用于计算剩余给商户的金额，可选） */
    private BigDecimal totalAmount;

    /** 商户分账单号 */
    private String outOrderNo;

    /** 是否完结 */
    private boolean finish;

    /** 分账描述 */
    private String description;
}

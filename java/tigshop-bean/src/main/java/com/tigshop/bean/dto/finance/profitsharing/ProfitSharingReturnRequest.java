package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 分账回退请求
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class ProfitSharingReturnRequest {

    /** 子商户号 */
    private String subMchId;

    /** 原分账单号 */
    private String outOrderNo;

    /** 回退单号 */
    private String outReturnNo;

    /** 微信支付交易号 */
    private String transactionId;

    /**
     * 原交易请求日期（yyyyMMdd）。
     * <p>汇付分账回退需按「原支付日」定位原交易，跨天回退时不能用当天。
     * 由业务层从 paylog.addTime 推导填入；微信渠道忽略此字段。</p>
     */
    private String orgReqDate;

    /** 回退金额-元 */
    private BigDecimal returnAmount;

    /** 回退描述 */
    private String description;
}

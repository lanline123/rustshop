package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 完结分账请求
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class FinishProfitSharingRequest {

    /** 子商户号 */
    private String subMchId;

    /** 微信支付交易号 */
    private String transactionId;

    /** 商户分账单号 */
    private String outOrderNo;

    /** 分账描述 */
    private String description;
}

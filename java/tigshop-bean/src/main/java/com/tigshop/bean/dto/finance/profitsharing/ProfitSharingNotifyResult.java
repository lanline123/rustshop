package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 分账回调通知解析结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class ProfitSharingNotifyResult {

    /** 商户分账单号 */
    private String outOrderNo;

    /** 微信支付交易号 */
    private String transactionId;

    /** 微信分账单号 */
    private String orderId;

    /** 状态 PROCESSING/FINISHED */
    private String status;

    /** 成功时间 */
    private String successTime;
}

package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

/**
 * 分账单结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class ProfitSharingOrderResult {

    /** 商户分账单号 */
    private String outOrderNo;

    /** 微信分账单号 */
    private String orderId;

    /** 微信返回状态 PROCESSING/FINISHED */
    private String status;

    /** 描述 */
    private String description;
}

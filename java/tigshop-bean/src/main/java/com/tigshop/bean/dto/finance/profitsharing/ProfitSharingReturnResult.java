package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 分账回退结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class ProfitSharingReturnResult {

    /** 商户回退单号 */
    private String outReturnNo;

    /** 微信回退单号 */
    private String returnId;

    /** 状态 PROCESSING/SUCCESS/FAILED */
    private String status;

    /** 回退金额-元 */
    private BigDecimal returnAmount;

    /** 描述 */
    private String description;
}

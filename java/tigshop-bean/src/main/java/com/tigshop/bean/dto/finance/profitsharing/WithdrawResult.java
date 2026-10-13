package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 提现结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class WithdrawResult {

    /** 微信提现单号 */
    private String withdrawId;

    /** 状态 CREATE_SUCCESS/SUCCESS/FAIL/REFUND/CLOSE */
    private String status;

    /** 提现金额-元 */
    private BigDecimal amount;

    /** 失败原因 */
    private String reason;
}

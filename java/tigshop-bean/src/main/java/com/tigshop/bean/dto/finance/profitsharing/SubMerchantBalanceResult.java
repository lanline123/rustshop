package com.tigshop.bean.dto.finance.profitsharing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 子商户余额查询结果
 *
 * @author Tigshop团队
 */
@Getter
@Setter
public class SubMerchantBalanceResult {

    /** 可用余额-元 */
    private BigDecimal availableAmount;

    /** 待结算金额-元 */
    private BigDecimal pendingAmount;
}

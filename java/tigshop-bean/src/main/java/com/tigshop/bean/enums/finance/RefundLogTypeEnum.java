package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款记录类型
 *
 * @author kidd
 * @since 2025/7/7 13:32
 */
@Getter
@AllArgsConstructor
public enum RefundLogTypeEnum {

    ONLINE_REFUND(1, "线上退款"),
    BALANCE_REFUND(2, "余额退款"),
    OFFLINE_REFUND(3, "线下退款"),
    ;

    private final int code;
    private final String desc;
}

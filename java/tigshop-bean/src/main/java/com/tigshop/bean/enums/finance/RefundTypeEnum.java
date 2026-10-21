package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款类型枚举
 *
 * @author kidd
 * @since 2025/4/27 14:53
 */
@Getter
@AllArgsConstructor
public enum RefundTypeEnum {

    REFUND_TYPE_ORDER(1, "订单"),
    REFUND_TYPE_PRODUCT(2, "商品"),
    ;

    private final int code;

    private final String desc;

    public static String getDescByCode(int code) {
        for (RefundTypeEnum refundStatusEnum : RefundTypeEnum.values()) {
            if (refundStatusEnum.getCode() == code) {
                return refundStatusEnum.getDesc();
            }
        }
        return "未知的退款类型";
    }
}

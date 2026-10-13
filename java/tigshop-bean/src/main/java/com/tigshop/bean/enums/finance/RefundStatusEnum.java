package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 *
 * @author kidd
 * @since 2025/4/27 14:53
 */
@Getter
@AllArgsConstructor
public enum RefundStatusEnum {

    REFUND_STATUS_WAIT(0, "待处理"),
    REFUND_STATUS_PROCESSING(1, "处理中"),
    REFUND_STATUS_PROCESSED(2, "已处理"),
    REFUND_STATUS_CANCEL(3, "已取消"),
    REFUND_STATUS_REJECTED(4, "已拒绝"),
    ;

    private final int code;

    private final String desc;

    public static String getDescByCode(int code) {
        for (RefundStatusEnum refundStatusEnum : RefundStatusEnum.values()) {
            if (refundStatusEnum.getCode() == code) {
                return refundStatusEnum.getDesc();
            }
        }
        return "未知的退款状态";
    }
}

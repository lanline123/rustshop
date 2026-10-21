package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 账期支付状态枚举
 *
 * @author Tigshop团队
 * @create 2026-01-31
 */
@Getter
public enum CreditPaymentStatusEnum {

    NORMAL(1, "正常"),
    OVERDUE(2, "逾期"),
    FROZEN(3, "冻结");

    private final Integer code;
    private final String description;

    CreditPaymentStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (CreditPaymentStatusEnum status : CreditPaymentStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}

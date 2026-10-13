package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金退还类型枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositRefundType {

    WITHDRAW(1, "部分退还"),
    FULL_REFUND(2, "全部退还");

    private final Integer code;
    private final String description;

    DepositRefundType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositRefundType type : DepositRefundType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }
}

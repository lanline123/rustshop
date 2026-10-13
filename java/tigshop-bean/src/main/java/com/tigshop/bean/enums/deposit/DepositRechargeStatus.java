package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金充值单状态枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositRechargeStatus {

    PENDING_REVIEW(0, "待审核"),
    CREDITED(1, "已通过"),
    REJECTED(2, "未通过");

    private final Integer code;
    private final String description;

    DepositRechargeStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositRechargeStatus status : DepositRechargeStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}

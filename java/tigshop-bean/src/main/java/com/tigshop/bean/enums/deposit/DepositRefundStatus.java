package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金提现/退还单状态枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositRefundStatus {

    PENDING_REVIEW(0, "待审核"),
    COMPLETED(1, "已通过"),
    REJECTED(2, "未通过");

    private final Integer code;
    private final String description;

    DepositRefundStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositRefundStatus status : DepositRefundStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}

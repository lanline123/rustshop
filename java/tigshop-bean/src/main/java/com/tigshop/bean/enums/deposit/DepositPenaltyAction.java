package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金惩罚动作枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositPenaltyAction {

    FORBID_ON_SHELF(1, "限制上架"),
    OFF_SHELF_ALL(2, "全店下架");

    private final Integer code;
    private final String description;

    DepositPenaltyAction(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositPenaltyAction action : DepositPenaltyAction.values()) {
            if (action.getCode().equals(code)) {
                return action.getDescription();
            }
        }
        return "";
    }
}

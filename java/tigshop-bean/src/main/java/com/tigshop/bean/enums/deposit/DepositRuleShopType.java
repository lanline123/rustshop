package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金规则适用店铺类型枚举
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Getter
public enum DepositRuleShopType {

    SHOP(1, "店铺"),
    SUPPLIER(2, "供应商");

    private final Integer code;
    private final String description;

    DepositRuleShopType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositRuleShopType type : DepositRuleShopType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }
}

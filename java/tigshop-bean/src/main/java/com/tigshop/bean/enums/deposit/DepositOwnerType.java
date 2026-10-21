package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金持有者类型枚举（owner 维度）
 *
 * <p>owner_id 含义：店铺为 shopId，供应商为 vendorId。与规则适用店铺类型
 * {@link DepositRuleShopType} 同码（1店铺 2供应商）。</p>
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Getter
public enum DepositOwnerType {

    SHOP(1, "店铺"),
    SUPPLIER(2, "供应商");

    private final Integer code;
    private final String description;

    DepositOwnerType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositOwnerType type : DepositOwnerType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }
}

package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金应缴调整记录来源枚举
 *
 * @author Tigshop团队
 * @create 2026-06-10
 */
@Getter
public enum DepositAdjustSource {

    BY_CATEGORY_MAX(1, "按类目最高"),
    MANUAL(2, "手动固定"),
    CATEGORY_CHANGE(3, "类目变更"),
    RULE_CHANGE(4, "规则调整");

    private final Integer code;
    private final String description;

    DepositAdjustSource(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositAdjustSource source : DepositAdjustSource.values()) {
            if (source.getCode().equals(code)) {
                return source.getDescription();
            }
        }
        return "";
    }
}

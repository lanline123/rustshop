package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费率规则适用范围（与微服务 RuleScopeTypeEnum 对齐）
 */
@Getter
@AllArgsConstructor
public enum RuleScopeTypeEnum {
    ALL(1, "全部"),
    SPECIFIED(2, "指定"),
    EXCLUDE(3, "不包含");

    private final int code;
    private final String label;

    public static boolean requiresTargetIds(int scopeType) {
        return scopeType == SPECIFIED.getCode() || scopeType == EXCLUDE.getCode();
    }
}

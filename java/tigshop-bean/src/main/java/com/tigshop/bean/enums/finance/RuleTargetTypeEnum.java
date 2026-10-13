package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 费率规则适用主体类型（与微服务 RuleTargetTypeEnum 对齐）
 */
@Getter
@AllArgsConstructor
public enum RuleTargetTypeEnum {
    SHOP(1, "店铺"),
    VENDOR(2, "供应商");

    private final int code;
    private final String label;
}

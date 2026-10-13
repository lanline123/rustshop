package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 授信会员状态枚举
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Getter
public enum CreditUserStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    CreditUserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (CreditUserStatusEnum status : CreditUserStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}

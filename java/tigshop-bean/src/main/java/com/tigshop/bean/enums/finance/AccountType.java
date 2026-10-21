package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户类型枚举
 * @author Tigshop项目组
 */
@Getter
@AllArgsConstructor
public enum AccountType {
    ACCOUNT_BALANCE(1, "账户余额");
    
    private final Integer code;
    private final String description;

    public static AccountType fromCode(Integer code) {
        for (AccountType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

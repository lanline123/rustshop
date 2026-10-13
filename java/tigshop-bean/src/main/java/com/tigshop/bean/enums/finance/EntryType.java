package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 入账方式枚举
 * @author Tigshop项目组
 */
@Getter
@AllArgsConstructor
public enum EntryType {
    AUTO("1", "自动"),
    MANUAL("2", "手动");
    
    private final String code;
    private final String description;

    public static EntryType fromCode(String code) {
        for (EntryType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

package com.tigshop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 时间单位枚举
 */
@Getter
@AllArgsConstructor
public enum TimeUnitEnum {
    
    /**
     * 天
     */
    DAY("d", "天"),
    
    /**
     * 小时
     */
    HOUR("h", "小时");
    
    private final String code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static TimeUnitEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (TimeUnitEnum timeUnit : values()) {
            if (timeUnit.getCode().equals(code)) {
                return timeUnit;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescByCode(String code) {
        TimeUnitEnum timeUnit = getByCode(code);
        return timeUnit != null ? timeUnit.getDesc() : null;
    }
}
package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Tigshop项目组
 */

@Getter
@AllArgsConstructor
public enum DateComponentType {
    DAY("day", "日"),
    MONTH("month", "月"),
    YEAR("year", "年");

    private final String code;
    private final String description;

    public static DateComponentType getType(String code) {
        for (DateComponentType value : DateComponentType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
    



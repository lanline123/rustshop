package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStyleTypeEnum {
    /**
     * 边框背景
     */
    BORDER(1, "边框背景"),
    /**
     * 活动背景
     */
    ACTIVITY_BACKGROUND(2, "活动背景");

    private final int code;
    private final String description;

    public static String getTypeName(Integer type) {
        for (ProductStyleTypeEnum typeEnum : values()) {
            if (typeEnum.code == type) {
                return typeEnum.getDescription();
            }
        }
        return null;
    }

    public static boolean isValidCode(int code) {
        for (ProductStyleTypeEnum typeEnum : values()) {
            if (typeEnum.code == code) {
                return true;
            }
        }
        return false;
    }
}

package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 门店选择类型枚举
 *
 * @author Tigshop团队
 * @create 2024年12月03日
 */
@Getter
@AllArgsConstructor
public enum ShopSelectionTypeEnum {

    /**
     * 选择全部门店
     */
    ALL_SHOPS(1, "全部门店"),

    /**
     * 选择全部门店，排除指定门店
     */
    EXCLUDE_SHOPS(2, "排除指定门店"),

    /**
     * 选择指定门店
     */
    SPECIFIC_SHOPS(3, "指定门店");

    /**
     * 类型代码
     */
    private final Integer code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 对应的枚举值
     */
    public static ShopSelectionTypeEnum fromCode(Integer code) {
        if (code == null) {
            return ALL_SHOPS; // 默认值
        }
        
        for (ShopSelectionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        
        return ALL_SHOPS; // 默认值
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 类型名称
     * @return 对应的枚举值
     */
    public static ShopSelectionTypeEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ALL_SHOPS; // 默认值
        }
        
        for (ShopSelectionTypeEnum type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        
        return ALL_SHOPS; // 默认值
    }
}
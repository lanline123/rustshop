package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStyleItemTypeEnum {
    /**
     * 商品
     */
    PRODUCT(1, "商品"),
    /**
     * 分组
     */
    GROUP(2, "分组"),
    /**
     * 品牌
     */
    BRAND(3, "品牌");

    private final int code;
    private final String description;

    public static String getTypeName(Integer type) {
        for (ProductStyleItemTypeEnum typeEnum : values()) {
            if (typeEnum.code == type) {
                return typeEnum.getDescription();
            }
        }
        return null;
    }
}

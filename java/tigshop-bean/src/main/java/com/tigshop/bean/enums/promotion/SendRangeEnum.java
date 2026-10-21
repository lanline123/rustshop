package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 使用范围
 *
 * @author Tigshop项目组
 * @create 2025年07月08日 16:01
 */
@Getter
public enum SendRangeEnum {
    // 全部商品
    ALL(0, "全部商品"),
    // 商品分类
    CATEGORY(1, "商品分类"),
    // 品牌
    BRAND(2, "品牌"),
    // 指定商品
    PRODUCT(3, "指定商品"),
    // 不包含指定商品
    NOT_PRODUCT(4, "不包含指定商品");

    private final int value;
    private final String desc;

    SendRangeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}

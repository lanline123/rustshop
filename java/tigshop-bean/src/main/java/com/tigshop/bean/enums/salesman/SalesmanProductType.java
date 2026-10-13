package com.tigshop.bean.enums.salesman;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigshop
 */
@Getter
public enum SalesmanProductType {
    /**
     * 不参与推广的商品
     */
    NOT_JOIN(1, "不参与推广的商品"),
    /**
     * 自定义比例的商品
     */
    CUSTOM_SCALE(2, "自定义比例的商品"),
    /**
     * 参与推广的商品
     */
    JOIN(3, "参与推广的商品"),
    /**
     * 自定义金额的商品
     */
    CUSTOM_AMOUNT(4, "自定义金额的商品");

    private final int code;
    private final String description;

    SalesmanProductType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据来源标签代码获取对应的描述
     *
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getDescription(int code) {
        for (SalesmanProductType tag : values()) {
            if (tag.getCode() == code) {
                return tag.getDescription();
            }
        }
        return "";
    }

    /**
     * 判断一个值是否为有效的来源标签代码
     *
     * @param code 来源标签代码
     * @return 如果代码有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (SalesmanProductType tag : values()) {
            if (tag.getCode() == code) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, String> getAll() {
        Map<Integer, String> map = new HashMap<>();
        for (SalesmanProductType tag : values()) {
            map.put(tag.getCode(), tag.getDescription());
        }
        return map;
    }
}

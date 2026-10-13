package com.tigshop.bean.enums.salesman;

import lombok.Getter;

/**
 * @author Tigshop
 */
@Getter
public enum ContentStatus {
    /**
     * 未开始
     */
    NOT_START(0, "未开始"),
    /**
     * 展示中
     */
    SHOWING(1, "展示中"),
    /**
     * 已失效
     */
    END(2, "已失效");

    private final int code;
    private final String description;

    ContentStatus(int code, String description) {
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
        for (ContentStatus tag : values()) {
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
        for (ContentStatus tag : values()) {
            if (tag.getCode() == code) {
                return true;
            }
        }
        return false;
    }

}

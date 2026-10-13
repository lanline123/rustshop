package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 拼团状态
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:52
 */
@Getter
public enum JoinStatusEnum {
    // 拼团状态；1-待成团，2-拼团成功，3-拼团失败，4-已取消
    WAIT_GROUP(1, "待成团"),
    SUCCESS(2, "拼团成功"),
    FAIL(3, "拼团失败"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    JoinStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(Integer code) {
        for (JoinStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.description;
            }
        }
        return "";
    }
}

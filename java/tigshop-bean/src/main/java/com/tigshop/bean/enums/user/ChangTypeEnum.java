package com.tigshop.bean.enums.user;

import lombok.Getter;

/**
 * 变更记录
 *
 * @author Tigshop项目组
 * @create 2025年07月07日 17:13
 */
@Getter
public enum ChangTypeEnum {
    /**
     * 增加
     */
    INC_POINTS(1, "增加"),
    /**
     * 减少
     */
    DEC_POINTS(2, "减少");

    private final int value;
    private final String description;

    ChangTypeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }
}

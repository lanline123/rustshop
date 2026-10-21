package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 参团类型
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:51
 */
@Getter
public enum JoinTypeEnum {
    // 参与类型；1-开团，2-参团
    OPEN_GROUP(1, "开团"),
    JOIN_GROUP(2, "参团");

    private final Integer code;
    private final String description;

    JoinTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}

package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 礼品卡到期类型
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:51
 */
@Getter
public enum ValidityTypeEnum {
    // 参与类型；1-永久有效，2-天数，3-固定时间
    PERMANENT(1, "永久有效"),
    NUMBER(2, "天数"),
    FIXED(3, "固定时间"),
    UNKNOWN(0, "未知")
    ;

    private final Integer code;
    private final String description;

    ValidityTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ValidityTypeEnum getByCode(Integer code) {
        for (ValidityTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}

package com.tigshop.bean.enums.wholesale;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批发价模式枚举：1=折扣换算 2=固定金额
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Getter
@AllArgsConstructor
public enum WholesalePriceModeEnum {

    DISCOUNT(1, "折扣换算"),
    FIXED(2, "固定金额");

    @JsonValue
    private final Integer code;
    private final String desc;

    public static WholesalePriceModeEnum fromCode(Integer code) {
        for (WholesalePriceModeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}

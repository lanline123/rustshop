package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 礼品卡类型
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:51
 */
@Getter
public enum GiftCardEnum {
    // 参与类型；1-电子卡，2-实体卡
    ELECTRONIC_CARD(1, "电子卡"),
    PHYSICAL_CARD(2, "实体卡"),
    UNKNOWN(0, "未知")
    ;

    private final Integer code;
    private final String description;

    GiftCardEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static GiftCardEnum getByCode(Integer code) {
        for (GiftCardEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}

package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 礼品卡组类型
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:51
 */
@Getter
public enum GiftGroupsEnum {
    // 参与类型；1-储值卡，2-兑换卡
    TOP_UP(1, "储值卡"),
    EXCHANGE(2, "兑换卡"),
    UNKNOWN(0, "未知")
    ;

    private final Integer code;
    private final String description;

    GiftGroupsEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static GiftGroupsEnum getByCode(Integer code) {
        for (GiftGroupsEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}

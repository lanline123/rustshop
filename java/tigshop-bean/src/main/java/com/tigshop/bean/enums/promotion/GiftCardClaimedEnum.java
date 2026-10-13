package com.tigshop.bean.enums.promotion;

import lombok.Getter;

/**
 * 礼品卡领取类型
 *
 * @author Tigshop项目组
 * @create 2025年09月29日 13:51
 */
@Getter
public enum GiftCardClaimedEnum {
    // 类型；0=不可领取，1=可领取，2=已领取
    NON_RECEIVABLE(0, "不可领取"),
    RECEIVABLE(1, "可领取"),
    RECEIVED(2, "已领取"),
    ;

    private final Integer code;
    private final String description;

    GiftCardClaimedEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static GiftCardClaimedEnum getByCode(Integer code) {
        for (GiftCardClaimedEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return NON_RECEIVABLE;
    }
}

package com.tigshop.bean.enums.order;

import lombok.Getter;

/**
 * 订单物流服务商快照类型。
 */
@Getter
public enum LogisticsProviderType {

    /** 历史未知或不适用。 */
    UNKNOWN(0),
    /** 快递鸟。 */
    KDNIAO(1),
    /** 快递100。 */
    KUAIDI100(2);

    private final int code;

    LogisticsProviderType(int code) {
        this.code = code;
    }

    /**
     * 未知或非法值统一按历史未知处理。
     */
    public static LogisticsProviderType fromCode(int code) {
        for (LogisticsProviderType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

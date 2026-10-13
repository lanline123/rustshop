package com.tigshop.bean.enums.salesman;

import lombok.Getter;

/**
 * @author Tigshop
 */
@Getter
public enum SalesmanOrderStatus {
    /**
     * 待结算
     */
    UNSETTLED(0, "待结算"),
    /**
     * 已结算
     */
    SETTLED(1, "已结算"),

    /**
     * 已失效
     */
    INVALID(2, "已失效");

    private final int code;
    private final String desc;

    SalesmanOrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getByCode(int code) {
        for (SalesmanOrderStatus status : SalesmanOrderStatus.values()) {
            if (status.getCode() == code) {
                return status.desc;
            }
        }
        return "";
    }
}

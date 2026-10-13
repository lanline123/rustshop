package com.tigshop.bean.enums.o2o;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Tigshop项目组
 * @create 2025年08月28日 13:31
 */
@Getter
@AllArgsConstructor
public enum ShipmentStatusEnum {
    NOT_SHIPPED(1, "待备货"),
    SHIPPED(2, "待核销"),
    RECEIVED(3, "已核销"),
    CANCELED(4, "已取消"),
    ;
    private final Integer code;
    private final String description;

    // getStatusName
    public static String getStatusName(Integer code) {
        for (ShipmentStatusEnum value : ShipmentStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getDescription();
            }
        }
        return "";
    }
}



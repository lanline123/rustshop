package com.tigshop.bean.enums.shop;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * @author tigshop
 */

@Getter
public enum StorePickupType {
    //开业
    OPEN(1, "启用"),
    //暂停运营
    PAUSE(4, "停业"),
    //关店
    CLOSE(10, "禁用");

    private final Integer code;
    private final String description;

    StorePickupType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code) {
        for (StorePickupType type : StorePickupType.values()) {
            if (type.code == code) {
                return type.description;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

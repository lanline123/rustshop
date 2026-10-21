package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 用户金额记录
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserBalanceLogType {
    CHANGE_TYPE_INCREASE(1, "增加"),
    CHANGE_TYPE_DECREASE(2, "减少"),
    CHANGE_TYPE_FROZEN(3, "其他");

    private final int code;
    private final String description;

    UserBalanceLogType(int code, String description){
        this.code = code;
        this.description = description;
    }
    public static String getDescription(int code) {
        for (UserBalanceLogType item : UserBalanceLogType.values()) {
            if (item.getCode() == code) {
                return item.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

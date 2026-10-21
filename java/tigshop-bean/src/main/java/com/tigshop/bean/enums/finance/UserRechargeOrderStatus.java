package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 用户充值订单表
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserRechargeOrderStatus {
    STATUS_WAIT(0, "待确认"),
    STATUS_SUCCESS(1, "已支付"),
    STATUS_FAIL(2, "无效");

    private final int code;
    private final String description;

    UserRechargeOrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(int code) {
        for (UserRechargeOrderStatus status : UserRechargeOrderStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}

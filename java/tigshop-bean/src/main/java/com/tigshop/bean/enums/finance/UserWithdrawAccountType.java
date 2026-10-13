package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 用户提现账户表
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserWithdrawAccountType {
    ACCOUNT_TYPE_BANK(1, "银行卡"),
    ACCOUNT_TYPE_ALIPAY(2, "支付宝"),
    ACCOUNT_TYPE_WECHAT(3, "微信");

    private final int code;
    private final String description;

    UserWithdrawAccountType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(int code) {
        for (UserWithdrawAccountType type : UserWithdrawAccountType.values()) {
            if (type.code == code) {
                return type.description;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}

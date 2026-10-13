package com.tigshop.bean.enums.merchant;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

@Getter
public enum MerchantStatus {
    //正常
    NORMAL(1, "正常"),
    //取消资格
    CANCEL(2, "取消资格");

    private final Integer code;
    private final String description;

    MerchantStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(int code) {
        for (MerchantStatus status : MerchantStatus.values()) {
            if (status.code == code) {
                return status.description;
           }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

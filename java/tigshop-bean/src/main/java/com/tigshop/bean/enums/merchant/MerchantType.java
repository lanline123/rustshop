package com.tigshop.bean.enums.merchant;

import com.tigshop.common.exception.GlobalException;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

public enum MerchantType {
    //个人
    PERSONAL(1, "个人认证"),
    //企业
    ENTERPRISE(2, "企业认证");

    private final Integer code;
    private final String description;

    MerchantType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code){
        for (MerchantType merchantType : MerchantType.values()) {
            if (merchantType.code == code) {
                return merchantType.description;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

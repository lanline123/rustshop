package com.tigshop.bean.enums.merchant;

import com.tigshop.common.exception.GlobalException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

@Getter
@AllArgsConstructor
public enum MerchantApplyTypeEnum {

    PERSONAL(1, "个人"),
    ENTERPRISE(2, "企业"),
    ;

    private final Integer code;
    private final String description;


    public static MerchantApplyTypeEnum fromStatusCode(Integer code) {
        for (MerchantApplyTypeEnum type : MerchantApplyTypeEnum.values()) {
            if (type.code.equals(code)) {
               return type;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

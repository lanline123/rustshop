package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 企业认证类型
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum UserCompanyType {
    PERSONAL(1, "个人"),
    COMPANY(2, "企业");

    private final Integer code;
    private final String description;

    UserCompanyType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code) {
        for (UserCompanyType type : UserCompanyType.values()) {
            if (type.getCode() == code) {
                return type.getDescription();
            }
       }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

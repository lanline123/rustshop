package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * @author 会员类型：0会员，1游客
 * @create  2024-12-25
 */
@Getter
public enum UserTypeEnum {
    MEMBER(0, "会员"),
    GUEST(1, "游客");

    private final Integer code;
    private final String description;

    UserTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code) {
        for (UserTypeEnum type : UserTypeEnum.values()) {
            if (type.getCode() == code) {
                return type.getDescription();
            }
       }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 会员成长类型
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum RankGrowthLogTypeEnum {
    GROWTH_TYPE_ORDER(1, "完成下单"),
    GROWTH_TYPE_REFUND(2, "退款"),
    GROWTH_TYPE_INFORMATION(3, "完善信息"),
    GROWTH_TYPE_BIND_PHONE(4, "绑定手机");

    private final Integer code;
    private final String description;

    RankGrowthLogTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code) {
        for (RankGrowthLogTypeEnum type : RankGrowthLogTypeEnum.values()) {
            if (type.getCode() == code) {
                return type.getDescription();
            }
        }
        throw  new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

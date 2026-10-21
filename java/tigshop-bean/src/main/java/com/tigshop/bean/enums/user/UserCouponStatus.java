package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 优惠券状态类型
 *
 * @author 优惠券实体类
 * @create  2024-12-25
 */

@Getter
public enum UserCouponStatus {
    //即将过期
    STATUS_EXPIRING_SOON(1, "即将过期"),
    STATUS_NORMAL(2, "正常"),
    STATUS_NOT_STARTED(3, "未开始"),
    STATUS_USED(4, "已使用"),
    STATUS_EXPIRED(5, "已过期");

    private final Integer code;
    private final String description;

    UserCouponStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(int code) {
        for (UserCouponStatus status : UserCouponStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
     throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

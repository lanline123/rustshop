package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 用户提现申请表
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserWithDrawApplyStatus {
    STATUS_WAIT(0, "待处理"),
    STATUS_FINISHED(1, "已完成"),
    STATUS_REJECT(2, "拒绝申请");

    private final int code;
    private final String description;

    UserWithDrawApplyStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(int code) {
        for (UserWithDrawApplyStatus status : UserWithDrawApplyStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

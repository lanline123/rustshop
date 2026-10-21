package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 反馈类型
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum UserFeedbackStatus {
    //待回复
    WAIT_REPLY(0, "待回复"),
    //已回复
    REPLIED(1, "已回复"),
    //已关闭
    CLOSED(2, "无效");

    private final Integer code;
    private final String description;

    UserFeedbackStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(int code) {
        for (UserFeedbackStatus status : UserFeedbackStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}

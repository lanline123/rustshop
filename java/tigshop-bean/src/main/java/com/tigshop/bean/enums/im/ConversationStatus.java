package com.tigshop.bean.enums.im;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * im状态
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum ConversationStatus {
    NOT_CONNECTED(0, "待接入"),
    IN_PROGRESS(1, "会话中"),
    CLOSED(2, "会话结束");

    private final Integer code;
    private final String description;

    ConversationStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(int code) {
        for (ConversationStatus status : ConversationStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }
}

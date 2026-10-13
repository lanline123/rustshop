package com.tigshop.service.workbuddy;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

/**
 * WorkBuddy 开放接口异常。
 *
 * <p>在 {@link GlobalException} 的业务码之外额外携带机器可读的 {@code errorCode} 与 {@code retryable}，
 * 供 MCP 连接器直接透传给 WorkBuddy 展示（REQ-07、REQ-09）。
 * {@code code} 复用父类字段并直接作为 HTTP 状态码使用。
 */
@Getter
public class WorkBuddyApiException extends GlobalException {

    private final String errorCode;

    private final boolean retryable;

    public WorkBuddyApiException(String errorCode, String message, int httpStatus, boolean retryable) {
        super(message, httpStatus);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
}

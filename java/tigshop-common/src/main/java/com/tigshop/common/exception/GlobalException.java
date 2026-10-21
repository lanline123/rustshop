package com.tigshop.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 全局异常
 *
 * @author Jayce
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlobalException extends RuntimeException {

    private String message;

    private int code;

    public GlobalException(String message) {
        this.message = message;
        this.code = SERVICE_DATA_ERROR;
    }

    public GlobalException(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
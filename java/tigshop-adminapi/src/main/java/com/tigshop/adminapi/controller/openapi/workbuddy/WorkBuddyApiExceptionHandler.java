package com.tigshop.adminapi.controller.openapi.workbuddy;

import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Set;

/**
 * WorkBuddy 开放接口专用异常处理。
 *
 * <p>仅作用于本包，优先级高于全局 GlobalExceptionHandler，因此其他 Controller 的
 * AjaxResult + HTTP 200 行为不受影响。本处理器返回真实 HTTP 状态码和扁平错误体，
 * 使 MCP 连接器的 {@code !response.ok} 分支真实可达（CR-20260827-164700 FIX-02 方案 A）。
 */
@IgnoreResponseAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.tigshop.adminapi.controller.openapi.workbuddy")
public class WorkBuddyApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WorkBuddyApiExceptionHandler.class);

    /** 可直接作为 HTTP 状态码的业务码；其余业务码归一化为 422。 */
    private static final Set<Integer> HTTP_STATUS_CODES = Set.of(
            HttpStatus.BAD_REQUEST,
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN,
            HttpStatus.NOT_FOUND,
            HttpStatus.CONFLICT);

    private static final int UNPROCESSABLE_ENTITY = 422;

    @ExceptionHandler(WorkBuddyApiException.class)
    public ResponseEntity<ErrorBody> handleWorkBuddyApiException(WorkBuddyApiException e) {
        int status = resolveStatus(e.getCode());
        log.warn("WorkBuddy 开放接口业务异常 errorCode={} status={}", e.getErrorCode(), status);
        return ResponseEntity.status(status)
                .body(new ErrorBody(e.getErrorCode(), e.getMessage(), e.isRetryable()));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorBody> handleGlobalException(GlobalException e) {
        int status = resolveStatus(e.getCode());
        log.warn("WorkBuddy 开放接口业务异常 code={} status={}", e.getCode(), status);
        return ResponseEntity.status(status)
                .body(new ErrorBody(defaultErrorCode(status), e.getMessage(), false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleException(Exception e) {
        log.error("WorkBuddy 开放接口未知异常", e);
        return ResponseEntity.status(HttpStatus.ERROR)
                .body(new ErrorBody("INTERNAL_ERROR", "服务内部错误", true));
    }

    /** 客户端格式错误属于不可重试的上传请求，降级为结构化 WARN，避免刷屏。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorBody> handleUploadMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("WorkBuddy 媒体上传请求方法不支持 method={}", e.getMethod());
        return ResponseEntity.status(HttpStatus.BAD_METHOD)
                .body(new ErrorBody("UPLOAD_METHOD_NOT_ALLOWED", "媒体上传必须使用 POST", false));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorBody> handleUploadMissingPart(MissingServletRequestPartException e) {
        log.warn("WorkBuddy 媒体上传缺少文件字段 part={}", e.getRequestPartName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorBody("UPLOAD_FILE_PART_REQUIRED", "multipart 请求必须包含 files 文件字段", false));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorBody> handleUploadContentType(HttpMediaTypeNotSupportedException e) {
        log.warn("WorkBuddy 媒体上传 Content-Type 不支持 contentType={}", e.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_TYPE)
                .body(new ErrorBody("UPLOAD_CONTENT_TYPE_REQUIRED", "媒体上传必须使用 multipart/form-data", false));
    }

    private static int resolveStatus(int businessCode) {
        return HTTP_STATUS_CODES.contains(businessCode) ? businessCode : UNPROCESSABLE_ENTITY;
    }

    private static String defaultErrorCode(int status) {
        return switch (status) {
            case HttpStatus.BAD_REQUEST -> "BAD_REQUEST";
            case HttpStatus.UNAUTHORIZED -> "WORKBUDDY_KEY_INVALID";
            case HttpStatus.FORBIDDEN -> "FORBIDDEN";
            case HttpStatus.NOT_FOUND -> "NOT_FOUND";
            case HttpStatus.CONFLICT -> "CONFLICT";
            default -> "VALIDATION_FAILED";
        };
    }

    @Data
    public static class ErrorBody {
        private final String errorCode;
        private final String message;
        private final boolean retryable;
    }
}

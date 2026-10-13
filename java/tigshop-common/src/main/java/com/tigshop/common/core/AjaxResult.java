package com.tigshop.common.core;

import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回类
 *
 * @author jayce
 * @create 2024年9月22日 10:56
 */
@Data
@Schema(description = "统一返回类")
public class AjaxResult<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    private AjaxResult() {
    }

    private AjaxResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> AjaxResult<T> success() {
        return new AjaxResult<>(Constants.SUCCESS, "success", null);
    }

    public static <T> AjaxResult<T> success(String message) {
        return new AjaxResult<>(Constants.SUCCESS, message, null);
    }

    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<>(Constants.SUCCESS, "success", data);
    }

    public static <T> AjaxResult<T> success(String message, T data) {
        return new AjaxResult<>(Constants.SUCCESS, message, data);
    }

    public static <T> AjaxResult<T> error(Integer code, String message) {
        return new AjaxResult<>(code, message, null);
    }

    public static <T> AjaxResult<T> error(String message) {
        return new AjaxResult<>(Constants.FAIL, message, null);
    }

    public static <T> AjaxResult<T> error(GlobalException exception) {
        return new AjaxResult<>(exception.getCode(), exception.getMessage(), null);
    }

    public static <T> AjaxResult<T> error(GlobalException exception, String message) {
        return new AjaxResult<>(exception.getCode(), message, null);
    }

}

package com.tigshop.common.handler;

//**---------------------------------------------------------------------+
//** 公共文件 -- 异常处理
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.tigshop.common.core.AjaxResult;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.provider.CurrencyContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static com.tigshop.common.constant.ExceptionConstants.*;
import static com.tigshop.common.constant.HttpStatus.*;

/**
 * @author Tigshop团队
 * @create 2024/10/8 18:07
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @Resource
    TranslatePackage translatePackage;

    @Resource
    MessageHelper messageHelper;

    @Resource
    TigshopProperties tigshopProperties;

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 拦截运行时异常
     */
    @ExceptionHandler(GlobalException.class)
    public AjaxResult<String> handleRuntimeException(GlobalException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生业务异常.", requestUri, e);
        return AjaxResult.error(e.getCode(), translatePackage.translate(e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public AjaxResult<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',文件上传异常", requestUri, e);
        return AjaxResult.error(SERVICE_DATA_ERROR, messageHelper.get("error.file.size.exceed"));
    }
    
    @ExceptionHandler(NoSuchAlgorithmException.class)
    public AjaxResult<String> handleNoSuchAlgorithmException(NoSuchAlgorithmException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',JWT异常", requestUri, e);
        return AjaxResult.error(FORBIDDEN, translatePackage.translate(e.getMessage()));
    }

    @ExceptionHandler(NoSuchMethodException.class)
    public AjaxResult<String> handleNoSuchMethodException(NoSuchMethodException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',请求参数错误", requestUri, e);
        return AjaxResult.error(BAD_REQUEST, messageHelper.get("error.param.validation"));
    }

    /**
     * 拦截数据类型异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public AjaxResult<String> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',数据类型异常", requestUri, e);
        return AjaxResult.error(ERROR, messageHelper.get("error.data.type"));
    }

    /**
     * 拦截数据库访问异常（如 SQL 语法错误 BadSqlGrammarException）。
     * 始终在服务端记录完整堆栈；返回给前端的内容根据 tigshop.is-debug 开关决定：
     * 1（本地/演示站）暴露真实报错便于定位；0（发客户，默认）返回通用提示，避免泄露 SQL。
     */
    @ExceptionHandler(DataAccessException.class)
    public AjaxResult<String> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',数据库异常", requestUri, e);
        if (tigshopProperties.debugEnabled()) {
            return AjaxResult.error(ERROR, e.getMessage());
        }
        return AjaxResult.error(ERROR, messageHelper.get("error.internal"));
    }

    /**
     * 拦截404异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public AjaxResult<String> handlerNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',未找到资源", requestUri, e);
        return AjaxResult.error(NOT_FOUND, messageHelper.get("error.resource.not.found"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public AjaxResult<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',参数缺失", requestUri, e);
        return AjaxResult.error(SERVICE_DATA_ERROR, messageHelper.get("error.param.interface.missing"));
    }

    /**
     * 拦截参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}', 参数校验异常", requestUri, e);

        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : e.getBindingResult().getAllErrors()) {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();

            // 替换参数（例如 {integral}）为配置值
            String resolvedMessage = null;
            if (errorMessage != null) {
                resolvedMessage = errorMessage.replace("{integral}", CurrencyContext.getCurrencyName());
            }

            // 再进行国际化翻译
            errors.put(fieldName, translatePackage.translate(resolvedMessage));
        }

        // 将错误信息合并成一个字符串
        String errorMessage = String.join(", ", errors.values());
        return AjaxResult.error(SERVICE_DATA_ERROR, errorMessage);
    }

    /**
     * 拦截权限不足异常
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public AjaxResult<String> handleAuthorizationDeniedException(AuthorizationDeniedException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',权限不足", requestUri, e);
        return AjaxResult.error(SERVICE_DATA_ERROR, messageHelper.get("error.authorization.denied"));
    }
    // HttpMessageNotReadableException
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',参数错误", requestUri, e);
        return AjaxResult.error(SERVICE_DATA_ERROR, messageHelper.get("error.param.invalid"));
    }
    /**
     * 拦截未知的运行时异常(根异常)
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult<String> handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        if (response.isCommitted()) {
            log.debug("异步/SSE连接断开({}): {}", request.getRequestURI(), e.getMessage());
            return null;
        }
        String requestUri = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestUri, e);
        return AjaxResult.error(SERVICE_DATA_ERROR, translatePackage.translate(e.getMessage()));
    }
}

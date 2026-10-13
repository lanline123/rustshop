package com.tigshop.common.handler;

import com.alibaba.fastjson.JSON;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.core.AjaxResult;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Set;

/**
 * 全局响应拦截
 *
 * @author kidd
 * @since 2025/3/31 16:25
 */
@Component
@ControllerAdvice
@Order(0)
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果类或者方法上加了 @IgnoreResponseAdvice，就跳过处理
        return !(returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)
                || returnType.hasMethodAnnotation(IgnoreResponseAdvice.class));
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 定义 Swagger 接口路径集合
//        Set<String> swaggerPaths = Set.of("/v3/api-docs/swagger-config", "/v3/api-docs/default");
        // 定义后端验证码接口路径集合（使用的jar包中的controller）
        Set<String> captchaPaths = Set.of("/adminapi/common/verification/get", "/adminapi/common/verification/captcha", "/adminapi/common/verification/check");

        if (request.getURI().getPath().startsWith("/v3/api-docs")) {
            return body;
        }
        // 如果是 Swagger 接口，直接返回 body
//        if (swaggerPaths.contains(request.getURI().getPath())) {
//            return body;
//        }
        // 如果是 Swagger 接口，直接返回 body
        if (captchaPaths.contains(request.getURI().getPath())) {
            return body;
        }

        if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            if (body instanceof AjaxResult) return body;

            if (body instanceof String) return JSON.toJSONString(AjaxResult.success(body));

            return AjaxResult.success(body);
        }
        if (mediaType.equals(MediaType.TEXT_PLAIN)) {
            return JSON.toJSONString(AjaxResult.success(body));
        }

        return body;
    }


}

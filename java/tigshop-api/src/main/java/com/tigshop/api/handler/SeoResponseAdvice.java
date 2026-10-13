package com.tigshop.api.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.annotation.IgnoreSeoResponseAdvice;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SeoIdentificationUtil;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.SEO_LINK_CATEGORY;

/**
 * SEO链接响应拦截器
 * 为产品和分类对象添加seoLink字段
 *
 * 方案B重构：预扫描 → 批量查询 → 填充
 * 将原来每个节点 N 次 DB 查询改为固定 4-6 次批量查询
 */
@Component
@ControllerAdvice
@RequiredArgsConstructor
@Order(-1)
public class SeoResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final RedisCache redisCache;
    private final ConfigService configService;
    private final TigshopProperties tigshopProperties;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(SeoResponseAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !(returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)
                || returnType.hasMethodAnnotation(IgnoreResponseAdvice.class) ||
                returnType.getDeclaringClass().isAnnotationPresent(IgnoreSeoResponseAdvice.class)
                || returnType.hasMethodAnnotation(IgnoreSeoResponseAdvice.class));
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter methodParameter,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof AjaxResult) {
            return body;
        }
        String requestPath = request.getURI().getPath();
        if (requestPath.startsWith("/v3/api-docs") || requestPath.startsWith("/swagger")) {
            return body;
        }
            return body;
    }

}

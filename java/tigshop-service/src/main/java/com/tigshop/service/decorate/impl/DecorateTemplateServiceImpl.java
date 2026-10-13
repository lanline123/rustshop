package com.tigshop.service.decorate.impl;

import com.tigshop.common.exception.GlobalException;
import com.tigshop.feign.OpenTemplateApiClient;
import com.tigshop.service.decorate.DecorateTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 装修模板库转发服务实现
 *
 * @author Tigshop团队
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecorateTemplateServiceImpl implements DecorateTemplateService {

    private final OpenTemplateApiClient openTemplateApiClient;

    @Override
    public Map<String, Object> category() {
        return unwrap(openTemplateApiClient.category());
    }

    @Override
    public Map<String, Object> list(String keyword, String pageType, String categoryIds, Integer page, Integer size) {
        return unwrap(openTemplateApiClient.list(keyword, pageType, categoryIds, page, size));
    }

    @Override
    public Map<String, Object> detail(String sn) {
        return unwrap(openTemplateApiClient.detail(sn));
    }

    /**
     * 解包官网响应：{ code, msg, data: { errcode, message, ...业务数据 } }
     * <p>
     * errcode 非 0 视为业务错误抛出；成功时返回 data（业务字段保持 snake_case 原样透传）。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrap(Map<String, Object> response) {
        if (response == null) {
            throw new GlobalException("模板服务无响应");
        }
        Object dataObj = response.get("data");
        if (!(dataObj instanceof Map)) {
            throw new GlobalException("模板服务响应格式异常");
        }
        Map<String, Object> data = (Map<String, Object>) dataObj;
        Object errcode = data.get("errcode");
        if (errcode instanceof Number && ((Number) errcode).intValue() != 0) {
            Object message = data.get("message");
            throw new GlobalException(message != null ? String.valueOf(message) : "模板服务错误");
        }
        return data;
    }
}

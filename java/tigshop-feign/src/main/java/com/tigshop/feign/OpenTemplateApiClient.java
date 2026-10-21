package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 装修模板库开放接口（官网对外接口转发）
 * <p>
 * 文档：OpenTemplate Partner API。请求地址按环境通过 open-template.base-url 配置，
 * 缺省回退线上 https://api.tigshop.com/index 。
 *
 * @author Tigshop团队
 */
@FeignClient(name = "openTemplate", url = "${open-template.base-url:https://api.tigshop.com/index}")
public interface OpenTemplateApiClient {

    /**
     * 模板分类树（页面/行业/节日/颜色四个维度）
     */
    @GetMapping("/open/template/category")
    Map<String, Object> category();

    /**
     * 页面列表（页面维度，支持页面类型/分类/关键字筛选）
     */
    @GetMapping("/open/template/list")
    Map<String, Object> list(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "pageType", required = false) String pageType,
                             @RequestParam(value = "categoryIds", required = false) String categoryIds,
                             @RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "size", required = false) Integer size);

    /**
     * 页面详情（按 page_sn 获取该页装修 JSON）
     */
    @GetMapping("/open/template/detail")
    Map<String, Object> detail(@RequestParam("sn") String sn);
}

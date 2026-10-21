package com.tigshop.service.decorate;

import java.util.Map;

/**
 * 装修模板库转发服务
 * <p>
 * 薄代理官网「OpenTemplate 对外接口」，统一解包官网响应并校验业务错误码后，
 * 将业务数据原样（snake_case）透传给前端。
 *
 * @author Tigshop团队
 */
public interface DecorateTemplateService {

    /**
     * 模板分类树
     */
    Map<String, Object> category();

    /**
     * 页面列表
     *
     * @param keyword     模板名称模糊搜索
     * @param pageType    页面类型枚举（theme/home/nav/category/cart/member）
     * @param categoryIds 分类筛选，逗号分隔的 category_id
     * @param page        页码
     * @param size        每页条数
     */
    Map<String, Object> list(String keyword, String pageType, String categoryIds, Integer page, Integer size);

    /**
     * 页面详情
     *
     * @param sn 页面编号（page_sn）
     */
    Map<String, Object> detail(String sn);
}

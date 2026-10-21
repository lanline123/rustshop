package com.tigshop.bean.enums.decorate;

import lombok.Getter;

/** 装修页面用途类型。 */
@Getter
public enum DecoratePageType {
    HOME(1, "商城首页"),
    MEMBER(2, "个人中心"),
    ACTIVITY(3, "活动页"),
    CATEGORY(4, "分类页"),
    LOGIN(5, "登录页"),
    SEARCH(6, "搜索页"),
    NAVIGATION(7, "导航页"),
    H5_PRODUCT_DETAIL(8, "H5商品详情页"),
    O2O_PLATFORM_HOME(9, "平台O2O门店首页");

    private final Integer code;
    private final String name;

    DecoratePageType(Integer code, String name) { this.code = code; this.name = name; }
}

/**
 * 装修编辑器口径常量
 *
 * decorateType 与旧入口 prop 口径一致（Pc.vue=2、PcCustomPage.vue=3）；
 * pageType 与移动端路由 query 口径一致（ToolBar 三套面板按此切换）。
 */

/** 装修类型 */
export const DecorateType = {
    /** 移动端 */
    MOBILE: 1,
    /** PC 首页 */
    PC_HOME: 2,
    /** PC 自定义页 */
    PC_CUSTOM: 3
} as const;

export type DecorateTypeValue = (typeof DecorateType)[keyof typeof DecorateType];

/** 移动端页面类型 */
export const PageType = {
    /** 首页 */
    HOME: 1,
    /** 会员中心 */
    MEMBER: 2,
    /** 分类中心 */
    CATE_CENTER: 4,
    /** 登录页（PC decorateType=2 / 移动端 decorateType=1 共用此 pageType） */
    LOGIN: 5,
    /** 搜索页（PC decorateType=2 / 移动端 decorateType=1 共用此 pageType） */
    SEARCH: 6,
    /** 商品详情页 */
    PRODUCT_DETAIL: 8
} as const;

export type PageTypeValue = (typeof PageType)[keyof typeof PageType];

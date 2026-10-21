/**
 * 商品详情模块 - 类型定义（移动端）
 *
 * A1/A2/A3 三个模块共享同一份配置结构，差异仅体现在预览风格与 decorateStyle。
 */

/** 商品详情页风格标识（与模块 productDetailA1/A2/A3 一一对应） */
export type ProductDetailStyle = "A1" | "A2" | "A3";

export interface ProductDetailModuleSortItem {
    /** 模块标识 */
    key: string;
    /** 模块名称 */
    label: string;
    /** 是否显示 */
    visible: boolean;
}

export interface ProductDetailModule {
    /** 风格标识：A1-标准 / A2-门店 / A3-跨境 */
    decorateStyle?: ProductDetailStyle;
    /** 商品图片比例：4:3 / 4:5(兼容旧值) / 1:1 */
    imageRatio?: "4:3" | "4:5" | "1:1";
    /** 轮播图左右边距（px） */
    imageMargin?: number;
    /** 商品图显示模式：1-真实，2-大图模式 */
    imageMode?: number;
    /** 轮播图圆角样式 */
    imageRadiusStyle?: "square" | "round" | "large";
    /** 标题大小：large-大，medium-中，small-小 */
    titleSize?: "large" | "medium" | "small";
    /** 标题颜色 */
    titleColor?: string;
    /** 最高显示行数 */
    titleMaxLines?: number;
    /** 价格颜色 */
    priceColor?: string;
    /** 商品划线价颜色 */
    marketPriceColor?: string;
    /** 商品信息模块排序 */
    moduleSortList?: ProductDetailModuleSortItem[];
    /** 购买栏样式：1-圆润风，2-直角风 */
    buyBarStyle?: number;
    /** 按钮圆角 */
    buttonRadius?: number;
    /** 是否显示商城首页 */
    showHome?: boolean;
    /** 是否显示客服入口 */
    showService?: boolean;
    /** 是否显示购物车 */
    showCart?: boolean;
    /** 是否显示店铺信息 */
    showShop?: boolean;
    /** 是否显示 A3 商城头部 */
    showHeader?: boolean;
    /** 背景颜色 */
    backgroundColor?: string;
    /** 内容区块背景颜色 */
    contentBackgroundColor?: string;
    [key: string]: any;
}

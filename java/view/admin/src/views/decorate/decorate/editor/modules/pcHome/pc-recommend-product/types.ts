/**
 * PC 推荐商品模块 - 类型定义
 */

/**
 * 商品选择配置（与 Vort ProductSelectConfig 同构，亦与旧 commonProduct 的 ModuleProductsType 一致）
 */
export interface ProductSelectConfig {
    /** 商品选择类型：1=选商品, 2=按分类, 3=按标签 */
    productSelectType?: number;
    /** 选中的商品ID列表 */
    productIds?: number[];
    /** 分类ID */
    productCategoryId?: number | string;
    /** 商品标签 */
    productTag?: string;
    /** 商品数量（按分类/标签时有效） */
    productNumber?: number;
}

/**
 * 模块框架配置（含 itemGap/columns，为本模块特有，不复用 _shared ModuleFrame）
 */
export interface PcRecommendProductFrame {
    /** 左右内边距 */
    boxPadding?: number;
    /** 上内边距 */
    boxPaddingTop?: number;
    /** 下内边距 */
    boxPaddingBottom?: number;
    /** 容器圆角 */
    boxRadius?: number;
    /** 背景颜色 */
    backgroundColor?: string;
    /** 商品间距 */
    itemGap?: number;
    /** 每行显示数量 */
    columns?: number;
}

export interface PcRecommendProductModule {
    /** 框架配置 */
    frame?: PcRecommendProductFrame;
    /** 标题文本（空则不显示） */
    titleName?: string;
    /** 商品选择配置 */
    products?: ProductSelectConfig;
}

/** 预览用商品项 */
export interface ProductItem {
    productId?: number;
    productName?: string;
    productPrice?: number | string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
    brief?: string;
}

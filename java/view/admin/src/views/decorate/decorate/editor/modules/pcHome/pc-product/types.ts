/**
 * PC 商品模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle } from "../_shared/types";

/**
 * 商品选择配置（与移动端商品模块保持一致）
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

export interface PcProductModule {
    /**
     * 1=一行四个, 2=一行五个
     */
    style?: number;
    goodsStyle?: number;
    goodsRadioStyle?: number;
    textAlign?: number;
    textWeight?: number;
    goodsNameRow?: number;
    goodsNamePadding?: number;
    showName?: number;
    showBrief?: number;
    showPrice?: number;
    goodsPadding?: number;
    buyBtnStyle?: number;
    backgroundColor?: string;
    boxRadius?: number;
    innerPadding?: number;
    boxPadding?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    imgPadding?: number;
    waterfall?: number;
    swiperPageColor?: string;
    frame?: ModuleFrame;
    title?: ModuleTitle;
    products?: ProductSelectConfig;
}

export interface ProductItem {
    productId?: number;
    productName?: string;
    productPrice?: number | string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
    brief?: string;
}

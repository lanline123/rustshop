/**
 * 商品分组模块 - 类型定义
 */

import type { PicItem, LinkItem, PicFillType, GradientType, ModuleFrame } from "../_shared/types";

/**
 * 商品来源类型
 */
export type ProductSourceType = "products" | "productGroup";

/**
 * 商品项数据
 */
export interface ProductItem {
    productId?: number;
    productSn?: string;
    productName?: string;
    productDesc?: string;
    productPrice?: number | string;
    marketPrice?: number | string;
    productStock?: number;
    virtualSales?: number;
    shopTitle?: string;
    picThumb?: string;
    picUrl?: string;
    picLarge?: string;
}

/**
 * 商品分组项
 */
export interface ProductGroupItem {
    /** 列表项稳定唯一标识（仅前端拖拽/diff 使用，避免 vuedraggable item-key 重复导致切换无响应） */
    id?: string | number;
    /** 数据来源：products=手动选商品, productGroup=商品分组 */
    sourceType?: ProductSourceType;
    /** 商品分组ID（sourceType=productGroup 时使用） */
    productGroupId?: string | number;
    /** 商品分组名称（展示用） */
    productGroupName?: string;
    /** 商品个数类型（sourceType=productGroup 时使用） */
    productNumType?: "number" | "all";
    /** 商品个数（sourceType=productGroup 时使用） */
    productNum?: number;
    /** 分组标题 */
    title: string;
    /** 标题图片 */
    titlePic?: PicItem;
    /** 标题链接 */
    titleLink?: LinkItem;
    /** Banner图片 */
    bannerPic?: PicItem;
    /** Banner链接 */
    bannerLink?: LinkItem;
    /** 商品列表 */
    productList: ProductItem[];
}

/**
 * 商品分组来源（按商品分组选择时）
 */
export interface ProductGroupSource {
    /** 商品分组ID */
    productGroupId?: string | number;
    /** 商品分组名称 */
    productGroupName?: string;
    /** 菜单名称 */
    menuTitle?: string;
    /** 商品个数类型 */
    productNumType?: "number" | "all";
    /** 商品个数 */
    productNum?: number;
    /** 主标题 */
    title?: string;
}

/**
 * 购买按钮样式配置
 */
export interface BuyBtnStyle {
    /** 是否显示购买按钮：0=不显示, 1=显示 */
    showBtn?: number;
    /** 按钮图标样式：0=不显示, 1-4=图标样式 */
    btnType?: number;
    /** 按钮文字内容 */
    text?: string;
    /** 按钮文字颜色 */
    textColor?: string;
    /** 按钮图标颜色 */
    iconColor?: string;
    /** 按钮宽度(左右内边距) */
    paddingLeftRight?: number;
}

/**
 * 商品颜色配置
 */
export interface ProductColorConfig {
    /** 信息区左右内距 */
    infoLeftRightPadding?: number;
    /** 商品背景颜色 */
    backgroundColor?: string;
    /** 商品标题颜色 */
    titleColor?: string;
    /** 商品价格颜色 */
    priceColor?: string;
    /** 商品划线价颜色 */
    crossedOutPriceColor?: string;
}

/**
 * 分组标题配置
 */
export interface GroupTitleConfig {
    /** 分组标题间距 */
    marginLeftRight?: number;
    /** 是否显示分组标题：0=不显示, 1=显示 */
    showTitle?: number;
    /** 分组标题字体大小 */
    bigTitleFontSize?: number;
    /** 分组标题颜色 */
    titleColor?: string;
    /** 分组标题选中颜色 */
    activeTitleColor?: string;
    /** 分组标题选中背景色 */
    activeTitleBgColor?: string;
    /** 分组标题最小宽度 */
    titleMinWidth?: number;
    /** 分组标题上下内边距 */
    titlePaddingTopBottom?: number;
    /** 分组标题左右内边距 */
    titlePaddingLeftRight?: number;
    /** 分组标题圆角 */
    titleRadius?: number;
    /** 标题分割线颜色 */
    splitLineColor?: string;
}

/**
 * 模块样式设置（Vort 公共类型 ModuleStyle，pcHome/_shared 未提供，保留在模块内）
 */
export interface ModuleStyle {
    boxPaddingTopBottom?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    boxPadding?: number;
    bannerMarginTop?: number;
    backgroundColor?: string;
    backgroundPic?: PicItem;
    backgroundPicFillType?: PicFillType;
}

/**
 * 内容区样式设置（Vort 公共类型 ContentStyle，pcHome/_shared 未提供，保留在模块内）
 */
export interface ContentStyle {
    paddingTopBottom?: number;
    paddingTop?: number;
    paddingBottom?: number;
    paddingLeftRight?: number;
    padding?: number;
    boxRadiusTop?: number;
    boxRadiusBottom?: number;
    boxRadius?: number;
    gradientType?: GradientType;
    gradientColorA?: string;
    gradientColorB?: string;
    backgroundColor?: string;
    backgroundPic?: PicItem;
    backgroundPicFillType?: PicFillType;
    boxShadow?: number;
}

/**
 * 商品分组模块配置
 */
export interface ProductGroupModule {
    /** 商品来源：products=手动选择, productGroup=商品分组 */
    productSource?: ProductSourceType;
    /** 手动选择的商品分组列表 */
    productGroups?: ProductGroupItem[];
    /** 商品分组来源列表（按分组选择时） */
    groupList?: ProductGroupSource[];

    /** 商品样式：1=扁平, 2=卡片, 3=描边 */
    productCardStyle?: number;
    /** 列表样式：4=一行四个, 6=一行五个 */
    listStyle?: number;
    /** 商品内边距 */
    cardPadding?: number;
    /** 商品圆角半径 */
    cardRadius?: number;

    /** 是否显示商品标题：0=不显示, 1=显示 */
    showProductName?: number;
    /** 商品标题行数 */
    productNameRow?: number;
    /** 商品标题字体粗细 */
    productNameWeight?: number;
    /** 商品标题字体大小 */
    productNameSize?: number;

    /** 是否显示商品价格：0=不显示, 1=显示 */
    showProductPrice?: number;
    /** 价格上距 */
    productPriceMarginTop?: number;
    /** 价格文字粗细 */
    productPriceWeight?: number;
    /** 价格文字大小 */
    productPriceSize?: number;

    /** 商品划线价：0=不显示, 1=显示 */
    crossedOutPrice?: number;

    /** 是否显示已售：0=不显示, 1=显示 */
    showSales?: number;

    /** 是否显示店铺：0=不显示, 1=显示 */
    showShopTag?: number;

    /** 购买按钮样式 */
    buyBtnStyle?: BuyBtnStyle;

    /** 商品颜色配置 */
    productColor?: ProductColorConfig;
    /** 分组标题配置 */
    groupTitle?: GroupTitleConfig;

    /** 模块样式 */
    moduleStyle?: ModuleStyle;
    /** 内容样式 */
    contentStyle?: ContentStyle;
    /** 边框设置 */
    frame?: ModuleFrame;
}

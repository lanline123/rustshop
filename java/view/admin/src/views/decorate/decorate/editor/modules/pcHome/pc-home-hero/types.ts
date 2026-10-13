/**
 * PC 营销通栏（pc-home-hero） - 类型定义
 * - 结构：左侧类目列表 + 中间轮播/卡位 + 右侧会员信息/快捷入口
 * - 内容以手动配置为主（含演示数据）
 */

import type { LinkItem, PicItem, PicListItem, GradientType, ModuleFrame } from "../_shared/types";

export type { PicListItem };

/**
 * 左侧类目配置
 */
export interface PcHomeHeroLeftConfig {
    /** 左侧标题（可选） */
    title?: string;
    /** 类目列表（图标 + 文本 + 链接） */
    categories?: PicListItem[];
    /** 默认展示数量（超出可在 Preview 里裁切/滚动；由 Preview 决定表现） */
    showCount?: number;
    /** 背景颜色 */
    backgroundColor?: string;
    /** 文字颜色 */
    textColor?: string;
}

/**
 * 中间卡位（活动/商品卡）配置
 * - 允许仅配置图片/标题作为占位展示
 * - 若选择商品，可配置 productId 供后续联动商品信息
 */
export interface PcHomeHeroCenterCard {
    /** 卡位标题 */
    title?: string;
    /** 角标文案 */
    badgeText?: string;
    /** 占位/活动图片 */
    pic?: PicItem;
    /** 跳转链接（活动页/商品页等） */
    link?: LinkItem;
    /** 关联商品 ID（可选） */
    productId?: string | number;
    /** 副图（可选，用于展示第二张商品图） */
    subPic?: PicItem;
    /** 价格文案（可选，纯展示） */
    priceText?: string;
    /** 商品来源：custom=自定义商品, productGroup=商品分组 */
    productSource?: "custom" | "productGroup";
    /** 商品分组ID（productSource 为 productGroup 时有效） */
    productGroupId?: string;
    /** 商品分组名称（productSource 为 productGroup 时有效） */
    productGroupName?: string;
    /** 商品分组显示数量 */
    productGroupNum?: number;
    /** 子商品列表（用于双商品/多商品展示模式） */
    products?: Array<{
        /** 商品ID（关联商品库） */
        productId?: string | number;
        /** 商品SN（前台详情跳转优先使用） */
        productSn?: string;
        /** 商品名称（选择商品后自动填充） */
        productName?: string;
        /** 商品图片 */
        pic?: PicItem;
        /** 商品价格 */
        price?: string;
        /** 标签文案 */
        label?: string;
        /** 标签背景渐变色A（起始色） */
        labelBgColorA?: string;
        /** 标签背景渐变色B（结束色） */
        labelBgColorB?: string;
        /** 标签背景渐变方向 */
        labelBgType?: GradientType;
    }>;
    /** 模块标题（原 title） */
    moduleTitle?: string;
    /** 模块子标题/角标（原 badgeText） */
    moduleSubTitle?: string;
    /** 子标题背景渐变色A（起始色） */
    moduleSubTitleBgColorA?: string;
    /** 子标题背景渐变色B（结束色） */
    moduleSubTitleBgColorB?: string;
    /** 子标题背景渐变方向 */
    moduleSubTitleBgType?: GradientType;
    /** 是否展示价格 */
    showPrice?: 0 | 1;
}

/**
 * 中间区域配置
 */
export interface PcHomeHeroCenterConfig {
    /** 轮播 Banner（图 + 链接） */
    banners?: PicListItem[];
    /**
     * 频道卡片列表（共6个：前2个为Banner右侧推荐位，后4个为下方商品位）
     * - 统一配置结构，不再区分 promoCards 和 productCards
     */
    modules?: PcHomeHeroCenterCard[];
}

/**
 * 右侧指标项（纯文案展示）
 */
export interface PcHomeHeroStatItem {
    label?: string;
    value?: string;
}

/**
 * 右侧区域配置
 */
export interface PcHomeHeroRightConfig {
    /** 会员信息占位（编辑器预览用） */
    user?: {
        avatar?: PicItem;
        nickname?: string;
        welcomeText?: string;
    };
    /** 会员卡背景（支持渐变色，如 linear-gradient(...)） */
    userCardBackground?: string;
    /** 会员卡渐变色A（起始色） */
    userCardGradientColorA?: string;
    /** 会员卡渐变色B（结束色） */
    userCardGradientColorB?: string;
    /** 会员卡渐变方向 */
    userCardGradientType?: GradientType;
    /** 右上角付费会员入口 */
    premiumMember?: {
        show?: boolean;
        title?: string;
        description?: string;
        link?: LinkItem;
    };
    /** 数据指标（如：优惠券、积分等） */
    stats?: PcHomeHeroStatItem[];
    /** 快捷入口（图标 + 文本 + 链接） */
    shortcuts?: PicListItem[];

    // ========== 店铺街配置 ==========
    /** 店铺街标题 */
    shopsTitle?: string;
    /** 店铺街子标题/角标 */
    shopsSubTitle?: string;
    /** 店铺街子标题背景色 */
    shopsSubTitleBg?: string;
    /** 查看更多文字 */
    shopsMoreText?: string;
    /** 查看更多链接 */
    shopsMoreLink?: LinkItem;
    /** 图文入口文字颜色 */
    shopsTextColor?: string;
}

/**
 * 样式配置（Preview 使用 CSS 变量时的配置源）
 */
export interface PcHomeHeroStyle {
    /** 整体高度（px），对齐前台主视觉区块高度 */
    height?: number;
    /** 三列间距（px） */
    gap?: number;

    /** 左侧列宽（px） */
    leftWidth?: number;
    /** 右侧列宽（px） */
    rightWidth?: number;

    /** 面板背景色（默认白色） */
    panelBackgroundColor?: string;
    /** 面板圆角（px） */
    panelRadius?: number;
    /** 面板阴影：0=无，1=有 */
    panelShadow?: 0 | 1;

    /** 标题字号（px） */
    titleFontSize?: number;
    /** 标题颜色 */
    titleColor?: string;
    /** 次要文字颜色 */
    mutedColor?: string;
    /** 价格颜色 */
    priceColor?: string;

    /** Banner 圆角（px） */
    bannerRadius?: number;
    /** 卡片圆角（px） */
    cardRadius?: number;
}

/**
 * PC 营销通栏模块完整类型
 */
export interface PcHomeHeroModule {
    /** 容器框架配置（背景/边距/圆角等） */
    frame?: ModuleFrame;
    /** 左侧配置 */
    left?: PcHomeHeroLeftConfig;
    /** 中间配置 */
    center?: PcHomeHeroCenterConfig;
    /** 右侧配置 */
    right?: PcHomeHeroRightConfig;
    /** 样式配置 */
    style?: PcHomeHeroStyle;
}

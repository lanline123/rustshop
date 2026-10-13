export interface DecoratePicItem {
    picId?: number;
    picThumb?: string;
    picUrl: string;
    picName?: string;
}

export interface DecorateVideoItem {
    videoCover?: string;
    videoUrl?: string;
    videoName?: string;
    format?: string;
}

type moduleType = DecorateTopNavModule | DecorateStorefront | DecorateStoreInfo | DecorateProduct | DecorateSettlement;
export interface DecorateStoreFormState {
    type: string;
    label: string;
    module: moduleType;
}

/**
 * 独立「顶部导航」装修模块配置
 * 固定两个 tab（自提 / 配送），每个 tab 对应一组配送方式；由 enabled 控制是否启用
 *
 * 配送方式枚举：
 *   1 = 到店自提
 *   2 = 上门服务
 *   3 = 预约到店
 *   4 = 同城配送
 *   5 = 快递配送
 */
export interface DecorateTopNavModule {
    /** 是否启用顶部导航（0 = 关闭，首页顶部走搜索样式；1 = 开启自提/配送 tab） */
    enabled: number;
    /** 门店模式（1 = 标准门店/商城模式；2 = 即时零售门店/外卖模式） */
    mode: number;
    /** 自提 tab 标题（最多 5 字） */
    title1: string;
    /** 自提 tab 配送方式（候选 1/2/3） */
    deliveryModes1: number[];
    /** 配送 tab 标题（最多 5 字） */
    title2: string;
    /** 配送 tab 配送方式（候选 4/5） */
    deliveryModes2: number[];
    /** 卡片背景色（默认 #ffffff） */
    backgroundColor: string;
    /** 文字颜色（默认 #1a1a1a） */
    textColor: string;
    /** 门店信息区域上下内边距 */
    contentPaddingVertical: number;
    /** 门店信息区域左右内边距 */
    contentPaddingHorizontal: number;
}

/**
 * 顶部导航模块配置
 * 固定两个标签页，每个标签页对应一组配送方式
 *
 * 配送方式枚举：
 *   1 = 到店自提
 *   2 = 上门服务
 *   3 = 预约到店
 *   4 = 同城配送
 *   5 = 快递配送
 */
export interface DecorateTopNav {
    /** 标签1 文本（最多 5 字） */
    title1: string;
    /** 标签1 关联的配送方式列表 */
    deliveryModes1: number[];
    /** 标签2 文本（最多 5 字） */
    title2: string;
    /** 标签2 关联的配送方式列表 */
    deliveryModes2: number[];
}
/**
 * 首页图片"顶部导航"子配置（合并自原独立顶部导航模块）
 * 固定两个 tab（自提 / 配送），每个 tab 对应一组配送方式
 *
 * 配送方式枚举：
 *   1 = 到店自提
 *   2 = 上门服务
 *   3 = 预约到店
 *   4 = 同城配送
 *   5 = 快递配送
 */
export interface DecorateStorefrontTopNav {
    /** 自提 tab 标题（最多 5 字） */
    title1: string;
    /** 自提 tab 配送方式（候选 1/2/3） */
    deliveryModes1: number[];
    /** 配送 tab 标题（最多 5 字） */
    title2: string;
    /** 配送 tab 配送方式（候选 4/5） */
    deliveryModes2: number[];
    /** 卡片背景色（默认 #ffffff） */
    backgroundColor: string;
    /** 文字颜色（默认 #1a1a1a） */
    textColor: string;
}

export interface DecorateStorefront {
    /** 是否显示该模块（0 = 不显示，1 = 显示；缺省按显示） */
    enabled?: number;
    urlType: number;
    showTime: number;
    /**
     * 顶部导航模式：1 = 旧样式（搜索条）；2 = 自提/配送 tab 装修
     */
    topNavMode?: number;
    /**
     * 顶部导航子配置（仅模式 2 生效）
     */
    topNav?: DecorateStorefrontTopNav;
    /**
     * 自定义背景图片（多张）
     * 兼容：历史数据可能为单对象（会在页面初始化时转换为数组）
     */
    customPic: DecoratePicItem[];
    /** 门店视频（非必填） */
    storeVideo?: DecorateVideoItem[];
    /**
     * 门店相册（用于预览）
     * 注意：该字段不再保存到装修配置中，实际数据来源/保存走门店信息字段 shopShowPicture
     */
    shopShowPicture?: DecoratePicItem[];
}

export interface DecorateStoreInfo {
    /** 是否显示该模块（0 = 不显示，1 = 显示；缺省按显示） */
    enabled?: number;
    cardType: number;
    backgroundColor: string;
    boxRadius: number;
    innerPadding: number;
    boxPadding: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;

    showTitle: number;
    shopLogoPic: DecoratePicItem;
    titleColor: string;
    shopLabel: number;
    shopRating: number;
    shopAddress: number;
    showLogo: number;
    /**
     * 门店相册（兼容旧数据：历史上可能保存在 storeInfo 模块中）
     * 当前以 storefront.shopShowPicture 为准
     */
    shopShowPicture?: DecoratePicItem[];
}

export interface DecorateProduct {
    radioStyle: number;
    textWeight: number;
    showCategory: number;
    productStyle: number;
    buyBtnStyle: number;
}
export interface DecorateSettlement {
    type: number;
}

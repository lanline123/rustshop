/**
 * 移动端搜索结果模块 - 类型与默认值
 *
 * 默认值是三处单一来源之一，另两处为：
 * - uniapp `src/components/modules/search-result/config.ts`
 * - 各端 Preview / 渲染组件里的 `??` 兜底
 * 三处必须保持一致，改这里时另两处同步改。
 */

export interface MobileSearchResultModule {
    /** 搜索结果类型：1-商品搜索，2-门店聚合搜索（仅多门店版本可配） */
    searchResultType?: 1 | 2;
    /** 背景颜色 */
    backgroundColor?: string;
    /** 搜索背景色（搜索框及筛选栏区域） */
    searchItemBackgroundColor?: string;
    /** 搜索项左右内间距（px，控制搜索框及筛选栏区域） */
    searchItemPaddingLeftRight?: number;
    /** 商品项背景色 */
    itemBackgroundColor?: string;
    /** 主题色 */
    themeColor?: string;
    /** 是否显示店铺标签（仅单列） */
    showShopTag?: boolean;
    /** 是否显示销量（仅单列） */
    showSales?: boolean;
    /** 是否显示添加购物车按钮（仅单列） */
    showCartButton?: boolean;
    /** 是否显示优惠券（仅双列） */
    showCoupon?: boolean;
    /** 布局样式：1-国内版，2-海外版 */
    layoutStyle?: 1 | 2;
    /** 列表样式：1-双列，2-单列 */
    listStyle?: 1 | 2;
    /** 商品标题颜色 */
    productTitleColor?: string;
    /** 商品标题行数：1-单行，2-双行 */
    productTitleLine?: 1 | 2;
    /** 商品价格颜色 */
    priceColor?: string;
    /** 购物车按钮样式：0-不显示，1-8 对应不同样式（仅双列） */
    buyBtnStyle?: number;
    /** 商品列表容器外边距（px），不对搜索头/筛选栏生效 */
    margin?: number;
    /**
     * 模块内边距（px）
     * @deprecated 设置面板已隐藏；前台仍可读历史值，默认 10
     */
    padding?: number;
    /** 模块上下内间距（px）；缺省时兼容历史 padding */
    paddingTopBottom?: number;
    /** 模块左右内间距（px）；缺省时兼容历史 padding */
    paddingLeftRight?: number;
    /**
     * 模块圆角（px）
     * @deprecated 设置面板已隐藏；前台仍可读历史值，默认 0
     */
    moduleRadius?: number;
    /** 商品卡圆角（px），历史字段名保留兼容；同时作用于卡片与商品图 */
    productImageRadius?: number;
    // ===== 门店搜索风格（searchResultType=2）字段，值口径 0-隐藏 / 1-显示 =====
    /** 门店评分与星级标 */
    storeShowRating?: number;
    /** 起送价 */
    storeShowMinOrder?: number;
    /** 配送费 */
    storeShowDeliveryFee?: number;
    /** 距离 */
    storeShowDistance?: number;
    /** 关注按钮 */
    storeShowFollow?: number;
    /** 门店商品名称 */
    storeShowProductName?: number;
    /** 门店商品名称行数：1-单行，2-双行 */
    storeProductNameRow?: 1 | 2;
    /** 门店商品价格 */
    storeShowProductPrice?: number;
    /** 门店商品项间距（px） */
    storeProductGap?: number;
    /** 门店卡片间距（px） */
    storeGap?: number;
    /** 门店卡片上下内间距（px） */
    storePaddingTopBottom?: number;
    /** 门店卡片左右内间距（px） */
    storePaddingLeftRight?: number;
    /** 门店卡片圆角（px） */
    storeCardRadius?: number;
    /** 门店 Logo 圆角百分比（0-50，50 为圆形） */
    storeLogoRadius?: number;
    /** 门店名称颜色 */
    storeShopTitleColor?: string;
    /** 门店名称字号（px） */
    storeShopTitleFontSize?: number;
    /** 门店名称粗细 */
    storeShopTitleFontBold?: number;
    // 装修容器通用字段
    type?: string;
    moduleIndex?: number;
}

export const mobileSearchResultDefaults: MobileSearchResultModule = {
    searchResultType: 1,
    backgroundColor: "#f5f5f5",
    searchItemBackgroundColor: "#ffffff",
    searchItemPaddingLeftRight: 10,
    itemBackgroundColor: "#ffffff",
    themeColor: "#ff4d4f",
    showShopTag: true,
    showSales: true,
    showCartButton: true,
    showCoupon: true,
    layoutStyle: 1,
    listStyle: 1,
    productTitleColor: "#333333",
    productTitleLine: 2,
    priceColor: "#ff4d4f",
    buyBtnStyle: 1,
    margin: 0,
    padding: 10,
    paddingTopBottom: 10,
    paddingLeftRight: 10,
    moduleRadius: 0,
    productImageRadius: 4,
    storeShowRating: 1,
    storeShowMinOrder: 1,
    storeShowDeliveryFee: 1,
    storeShowDistance: 1,
    storeShowFollow: 1,
    storeShowProductName: 1,
    storeProductNameRow: 1,
    storeShowProductPrice: 1,
    storeProductGap: 8,
    storeGap: 10,
    storePaddingTopBottom: 12,
    storePaddingLeftRight: 12,
    storeCardRadius: 8,
    storeLogoRadius: 50,
    storeShopTitleColor: "#1a1a1a",
    storeShopTitleFontSize: 15,
    storeShopTitleFontBold: 700
};

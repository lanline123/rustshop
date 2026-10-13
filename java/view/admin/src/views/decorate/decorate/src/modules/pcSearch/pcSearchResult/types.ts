/**
 * PC 搜索结果模块 - 类型与默认值
 */

export interface PcSearchResultModule {
    /** 背景颜色 */
    backgroundColor?: string;
    /** 筛选布局：1=左侧筛选，2=顶部筛选 */
    filterStyle?: 1 | 2;
    /** 筛选模板：1=一行五个，2=一行六个 */
    filterTemplate?: 1 | 2;
    /** 是否显示筛选面板 */
    showFilterPanel?: boolean;
    /** 是否显示价格区间 */
    showPriceRange?: boolean;
    /** 是否显示店铺标签 */
    showShopTag?: boolean;
    /** 是否显示销量 */
    showSales?: boolean;
    /** 是否显示添加购物车按钮 */
    showCartButton?: boolean;
    /** 商品样式：1=扁平, 2=卡片, 3=描边 */
    goodsStyle?: number;
    /** 边角样式：1=直角, 2=圆角, 3=大圆角 */
    goodsRadiusStyle?: number;
    /** 购买样式：1=样式一, 2=样式二, 3=样式三 */
    buyStyle?: number;
    /** 组件上边距（px） */
    componentMarginTop?: number;
    /** 组件下边距（px） */
    componentMarginBottom?: number;
    // 装修容器通用字段
    type?: string;
    moduleIndex?: number;
}

export const pcSearchResultDefaults: PcSearchResultModule = {
    backgroundColor: "#fff",
    filterStyle: 2,
    filterTemplate: 2,
    showFilterPanel: true,
    showPriceRange: true,
    showShopTag: true,
    showSales: true,
    showCartButton: true,
    goodsStyle: 1,
    goodsRadiusStyle: 1,
    buyStyle: 1,
    componentMarginTop: 8,
    componentMarginBottom: 8
};

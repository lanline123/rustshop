/**/ // 列表查询时筛选参数类型
export interface CartFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    payStatus?: number;
    orderStatus?: number;
    addTime?: string[];
    shippingStatus?: number;
    commentStatus?: number;
}

// 获取列表返回参数类型
export interface CartFilterState {
    shopId: number;
    shopTitle: string;
    isChecked: boolean;
    carts: CartItmes[];
    gift: any[];
    /** 该分组是否已配置批发规则 */
    wholesaleConfigured?: boolean;
    /** SPU 总量起批提示 */
    wholesaleGroupHint?: string | null;
}

export interface CartItmes {
    activityInfo: any;
    cartId: number;
    userId?: number;
    productId?: number;
    productSn?: string;
    seoLink?: string;
    productName?: string;
    picThumb?: string;
    marketPrice?: string;
    originalPrice?: string;
    quantity?: number;
    skuId?: number;
    skuData?: SkuData[];
    productType?: number;
    isChecked?: boolean;
    /** 当前销售门店；批发 SPU 聚合必须与 productId 共同作为作用域 */
    shopId?: number;
    type?: number;
    shopTitle?: string;
    productWeight?: string;
    shippingTplId?: number;
    freeShipping?: number;
    productStatus?: number;
    price?: string;
    stock?: number;
    isDisabled?: boolean;
    sku?: { isEnabled?: number | null };
    subtotal?: number;
    extraSkuData: any[];
    limitNumber?: number;
    /**
     * 批发身份标识（稳定）：该行是否为批发商品
     * - 不随数量是否命中阶梯变化，恒定；用于决定走批发控件 / 数量倍数逻辑
     * - 与 `isWholesaleLine`（动态：当前是否按批发价成交）区分，后者会随数量减到不达阶梯而变 false
     */
    wholesaleScope?: boolean;
    /** 该行当前是否按批发价成交（动态，随数量/阶梯变化；勿用于控件分流） */
    isWholesaleLine?: boolean;
    /** 批发计价方式：1=折扣 / 2=固定金额 */
    priceMode?: 1 | 2;
    /** 后端行级起批、倍数或自动调整提示 */
    wholesaleHint?: string | null;
    /** 起批数量 */
    wholesaleMoq?: number;
    /** 起批维度：1=SPU 总量起批 / 2=SKU 起批 */
    wholesaleMoqDimension?: 1 | 2;
    /** 倍数购买的倍数；1=不限制 */
    wholesaleMultiple?: number;
}

export interface SkuData {
    name: string;
    value: string;
}

export interface CartFilterResult {
    cartList: CartFilterState[];
    total: TotalObj;
    /** 当前用户是否批发商身份；false=普通会员，全部按零售展示 */
    isWholesaleBuyer?: boolean;
}

export interface TotalObj {
    productAmount: number;
    checkedCount: number;
    discounts: number;
    discountAfter: number;
    totalCount: number;
    discountCouponAmount: number;
    discountSeckillAmount: number;
    discountProductPromotionAmount: number;
    discountTimeDiscountAmount: number;
    discountDiscountAmount: number;
    serviceFee?: number;
}

// 获取详情返回参数类型
export interface CartFormState {
    orderId?: number;
    orderStatus?: number;
    shopId?: number;
    payTypeId?: number;
    orderSn?: string;
    addTime?: string;
}

export interface CartFormResult {
    item: CartFormState;
}

// 编辑表单

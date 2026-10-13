/**
 * 代客下单（proxyShopping）最小可用类型定义
 * - 以接口文档 `.vscode/代客下单.md` 为准
 * - 字段尽量保持可扩展（使用可选字段 + 索引签名）
 */

// -------------------------
// 商品
// -------------------------

export interface ProxyProductAvailabilityParams {
    id: number;
    skuId?: number;
    extraAttrIds?: string;
    shopId?: number;
    addressId?: number;
    userId?: number;
    [key: string]: any;
}

export interface ProxyProductAvailabilityVO {
    productId?: number;
    skuId?: number;
    price?: number;
    stock?: number;
    available?: boolean;
    [key: string]: any;
}

export interface ProxyProductListDTO {
    page?: number;
    size?: number;
    keyword?: string;
    categoryId?: number;
    brandId?: number;
    shopId?: number;
    minPrice?: number;
    maxPrice?: number;
    sortField?: string;
    sortOrder?: "asc" | "desc" | string;
    [key: string]: any;
}

export interface ProxyProductListItem {
    productId: number;
    productName?: string;
    productSn?: string;
    productType?: number; // 1普通 2虚拟 3卡密
    price?: number;
    stock?: number;
    pic?: string;
    [key: string]: any;
}

export interface ProxyProductDetailParams {
    id?: number;
    productId?: number;
    productSn?: string;
    productTsn?: string;
    [key: string]: any;
}

export interface ProxyProductListResDTO {
    records: ProxyProductListItem[];
    total?: number;
    current?: number;
    size?: number;
    [key: string]: any;
}

// -------------------------
// 购物车
// -------------------------

export interface ProxyCartListDTO {
    userId: number;
    shopId?: number;
    [key: string]: any;
}

export interface ProxyCartItem {
    cartId: number;
    userId?: number;
    productId?: number;
    productName?: string;
    productSn?: string;
    productType?: number; // 1普通 2虚拟 3卡密
    picThumb?: string;
    marketPrice?: number;
    memberPrice?: number;
    originalPrice?: string;
    subtotal?: number;
    skuId?: number;
    skuData?: string;
    sku?: any;
    price?: number;
    quantity?: number;
    shopId?: number;
    isChecked?: boolean;
    isDisabled?: boolean;
    hasSku?: boolean;
    stock?: number;
    freeShipping?: number;
    noShipping?: number;
    productWeight?: number;
    shippingTplId?: number;
    productStatus?: number;
    activityInfo?: any[];
    serviceFee?: string | number;
    saleMode?: number;
    isPresale?: boolean;
    isSeckill?: number;
    limitNumber?: number;
    type?: number;
    updateTime?: number;
    salesmanId?: number;
    extraData?: string;
    [key: string]: any;
}

export interface ProxyCartStoreItem {
    shopId?: number;
    shopTitle?: string;
    carts: ProxyCartItem[];
    total?: Record<string, any>;
    usedPromotions?: any[];
    enableUsePromotion?: any[];
    gift?: any[];
    noShipping?: number;
    isNotReachMinOrderAmount?: boolean;
    minOrderAmountDiff?: number;
    [key: string]: any;
}

export interface ProxyCartListByStoreVO {
    cartList: ProxyCartStoreItem[];
    totalAmount?: number;
    totalQuantity?: number;
    [key: string]: any;
}

export interface ProxyCartAddToCartDTO {
    userId: number;
    id: number; // 商品ID
    number: number;
    skuId?: number;
    extraAttrIds?: string;
    shopId?: number;
    [key: string]: any;
}

export interface ProxyExchangeProductToCartVO {
    item?: boolean;
    flowType?: number;
    [key: string]: any;
}

export interface ProxyCartRemoveItemDTO {
    cartIds: number[];
    [key: string]: any;
}

export interface ProxyCartUpdateItemDTO {
    cartId: number;
    data: {
        quantity: number;
        [key: string]: any;
    };
    [key: string]: any;
}

export interface ProxyCartGetCountParams {
    userId: number;
    shopId?: number;
    [key: string]: any;
}

// -------------------------
// 订单结算 / 提交
// -------------------------

export interface ProxyOrderCheckParam {
    userId: number;
    flowType?: number;
    addressId?: number;
    shopId?: number;
    deliveryType?: number; // 1物流 2自提 3同城
    longitude?: number;
    latitude?: number;
    payTypeId?: number;
    usePoint?: number;
    useBalance?: number;
    useGiftCard?: number;
    useCouponIds?: number[];
    selectUserCouponIds?: number[];
    buyerNote?: string;
    invoiceData?: any;
    shippingType?: Record<string, any>;
    address?: any; // 新增地址（与 addressId 二选一）
    [key: string]: any;
}

export interface ProxyPaymentType {
    typeId?: number;
    typeName?: string;
    disabled?: boolean;
    disabledDesc?: string;
    isShow?: boolean;
}

export interface ProxyOrderCheckProductItem {
    productId?: number;
    productName?: string;
    skuId?: number;
    skuValue?: string;
    price?: number;
    stock?: number;
    quantity?: number;
    [key: string]: any;
}

export interface ProxyOrderCheckTotal {
    productAmount?: number;
    checkedCount?: number;
    totalCount?: number;
    discounts?: number;
    discountCouponAmount?: number;
    discountDiscountAmount?: number;
    discountProductPromotionAmount?: number;
    discountTimeDiscountAmount?: number;
    discountSeckillAmount?: number;
    discountChangePrice?: number;
    shippingFee?: number;
    storeShippingFee?: Record<string, number>;
    storeShippingRule?: Record<string, any>;
    totalAmount?: number;
    exchangePoints?: number;
    pointsAmount?: number;
    balance?: number;
    giftCardAmount?: number;
    unpaidAmount?: number;
    couponAmount?: number;
    orderSendPoint?: number;
    serviceFee?: number;
    paidAmount?: number;
    cashAmount?: number;
    cashChangeAmount?: number;
    roundOffCentsAmount?: number;
    roundOffDimeAmount?: number;
    posOtherDiscountAmount?: number;
    depositAmount?: number;
    actualPayDepositAmount?: number;
    finalPaymentAmount?: number;
    [key: string]: any;
}

export interface ProxyOrderCheckCoupon {
    id?: number;
    couponId?: number;
    couponName?: string;
    couponType?: number;
    couponMoney?: number;
    couponDiscount?: number | null;
    minOrderAmount?: number;
    couponDesc?: string;
    isGlobal?: boolean;
    shopId?: number;
    endDate?: string;
    userCouponId?: number;
    selected?: boolean;
    [key: string]: any;
}

export interface ProxyOrderCheckCouponList {
    enableCoupons?: ProxyOrderCheckCoupon[];
    disableCoupons?: ProxyOrderCheckCoupon[];
    [key: string]: any;
}

export interface ProxyOrderCheckShippingType {
    typeId?: number;
    shopId?: number;
    typeName?: string;
    pickupId?: number | null;
    expectPickupTime?: string | null;
    [key: string]: any;
}

/**
 * 非多门店：可选配送方式列表项（结构对齐 PC 端 `getShippingType` 返回）
 */
export interface ProxyStoreShippingTypeItem {
    shippingTypeId: number;
    shippingTypeName: string;
    shopId: number;
    [key: string]: any;
}

/**
 * 非多门店：按 shopId 分组的可选配送方式
 */
export type ProxyStoreShippingTypeData = Record<string, ProxyStoreShippingTypeItem[]>;

export interface ProxyOrderCheckItem {
    addressId?: number;
    shippingType?: Record<string, ProxyOrderCheckShippingType>;
    payTypeId?: number;
    usePoint?: number;
    useBalance?: number;
    useCouponIds?: number[];
    selectUserCouponIds?: number[];
    flowType?: number;
    deliveryType?: number;
    hasChangePrice?: boolean;
    roundOffCents?: boolean;
    roundOffDime?: boolean;
    defaultCollectionMethod?: number;
    systemUsePoint?: number;
    systemRoundOffCents?: boolean;
    systemRoundOffDime?: boolean;
    [key: string]: any;
}

export interface ProxyOrderCheckVO {
    cartList: ProxyCartStoreItem[];
    total?: ProxyOrderCheckTotal;
    couponList?: ProxyOrderCheckCouponList | ProxyOrderCheckCoupon[];
    item?: ProxyOrderCheckItem;
    availablePoints?: number;
    useCouponIds?: number[];
    selectUserCouponIds?: number[];
    flowType: number;
    message?: string | null;
    deliveryOption?: any;
    hasChangePrice?: boolean;
    productPresale?: any;
    tmplIds?: string[];
    addressInfo?: any;
    paymentTypes?: ProxyPaymentType[];
    totalAmount?: number;
    payAmount?: number;
    discountAmount?: number;
    [key: string]: any;
}

export interface ProxyOrderSubmitVO {
    orderId?: number;
    orderSn?: string;
    payAmount?: number;
    payType?: number;
    [key: string]: any;
}

export interface ProxyPickupPoint {
    shopId?: number;
    pickupId?: number;
    shopTitle?: string;
    shopName?: string;
    shopDetailedAddress?: string;
    address?: string;
    shopOpenCloseJson?: string;
    businessHours?: string;
    [key: string]: any;
}

export interface ProxyPickupContact {
    id?: number;
    contactId?: number;
    name?: string;
    contactName?: string;
    mobile?: string;
    contactMobile?: string;
    contactPhone?: string;
    [key: string]: any;
}

// -------------------------
// 地址（代客下单）
// -------------------------

export interface ProxyShoppingAddress {
    addressId?: number;
    userId?: number;
    consignee?: string;
    mobile?: string;
    telephone?: string;
    email?: string;
    regionName?: string;
    regionNames?: string[];
    regionIds?: number[];
    address?: string;
    positionAddress?: string;
    longitude?: number;
    latitude?: number;
    mobileAreaCode?: string;
    isDefault?: number;
    isSelected?: number;
    [key: string]: any;
}
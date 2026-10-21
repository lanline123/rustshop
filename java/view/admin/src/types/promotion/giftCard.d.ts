export interface GiftCardProduct {
    giftProductId?: number;
    cardGroupsId: number;
    productId: number;
    skuId?: number;
}
/**
 * 礼品卡分组表单类型
 */
export interface GiftCardGroupFormState {
    groupType: number;
    cardType: number; // 1: 电子卡, 2: 实体卡
    // 基础字段
    cardGroupName: string;
    imageUrl: string;
    status: number;
    remark: string;
    // 有效期相关
    validityType: number; // 1: 永久有效, 2: 天数, 3: 固定时间
    validityDays?: number;
    expireTime?: string;
    faceAmount?: number; // 面额
    // 实体卡特有字段
    giftCardProducts?: GiftCardProduct[];
    canDelete?: boolean; // 是否可删除
}

/**
 * 礼品卡分组筛选参数
 */
export interface GiftCardGroupFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

/**
 * 礼品卡分组筛选结果
 */
export interface GiftCardGroupFilterResult {
    records: GiftCardGroupItem[];
    filter: {
        page: number;
    };
    total: number;
}

/**
 * 礼品卡分组对象
 */
export interface GiftCardGroupItem {
    cardGroupsId: number;
    cardGroupName: string;
    groupType: number; // 礼品卡类型 1兑换卡 2储值卡
    groupTypeName: string; // 卡组类型名称
    cardType: number; // 1: 电子卡, 2: 实体卡
    cardTypeName: string; // 卡类型名称
    imageUrl: string;
    status: number; // 1-开启 0-关闭
    remark: string; // 备注
    addTime: string; // 创建时间
    updateTime: string; // 更新时间
    // 有效期相关
    validityType: number; // 有效期类型 1: 永久有效, 2: 天数, 3: 固定时间
    validityTypeName: string; // 有效期类型名称
    validityDays?: number;
    expireTime?: string;
    faceAmount?: number; // 面额 储值卡
    canDelete: boolean;
}

/**
 * 礼品卡对象
 */
export interface GiftCardItem {
    giftCardsId: number;
    cardGroupsId: number;
    cardNumber: string;
    cardPassword: string;
    isAvailable: number; // 1-可用 0-不可用
    status: number; // 0-未使用 1-已使用 2-已过期
    isSold: string;
    isExchange: string; // 是否兑换
    usedTime: number;
    userId: number; // 使用者ID
    addTime: string;
    expiredTime: string;
}

export interface GiftCardFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    cardGroupsId: number;
}

/**
 * 礼品卡分组筛选结果
 */
export interface GiftCardFilterResult {
    records: GiftCardItem[];
    filter: {
        page: number;
    };
    total: number;
}

export interface GiftCardFormState {
    cardId?: number;
    cardGroupsId: number;
    cardNumber: string;
    cardPassword: string;
    isAvailable: number;
}

export interface GiftCardImportResult {
    successCount: number;
    failCount: number;
    failList: GiftCardItem[];
}

export interface GiftCardImportFormState {
    file?: any;
}

export interface GiftCardDetailReceiverInfo {
    username?: string; // 用户名
    consignee?: string; // 收货人名称
    mobile?: string; // 手机号
    email?: string; // 邮箱
    regionNames?: string; // 地区
    address?: string; // 用户地址
    buyerNote?: string; // 订单备注
}

export interface GiftCardDetailDeliveryInfo {
    shippingTypeName?: string; // 配送方式
    shippingTime?: string; // 发货时间
    logisticsName?: string; // 物流名称
    trackingNo?: string; // 物流单号
}

export interface GiftCardDetailPaymentInfo {
    payTypeName?: string; // 支付类型
    exchangeTime?: string; // 兑换时间
    exchangeAmount?: number; // 兑换金额
    payTime?: string; // 支付时间
}

export interface GiftCardDetailExchangeInfo {
    userAddressInfo?: GiftCardDetailReceiverInfo;
    deliveryInfo?: GiftCardDetailDeliveryInfo;
    payInfo?: GiftCardDetailPaymentInfo;
}

export interface GiftCardDetail {
    giftCardsId: number;
    cardGroupsId: number;
    groupType: number;
    groupTypeName?: string;
    cardType: number;
    cardTypeName?: string;
    cardNumber: string;
    cardPassword?: string;
    isSold?: string | number | boolean;
    isExchange?: string | number | boolean;
    orderSn?: string;
    orderId?: number;
    exchangeInfo?: GiftCardDetailExchangeInfo;
}

/**
 * 礼品卡分组商品对象
 */
export interface GiftCardGroupProduct {
    giftProductId: number;
    cardGroupsId: number;
    productId: number;
    skuId: number;
}

/**
 * 礼品卡分组商品表单
 */
export interface GiftCardGroupProducts {
    cardGroupsId: number;
    giftCardProducts: GiftCardGroupProduct[];
}

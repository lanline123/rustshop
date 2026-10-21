// 列表查询时筛选参数类型
export interface customerFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    salesmanId?: string;
    endTime?: string;
    startTime?: string;
    keyword?: string;
}

// 获取列表返回参数类型
export interface customerFilterResult {
    records: customerFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface customerFilterState {
    orderId?: number;
    payTypeId?: number;
    addTime?: string;
    checkBox?: boolean;
    orderSn?: string;
    parentOrderId: number;
    shopId: number;
    storeTitle?: string;
    orderSource?: string;
    productAmount: number;
    isExchangeOrder?: number;
    userId?: number;
    username?: string;
    consignee?: string;
    mobile?: string;
    payType?: number;
    payStatus?: number;
    logisticsName?: string;
    shippingTypeName?: string;
    shippingFee?: string;
    orderStatus?: number;
    isDel?: number;
    totalAmount?: number;
    status?: number;
    aftersalesSn?: string;
    aftersaleId?: number;
    adminNote?: string;
    orderStatusName?: string;
    items: ItemInfo[];
    availableActions: AvailableActionsInfo;
    mark?: number;
    userAddress: string;
    receivedTime: string;
    salesmanName: string;
    salesman: {
        baseUserInfo: {
            nickname?: string;
            userId?: number;
            username?: string;
        }

    };
    user: {
        nickname?: string;
        userId?: number;
        username?: string;
    };
    shop: {
        shopTitle?: string;
        shopId?: number;
    };
}
export interface ItemInfo {
    productId?: number;
    picThumb?: string;
    productPicThumb?: string;
    url?: string;
    productName?: string;
    productType?: number;
    price: number;
    quantity?: number;
    aftersalesItem?: {
        aftersalesItemId: number;
        orderItemId: number;
        number: number;
        aftersaleId: number;
    };
}

export interface AvailableActionsInfo {
    cancelOrder?: boolean;
    setConfirm?: boolean;
    confirmReceipt?: boolean;
    modifyOrder?: boolean;
    modifyOrderMoney?: boolean;
    modifyShippingInfo?: boolean;
    setPaid?: boolean;
    setUnpaid?: boolean;
    deliver?: boolean;
    splitOrder?: boolean;
    delOrder?: boolean;
}

// 获取详情返回参数类型
export interface customerFormResult {
    item: customerFormState;
    statistical: any;
}
export interface customerFormState {
    userId?: string;
    pidMobile?: string;
    level?: number;
    groupId?: number;
    pid?: number;
    salesmanId?: number;
    nickname?: string;
    baseUserInfo?: any;
    pidUserInfo?: any;
}


export interface customerDetailsFilterResult {
    records: customerDetailsFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface customerDetailsFormResult {
    salesmanVO: customerDetailsFilterState;
    statistical: any;
}
export interface BaseUserInfo {
    mobile?: string;
    username?: string;
    nickname?: string;
    avatar?: string;
    userId?: number;
    distributionRegisterTime?: string;
}

export interface GroupInfo {
    groupId: number;
    groupName: string;
}

export interface SalesmanOrderInfo {
    statusText: string;
    salesmanOrderId: number;
    orderId: number;
    salesmanId: number;
    amount: string;
    status: number;
    itemId: number;
    orderAmount: string;
    addTime: string;
}

export interface customerDetailsFilterState {
    salesmanId?: number;
    userId?: number;
    level?: number;
    groupId?: number;
    pid?: number;
    addTime?: string;
    shopId?: number;
    saleAmount?: string;
    levelText?: string;
    baseUserInfo: BaseUserInfo;
    pidUserInfo?: any;
    groupInfo?: GroupInfo;
}

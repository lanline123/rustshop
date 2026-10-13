// 列表查询时筛选参数类型
export interface OrderFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    /** 交易单号 */
    outTradeNo?: string;
    userId?: number;
    shopId?: number | string;
    orderStatus?: number | string;
    payStatus?: number | string;
    shippingStatus?: number | string;
    address?: string;
    email?: string;
    mobile?: string;
    logisticsId?: number | string;
    isExchangeOrder?: number;
    addTime?: string[];
    addEndTime?: string;
    addStartTime?: string;
    endDate?: string;
    startDate?: string;
    payCode?: number | string;
    isSettlement?: number | string;
    mark?: number | null;
    /** 会员标签ID集合（二期，按会员标签筛选订单） */
    memberTagIds?: number[];
    /** 会员标签匹配模式：0-满足任一，1-满足全部 */
    memberTagMatchMode?: number;
    deliveryType?: number | string;
    shopType?: number | null;
    vendorId?: number | string;
}

// 获取列表返回参数类型
export interface OrderFilterResult {
    records: OrderFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

// 订单各状态数量统计（对齐后端 OrderStatusCountVO，字段顺序与 OrderStatusEnum 状态码一致）
export interface OrderStatusCount {
    /** 全部（不含已删除） */
    allCount: number;
    /** 待支付（状态 0） */
    pendingCount: number;
    /** 待发货（状态 1） */
    confirmedCount: number;
    /** 已发货（状态 2） */
    processingCount: number;
    /** 已取消（状态 3） */
    cancelledCount: number;
    /** 无效（状态 4） */
    invalidCount: number;
    /** 已完成（状态 5） */
    completedCount: number;
    /** 已删除（虚拟状态 -2） */
    deletedCount: number;
}

export interface OrderPrintItems {
    productName: number;
    productSn: string;
    skuSn: string;
    skuValue: string;
    price: number;
    subtotal: number;
    quantity?: number;
}

export interface OrderPrintFormResult {
    username?: string;
    payTime?: string;
    addTime?: string;
    shippingTime?: string;
    preOrderStatus?: string;
    preOrderStatusDesc?: string;
    payTypeName?: string;
    logisticsName?: string;
    orderSn?: string;
    orderSource?: string;
    consignee?: string;
    shippingTypeName?: string;
    trackingNo?: number;
    payTypeId?: number;
    mobile?: number;
    productAmount?: number;
    totalAmount?: number;
    serviceFee?: number;
    shippingFee?: number;
    couponAmount?: number;
    shopName?: string;
    host?: string;
    printTime?: string;
    userAddress?: string;
    user: {
        username?: string;
    };
    address?: {
        telephone?: string;
        address?: string;
        regionName?: string[];
    };
    shop?: {
        shopTitle?: string;
    };
    items: OrderPrintItems[];
}
export interface OrderPrintBillFormResult {
    id?: number;
    orderId?: number;
    orderCode?: string;
    logisticCode?: string;
    addTime?: string;
    orderStatusName?: string;
    printTemplate?: string;
}

export interface OrderAddressData {
    postcode?: string;
    [key: string]: unknown;
}

export interface OrderFilterState {
    isPresale?: number;
    orderId?: number;
    vendorId: number;
    vendorName: string;
    /** 交易单号 */
    outTradeNo?: string;
    payTypeId?: number;
    addTime?: string;
    checkBox?: boolean;
    isPickup?: boolean;
    orderSn?: string;
    parentOrderSn?: any;
    preOrderStatusDesc: string;
    preOrderStatus: number;
    parentOrderId: number;
    isChangeOrderStatus: number;
    shippingStatus: number;
    shopId: number;
    storeTitle?: string;
    orderSource?: string;
    productAmount: number;
    taxRateAmount?: number;
    taxFee?: number;
    taxAmount?: number;
    orderTaxFee?: number;
    taxRate?: number;
    isExchangeOrder?: number;
    userId?: number;
    username?: string;
    consignee?: string;
    mobile?: string;
    payType?: number;
    payStatus?: number;
    logisticsName?: string;
    shippingTypeId?: number;
    shippingTypeName?: string;
    shippingFee?: string;
    orderStatus?: number;
    isDel?: number;
    totalAmount?: number;
    status?: number;
    aftersalesSn?: string;
    aftersaleId?: number;
    /** 订单是否存在进行中的售后（状态列显示「售后中」标签，需后端返回） */
    afterSalesIng?: boolean;
    adminNote?: string;
    /** 买家主动取消未支付订单时选择的原因 */
    cancelRemark?: string;
    orderStatusName?: string;
    items: ItemInfo[];
    availableActions: AvailableActionsInfo;
    mark?: number;
    userAddress: string;
    postcode?: string;
    addressData?: OrderAddressData;
    user: {
        nickname?: string;
        userId?: number;
        username?: string;
    };
    shop: {
        shopTitle?: string;
        shopId?: number;
    };
    payLog: {
        payCode: string;
        transactionId: string;
    };
    joinStatus?: number;
    joinStatusDesc?: string;
    orderType?: number;
    // 配送/服务订单信息
    deliveryOrderInfo?: DeliveryOrderInfo;
}
export interface ItemInfo {
    saleMode?: number;
    productId?: number;
    picThumb?: string;
    productPicThumb?: string;
    url?: string;
    productName?: string;
    productType?: number;
    skuData?: string;
    skuValue?: string;
    price: number;
    /** 供应商订单商品下单时的供货价快照 */
    supplyPrice?: number | string;
    /** 供货价小计 */
    supplySubtotal?: number | string;
    taxFee?: number;
    taxAmount?: number;
    taxRate?: number;
    subtotal?: number;
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
    modifyOrderConsignee?: boolean; // [新增] 修改收货人信息权限
    setPaid?: boolean;
    setUnpaid?: boolean;
    deliver?: boolean;
    splitOrder?: boolean;
    delOrder?: boolean;
}

// 配送/服务订单信息
export interface DeliveryOrderInfo {
    deliveryOrderId: number;
    deliveryOrderSn: string;
    status: number;
    statusName: string;
    pickupTime: string;
    completeTime: string;
    staffId: number;
    staffName: string;
    staffPhone: string;
    staffAvatar: string | null;
    type: number;
    typeName: string;
    pickupPhoto: string[] | null;
    completePhoto: string[] | null;
    beforeServicePhoto: string[] | null;
    afterServicePhoto: string[] | null;
    staffEarnings?: number;
    /** 运力类型：1=自建配送，2=美团（第三方）配送 */
    carrierType?: number;
    /** 运力类型名称（可直接展示，如「商家配送」/「美团配送」） */
    carrierTypeName?: string;
    /** 美团订单状态码：0 待调度 / 20 已接单 / 30 已取货 / 50 已送达 / 99 已取消 */
    mtStatus?: number;
    /** 美团状态文案（可直接展示，优先于自建 statusName） */
    mtStatusName?: string;
    /** 美团配送距离（米），发单成功后回填 */
    mtDistance?: number;
    /** 美团配送费（元），发单成功后回填 */
    mtFee?: number;
    /** 美团发单时间（日期时间字符串或时间戳），用于展示等待时长；未发单为空 */
    mtDispatchTime?: string | number;
}

/** 预约时间信息（服务/预约类订单） */
export interface ReservationTimeInfo {
    reservationDate?: string;
    timeSlotDescription?: string;
}

export interface OrderFormState {
    paySn?: string;
    isCreditOrder?: number;
    isPresale?: number;
    orderId?: number;
    /** 父订单详情返回的子订单ID集合（用于查询子订单列表） */
    subOrderIds?: number[];
    vendorId?: number;
    shopId?: number;
    isPickup?: boolean;
    mark?: number;
    orderSn?: string;
    addTime?: string;
    parentOrderId?: number;
    isChangeOrderStatus?: number;
    preOrderStatus?: number;
    parentOrderSn?: string;
    cancelRemark?: string;
    orderStatus?: number;
    /** 订单状态名称（顶部状态卡标题，缺失时从 stepStatus 兜底） */
    orderStatusName?: string;
    /** 订单状态副文案（顶部状态卡副行，后端可选返回） */
    orderStatusTip?: string;
    storeTitle?: string;
    items: OrderItem[];
    username?: string;
    userAddress?: string;
    address?: string;
    consignee?: string;
    email?: string;
    mobile?: string;
    regionData?: number[];
    regionIds?: number[];
    regionName?: string[];
    regionNames?: string[];
    countryCode?: string;
    countryName?: string;
    mobileAreaCode?: string;
    internationalMobile?: string;
    formattedAddress?: string;
    fullAddress?: string;
    telephone?: string;
    shippingType?: number;
    shippingMethod?: number;
    shippingTypeId?: number;
    shippingTypeName?: string;
    wayBill?: boolean;
    postcode?: string;
    addressData?: OrderAddressData;
    logisticsId?: number;
    logisticsName?: string;
    payType?: number;
    payTypeId?: number;
    isStoreSplited?: number;
    payTime?: string;
    productAmount: number;
    taxRateAmount?: number;
    taxFee?: number;
    taxAmount?: number;
    orderTaxFee?: number;
    taxRate?: number;
    totalAmount: number;
    couponAmount: number;
    pointsAmount: number;
    invoiceFee?: number;
    serviceFee?: number;
    shippingFee?: number;
    paidAmount: number;
    balance: number;
    discountAmount: number;
    updateOrderSn: number;
    unpaidAmount: number;
    totalProductWeight: number;
    buyerNote?: string;
    adminNote?: string;
    remark?: string;
    availableActions: AvailableActionsInfo;
    shippingTime?: string;
    trackingNo?: string;
    shipping?: number;
    shippingList?: ShippingListType[];
    stepStatus?: StepStatus;
    user?: {
        nickname?: string;
        userId?: number;
        username?: string;
    };
    shop?: {
        shopTitle?: string;
        shopId?: number;
    };
    pickupDetail?: {
        shopId: number;
        shopTitle: string;
        shopRegionNames: string[];
        address: string;
    };
    // [新增] 自提联系人信息
    userPickupInfo?: {
        consignee?: string;
        mobile?: string;
    };
    expectPickupTime?: string;
    orderReservation?: ReservationTimeInfo;
    orderType?: number;
    // 配送/服务订单信息
    deliveryOrderInfo?: DeliveryOrderInfo;
}

export interface StepStatus {
    current: number;
    status: any;
    steps: {
        title?: string;
        description?: string;
    }[];
}

export interface OrderItem {
    itemId: number;
    productId?: number;
    productType: number;
    productName?: number;
    skuData?: string;
    skuValue?: string;
    picThumb?: string;
    productSn?: string;
    quantity: number;
    price?: number;
    /** 供应商订单商品下单时的供货价快照 */
    supplyPrice?: number | string;
    /** 供货价小计 */
    supplySubtotal?: number | string;
    taxRateAmount?: number;
    taxFee?: number;
    taxAmount?: number;
    taxRate?: number;
    subtotal?: number;
    isDeleted?: number;
    toDeliveryQuantity: number;
    allowDeliverNum: number;
    aftersalesItem?: {
        aftersalesItemId: number;
        orderItemId: number;
        number: number;
        aftersaleId: number;
    };
    // 累计售后占用数量，新版后端返回；缺失时回退 aftersalesItem.number
    aftersalesNumber?: number;
    items?: OrderItem[];
    trackingNo?: string;
    orderSn?: string;
    orderStatus?: number;
}
export interface ShippingListType {
    logisticsId: number;
    logisticsName: string;
    shippingTypeName: string;
    totalFee: string;
    freeMoney: number;
}
export interface ProductOperation {
    description?: string;
}
export interface AddProductInfoByIdsResult {
    item: OrderItem[];
}

export interface OrderToShipFormState {
    items: OrderItem[];
    shippingMethod?: number;
    shippingTypeId?: number;
    logisticsId?: number;
    orderStatus?: number;
    trackingNo?: string;
    billRemark?: string;
    pickupSn?: string;
    // 当前物流服务商凭证已配齐、可用电子面单
    wayBill?: boolean;
}

export interface ShippingInfoTrace {
    action: string;
    acceptTime: string;
    acceptStation: string;
    location: string;
}

import type { BaseResponseListWrap } from "~/types/api";
import type { GiftCardInfo } from "~/types/giftCard/giftCard";
// 列表查询时筛选参数类型
export interface OrderFilterParams {
    page: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    payStatus?: number;
    orderStatus?: number;
    addTime?: string[];
    shippingStatus?: number;
    commentStatus?: number;
    dateType?: number;
    joinType?: number;
}

// 获取列表返回参数类型
export interface OrderFilterState {
    /** 是否预售订单 */
    isPresale?: number;
    showPopover: boolean;
    orderId: number;
    addTime: string;
    balance: string;
    availableActions: AvailableActionsInfo;
    shippingFee: string;
    orderStatusName: string;
    cancelRemark?: string;
    trackingNo: string;
    logisticsName: string;
    payStatusName: string;
    totalAmount?: string;
    unpaidAmount?: string;
    orderStatus?: number;
    grouponPromotionRecordId?: number;
    joinStatus?: number;
    shippingMethod?: number;
    payTypeId?: number;
    orderSn?: string;
    isShow?: boolean;
    items?: ProductItems[];
    shopId?: number;
    shop?: any;
    invoiceData?: InvoiceData;
    joinStatusDesc?: string;
    orderType?: number;
    isPickup?: boolean;
    isCycleOrder?: number;
    /** 按需兑换订单的次卡权益信息 */
    cycleCardInfo?: {
        expireTimeStr?: string;
        statusName?: string;
    } | null;
    orderSource?: string;
}
export interface InvoiceData {
    status: number;
}

export interface ProductItems {
    productPicThumb: string;
    picThumb: string;
    aftersalesItem: object;
    aftersalesNumber?: number;
    productName: string;
    skuData: string;
    productSn: string;
    seoLink?: string;
    productId: number;
    quantity: number;
    itemId: number;
    price: string;
    subtotal: string;
    isGift: number;
    joinStatus: number;
    eCard?: any[];
    productType?: number;
    extraSkuData?: any;
}

export interface OrderFilterResult extends BaseResponseListWrap<OrderFilterState[]> {
    filter: OrderFilterParams;
}

// 获取详情返回参数类型
export interface OrderFormState {
    /** 是否预售订单 */
    isPresale?: number;
    /** 预售订单信息（含 presaleStatus、saleMode、productPresale、尾款时间等） */
    presaleInfo?: any;
    isCycleOrder?: number;
    orderId?: number;
    orderStatus?: number;
    shopId?: number;
    payTypeId?: number;
    orderSn?: string;
    addTime?: string;
    shopTitle?: string;
    consignee?: string;
    mobile?: string;
    payStatus?: number;
    orderStatusName?: string;
    cancelRemark?: string;
    detailedAddress?: string;
    items?: ProductItems[];
    userName?: string;
    address?: OrderAddress;
    shippingTypeId?: number;
    shippingTypeName?: string;
    deliveryOrderInfo?: DeliveryOrderInfo;
    afterSalesIng?: boolean;
    orderType?: number;
    orderReservation?: OrderReservationInfo;
    orderSource?: string;
    isPickup?: boolean;
    isReservationHome?: boolean;
    pickupDetail?: any;
    userAddress?: string;
    logisticsId?: number;
    logisticsName?: string;
    payType?: number;
    isStoreSplited?: number;
    payTime?: string;
    productAmount?: number;
    totalAmount?: number;
    couponAmount?: number;
    pointsAmount?: number;
    invoiceFee?: number;
    serviceFee?: number;
    shippingFee?: number;
    paidAmount?: number;
    balance?: number;
    discountAmount?: number;
    updateOrderSn?: boolean;
    unpaidAmount?: number;
    totalWeight?: number;
    buyerNote?: string;
    adminNote?: string;
    remark?: string;
    availableActions: AvailableActionsInfo;
    shippingTime?: string;
    trackingNo?: string;
    shipping?: number;
    shippingMethod?: number;
    shippingList?: ShippingListType[];
    stepStatus?: StepStatus;
    shop?: any;
    autoDeliveryDays?: number | null;
    joinStatusDesc?: string;
    joinStatus: number;
    giftCardOrder?: GiftCardOrder;
}

export interface StepStatus {
    current: any;
    status: any;
    steps: {
        title?: string;
        description?: string;
        steps?: any;
    }[];
}

export interface OrderFormResult {
    item: OrderFormState;
}
// 获取详情返回参数类型
export interface OrderFormState {
    /** 是否预售订单 */
    isPresale?: number;
    /** 预售订单信息（含 presaleStatus、saleMode、productPresale、尾款时间等） */
    presaleInfo?: any;
    orderId?: number;
    orderStatus?: number;
    shopId?: number;
    payTypeId?: number;
    orderSn?: string;
    addTime?: string;
    shopTitle?: string;
    consignee?: string;
    mobile?: string;
    payStatus?: number;
    orderStatusName?: string;
    cancelRemark?: string;
    detailedAddress?: string;
    items?: ProductItems[];
    userName?: string;
    address?: OrderAddress;
    shippingTypeId?: number;
    shippingTypeName?: string;
    deliveryOrderInfo?: DeliveryOrderInfo;
    afterSalesIng?: boolean;
    orderType?: number;
    orderReservation?: OrderReservationInfo;
    orderSource?: string;
    isPickup?: boolean;
    isReservationHome?: boolean;
    pickupDetail?: any;
    userAddress?: string;
    logisticsId?: number;
    logisticsName?: string;
    payType?: number;
    isStoreSplited?: number;
    payTime?: string;
    productAmount?: number;
    totalAmount?: number;
    couponAmount?: number;
    pointsAmount?: number;
    invoiceFee?: number;
    serviceFee?: number;
    shippingFee?: number;
    paidAmount?: number;
    balance?: number;
    discountAmount?: number;
    updateOrderSn?: boolean;
    unpaidAmount?: number;
    totalWeight?: number;
    buyerNote?: string;
    adminNote?: string;
    remark?: string;
    availableActions: AvailableActionsInfo;
    shippingTime?: string;
    trackingNo?: string;
    shipping?: number;
    shippingMethod?: number;
    shippingList?: ShippingListType[];
    stepStatus?: StepStatus;
    shop: any;
    autoDeliveryDays?: number | null;
    giftCardOrder?: GiftCardOrder;
    isCardOrder?: number;
    parentOrderSn?: string;
}

export interface DeliveryOrderInfo {
    deliveryOrderId?: number;
    deliveryOrderSn?: string;
    status?: number;
    statusName?: string;
    staffId?: number;
    staffName?: string;
    staffPhone?: string;
    staffAvatar?: string | null;
    type?: number;
    typeName?: string;
    pickupPhoto?: string | null;
    completePhoto?: string | null;
    beforeServicePhoto?: string | null;
    afterServicePhoto?: string | null;
}

export interface AvailableActionsInfo {
    cancelOrder?: boolean;
    toPay?: boolean;
    toAftersales?: boolean;
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
    rebuy?: boolean;
    toComment?: boolean;
}

export interface OrderNumResult {
    item: OrderNumState;
}

export interface OrderNumState {
    awaitPay: number;
    awaitShipping: number;
    awaitReceived?: number;
    awaitComment: number;
    orderCompleted: number;
    /** 售后中订单数 */
    awaitAftersalesCollect?: number;
    /** 商品收藏数量 */
    productCollect?: number;
    /** 店铺/门店收藏数量 */
    shopCollect?: number;
}

export interface ShippingInfoResponse {
    shipperCode: string;
    logisticCode: string;
    state: string;
    stateEx: string;
    location: string;
    traces: ShippingInfoTrace[];
    success: string;
    ebusinessID: string;
}

export interface ShippingInfoTrace {
    action: string;
    acceptTime: string;
    acceptStation: string;
    location: string;
}

export interface GiftCardOrder {
    giftCardOrderInfo: GiftCardInfo[];
    num: number;
}

export interface PickupInfoData {
    mobile?: string;
    orderItems?: OrderItem[];
    pickupQrCode?: string;
    pickupSn?: string;
    shipmentStatus?: number;
    shipmentStatusName?: string;
    shopDetailedAddress?: string;
    expectPickupTime: any;
    pickupName: string;
    store: any;
    userPickupInfo: any;
    userAddressInfo: any;
}

export interface OrderItem {
    cardGroupName: string;
    commission: string;
    deliveryQuantity: number;
    extraSkuData: string;
    isGift: number;
    isPin: number;
    isSeckill: number;
    itemId: number;
    orderId: number;
    orderSn: string;
    originPrice: number;
    picThumb: string;
    prepayPrice: number;
    price: number;
    productId: number;
    productName: string;
    productSn: string;
    productType: number;
    promotionData: string;
    quantity: number;
    shopId: number;
    skuData: string;
    skuId: number;
    suppliersId: number;
    userId: number;
    vendorId: any; // 可以为 null
    vendorProductId: any; // 可以为 null
    vendorProductSkuId: number;
    vendorProductSupplyPrice: any; // 可以为 null
}

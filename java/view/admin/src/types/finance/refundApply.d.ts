export interface RefundApplyFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    refundStatus?: number | string;
}

// 获取列表返回参数类型
export interface RefundApplyFilterState {
    refundId: number;
    refundTypeName: string;
    orderSn: string;
    productName: string;
    picThumb: string;
    refundStatusName: string;
    refundStatus: number;
    addTime: string;
    aftersales: {
        aftersaleId: number;
        aftersalesSn: string;
    };
}
export interface RefundApplyFilterResult {
    records: RefundApplyFilterState[];
    filter: RefundApplyFilterParams;
    total: number;
}

// 获取详情返回参数类型
export interface RefundApplyFormState {
    refundId?: number;
    refundType?: number;
    orderId?: number;
    orderSn?: string;
    shippingFee?: string;
    productName?: string;
    picThumb?: string;
    productSn?: string;
    quantity?: number;
    refundNumber?: number;
    price?: string;
    subtotal?: string;
    refundAmount: number;
    refundStatus?: number;
    refundNote?: string;
    aftersales?: {
        orders: any;
        aftersaleId: number;
        aftersalesSn: string;
        refundAmount: number;
    };
    refundBalance: number | string;
    balance?: number;
    effectiveBalance: number;
    onlineBalance: number | string;
    effectiveOnlineBalance: number;
    onlinePaidAmount?: number;
    isOnline?: number;
    offlineBalance?: number | string;
    offlinePaidAmount?: number;
    paidAmount: number;
    isOffline?: number;
    items?: any;
    paymentVoucher?: string[];
    creditAmount?: number;
    isCreditOrder?: number;
    creditRefundAmount?: number;
    creditRefundAmountOrgin?: number;
}

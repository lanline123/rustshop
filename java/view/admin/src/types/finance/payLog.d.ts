
export interface PayLogFilterParams {
    page: number,
    size: number,
    payStatus: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface PayLogFilterState {
    paylogId?: number;
    paySn?: string;
    payName?: string;
    payCode?: string;
    orderId: number;
    orderSn: string;
    orderAmount?: string | number;
    payAmount?: string | number;
    payStatus: number;
    payStatusName: string;
    addTime?: string;
    transactionId?: string;
    refundAmount?: string | number;
    consignee?: string;
}
export interface PayLogFilterResult {
    records: PayLogFilterState[];
    filter: PayLogFilterParams;
    total: number;
}

export interface PayLogFormResult {
    item: PayLogFormState;
}

// 获取详情返回参数类型
export interface PayLogFormState {
    paylogId?: number;
    paySn?: string;
    payName?: string;
    payCode?: string;
    orderId?: number;
    orderSn?: string;
    orderAmount?: string | number;
    orderType?: number;
    payAmount?: string | number;
    payStatus?: number;
    payStatusName?: string;
    addTime?: string;
    transactionId?: string;
    notifyData?: string;
    refundAmount?: string | number;
    tokenCode?: string;
    consignee?: string;
}






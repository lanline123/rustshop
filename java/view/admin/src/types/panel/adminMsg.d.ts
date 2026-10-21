// 列表查询时筛选参数类型
export interface AdminMsgFilterParams {
    page: number;
    size: number;
    msgType: number;
}

// 获取列表返回参数类型
export interface AdminMsgFilterState {
    adminId: number;
    goodsId: number;
    orderId: number;
    shopId: number;
    isReaded: number;
    msgType: number;
    msgId: number;
    content: string;
    items?: object[];
    order?: object[];
    msgLink?: string;
    sendTime?: string;
    title?: string;
    msgTypeName?: string;
    route?: string;
    relatedData?: AdminMsgRelatedData;
}

export interface AdminMsgRelatedData {
    id?: number;
    feedbackId?: number;
    productId?: number;
    inquiryId?: number;
    type?: number;
    route?: string;
    orderId?: number;
    aftersaleId?: number;
    refundId?: number;
    withdrawApplyId?: number;
    shopWithdrawLogId?: number;
    invoiceId?: number;
    orderInvoiceId?: number;
    ownerId?: number;
    ownerType?: number | string;
    shopId?: number;
    [key: string]: any;
}
export interface AdminMsgMsgTypeFilterState {
    catId: number;
    catName: string;
    child?: object;
    unreadCount: number;
}
export interface AdminMsgSearchFilterResult {
    records: AdminMsgFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface AdminMsgFilterResult {
    records: AdminMsgFilterState[];
    filter: AdminMsgFilterParams;
    total: number;
}

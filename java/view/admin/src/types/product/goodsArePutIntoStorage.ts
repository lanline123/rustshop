// 获取列表返回参数类型
export interface InventoryFilterState {
    productId: number;
    productName: string;
    type?: number;
    purchaseOrderSn?: string;
    purchaseInboundSn?: string;
    purchaseReturnSn?: string;
}

export interface InventoryFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    inOrOut: number;
    beginTime?: string;
    endTime?: string;
    type?: number;
}


export interface InventoryFilterResult {
    records: InventoryFilterState[];
    filter: InventoryFilterParams;
    total: number;
    waitingCheckedCount: number;
}

export interface FormState {
    type?: number;
    comment?: string;
    productStockRecordDetailList: any[];
    productIds?: number[];
    productType?: number;
    purchaseOrderSn?: string;
    purchaseInboundSn?: string;
    purchaseReturnSn?: string;
}

export interface statisticsGoodsFilterParams {
    startTime: string;
    endTime: string;
    isExport?: string;
    keyword: string;
    page: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
}



export interface statisticsGoodsFilterState {
    records: FilterResult[];
    filter: Filter;
    total: number;
    errcode: number;
    message: string;
}

export interface Filter {
    startTime: string;
    endTime: string;
    keyword: string;
    isExport: number;
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
}

export interface FilterResult {
    itemId: number;
    orderId: number;
    orderSn: string;
    userId: number;
    price: string;
    quantity: number;
    productId: number;
    productName: string;
    productSn: string;
    picThumb: string;
    skuId: number;
    skuData: SkuData | null;
    deliveryQuantity: number;
    productType: number;
    isGift: number;
    shopId: number;
    isPin: number;
    prepayPrice: string;
    totalSalesAmount: string;
    totalSalesNum: string;
    thinkCount: number;
}

export interface SkuData {
    name: string;
    value: string;
}

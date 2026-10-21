// 列表查询时筛选参数类型
export interface ProductGiftFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}
// 获取列表返回参数类型
export interface ProductGiftFilterResult {
    records: ProductGiftFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface ProductGiftFilterState {
    giftId: number;
    giftName?: string;
    productId?: any;
    giftStock?: number;
    num?: number;
    productInfo?: {
        productName: string;
        productPrice: string;
        picThumb: string;
    }
}
// 获取详情返回参数类型
export interface ProductGiftFormState {
    productId?: any;
    giftName?: string;
    giftStock?: number;
    skuId?: any;
}

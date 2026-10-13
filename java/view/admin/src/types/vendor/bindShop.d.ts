// 列表查询时筛选参数类型
export interface FilterParams {
    endTime?: string;
    keyword?: string;
    merchantId?: string;
    page?: number;
    shopId?: string;
    shopName?: string;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    startTime?: string;
}

// 获取列表返回参数类型
export interface FilterState {
    downProductCount?: number;
    merchantId?: number;
    merchantName?: string;
    orderAmount?: number;
    orderCount?: number;
    shopId?: number;
    shopLogo?: string;
    shopTitle?: string;
    upProductCount?: number;
    [property: string]: any;
}

export interface FilterResult {
    records: FilterState[];
    filter: FilterParams;
    total: number;
}


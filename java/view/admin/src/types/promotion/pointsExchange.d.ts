// 列表查询时筛选参数类型
export interface PointsExchangeFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    isHot?: number | string;
    isEnabled?: number | string;
}

// 获取列表返回参数类型
export interface PointsExchangeFilterResult {
    records: PointsExchangeFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface PointsExchangeFilterState {
    id: number;
    productId?: any;
    exchangeIntegral?: number;
    pointsDeductedAmount?: number;
    isHot?: number;
    isEnabled?: number;
}


// 获取详情返回参数类型
export interface PointsExchangeFormState {
    id?: number;
    productId?: any;
    skuId?: any;
    exchangeIntegral?: number;
    pointsDeductedAmount?: number;
    isHot?: number;
    isEnabled?: number;
}

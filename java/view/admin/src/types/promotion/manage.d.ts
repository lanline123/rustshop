// 列表查询时筛选参数类型
export interface PromotionFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    timeType?: number;
    types?: number;
}

// 获取列表返回参数类型
export interface PromotionFilterResult {
    records: PromotionFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface RimeTypeCount {
    timeType1Count: number;
    timeType2Count: number;
    timeType3Count: number;
}
export interface PromotionFilterState {
    typeText: string;
    timeText: string;
    promotionId: number;
    promotionName: string;
    startTime: string | number;
    endTime: string | number;
    type: number;
    shopId: number;
    relationId: number;
}

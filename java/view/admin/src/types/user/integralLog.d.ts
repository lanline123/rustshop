// 列表查询时筛选参数类型
export interface IntegralLogFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface IntegralLogFilterState {
    logId: number;
    userId: number;
    username: string;
    points: number;
    changeTime: string;
    changeDesc: string;
    changeType: number;
}
export interface IntegralLogFilterResult {
    records: IntegralLogFilterState[];
    filter: IntegralLogFilterParams;
    total: number;
}




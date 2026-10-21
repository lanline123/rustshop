// 查询参数类型
export interface consumerRankingFilterParams {
    startTime: string;
    endTime: string;
    isExport?: string;
    keyword: string;
    page: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
}

export interface consumerRankingFilterState {
    records: FilterResult[];
    errcode: number;
    message: string;
    total: number;
}

export interface FilterResult {
    username: string;
    mobile: string;
    orderNum: number;
    orderAmount: string;
}

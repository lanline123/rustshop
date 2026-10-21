// 查询参数类型
export interface newMemberFilterParams {
    startEndTime: string;
    isExport?: string;
    dateType: string;
}

// 结果返回类型
export interface newMemberFilterState {
    records: FilterResult;
    errcode: number;
    message: string;
}
export interface FilterResult {
    horizontalAxis: string[];
    longitudinalAxis: number[];
}

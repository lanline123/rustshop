// 列表查询时筛选参数类型
export interface LiveFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    wechatLiveTitle?: string;
}

// 获取列表返回参数类型
export interface LiveFilterResult {
    records: LiveFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface LiveFilterState {
    roomId: number;
    wechatLiveTitle?: string;
    actRange?: number;
    actRangeExt?: number[];
}


export interface LiveFormState {
    wechatLiveTitle?: string;
    actRange?: number;
    actRangeExt?: number[];
}

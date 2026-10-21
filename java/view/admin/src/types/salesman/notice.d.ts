// 列表查询时筛选参数类型
export interface noticeFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    status?: number | string;
    title?: string;
    endTime?: string;
    startTime?: string;
}

// 获取列表返回参数类型
export interface noticeFilterResult {
    records: noticeFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface noticeFilterState {
    id?: number;
    statusText?: string;
    title?: string;
    img?: string;
    startTime?: string;
    endTime?: string;
    describe?: string;
    content?: string;
    isTop?: number;
}

// 获取详情返回参数类型
export interface noticeFormState {
    id?: number;
    statusText?: string;
    status?: number;
    title?: string;
    img?: string;
    startTime?: string;
    endTime?: string;
    describe?: string;
    content?: string;
    isTop?: number;
}

// 列表查询时筛选参数类型
export interface categoryFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    categoryName?: string;
}

// 获取列表返回参数类型
export interface categoryFilterResult {
    records: categoryFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface categoryFilterState {
    categoryId?: number;
    categoryName?: string;
}

// 获取详情返回参数类型
export interface categoryFormState {
    categoryId?: number;
    sortOrder?: number;
    categoryName?: string;
}

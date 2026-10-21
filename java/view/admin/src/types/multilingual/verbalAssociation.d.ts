// 列表查询时筛选参数类型
export interface LocalesRelationFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
}

// 获取列表返回参数类型
export interface LocalesRelationFilterResult {
    records: LocalesRelationFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface LocalesRelationFilterState {
    id?: number;
    code?: string;
    name?: string;
    localesId?: string;
}


// 获取详情返回参数类型
export interface LocalesRelationFormState {
    id?: number;
    code?: string;
    name?: string;
    localesId?: number;
}

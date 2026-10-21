// 列表查询时筛选参数类型
export interface SignInSettingFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface SignInSettingFilterResult {
    records: SignInSettingFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface SignInSettingFilterState {
    id?: number;
    name?: string;
    points?: number;
    dayNum?: number;
    sortOrder?: number;
}


// 获取详情返回参数类型
export interface SignInSettingFormState {
    id?: number;
    name?: string;
    points?: number;
    dayNum?: number;
    sortOrder?: number;
}

// 列表查询时筛选参数类型
export interface ExampleFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    isShow?: number;
}

// 获取列表返回参数类型
export interface ExampleFilterResult {
    records: ExampleFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface ExampleFilterState {
    exampleId?: number;
    exampleName?: string;
    exampleLogo?: string;
    isShow?: boolean;
    sortOrder?: number;
}


// 获取详情返回参数类型
export interface ExampleFormResult {
    item: ExampleFormState[];
}
export interface ExampleFormState {
    exampleName?: string;
    exampleLogo?: string;
    exampleDesc?: string;
    sortOrder?: string;
    isShow?: number;
}

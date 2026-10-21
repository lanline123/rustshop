// 列表查询时筛选参数类型
export interface ProductServicesFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface ProductServicesFilterResult {
    records: ProductServicesFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface ProductServicesFilterState {
    productServiceId?: number;
    productServiceName?: string;
    productServiceDesc?: string;
    icoImg?: string;
    sortOrder?: number;
    defaultOn?: number;
}


// 获取详情返回参数类型
export interface ProductServicesFormState {
    productServiceId?: number;
    productServiceName?: string;
    productServiceDesc?: string;
    icoImg?: string;
    sortOrder?: number;
    defaultOn?: number;
}

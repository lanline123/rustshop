// 列表查询时筛选参数类型
export interface materialFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    status?: number;
    title?: string;
    endTime?: string;
    startTime?: string;
    categoryId?: string;
}

// 获取列表返回参数类型
export interface materialFilterResult {
    records: materialFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface materialFilterState {
    id?: number;
    categoryId?: number;
    category?: {
        categoryId: number;
        categoryName: string;
    };
    product?: {
        productName: string;
        productSn: string;
    }
    content?: string;
    pics?: any[];
    isTop?: number;
}

// 获取详情返回参数类型
export interface materialFormResult {
    item: materialFormState[];
}
export interface materialFormState {
    id?: number;
    categoryId?: number;
    productId?: number[];
    content?: string;
    pics?: any[];
    isTop?: number;
}

// 列表查询时筛选参数类型

export interface ProductInventoryLogFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    type?: number;
}

// 获取列表返回参数类型
export interface ProductInventoryLogFilterResult {
    records: ProductInventoryLogFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface ProductInventoryLogFilterState {
    logId?: number;
    productName?: string;
    desc?: string;
    oldNumber?: number;
    number?: number;
    changeNumber?: number;
    addTime?: string;
}


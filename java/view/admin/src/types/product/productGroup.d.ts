// 列表查询时筛选参数类型
export interface ProductGroupFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    isShow?: number;
    productGroupIsHot?: number;
    firstWord?: string;
    productGroupIds?: string;
}

// 获取列表返回参数类型
export interface ProductGroupFilterState {
    productGroupId?: number;
    productGroupName?: string;
    productGroupSn?: string;
    productGroupDescription?: string;
    productIds?: number[];
    addTime?: string;
    introType?: string;
}
export interface ProductGroupSearchFilterResult {
    productGroupList: ProductGroupFilterState[];
    message: string;
    errcode: number;
}

export interface ProductGroupFilterResult {
    records: ProductGroupFilterState[];
    filter: ProductGroupFilterParams;
    total: number;
}


// 获取详情返回参数类型
export interface ProductGroupFormState {
    productGroupId?: number;
    productGroupName?: string;
    productGroupSn?: string;
    productGroupDescription?: string;
    productIds?: number[];
}


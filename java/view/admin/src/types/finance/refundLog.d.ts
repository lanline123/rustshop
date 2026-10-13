
export interface RedundLogFilterParams {
    page: number,
    size: number,
    type: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface RedundLogFilterState {
    brandId: number;
    brandName: string;
    brandLogo: string;
    firstWord: string;
    brandIsHot: boolean;
    isShow: boolean;
    sortOrder: number;
}
export interface RedundLogFilterResult {
    records: RedundLogFilterState[];
    filter: RedundLogFilterParams;
    total: number;
}






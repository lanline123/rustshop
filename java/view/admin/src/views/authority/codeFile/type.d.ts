// 列表筛选
export interface FilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword: string;
    isShow: number;
    brandIsHot: number;
    firstWord: string;
}

export interface FilterState {
    brandId: number;
    brandName: string;
    brandLogo: string;
    firstWord: string;
    brandIsHot: boolean;
    isShow: boolean;
    sortOrder: number;
}
export interface FilterResult {
    records: FilterState[];
    filter: {
        page: number;
    };
    total: number;
}

// 编辑表单
export interface FormState {
    brandName: string;
    brandLogo: string;
    brandDesc: string;
    sortOrder: string;
    firstWord: string;
    isShow: number;
    brandIsHot: number;
}
export interface FormResult {
    item: FormState;
}

// 列表查询时筛选参数类型
export interface BrandFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    isShow?: number | string;
    status?: number | string;
    brandIsHot?: number | string;
    firstWord?: string;
    shopId?: string;
}

// 获取列表返回参数类型
export interface BrandFilterState {
    brandId: number;
    brandName: string;
    brandLogo?: string;
    firstWord?: string;
    brandIsHot?: boolean;
    isShow?: boolean;
    sortOrder?: number;
}
export interface BrandSearchFilterResult {
    brandList: BrandFilterState[];
    firstWordList: string[];
    message: string;
    errcode: number;
}

export interface BrandFilterResult {
    records: BrandFilterState[];
    filter: BrandFilterParams;
    total: number;
}


// 获取详情返回参数类型
export interface BrandFormState {
    brandName?: string;
    brandLogo?: string;
    brandDesc?: string;
    sortOrder?: number;
    firstWord?: string;
    isShow?: number;
    brandIsHot?: number;
}


export interface auditBrandForm {
    brandId: number;
    status: number;
    rejectRemark: string;
}

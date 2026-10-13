// 列表筛选
export interface CategoryFilterParams {
    page?: number;
    size?: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    isShow?: number;
    parentId: number;
}

export interface CategoryFilterState {
    categoryId: number;
    categoryName: string;
    categoryPic: string;
    categoryIco: string;
    measureUnit: string;
    isHot: number;
    isShow: number;
    sortOrder: number;
}
export interface CategoryFilterResult {
    records: CategoryFilterState[];
    filter: {
        page: number;
    };
    parentName:string;
    total: number;
}

// 编辑表单
export interface CategoryFormState {
    categoryId?: number;
    categoryName?: string;
    shortName?: string;
    parentId?: number;
    categoryPic?: string;
    measureUnit?: string;
    categoryIco?: string;
    seoTitle?: string;
    searchKeywords?: string;
    keywords?: string;
    categoryDesc?: string;
    sortOrder?: number;
    isHot?: number;
    isShow?: number;
    seoIdentification?: string;
}

export interface CategoryMoveState {
    id?:number;
    targetCatId?: number;
}

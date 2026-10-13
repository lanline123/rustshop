// 列表筛选
export interface ShopProductCategoryFilterParams {
    page?: number;
    size?: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    isShow?: number;
    parentId: number;
}

export interface ShopProductCategoryFilterState {
    categoryId?: number;
    parentId?: number;
    categoryName?: string;
    sortOrder?: number;
    isShow?: number;
}
export interface ShopProductCategoryFilterResult {
    records: ShopProductCategoryFilterState[];
    filter: {
        page: number;
    };
    parentName:string;
    total: number;
}

// 编辑表单
export interface ShopProductCategoryFormState {
    categoryId?: number;
    parentId?: number;
    categoryPic?: string;
    categoryName?: string;
    sortOrder?: number;
    isShow?: number;
}

export interface ShopProductCategoryMoveState {
    id?:number;
    targetCatId?: number;
}

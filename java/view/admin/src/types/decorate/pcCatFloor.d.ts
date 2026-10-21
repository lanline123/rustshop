// 列表查询时筛选参数类型
export interface PcCatFloorFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface PcCatFloorFilterState {
    catFloorId: number;
    catFloorName?: string;
    categoryIds?: string;
    categoryNames?: string;
    brandIds?: string;
    hotCat?: string;
    floorIco?: string;
    floorIcoFont?: string;
    isShow?: boolean;
    sortOrder?: number;

}
export interface PcCatFloorFilterResult {
    records: PcCatFloorFilterState[];
    filter: PcCatFloorFilterParams;
    total: number;
}


// 获取详情返回参数类型
export interface PcCatFloorFormState {
    categoryList?: CategoryListItem[];
    brandList?: CategoryListItem[];
    catFloorId?: number;
    catFloorName?: string;
    categoryIds?: number[];
    categoryNames?: string[];
    brandIds?: number[];
    hotCat?: string;
    floorIco?: string;
    floorIcoFont?: string;
    isShow?: number;
    sortOrder?: number;

}

export interface CategoryListItem{
    categoryId?:number;
    brandId?:number;
    categoryName?:string;
}

// 编辑表单



// 列表查询时筛选参数类型
export interface UserRankFilterParams {
    sortField: string,
    sortOrder: string,
    keyword?: string;
    isPage?: number,
}

// 获取列表返回参数类型
export interface UserRankFilterState {
    rankId?: number;
    rankName?: string;
    rankType?: number;
    rankTypeName?: string;
    showPrice?: number;
    rankIco?: string;
    rankBg?: string;
    minGrowthPoints?: number;
    maxGrowthPoints?: number;
    discount?: number;
}
export interface UserRankFilterResult {
    records: UserRankFilterState[];
    filter: UserRankFilterParams;
    total: number;
}

export interface UserRankFormResult {
    item: UserRankFormState;
}

// 获取详情返回参数类型
export interface UserRankFormState {
    rankId?: number;
    rankName?: string;
    rankType?: number;
    showPrice?: number;
    rankIco?: string;
    rankBg?: string;
    minGrowthPoints?: number;
    maxGrowthPoints?: number;
    discount?: number;
}



// 编辑表单



interface buyerShowGroupFilterState {
    groupName: string;
    buyerShowCount: number;
    buyerShowGroupId: number;
    productGroupName: Array<any>;
}
// 获取详情返回参数类型
export interface buyerShowGroupFormState {
    groupName?: string;
    productGroups?: Array<any>;
    groupIds?: Array<any>;
}


export interface buyerShowGroupFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
}

// 获取列表返回参数类型
export interface buyerShowGroupFilterResult {
    records: buyerShowGroupFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

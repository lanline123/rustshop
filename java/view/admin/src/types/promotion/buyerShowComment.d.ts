interface buyerShowCommentFilterState {
    content: string;
    addTime: string;
    userName: string;
    status: number;
}
// 获取详情返回参数类型
export interface buyerShowCommentFormState {
    groupName?: string;
    productGroups?: Array<any>;
    groupIds?: Array<any>;
}


export interface buyerShowCommentFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    buyerShowId?: number;
    status?: number | string;
    startTime?: string;
    endTime?: string;
}

// 获取列表返回参数类型
export interface buyerShowCommentFilterResult {
    records: buyerShowCommentFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

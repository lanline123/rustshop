// 列表查询时筛选参数类型
export interface CommentFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    isShowed?: number;
    keyword?: string;
}

// 获取列表返回参数类型
export interface CommentFilterResult {
    records: CommentFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface CommentFilterState {
    commentId?: number;
    content?: string;
    commentTitle?: string;
    commentDesc?: string;
    isShow?: boolean;
    sortOrder?: number;
    isRecommend?: number;
}

// 获取详情返回参数类型
export interface CommentFormState {
    productIds: number[];
    productId?: number;
    isRecommend?: number;
    username?: string;
    avatar?: string;
    commentRank?: number;
    commentTag: string[];
    content?: string;
    addTime?: string;
    showPics?: Array<string>;
    sortOrder?: number;
    status?: number;
    isTop?: number;
    replies?: {
        commentId: number;
        userId: number;
        username: string;
        content: string;
        addTime: string;
        parentId: number;
    }[];
}

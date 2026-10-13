// 列表查询时筛选参数类型
export interface FeedbackFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    shopId?: number;
    type?: string;
    status?: string;
    excludeReply: boolean;
}

// 获取列表返回参数类型
export interface FeedbackFilterState {
    id?: number;
    addTime?: string;
    content?: string;
    complaintInfo?: string;
    email?: string;
    feedbackPics?: string[];
    mobile?: string;
    orderId?: number;
    orderSn?: string;
    parentId?: number;
    productId?: number;
    product?: product;
    reply?: reply;
    shopId?: number;
    shop?: shop;
    status?: number;
    statusName?: string;
    title?: string;
    type?: number;
    typeName?: string;
    userId?: number;
    username?: string;
    orderInfo?: orderInfo;
}
export interface FeedbackFilterResult {
    records: FeedbackFilterState[];
    filter: FeedbackFilterParams;
    total: number;
}

export interface FeedbackFormResult {
    item: FeedbackFormState;
}

interface product {
    productId: number;
    productName: string;
}

interface orderInfo {
    orderId: number;
    orderSn: string;
}

interface shop {
    shopId: number;
    shopTitle: string;
}

interface reply {
    content: string;
    email: string;
    id: number;
    mobile: string;
    parentId: number;
    status: number;
    statusName: string;
    type: number;
    typeName: string;
    username: string;
}

// 回复列表项类型
export interface ReplyItem {
    id: number;
    parentId: number;
    username: string;
    email: string;
    mobile: string;
    content: string;
    status: number;
    type: number;
}

// 获取详情返回参数类型
export interface FeedbackFormState {
    id?: number;
    addTime?: string;
    content?: string;
    complaintInfo?: string;
    email?: string;
    feedbackPics?: string[];
    mobile?: string;
    orderId?: number;
    orderInfo?: orderInfo;
    parentId?: number;
    shopId?: number;
    shop?: shop;
    status?: number;
    statusName?: string;
    title?: string;
    type?: number;
    typeName?: string;
    userId?: number;
    username?: string;
    productId?: number;
    product: product;
    reply?: reply;
    replyList?: ReplyItem[];
}



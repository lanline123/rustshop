// 列表查询时筛选参数类型
export interface PriceInquiryFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    mobile?: string;
    status?: string;
}

// 获取列表返回参数类型
export interface PriceInquiryFilterState {
    id: number;
    userId?: number | null;
    content: string;
    createTime?: string;
    mobile?: string;
    email?: string;
    remark?: string;
    shopInfo?: {
        shopTitle: string;
        shopId: number;
    };
    product?: {
        productName: string;
        productSn: string;
        productId: number;
    };
    status?: number;
}
export interface PriceInquiryFilterResult {
    records: PriceInquiryFilterState[];
    filter: PriceInquiryFilterParams;
    total: number;
}

export interface PriceInquiryReplyRecord {
    replyName: string;
    createTime: string;
    content: string;
}

// 获取详情返回参数类型
export interface PriceInquiryFormState {
    id: number;
    userId?: number | null;
    content: string;
    createTime?: string;
    mobile?: string;
    email?: string;
    remark?: string;
    shopInfo?: {
        shopTitle: string;
        shopId: number;
    };
    product?: {
        productName: string;
        productSn: string;
        productId: number;
    };
    status?: number;
    replyRecords?: PriceInquiryReplyRecord[];
}



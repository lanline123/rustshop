export interface StatementListFilterParams {
    accountType?: number;
    endDateTime?: string;
    keyword?: string;
    page?: number;
    paymentType?: string;
    recordSn?: string;
    shopId?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    startDateTime?: string;
    timeType?: number;
    type?: number;
    vendorId?: number;
    source: string;
}

// 获取列表返回参数类型
export interface StatementListFilterState {
    countId: string;
    current: number;
    maxLimit: number;
    orders: OrderItem[];
    records: StatementListItem[];
    searchCount: boolean;
    size: number;
    total: number;
}

export interface OrderItem {
    asc?: boolean;
    column?: string;
}

export interface StatementListItem {
    accountBalance?: number;
    accountType?: number;
    accountTypeName?: string;
    amount?: number;
    entryType?: string;
    entryTypeName?: string;
    paymentType?: string;
    paymentTypeName?: string;
    recordSn?: string;
    recordTime?: string;
    settlementStatus?: number;
    settlementTime?: string;
    shopId?: number;
    shopName?: string;
    type?: number;
    typeName?: string;
    vendorId?: number;
    vendorName?: string;
}
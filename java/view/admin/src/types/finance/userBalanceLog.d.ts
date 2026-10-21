
export interface UserBalanceLogFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    username?: string;
    shopId?: string;
}

// 获取列表返回参数类型
export interface UserBalanceLogFilterState {
    logId?: number;
    changeTime?: string;
    username?: string;
    changeDesc?: string;
    balance?: number;
    frozenBalance?: string;
    
}
export interface UserBalanceLogFilterResult {
    records: UserBalanceLogFilterState[];
    filter: UserBalanceLogFilterParams;
    total: number;
}






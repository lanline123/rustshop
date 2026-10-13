
export interface AccountPanelFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    searchDate?: string[];
    searchStartDate?: string;
    searchEndDate?: string;
}

// 获取列表返回参数类型
export interface AccountPanelFilterState {
    voucherAmount: number;
    toCashAmount: number;
    balance: number;
    frozenMoney: number;
    surplus: number;
    usePoints: number;
}
export interface AccountPanelFilterResult {
    records: AccountPanelFilterState;
    filter: AccountPanelFilterParams;
    total: number;
}






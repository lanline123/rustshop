
export interface UserWithdrawApplyFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    status?: number | string;
    accountType?: number;
    walletType?: number;
}

// 获取列表返回参数类型
export interface UserWithdrawApplyFilterState {
    id: number;
    userId: number;
    amount: string;
    addTime: string;
    finishedTime: string;
    postscript: string;
    status: number;
    accountData?: AccountDataItem;
}
export interface UserWithdrawApplyFilterResult {
    records: UserWithdrawApplyFilterState[];
    filter: UserWithdrawApplyFilterParams;
    total: number;
}

// 获取详情返回参数类型
export interface UserWithdrawApplyFormState {
    id?: number;
    userId?: number;
    amount: number;
    addTime?: string;
    finishedTime?: string;
    postscript?: string;
    status?: number;
    statusType?: string;
    username?: string;
    accountData: AccountDataItem;
}
export interface AccountDataItem{
    accountType?:number;
    accountName?:string;
    accountNo?:string;
    identity?:string;
    bankName?:string;
}






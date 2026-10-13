
export interface UserRechargeOrderFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    accountType?: number;
    status?: number | string;
}

// 获取列表返回参数类型
export interface UserRechargeOrderFilterState {
    orderId: number;
    userId: number;
    amount: string;
    addTime: string;
    finishedTime: string;
    postscript: string;
    status: number;
}
export interface UserRechargeOrderFilterResult {
    records: UserRechargeOrderFilterState[];
    filter: UserRechargeOrderFilterParams;
    total: number;
}

// 获取详情返回参数类型
export interface UserRechargeOrderFormState {
    id?: number;
    userId?: number;
    amount: number;
    discountMoney?: number;
    username?: string;
    addTime?: string;
    finishedTime?: string;
    postscript?: string;
    status?: number;
    statusType?: string;
    user?: any;
}






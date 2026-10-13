
export interface saleTargetsFilterState {
    records: FilterResult;
    errcode: number;
    message: string;
}

export interface FilterResult {
    orderNum: string;
    orderProductNum: string;
    orderTotalAmount: string;
    userNum: string;
    consumerMembershipNum: string;
    capitaConsumption: string;
    clickCount: string;
    clickRate: string;
    orderRate: string;
    consumerMembershipRate: string;
    purchaseRate: string;
}

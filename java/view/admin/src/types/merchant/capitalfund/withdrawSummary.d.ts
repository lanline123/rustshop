// 门店/店铺提现汇总 - 列表查询参数
export interface ShopWithdrawSummaryFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    shopTitle?: string;
    status?: number | string;
    shopType?: number;
    addTimeStart?: string;
    addTimeEnd?: string;
}

// 门店/店铺提现汇总 - 单条记录
export interface ShopWithdrawSummaryItem {
    shopId: number;
    shopTitle: string;
    shopLogo: string;
    status: number;
    statusText: string;
    shopMoney: number;
    withdrawnAmount: number;
    pendingWithdrawAmount: number;
    addTime: number; // 秒级时间戳
}

// 门店/店铺提现汇总 - 列表返回
export interface ShopWithdrawSummaryListResult {
    records: ShopWithdrawSummaryItem[];
    total: number;
    size: number;
    current: number;
}


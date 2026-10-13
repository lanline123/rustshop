// 供应商提现汇总 - 列表查询参数
export interface VendorWithdrawSummaryFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    vendorName?: string;
    status?: number | string;
    addTimeStart?: string;
    addTimeEnd?: string;
}

// 供应商提现汇总 - 单条记录
export interface VendorWithdrawSummaryItem {
    vendorId: number;
    vendorName: string;
    vendorLogo: string;
    status: number;
    statusText: string;
    vendorMoney: number;
    withdrawnAmount: number;
    pendingWithdrawAmount: number;
    addTime: number; // 秒级时间戳
}

// 供应商提现汇总 - 列表返回
export interface VendorWithdrawSummaryListResult {
    records: VendorWithdrawSummaryItem[];
    total: number;
    size: number;
    current: number;
}


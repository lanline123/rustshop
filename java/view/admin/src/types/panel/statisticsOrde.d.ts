export interface statisticsOrdeFilterParams {
    startEndTime: string;
    dateType: string;
    statisticType: string;
    isExport?: string;
}

export interface statisticsOrdeFilterState {
    salesData: SalesData;
    salesStatisticsData: SalesStatisticsData;
    errcode: number;
    message: string;
}

export interface SalesData {
    productPayment: number;
    productPaymentGrowthRate: string;
    productRefund: number;
    productRefundGrowthRate: string;
    turnover: number;
    turnoverGrowthRate: string;
    rechargeAmount: number;
    rechargeAmountGrowthRate: string;
    balancePayment: number;
    balancePaymentGrowthRate: string;
    productPaymentNum: number;
    productPaymentNumGrowthRate: string;
    productRefundNum: number;
    rechargeNumGrowthRate: string;
    rechargeNum: number;
    productRefundNumGrowthRate: string;
    balancePaymentNum: number;
    balancePaymentNumGrowthRate: string;
}

export interface SalesStatisticsData {
    horizontalAxis: string[];
    longitudinalAxis: number[];
}

// 核心数据汇总查询时筛选参数类型
export interface CoreSummaryFilterParams {
    summaryType: number;
}
export interface CoreSummaryFormState {
    newSalesmanCount: number;
	salesmanAmount: number;
	customNum: number;
	salesmanCommission: number;
}

// 核心指标趋势查询时筛选参数类型
export interface CoreTrendFilterParams {
    dateType: number;
    startEndTime: any;
}
export interface CoreTrendFormResult {
    item: CoreTrendFormState;
}
export interface CoreTrendFormState {
    horizontalAxis: number[];
	longitudinalAxis: number[];
}


// 分销员排行查询时筛选参数类型
export interface RankingFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    dateType: number;
    startEndTime: any;
}
export interface RankingFormResult {
    records: RankingFormState[];
    total: number;
}
export interface RankingFormState {
    username: string;
	nickname: string;
	totalSaleAmount: number;
	totalCustomers: number;
	totalPayCustomers: number;
	orderNum: number;
}

// 分销员排行查询时筛选参数类型
export interface AnalysisFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    dateType: number;
    startEndTime: any;
}
export interface AnalysisFormResult {
    records: AnalysisFormState[];
    total: number;
}
export interface AnalysisFormState {
    productId: number;
	productName: string;
	totalProductMoney: number;
	commissionExpenses: number;
}
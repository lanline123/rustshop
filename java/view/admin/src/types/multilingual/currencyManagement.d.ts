// 列表查询时筛选参数类型
export interface CurrencyFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    name?: string;
}

// 获取列表返回参数类型
export interface CurrencyFilterResult {
    records: CurrencyFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface CurrencyFilterState {
	id?: number;
	name?: string;
	symbol?: string;
	isDefault?: number;
	rate?: string;
	code?: string;
}
// 获取详情返回参数类型
export interface CurrencyFormState {
    id?: number;
	name?: string;
	symbol?: string;
	localesId?: string;
	isDefault?: number;
	rate?: string;
	code?: string;
}

// 汇率自动更新配置（Open Exchange Rates，仅海外版）
export interface ExchangeRateConfig {
	autoSync: number; // 0-手动维护，1-自动更新
	appId?: string; // App ID（脱敏展示）
	syncIntervalHours: number; // 更新周期（小时）
	lastSyncTime?: string; // 最近同步时间（只读）
}

// 立即更新汇率返回结果
export interface ExchangeRateSyncResult {
	success: boolean;
	updatedCount: number;
	skippedCount: number;
	message: string;
	lastSyncTime?: string;
}





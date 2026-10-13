// 列表查询时筛选参数类型
export interface AccountFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface AccountFilterResult {
    records: AccountFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface AccountFilterState {
    accountId?: number;
	accountType: number;
    accountTypeText: string;
	accountName?: string;
	bankName?: string;
	bankBranch?: string;
	accountNo?: string;
}

// 获取详情返回参数类型
export interface AccountFormState {
    accountId?: number;
	accountType?: number;
    accountTypeText?: string;
	accountName?: number;
	bankName?: string;
	bankBranch?: string;
	accountNo?: string;
}

export interface AccountTypeList {
    accountType: number;
    accountTypeText: string;
}
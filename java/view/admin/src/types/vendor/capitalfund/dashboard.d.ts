// 列表查询时筛选参数类型
export interface AccountFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    shopId?: string;
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

export interface AccountFormState {
    vendorMoney: number;
    shopMoney?: number;
    frozenMoney?: number;
    unSettlementMoney?: number;
    cardCount?: number;
    merchant?: {
        companyName?: string;
        corporateName?: string;
        type: number;
        typeText?: string;
        merchantData?: {
            contactName?: string;
            detailedAddress?: string;
            merchantName?: string;
        }
    }
}

export interface StaffFrom {
    usingUser?: number;
    stopUsingUser?: number;
    residue?: number;
    adminLog: AdminLog[];
}
export interface AdminLog {
    logId?: number;
	userId?: number;
    logInfo?: string;
    logTime?: string;
	ipAddress?: string;
	username?: string;
}
// 列表查询时筛选参数类型
export interface promoterFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    groupId?: string;
    level?: string;
    mobile?: string;
    pidMobile?: string;
    addTimeStart?: string;
    addTimeEnd?: string;
}

// 列表查询时筛选参数类型
export interface salesmanCustomerFilterParams {
    page: number;
    size: number;
    salesmanId: number;
    sortField?: string;
    sortOrder?: string;
}

// 获取列表返回参数类型
export interface promoterFilterResult {
    records: promoterFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface promoterFilterState {
    userId?: number;
	level?: number;
	groupId?: number;
	pid?: number;
	nickname?: string;
}

// 获取详情返回参数类型
export interface promoterFormResult {
    item: promoterFormState;
    statistical: any;
}
export interface promoterFormState {
    userId?: string;
	pidMobile?: string;
	level?: number;
	groupId?: number;
	pid?: number | string;
	salesmanId?: number;
    nickname?: string;
	baseUserInfo?: any;
	pidUserInfo?: any;
}


export interface promoterDetailsFilterResult {
    records: promoterDetailsFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface promoterDetailsFormResult {
    salesmanVO: promoterDetailsFilterState;
    statistical: any;
}
export interface BaseUserInfo {
	mobile?: string;
	username?: string;
	nickname?: string;
	avatar?: string;
	userId?: number;
	distributionRegisterTime?: string;
}

export interface GroupInfo {
	groupId: number;
	groupName: string;
}

export interface SalesmanOrderInfo {
	statusText: string;
	salesmanOrderId: number;
	orderId: number;
	salesmanId: number;
	amount: string;
	status: number;
	itemId: number;
	orderAmount: string;
	addTime: string;
}

export interface promoterDetailsFilterState {
	salesmanId?: number;
	userId?: number;
	level?: number;
	groupId?: number;
	pid?: number;
	addTime?: string;
	shopId?: number;
	saleAmount?: string;
	levelText?: string;
	baseUserInfo: BaseUserInfo;
	pidUserInfo?: any;
	groupInfo?: GroupInfo;
}


// 获取列表返回参数类型
export interface salesmanCustomerFilterResult {
    records: salesmanCustomerFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface salesmanCustomerFilterState {
    userId?: number;
	level?: number;
	groupId?: number;
	pid?: number;
	nickname?: string;
	avatar?: string;
}
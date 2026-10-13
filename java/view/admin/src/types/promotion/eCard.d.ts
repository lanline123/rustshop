// 列表查询时筛选参数类型
export interface ECardGroupFilterParams {
    page?: number;
    size: number;
    sortField?: string;
    sortOrder?: string,
    keyword?: string;
    isDownload?: number;
}

// 获取列表返回参数类型
export interface ECardGroupFilterState {
    groupId: number;
    groupName: string;
    shopId: number;
    isUse: number;
    addTime: string;
    upTime: string;
}
export interface ECardGroupFilterResult {
    records: ECardGroupFilterState[];
    filter: ECardGroupFilterParams;
    total: number;
}
// 获取详情返回参数类型
export interface ECardGroupFormState {
    groupId?: number;
    groupName?: string;
    remark?: string;
    shopId?: number;
    isUse?: number;
    addTime?: string;
    upTime?: string;
}



// 列表查询时筛选参数类型
export interface ECardFilterParams {
    page?: number;
    size: number;
    sortField?: string;
    sortOrder?: string,
    keyword?: string;
    groupId?: number;
}

// 获取列表返回参数类型
export interface ECardFilterState {
    cardId: number;
    groupId: number;
    cardNumber: string;
    cardPwd: string;
    orderId: number;
    isUse: number;
    addTime: string;
    upTime: string;
}
export interface ECardFilterResult {
    records: ECardFilterState[];
    filter: ECardFilterParams;
    total: number;
}
// 获取详情返回参数类型
export interface ECardFormState {
    cardId?: number;
    groupId?: number;
    cardNumber?: string;
    cardPwd?: string;
    orderId?: number;
    isUse?: number;
    addTime?: string;
    upTime?: string;
    file?: any;
}
// 列表查询时筛选参数类型
export interface RechargeSettingFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
}

// 获取列表返回参数类型
export interface RechargeSettingFilterResult {
    records: RechargeSettingFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface RechargeSettingFilterState {
    rechargeId: number;
    money?: number;
    discountMoney?: number;
    isShow?: number;
    sortOrder?: number;
}


// 获取详情返回参数类型
export interface RechargeSettingFormState {
    rechargeId?: number;
    money?: number | string;
    discountMoney?: number | string;
    isShow?: number;
    sortOrder?: number;
}


export interface LogisticsCompanyFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    // 1 快递鸟 / 2 快递100；不传或非法值时后端返回全量
    providerType?: number;
}

export interface LogisticsCompanyFilterResult {
    records: LogisticsCompanyFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface LogisticsCompanyFilterState {
    logisticsId: number;
    logisticsCode?: string;
    apiLogisticsCode?: string;
    kuaidi100Code?: string;
    logisticsName?: string;
    logisticsDesc?: string;
    isEnabled?: number;
    isBound?: number;
    isDefault?: number;
    sortOrder?: number;
}

// syncKdn / syncKuaidi100 的同步结果
export interface LogisticsCompanySyncResult {
    remoteTotal?: number;
    inserted?: number;
    updated?: number;
    // 多候选或编码冲突的条数，后端不自动合并，需人工核对
    conflicted?: number;
}

export interface LogisticsCompanyFormResult {
    item: LogisticsCompanyFormState;
}

export interface LogisticsCompanyFormState {
    logisticsId?: number;
    logisticsName?: string;
    logisticsCode?: string;
    apiLogisticsCode?: string;
    logisticsDesc?: string;
    sortOrder?: number;
    isShow?: number;
    isEnabled?: number;
    isBound?: number;
    isDefault?: number;
    // 快递鸟电子面单（网点）参数
    customerName?: string;
    customerPwd?: string;
    monthCode?: string;
    sendSite?: string;
    sendStaff?: string;
    expType?: number | string;
    // 快递100 编码与电子面单参数
    kuaidi100Code?: string;
    kuaidi100PartnerId?: string;
    kuaidi100PartnerKey?: string;
    kuaidi100PartnerSecret?: string;
    kuaidi100Net?: string;
    kuaidi100CheckMan?: string;
    kuaidi100ExpType?: string;
    kuaidi100TempId?: string;
    kuaidi100ChildTempId?: string;
    kuaidi100BackTempId?: string;
}

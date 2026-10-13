
export interface LogisticsCompanyConfigFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    // 1 快递鸟 / 2 快递100；不传或非法值时后端返回全量
    providerType?: number;
}

export interface LogisticsCompanyConfigFilterResult {
    records: LogisticsCompanyConfigFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface LogisticsCompanyConfigFilterState {
    configId?: number;
    logisticsId: number;
    logisticsCode?: string;
    apiLogisticsCode?: string;
    kuaidi100Code?: string;
    logisticsName?: string;
    logisticsDesc?: string;
    customerName?: string;
    customerPwd?: string;
    monthCode?: string;
    sendSite?: string;
    sendStaff?: string;
    expType?: number | string;
    kuaidi100PartnerId?: string;
    kuaidi100PartnerKey?: string;
    kuaidi100PartnerSecret?: string;
    kuaidi100Net?: string;
    kuaidi100CheckMan?: string;
    kuaidi100ExpType?: string;
    kuaidi100TempId?: string;
    kuaidi100ChildTempId?: string;
    kuaidi100BackTempId?: string;
    isEnabled?: number;
    isDefault?: number;
    sortOrder?: number;
}

export interface LogisticsCompanyConfigFormState {
    configId?: number;
    logisticsId?: number;
    logisticsCode?: string;
    apiLogisticsCode?: string;
    kuaidi100Code?: string;
    logisticsName?: string;
    logisticsDesc?: string;
    customerName?: string;
    customerPwd?: string;
    monthCode?: string;
    sendSite?: string;
    sendStaff?: string;
    expType?: number | string;
    kuaidi100PartnerId?: string;
    kuaidi100PartnerKey?: string;
    kuaidi100PartnerSecret?: string;
    kuaidi100Net?: string;
    kuaidi100CheckMan?: string;
    kuaidi100ExpType?: string;
    kuaidi100TempId?: string;
    kuaidi100ChildTempId?: string;
    kuaidi100BackTempId?: string;
    isEnabled?: number;
    isDefault?: number;
    sortOrder?: number;
}

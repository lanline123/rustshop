// 列表查询时筛选参数类型
export interface LocalesFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
}

// 获取列表返回参数类型
export interface LocalesFilterResult {
    records: LocalesFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface LocalesFilterState {
    id?: number;
    localeId?: number;
    localeCode?: string;
    translationValue?: string;
    language?: string;
    flagPicture?: string;
    lastUpdated?: string;
    isEnabled?: number;
    isDefault?: number;
    /** 该语言的独立域名，不含协议与端口；未配置时为空串 */
    domain?: string;
}


// 获取详情返回参数类型
export interface LocalesFormState {
    id?: number;
    currencyId?: number;
    localeCode?: string;
    language?: string;
    flagPicture?: string;
    isEnabled?: number;
    isDefault?: number;
    sort?: number;
    /** 该语言的独立域名，不含协议与端口；未配置时为空串 */
    domain?: string;
}

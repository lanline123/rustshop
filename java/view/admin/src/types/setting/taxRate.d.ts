export interface TaxRateFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    regionName?: string;
    isActive?: number | "";
    startTime?: string;
    endTime?: string;
}

export interface TaxRateFilterResult {
    records: TaxRateFilterState[];
    filter?: {
        page: number;
    };
    total: number;
}

export interface TaxRateFilterState {
    taxRateId: number;
    regionId?: number;
    regionName?: string;
    rate?: string | number;
    isActive?: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface TaxRateFormState {
    taxRateId?: number;
    regionId?: number;
    regionName?: string;
    rate?: string | number;
    isActive?: number;
    remark?: string;
}

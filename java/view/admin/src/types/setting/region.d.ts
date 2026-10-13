export interface RegionFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    id?: number;
    parentId: number;
    regionId?: number;
}

export interface RegionFilterResult {
    records: RegionFilterState[];
    filter: {
        page: number;
    };
    total: number;
    data?: Object;
}

export interface RegionFilterState {
    regionId: number;
    isHot?: number;
    sortOrder?: number;
    id?: number;
    regionName?: string;
    firstWord?: string;
    parentId?: number;
    standardCode?: string;
    mobileAreaCode?: string;
    regionInputMode?: string;
    nextLevelAvailable?: boolean;
}

export interface CountryRegionState extends RegionFilterState {
    standardCode: string;
    mobileAreaCode: string;
}

export interface RegionFormResult {
    item: RegionFormState;
}

export interface RegionFormState {
    regionId?: number;
    regionName?: string;
    isHot?: number;
    sortOrder?: number;
    firstWord?: string;
    standardCode?: string;
    parentName?: string;
    parentId?: number;
}

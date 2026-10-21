
export interface ShippingTplFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface ShippingTplFilterResult {
    records: ShippingTplFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface ShippingTplFilterState {
    shippingTplId: number;
    shippingTplName?: string;
    shippingTime?: string;
    isDefault?: number;
    isFree?: number;
    pricingType?: number;
    isShow?: number;
    sortOrder?: number;
}

export interface ShippingTplFormState {
    shippingTplId?: number;
    shippingTplName?: string;
    shippingTime?: string;
    isDefault?: number;
    isFree?: number;
    sortOrder?: number;
    pricingType?: number;
    shippingTplInfo: ShippingTplInfo[];
}

export interface ShippingTplInfo {
    shippingTypeId?: number;
    shippingTypeName?: string;
    isChecked:number;
    defaultTplInfo: ShippingTplInfoItem;
    areaTplInfo: ShippingTplInfoItem[];
}
export interface ShippingTplInfoItem {
    id?: number;
    shippingTplId?: number;
    shippingTypeId?: number;
    isDefault?: number;
    shippingTypeName?: string;
    startNumber?: string;
    startPrice?: string;
    addNumber?: string;
    addPrice?: string;
    freePrice?: string;
    regionData: RegionDataItem;

}

export interface RegionDataItem {
    areaRegions: number[];
    areaRegionNames: string[];
}

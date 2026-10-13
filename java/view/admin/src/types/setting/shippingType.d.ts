
export interface ShippingTypeFilterParams {
    page: number;
    size: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface ShippingTypeFilterResult {
    records: ShippingTypeFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface ShippingTypeFilterState {
    shippingTypeId: number;
    isSupportCod?: string;
    logisticsName?: string;
    shippingDefaultId?: number;
    shippingTimeDesc?: string;
    shippingTypeDesc?: string;
    shippingTypeName?: string;
    sortOrder?: number;
    shopId?: number;
}
export interface ShippingTypeFormState {
    shippingTypeId?:number;
    shippingDefaultId?:number;
    logisticsName?: string;
    shippingTypeName?:string;
    shippingTypeDesc?:string;
    shippingTimeDesc?:string;
    sortOrder?:number;
    isSupportCod?:number;
}

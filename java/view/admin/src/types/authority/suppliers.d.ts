// 列表筛选
export interface SuppliersFilterParams {
    page: number,
    size: number,
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

export interface SuppliersFilterResult {
    records: SuppliersFilterState[];
    filter: {
        page: number;
    };
    total: number;
}


export interface SuppliersFilterState {
    isCheck?: number;
    suppliersId: number;
    suppliersName?: string;
    suppliersDesc?: string;
    contactName?: string;
    contactPhone?: string;
    contactAddress?: string;
    regions?: number[];

}

export interface SuppliersFormState {
    isCheck?: number;
    suppliersId?: number;
    suppliersName?: string;
    suppliersDesc?: string;
    contactName?: string;
    contactPhone?: string;
    contactAddress?: string;
    regions?: number[];
}

// 列表查询时筛选参数类型
export interface ProductAttributesTplFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface ProductAttributesTplFilterResult {
    records: ProductAttributesTplFilterState[];
    filter: {
        page: number;
    };
    total: number;
}

export interface ProductAttributesTplFilterState {
    tplId?: number;
    tplName?: string;
    tplData?: tplData;
    shopId?: number;
    categoryId?: number;
}


// 获取详情返回参数类型
export interface ProductAttributesTplFormState {
    tplId?: number;
    tplName?: string;
    tplData?: tplData;
    shopId?: number;
    categoryId?: number | string;
}

export interface tplData {
    normal?: tplDataField[];
    spe?: tplDataField[];
    extra?: tplDataField[];
}

export interface tplDataField {
    attrName?: string;
    attrValue?: string;
}

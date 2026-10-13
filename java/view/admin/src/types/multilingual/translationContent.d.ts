// 列表查询时筛选参数类型
export interface TranslationsFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    translationName?: string;
    localeCode?: string;
    dataType: number;
}

// 获取列表返回参数类型
export interface TranslationsFilterResult {
    records: TranslationsFilterState[];
    filter: {
        page: number;
    };
    total: number;
    ids: number[];
}
export interface TranslationsFilterState {
	id?: number;
	translationName?: string;
	translationKey?: string;
	dataType?: number | number[];
	items: Item[];
}
export interface Item {
	id: number;
	localeId: number;
	translationName: string;
	translationKey: string;
	translationValue: string;
	dataType: number;
	dataId: number;
}

// 获取详情返回参数类型
export interface TranslationsFormState {
    id?: number;
    code?: string;
    name?: string;
    localesId?: number;
}





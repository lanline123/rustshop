// 列表查询时筛选参数类型
export interface AreaCodeFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    name?: string;
}

// 获取列表返回参数类型
export interface AreaCodeFilterResult {
    records: AreaCodeFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface AreaCodeFilterState {
	id?: number;
	name?: string;
	code?: string;
	isDefault?: number;
	isAvailable?: number;
}
// 获取详情返回参数类型
export interface AreaCodeFormResult {
    item: AreaCodeFormState[];
}
export interface AreaCodeFormState {
    id?: number;
	name?: string;
	code?: string;
	isDefault?: number;
	isAvailable?: number;
}





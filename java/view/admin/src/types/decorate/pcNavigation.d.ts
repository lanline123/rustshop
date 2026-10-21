
import type { LinkType } from "@/types/decorate/decorate"
// 列表查询时筛选参数类型
export interface PcNavigationFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    type?:number
    parentId?:number
}

// 获取列表返回参数类型
export interface PcNavigationFilterState {
    id: number;
    title?: string;
    link?: LinkType;
    isShow?: boolean;
    isBlank?: boolean;
    sortOrder?: number;
    type?: number;
    parentId?: number;
}
export interface PcNavigationFilterResult {
    records: PcNavigationFilterState[];
    filter: PcNavigationFilterParams;
    total: number;
}


// 获取详情返回参数类型
export interface PcNavigationFormState {
    id?: number;
    title?: string;
    icon?: string;
    link?: LinkType;
    isShow?: number;
    isBlank?: number;
    sortOrder?: number;
    type: number;
    typeName?: string;
    parentId?: number;
}

export interface LinkFilterState{
    name?:string;
    appLink?:string;
    link?:string;
}



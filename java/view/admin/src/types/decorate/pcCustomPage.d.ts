import { FilterState } from "@/types/decorate/decorate";

// 列表查询时筛选参数类型
export interface FilterParams {
    page: number;
    size: number;
    keyword?: string;
    sortField?: string;
    sortOrder?: string;
}

// 获取列表返回参数类型
export interface FilterResult {
    records: PcCustomPageFilterState[];
    total: number;
}

export interface PcCustomPageFilterState extends FilterState {
    id: number;
    name: string;
    isShow: boolean;
    isHome: boolean;
    createTime: string;
    updateTime: string;
    pageType: number;
}

/**
 * PC自定义页面表单参数类型
 */
export interface PcCustomPageFormState {
    decorateId: number;
    decorateTitle?: string;
    sortOrder?: number;
}

/**
 * 绑定H5页面表单参数类型
 */
export interface BindH5DecorateFormState {
    decorateId: number;
    bindedH5Id: number;
}

/**
 * 取消绑定H5页面表单参数类型
 */
export interface CancelBindingFormState {
    decorateId: number;
}
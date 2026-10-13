import type { SendUserTarget } from "@/types/user/memberAudience";

// 列表查询时筛选参数类型
export interface LimitdiscountFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    discountId?: number;
    activeState?: number;
}

// 获取列表返回参数类型
export interface LimitdiscountFilterResult {
    records: LimitdiscountFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface LimitdiscountFilterState {
    discountId?: number;
    promotionName?: string;
    endTime?: string;
    startTime?: string;
    item?: {
        productId?: number;
        skuIds?: number[];
        discountType?: number;
        value?: number;
    }
}

export interface LimitdiscountFormResult {
    discountInfo: LimitdiscountFormState
}
export interface LimitdiscountFormState {
    discountId?: number;
    promotionName?: string;
    endTime?: string;
    startTime?: string;
    item: discountItem[]
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}
export interface discountItem {
    productId?: number;
    skuIds?: number[];
    discountType?: number;
    value?: number;
}
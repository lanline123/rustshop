import type { SendUserTarget } from "@/types/user/memberAudience";

// 列表查询时筛选参数类型
export interface ProductActivityFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    isGoing?: number | string;
    promotionType?: string;
}

// 获取列表返回参数类型
export interface ProductActivityFilterResult {
    records: ProductActivityFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface Promotion_type_data {
	minAmount?: number;
	reduce?: number;
    giftId?: number;
    giftName?: string;
    num?: number;
}
export interface ProductActivityFilterState {
    unit?: number;
	promotionTypeName?: string;
	productStatus?: string;
	promotionId?: number;
	promotionName?: string;
	startTime?: string;
	endTime?: string;
	limitUserRank?: number[];
	range?: number;
	rangeData: number[];
	promotionType?: number;
	promotionTypeData?: Promotion_type_data[];
	rulesType?: number;
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}

// 获取详情返回参数类型
export interface RankList {
    rankId?: number;
    rankName?: string;
}
export interface ProductActivityFormResult {
    rankList: RankList[];
    promotionStatus: string[];
}
export interface ProductActivityFormState {
    unit?: number;
	promotionTypeName?: string;
	productStatus?: string;
	promotionId?: number;
	promotionName?: string;
	startTime?: string;
	endTime?: string;
	limitUserRank?: number[];
	range?: number;
	rangeData?: number[];
	promotionType?: number;
	promotionTypeData?: Promotion_type_data[];
	rulesType?: number;
	productTime?: string[];
    memberRange?: number;
    memberMarks?: number[];
    sendUserTarget?: SendUserTarget;
}

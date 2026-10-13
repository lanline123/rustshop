// 列表查询时筛选参数类型
import {getPushLogList} from "@/api/user/pushLog";

export interface PushLogFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface PushLogFilterState {
    brandId: number;
    brandName: string;
    brandLogo: string;
    firstWord: string;
    brandIsHot: boolean;
    isShow: boolean;
    sortOrder: number;
}
export interface PushLogFilterResult {
    records: PushLogFilterState[];
    filter: PushLogFilterParams;
    total: number;
}

export interface PushLogFormResult {
    item: PushLogFormState;
}

// 获取详情返回参数类型
export interface PushLogFormState {
    brandName?: string;
    brandLogo?: string;
    brandDesc?: number;
    sortOrder?: string;
    firstWord?: string;
    isShow?: number;
    brandIsHot?: number;
    sendUserType?: number
}






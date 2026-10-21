// 列表查询时筛选参数类型
import type { ImgListItem } from "@/types/product/product";

export interface buyerShowListFilterParams {
    page: number;
    size: number;
    sortField: string,
    sortOrder: string,
    keyword?: string;
    goodsId?: number | string;
    content?: string;
    endTime?: string;
    startTime?: string;
    status: number | string;
}

// 获取详情返回参数类型
export interface buyerShowListFormState {
    content?: string;
    groupData?: Array<any>;
    rangeData?: Array<any>;
    productCount?: number;
    likeCount?: number;
    commentCount?: number;
    addTime?: string;
    buyerName?: string;
    buyerPhoto?: string;
    status?: number;
    isShow?: number;
    imgList?: Array<ImgListItem>;
    buyerShowVideoList?: Array<any>;
    sortOrder?: number;
}


export interface buyerShowListFilterState {
    buyerShowId?: number;
    content?: string;
    groupList?: Array<any>;
    productCount?: number;
    likeCount?: number;
    commentCount?: number;
    addTime?: string;
    buyerName?: string;
    status?: number;
    isShow?: number;
    imgList?: Array<ImgListItem>;
    videoList?: Array<any>;
    sort?: number;
}

// 获取列表返回参数类型
export interface buyerShowListFilterResult {
    records: buyerShowListFilterState[];
    filter: {
        page: number;
    };
    total: number;
}



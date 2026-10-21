// 列表查询时筛选参数类型
export interface UserMsgFilterParams {
    page: number;
    size: number;
    unread: number;
}

export interface UserMsgFilterResult {
    data: UserMsgData;
    code: number;
    message: string;
}

export interface UserMsgData {
    records: UserMsgFilterState[];
    total: number;
}

import type { OrderListFilterResult } from "@/types/user/order";

export interface UserMsgFilterState {
    messageId: number;
    title: string;
    messageLogId: number;
    userId: number;
    userRank: number;
    isRead: number;
    content: string;
    link: Link;
    addTime: string;
    addTimeDate: string;
    addTimeHms: string;
    orderInfo?: OrderListFilterResult;
}

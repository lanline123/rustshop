import type { SendUserTarget } from "@/types/user/memberAudience";

// 列表查询时筛选参数类型
export interface MessageLogFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
}

// 获取列表返回参数类型
export interface MessageLogFilterState {
    sendTypeName: string;
	statusName: string;
	messageLogId: number;
	messageType: number;
	sendUserType: number;
	userIds: string;
	userRank: number;
	messageTitle: string;
	messageContent: string;
	messageLink: string;
	sendTime: string;
	isRecall: number;
}
export interface MessageLogFilterResult {
    records: MessageLogFilterState[];
    filter: MessageLogFilterParams;
    total: number;
}
export interface MessageLogFormResult {
    item: MessageLogFormState;
}

// 获取详情返回参数类型
export interface MessageLogFormState {
    messageLogId?: number;
    messageTitle?: string;
    messageContent?: string;
    messageLink?: Object;
    userIds?: string;
    userRank?: number;
    userList?: string;
    sendUserType?: number;
    /** 发送会员范围（指定会员/会员标签/会员等级三者互斥，只能选其中一种） */
    sendUserTarget?: SendUserTarget;
}




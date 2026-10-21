// 视频号账号列表筛选参数
export interface WechatVideoAccountFilterParams {
    page: number;
    size: number;
    finderId?: string;
    accountStatus?: number;
    relationTimeStart?: string;
    relationTimeEnd?: string;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
}

// 列表返回
export interface WechatVideoAccountFilterResult {
    records: WechatVideoAccountState[];
    total: number;
}

// 视频号账号列表项
export interface WechatVideoAccountState {
    id: number;
    finderId: string;
    nickName: string;
    relationTime: string;
    accountStatus?: number | null;
    accountStatusText: string;
    statusType: "normal" | "warning" | "danger" | "info";
    cover?: string;
    productImage?: string;
}

// 详情/表单数据
export interface WechatVideoAccountFormState {
    id: number;
    finderId: string;
    nickName: string;
}

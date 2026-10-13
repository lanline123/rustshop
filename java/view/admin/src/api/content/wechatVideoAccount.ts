import request from "@/utils/request";
import type {
    WechatVideoAccountFormState,
    WechatVideoAccountFilterParams,
    WechatVideoAccountFilterResult,
    WechatVideoAccountState
} from "@/types/content/wechatVideoAccount.d";

const accountStatusMap: Record<number, { text: string; type: WechatVideoAccountState["statusType"] }> = {
    0: { text: "草稿", type: "info" },
    1: { text: "预告中", type: "warning" },
    2: { text: "直播中", type: "normal" },
    3: { text: "已结束", type: "info" },
    4: { text: "禁播", type: "danger" },
    5: { text: "暂停", type: "warning" },
    6: { text: "异常", type: "danger" },
    7: { text: "已过期", type: "info" }
};

const normalizeWechatVideoAccount = (item: any): WechatVideoAccountState => {
    const accountStatus = item.accountStatus !== undefined && item.accountStatus !== null ? Number(item.accountStatus) : null;
    const statusMeta =
        accountStatus !== null
            ? accountStatusMap[accountStatus] || { text: "未知", type: "info" as const }
            : { text: "待同步", type: "info" as const };

    return {
        id: Number(item.id || 0),
        finderId: item.finderId || "",
        nickName: item.nickName || "",
        relationTime: item.relationTime || "",
        accountStatus,
        accountStatusText: item.accountStatusText || statusMeta.text,
        statusType: statusMeta.type
    };
};

/**
 * 获取视频号账号列表
 */
export const getWechatVideoAccountList = (params: WechatVideoAccountFilterParams) => {
    return request<any>({
        url: "content/wechatVideoAccount/list",
        method: "get",
        params: {
            page: params.page,
            size: params.size,
            finderId: params.finderId || params.keyword,
            accountStatus: params.accountStatus,
            relationTimeStart: params.relationTimeStart,
            relationTimeEnd: params.relationTimeEnd,
            sortField: params.sortField,
            sortOrder: params.sortOrder,
            keyword: params.keyword
        }
    }).then((result) => {
        return {
            records: Array.isArray(result.records) ? result.records.map(normalizeWechatVideoAccount) : [],
            total: Number(result.total || 0)
        } as WechatVideoAccountFilterResult;
    });
};

/**
 * 获取视频号账号详情
 */
export const getWechatVideoAccountDetail = (params: { id: number }) => {
    return request<any>({
        url: "content/wechatVideoAccount/detail",
        method: "get",
        params
    }).then((result) => {
        const normalized = normalizeWechatVideoAccount(result);
        return {
            id: normalized.id,
            finderId: normalized.finderId,
            nickName: normalized.nickName
        } as WechatVideoAccountFormState;
    });
};

/**
 * 创建/更新视频号账号
 */
export const updateWechatVideoAccount = (operation: "create" | "update", data: Partial<WechatVideoAccountFormState>) => {
    return request({
        url: `content/wechatVideoAccount/${operation}`,
        method: "post",
        data
    });
};

/**
 * 删除视频号账号
 */
export const delWechatVideoAccount = (data: { id: number }) => {
    return request({
        url: "content/wechatVideoAccount/delete",
        method: "post",
        data
    });
};

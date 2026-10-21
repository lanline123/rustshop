import request from "@/utils/request";
import type { UserFilterParams, UserFilterResult } from "@/types/user/user";

/**
 * 代客下单-获取会员列表
 * @description 分页获取会员数据，支持关键字搜索（名称/手机号）
 * @param params - 筛选和分页参数（与 getUserList 保持一致）
 * @returns 会员列表
 */
export const getProxyShoppingUserList = (params: UserFilterParams) => {
    return request<UserFilterResult>({
        url: "proxyShopping/user/list",
        method: "get",
        params
    });
};



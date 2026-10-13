import request from "@/utils/request";
import type {UserBalanceLogFilterParams, UserBalanceLogFilterResult} from "@/types/finance/userBalanceLog.d"

/**
 * 获取用户余额日志列表
 * @description 分页获取用户余额变动记录
 * @param params - 筛选和分页参数
 * @returns 余额日志列表
 */
export const getUserBalanceLogList = (params: UserBalanceLogFilterParams) => {
    return request<UserBalanceLogFilterResult>({
        url: "finance/userBalanceLog/list",
        method: "get",
        params,
    });
}

import request from "@/utils/request";
import type {UserRechargeOrderFilterParams, UserRechargeOrderFilterResult, UserRechargeOrderFormState} from "@/types/finance/userRechargeOrder.d"

/**
 * 获取用户充值订单列表
 * @description 分页获取用户充值订单数据
 * @param params - 筛选和分页参数
 * @returns 充值订单列表
 */
export const getUserRechargeOrderList = (params: UserRechargeOrderFilterParams) => {
    return request<UserRechargeOrderFilterResult>({
        url: "finance/userRechargeOrder/list",
        method: "get",
        params,
    });
}

/**
 * 删除充值订单
 * @description 删除指定充值订单记录
 * @param data - 订单ID
 * @returns 操作结果
 */
export const delUserRechargeOrder= (data: object) => {
    return request({
        url: "finance/userRechargeOrder/del",
        method: "post",
        data,
    });
}

/**
 * 撤回充值订单
 * @description 撤回指定充值订单
 * @param data - 订单ID
 * @returns 操作结果
 */
export const recallUserRechargeOrder= (data: object) => {
    return request({
        url: "finance/userRechargeOrder/recall",
        method: "post",
        data,
    });
}
/**
 * 获取充值订单详情
 * @description 获取充值订单的完整信息
 * @param action - 操作类型
 * @param params - 订单ID等参数
 * @returns 充值订单详情
 */
export const getUserRechargeOrder  = (action: string, params: object) => {
    return request<UserRechargeOrderFormState>({
        url: "finance/userRechargeOrder/" + action,
        method: "get",
        params
    });
}

/**
 * 更新充值订单
 * @description 更新充值订单状态
 * @param operation - 操作类型
 * @param data - 更新数据
 * @returns 操作结果
 */
export const updateUserRechargeOrder = (operation: string, data: object) => {
    return request({
        url: "finance/userRechargeOrder/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作充值订单
 * @description 批量处理充值订单
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/userRechargeOrder/batch",
        method: "post",
        data: {type, ...data},
    });
}

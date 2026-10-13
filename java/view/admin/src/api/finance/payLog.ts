import request from "@/utils/request";
import type {PayLogFilterParams, PayLogFilterResult, PayLogFormResult} from "@/types/finance/payLog.d"

/**
 * 获取支付日志列表
 * @description 分页获取支付流水记录
 * @param params - 筛选和分页参数
 * @returns 支付日志列表
 */
export const getPayLogList = (params: PayLogFilterParams) => {
    return request<PayLogFilterResult>({
        url: "finance/payLog/list",
        method: "get",
        params,
    });
}

/**
 * 删除支付日志
 * @description 删除指定支付日志记录
 * @param data - 日志ID
 * @returns 操作结果
 */
export const delPayLog= (data: object) => {
    return request({
        url: "finance/payLog/del",
        method: "post",
        data,
    });
}

/**
 * 撤回支付日志
 * @description 撤回支付记录
 * @param data - 日志ID
 * @returns 操作结果
 */
export const recallPayLog= (data: object) => {
    return request({
        url: "finance/payLog/recall",
        method: "post",
        data,
    });
}
/**
 * 获取支付日志详情
 * @description 获取支付日志的完整信息
 * @param action - 操作类型
 * @param params - 日志ID等参数
 * @returns 支付日志详情
 */
export const getPayLog  = (action: string, params: object) => {
    return request<PayLogFormResult>({
        url: "finance/payLog/" + action,
        method: "get",
        params
    });
}

/**
 * 更新支付日志
 * @description 创建或更新支付日志
 * @param operation - 操作类型
 * @param data - 日志数据
 * @returns 操作结果
 */
export const updatePayLog = (operation: string, data: object) => {
    return request({
        url: "finance/payLog/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作支付日志
 * @description 批量处理支付日志
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/payLog/batch",
        method: "post",
        data: {type, ...data},
    });
}

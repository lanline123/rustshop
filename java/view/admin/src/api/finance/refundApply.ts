import request from "@/utils/request";
import type {RefundApplyFilterParams, RefundApplyFilterResult, RefundApplyFormState} from "@/types/finance/refundApply.d"

/**
 * 获取退款申请列表
 * @description 分页获取退款申请数据
 * @param params - 筛选和分页参数
 * @returns 退款申请列表
 */
export const getRefundApplyList = (params: RefundApplyFilterParams) => {
    return request<RefundApplyFilterResult>({
        url: "finance/refundApply/list",
        method: "get",
        params,
    });
}

/**
 * 获取退款申请配置
 * @description 获取退款申请页面的配置项
 * @returns 配置信息
 */
export const getRefundApplyConfig = () => {
    return request<any>({
        url: "finance/refundApply/config",
        method: "get"
    });
}

/**
 * 删除退款申请
 * @description 删除指定的退款申请记录
 * @param data - 退款申请ID
 * @returns 操作结果
 */
export const delRefundApply= (data: object) => {
    return request({
        url: "finance/refundApply/del",
        method: "post",
        data,
    });
}


/**
 * 获取退款申请详情
 * @description 获取退款申请的完整信息
 * @param action - 操作类型
 * @param params - 申请ID等参数
 * @returns 退款申请详情
 */
export const getRefundApply  = (action: string, params: object) => {
    return request<RefundApplyFormState>({
        url: "finance/refundApply/" + action,
        method: "get",
        params
    });
}

/**
 * 审核退款申请
 * @description 审核并处理退款申请
 * @param data - 审核数据
 * @returns 操作结果
 */
export const updateRefundApply = (data: object) => {
    return request({
        url: "finance/refundApply/audit",
        method: "post",
        data
    });
}

/**
 * 批量操作退款申请
 * @description 批量处理退款申请
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/refundApply/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 确认线下转账
 * @description 确认线下退款转账已完成
 * @param data - 转账确认数据
 * @returns 操作结果
 */
export const updateOfflineAudit = (data: object) => {
    return request({
        url: "finance/refundApply/offlineAudit",
        method: "post",
        data
    });
}
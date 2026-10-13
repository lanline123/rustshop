import request from "@/utils/request";
import type {OrderInvoiceFilterParams, OrderInvoiceFilterResult, OrderInvoiceFormState} from "@/types/finance/orderInvoice.d"

/**
 * 获取订单发票列表
 * @description 分页获取订单发票申请数据
 * @param params - 筛选和分页参数
 * @returns 发票列表
 */
export const getOrderInvoiceList = (params: OrderInvoiceFilterParams) => {
    return request<OrderInvoiceFilterResult>({
        url: "finance/orderInvoice/list",
        method: "get",
        params,
    });
}

/**
 * 删除订单发票
 * @description 删除指定订单发票记录
 * @param data - 发票ID
 * @returns 操作结果
 */
export const delOrderInvoice= (data: object) => {
    return request({
        url: "finance/orderInvoice/del",
        method: "post",
        data,
    });
}

/**
 * 撤回订单发票
 * @description 撤回指定订单发票
 * @param data - 发票ID
 * @returns 操作结果
 */
export const recallOrderInvoice= (data: object) => {
    return request({
        url: "finance/orderInvoice/recall",
        method: "post",
        data,
    });
}
/**
 * 获取订单发票详情
 * @description 获取订单发票的完整信息
 * @param action - 操作类型
 * @param params - 发票ID等参数
 * @returns 发票详情
 */
export const getOrderInvoice  = (action: string, params: object) => {
    return request<OrderInvoiceFormState>({
        url: "finance/orderInvoice/" + action,
        method: "get",
        params
    });
}

/**
 * 更新订单发票
 * @description 处理订单发票（开票/作废等）
 * @param operation - 操作类型
 * @param data - 更新数据
 * @returns 操作结果
 */
export const updateOrderInvoice = (operation: string, data: object) => {
    return request({
        url: "finance/orderInvoice/" + operation,
        method: "post",
        data
    });
}

/**
 * 批量操作订单发票
 * @description 批量处理订单发票
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/orderInvoice/batch",
        method: "post",
        data: {type, ...data},
    });
}

import request from "@/utils/request";
import {AftersalesFilterParams, AftersalesFilterResult, FormState} from "@/types/order/aftersales"

/**
 * 获取售后申请列表
 * @description 分页获取售后服务申请数据
 * @param params - 筛选和分页参数
 * @returns 售后申请列表
 */
export const getAftersalesList = (params: AftersalesFilterParams) => {
    return request<AftersalesFilterResult>({
        url: "order/aftersales/list",
        method: "get",
        params,
    });
}
/**
 * 获取售后申请类型
 * @description 获取售后申请类型列表（退款/退货/换货）
 * @returns 申请类型列表
 */
export const getAftersalesApplyType = () => {
    return request<any>({
        url: "order/aftersales/applyType",
        method: "get"
    });
}

/**
 * 获取退换货状态
 * @description 获取退换货状态列表
 * @returns 状态列表
 */
export const getAftersalesReturnStatus = () => {
    return request<any>({
        url: "order/aftersales/returnGoodsStatus",
        method: "get"
    });
}

/**
 * 获取售后详情
 * @description 获取售后申请的完整信息
 * @param action - 操作类型
 * @param params - 售后ID等参数
 * @returns 售后详情
 */
export const getAftersales  = (action: string, params: object) => {
    return request<FormState>({
        url: "order/aftersales/" + action,
        method: "get",
        params
    });
}

/**
 * 更新售后状态
 * @description 处理售后申请（同意/拒绝等）
 * @param data - 更新数据
 * @returns 操作结果
 */
export const updateAftersales = ( data: object) => {
    return request({
        url: "order/aftersales/update",
        method: "post",
        data
    });
}
/**
 * 批量操作售后
 * @description 批量处理售后申请
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "order/aftersales/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 更新售后字段
 * @description 更新售后的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateAftersalesField = (data:object) => {
    return request({
        url: "order/aftersales/updateField",
        method: "post",
        data
    });
}

/**
 * 发布售后留言
 * @description 在售后申请中添加留言记录
 * @param data - 留言内容
 * @returns 操作结果
 */
export const addMessage = (data:object) => {
    return request({
        url: "order/aftersales/record",
        method: "post",
        data
    });
}

/**
 * 确认收货
 * @description 确认收到用户退回的商品
 * @param data - 确认数据
 * @returns 操作结果
 */
export const updataConfirmReceipt = (data:object) => {
    return request({
        url: "order/aftersales/receive",
        method: "post",
        data
    });
}

/**
 * 关闭售后
 * @description 将审核未通过（status=3）的售后单置为已取消（status=7）
 * @remark 订单售后流程已移除该入口（审核未通过即终态），当前仅 POS 门店退货在用
 * @param data - 关闭数据
 * @returns 操作结果
 */
export const completeAftersales = (data:object) => {
    return request({
        url: "order/aftersales/complete",
        method: "post",
        data
    });
}

/**
 * 商家拒收
 * @description 售后退货场景下，商家拒收退回商品（售后终止且不发起退款）
 * @param data - 拒收数据（id、拒收理由 reply）
 * @returns 操作结果
 */
export const rejectReceive = (data: object) => {
    return request({
        url: "order/aftersales/rejectReceive",
        method: "post",
        data
    });
}
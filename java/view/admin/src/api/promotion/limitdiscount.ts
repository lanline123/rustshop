import request from "@/utils/request";
import { LimitdiscountFilterResult, LimitdiscountFormResult } from "@/types/promotion/limitdiscount.d";

/**
 * 获取限时折扣列表
 * @description 分页获取限时折扣活动数据
 * @param params - 筛选和分页参数
 * @returns 限时折扣列表
 */
export const getLimitdiscountList = (params: object) => {
    return request<LimitdiscountFilterResult>({
        url: "promotion/timeDiscount/list",
        method: "get",
        params,
    });
}
/**
 * 批量操作限时折扣
 * @description 批量处理限时折扣活动
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/timeDiscount/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 删除限时折扣
 * @description 删除指定限时折扣活动
 * @param data - 活动ID
 * @returns 操作结果
 */
export const delLimitdiscount = (data: object) => {
    return request({
        url: "promotion/timeDiscount/del",
        method: "post",
        data,
    });
}
/**
 * 更新限时折扣字段
 * @description 更新限时折扣的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateLimitdiscountFiled = (data: object) => {
    return request({
        url: "promotion/timeDiscount/updateField",
        method: "post",
        data,
    });
}

/**
 * 获取限时折扣详情
 * @description 获取限时折扣活动的完整信息
 * @param action - 操作类型
 * @param params - 活动ID等参数
 * @returns 限时折扣详情
 */
export const getLimitdiscount = (action: string, params: object) => {
    return request<LimitdiscountFormResult>({
        url: "promotion/timeDiscount/" + action,
        method: "get",
        params
    });
}
/**
 * 更新限时折扣
 * @description 创建或更新限时折扣活动
 * @param operation - 操作类型（create/update）
 * @param data - 活动数据
 * @returns 操作结果
 */
export const updateLimitdiscount = (operation: string, data: object) => {
    return request({
        url: "promotion/timeDiscount/" + operation,
        method: "post",
        data
    });
}

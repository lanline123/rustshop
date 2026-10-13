import request from "@/utils/request";
import { PointsExchangeFilterResult, PointsExchangeFormState } from "@/types/promotion/pointsExchange.d";

/**
 * 获取积分兑换商品列表
 * @description 分页获取积分兑换商品数据
 * @param params - 筛选和分页参数
 * @returns 积分兑换商品列表
 */
export const getPointsExchangeList = (params: object) => {
    return request<PointsExchangeFilterResult>({
        url: "promotion/pointsExchange/list",
        method: "get",
        params,
    });
}
/**
 * 批量操作积分兑换商品
 * @description 批量处理积分兑换商品
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/pointsExchange/batch",
        method: "post",
        data: {type, ...data},
    });
}

/**
 * 删除积分兑换商品
 * @description 删除指定积分兑换商品
 * @param data - 商品ID
 * @returns 操作结果
 */
export const delPointsExchange = (data: object) => {
    return request({
        url: "promotion/pointsExchange/del",
        method: "post",
        data,
    });
}
/**
 * 更新积分兑换商品字段
 * @description 更新积分兑换商品的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updatePointsExchangeFiled = (data: object) => {
    return request({
        url: "promotion/pointsExchange/updateField",
        method: "post",
        data,
    });
}

/**
 * 获取积分兑换商品详情
 * @description 获取积分兑换商品的完整信息
 * @param action - 操作类型
 * @param params - 商品ID等参数
 * @returns 商品详情
 */
export const getPointsExchange = (action: string, params: object) => {
    return request<PointsExchangeFormState>({
        url: "promotion/pointsExchange/" + action,
        method: "get",
        params
    });
}
/**
 * 更新积分兑换商品
 * @description 创建或更新积分兑换商品
 * @param operation - 操作类型（create/update）
 * @param data - 商品数据
 * @returns 操作结果
 */
export const updatePointsExchange = (operation: string, data: object) => {
    return request({
        url: "promotion/pointsExchange/" + operation,
        method: "post",
        data
    });
}

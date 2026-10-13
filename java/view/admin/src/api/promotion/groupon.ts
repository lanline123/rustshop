import request from "@/utils/request";
import {
    GrouponFilterParams,
    GrouponFilterResult,
    GrouponFormState,
    GrouponItemFilterParams,
    GrouponItemFilterResult,
    GrouponItemProductFilterParams,
    GrouponItemProductFilterResult,
    GrouponRecordGroupMemberFilterParams,
    GrouponRecordGroupMemberFilterResult,
    GrouponRecordOpenJoinFilterParams,
    GrouponRecordOpenJoinFilterResult,
    GrouponRecordOrderFilterParams,
    GrouponRecordOrderFilterResult,
    GrouponItemBatchParams,
    GrouponItemFormState,
    GrouponRecordGroupMemberFilterState
} from "@/types/promotion/groupon";

/**
 * 获取拼团活动列表
 * @description 分页获取拼团活动数据
 * @param params - 筛选和分页参数
 * @returns 拼团活动列表
 */
export const getGrouponList = (params: GrouponFilterParams) => {
    return request<GrouponFilterResult>({
        url: "promotion/groupon/list",
        method: "get",
        params,
    });
}

/**
 * 获取拼团活动详情
 * @description 获取拼团活动的完整信息
 * @param action - 操作类型
 * @param params - 活动ID等参数
 * @returns 拼团活动详情
 */
export const getGroupon = (action: string, params: object) => {
    return request<GrouponFormState>({
        url: "promotion/groupon/" + action,
        method: "get",
        params
    });
};

/**
 * 获取商品推广数据
 * @param params 
 * @returns 
 */
export const getGrouponPromoteData = (params: { grouponPromotionId: number }) => {
    return request<any>({
        url: "promotion/groupon/promotionPromote",
        method: "get",
        params
    });
};

/**
 * 更新拼团活动
 * @description 创建或更新拼团活动
 * @param operation - 操作类型（create/update）
 * @param data - 活动数据
 * @returns 操作结果
 */
export const updateGroupon = (operation: string, data: object) => {
    return request({
        url: "promotion/groupon/" + operation,
        method: "post",
        data
    });
}

/**
 * 结束拼团活动
 * @description 立即结束正在进行的拼团活动
 * @param data - 活动ID
 * @returns 操作结果
 */
export const finishGroupon = (data: object) => {
    return request({
        url: "promotion/groupon/finish",
        method: "post",
        data
    });
};

/**
 * 删除拼团活动
 * @description 删除指定拼团活动
 * @param data - 活动ID
 * @returns 操作结果
 */
export const delGroupon = (data: object) => {
    return request({
        url: "promotion/groupon/del",
        method: "post",
        data
    });
};

/**
 * 更新拼团活动商品
 * @description 创建或更新拼团活动中的商品
 * @param operation - 操作类型（create/update）
 * @param data - 商品数据
 * @returns 操作结果
 */
export const updateGrouponItem = (operation: string, data: object) => {
    return request({
        url: "promotion/groupon/item/" + operation,
        method: "post",
        data
    });
}

/**
 * 获取拼团活动商品详情
 * @description 获取拼团活动商品的完整信息
 * @param action - 操作类型
 * @param params - 商品ID等参数
 * @returns 商品详情
 */
export const getGrouponItem = (action: string, params: object) => {
    return request<GrouponItemFormState>({
        url: "promotion/groupon/item/" + action,
        method: "get",
        params,
    });
};

/**
 * 删除拼团活动商品
 * @description 从拼团活动中移除商品
 * @param data - 商品ID
 * @returns 操作结果
 */
export const delGrouponItem = (data: object) => {
    return request({
        url: "promotion/groupon/item/del",
        method: "post",
        data
    });
};

/**
 * 获取拼团活动商品列表
 * @description 获取拼团活动中的商品列表
 * @param params - 筛选参数
 * @returns 商品列表
 */
export const getGrouponItemList = (params: GrouponItemFilterParams) => {
    return request<GrouponItemFilterResult>({
        url: "promotion/groupon/item/list",
        method: "get",
        params,
    });
};

/**
 * 批量操作拼团商品
 * @param data 批量操作参数
 * @returns 
 */
export const groupItemBatch = (data: GrouponItemBatchParams) => {
    return request({
        url: "promotion/groupon/item/batch",
        method: "post",
        data
    });
};

/**
 * 获取拼团商品列表
 * @description 获取可参与拼团的商品列表
 * @param params - 筛选参数
 * @returns 商品列表
 */
export const getGrouponItemProductList = (params: GrouponItemProductFilterParams) => {
    return request<GrouponItemProductFilterResult>({
        url: "promotion/groupon/item/productList",
        method: "get",
        params,
    });
};

/**
 * 获取拼团活动团员列表
 * @description 获取参与拼团的团员信息
 * @param params - 筛选参数
 * @returns 团员列表
 */
export const getGrouponRecordGroupMemberList = (params: GrouponRecordGroupMemberFilterParams) => {
    return request<GrouponRecordGroupMemberFilterState[]>({
        url: "promotion/groupon/record/groupMemberList",
        method: "get",
        params,
    });
};

/**
 * 获取开团/参团记录列表
 * @description 获取开团和参团的记录数据
 * @param params - 筛选参数
 * @returns 开团/参团记录列表
 */
export const getGrouponRecordOpenJoinList = (params: GrouponRecordOpenJoinFilterParams) => {
    return request<GrouponRecordOpenJoinFilterResult>({
        url: "promotion/groupon/record/openJoinList",
        method: "get",
        params,
    });
};

/**
 * 获取拼团订单记录列表
 * @description 获取拼团相关的订单记录
 * @param params - 筛选参数
 * @returns 订单记录列表
 */
export const getGrouponRecordOrderList = (params: GrouponRecordOrderFilterParams) => {
    return request<GrouponRecordOrderFilterResult>({
        url: "promotion/groupon/record/orderList",
        method: "get",
        params,
    });
};

/**
 * 手动成团
 * @description 管理员手动将拼团设为成团状态
 * @param grouponPromotionRecordId - 拼团记录ID
 * @returns 操作结果
 */
export const manualGroup = (grouponPromotionRecordId: number) => {
    return request({
        url: `promotion/groupon/record/manualGroup?grouponPromotionRecordId=${grouponPromotionRecordId}`,
        method: "post"
    });
};

/**
 * 批量成团
 * @description 批量将多个拼团设为成团状态
 * @param data - 拼团记录ID列表
 * @returns 操作结果
 */
export const batchGroup = (data: object) => {
    return request({
        url: "promotion/groupon/record/batchGroup",
        method: "post",
        data
    });
};
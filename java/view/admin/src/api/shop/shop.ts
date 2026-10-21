import request from "@/utils/request";
import type {ShopFilterResult, ShopFormResult, ShopFormState} from "@/types/shop/shop.d";

/**
 * 获取商家列表
 * @description 分页获取入驻商家数据
 * @param params - 筛选和分页参数
 * @returns 商家列表
 */
export const getShopList = (params: any) => {
    return request<ShopFilterResult>({
        url: "merchant/shop/list",
        method: "get",
        params,
    });
}
/**
 * 获取商家下拉列表
 * @description 获取商家下拉选项数据
 * @param params - 查询参数
 * @returns 商家下拉列表
 */
export const getShopSelectList = (params: object) => {
    return request<ShopFilterResult>({
        url: "merchant/shop/shopList",
        method: "get",
        params,
    });
}

/**
 * 批量操作商家
 * @description 批量处理商家（启用/禁用等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "merchant/shop/batch",
        method: "post",
        data: {type, ...data},
    });
}
/**
 * 删除商家
 * @description 删除指定商家
 * @param data - 商家ID
 * @returns 操作结果
 */
export const delShop = (data: object) => {
    return request({
        url: "merchant/shop/delShop",
        method: "post",
        data,
    });
}

/**
 * 更新商家字段
 * @description 更新商家的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateShopFiled = (data: object) => {
    return request({
        url: "merchant/shop/updateField",
        method: "post",
        data,
    });
}
/**
 * 获取商家详情
 * @description 获取商家的完整信息
 * @param action - 操作类型
 * @param params - 商家ID等参数
 * @returns 商家详情
 */
export const getShop = (action: string, params: object) => {
    return request<ShopFormState>({
        url: "merchant/shop/" + action,
        method: "get",
        params
    });
}

/**
 * 更新商家
 * @description 创建或更新商家信息
 * @param operation - 操作类型（create/update）
 * @param data - 商家数据
 * @returns 操作结果
 */
export const updateShop = (operation: string, data: object) => {
    return request({
        url: "merchant/shop/" + operation,
        method: "post",
        data
    });
}

/**
 * 获取店铺资金列表
 * @description 获取店铺资金账户数据
 * @param params - 筛选参数
 * @returns 资金列表
 */
export const getShopAccountList = (params: object) => {
    return request<ShopFilterResult>({
        url: "merchant/shopAccount/list",
        method: "get",
        params,
    });
}

/**
 * 获取店铺资金日志
 * @description 获取店铺资金变动记录
 * @param params - 筛选参数
 * @returns 资金日志列表
 */
export const getShopAccountLog = (params: object) => {
    return request<ShopFilterResult>({
        url: "merchant/shopAccount/logList",
        method: "get",
        params,
    });
}

/**
 * 获取商家结算设置
 * @description 获取商家结算相关配置
 * @param params - 商家ID
 * @returns 结算设置
 */
export const getShopSettlement = (params: object) => {
    return request<ShopFormState>({
        url: "order/config/detail",
        method: "get",
        params
    });
}

/**
 * 更新商家结算设置
 * @description 保存商家结算相关配置
 * @param data - 结算配置数据
 * @returns 操作结果
 */
export const updateShopSettlement = (data: object) => {
    return request({
        url: "order/config/save",
        method: "post",
        data
    });
}

/**
 * 检查是否平台分配商品
 * @description 判断商品是否由平台分配
 * @param params - 查询参数
 * @returns 是否平台分配
 */
export const getPlatformAllocation = (params: object) => {
    return request<any>({
        url: "merchant/shop/isPlatformAllocation",
        method: "get",
        params
    });
}

/**
 * 获取商品已分配门店
 * @description 根据商品ID获取已分配的门店列表
 * @param params - 商品ID
 * @returns 门店列表
 */
export const getStoreListByProductId = (params: object) => {
    return request<any>({
        url: "merchant/shop/getStoreListByProductId",
        method: "get",
        params
    });
}

/**
 * 获取首页秒杀活动
 * @description 获取首页展示的秒杀活动数据
 * @param params - 查询参数
 * @returns 秒杀活动数据
 */
export const getHomeSeckill = (params?: object) => {
    return request<any>({
        url: "home/home/getSeckill",
        method: "get",
        params
    });
};

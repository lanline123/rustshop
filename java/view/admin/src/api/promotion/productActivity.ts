import request from "@/utils/request";
import { ProductActivityFilterResult, ProductActivityFormResult, ProductActivityFormState } from "@/types/promotion/productActivity.d";
// 获取优惠活动列表
export const getProductActivityList = (params: object) => {
    return request<ProductActivityFilterResult>({
        url: "promotion/productPromotion/list",
        method: "get",
        params,
    });
}
// 获取优惠活动冲突列表
export const getProductConflict = (params: object) => {
    return request<ProductActivityFilterResult>({
        url: "promotion/productPromotion/conflict",
        method: "get",
        params,
    });
}
// 优惠活动列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/productPromotion/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 优惠活动列表页面删除项
export const delProductActivity = (data: object) => {
    return request({
        url: "promotion/productPromotion/del",
        method: "post",
        data,
    });
}
// 优惠活动列表页面更新项
export const updateProductActivityFiled = (data: object) => {
    return request({
        url: "promotion/productPromotion/updateField",
        method: "post",
        data,
    });
}
// 获取优惠活动详情
export const getProductActivity = (action: string, params: object) => {
    return request<ProductActivityFormState>({
        url: "promotion/productPromotion/" + action,
        method: "get",
        params
    });
}
// 获取优惠活动配置项
export const getProductActivityConfig = () => {
    return request<ProductActivityFormResult>({
        url: "promotion/productPromotion/config",
        method: "get",
    });
}
// 更新优惠活动
export const updateProductActivity = (operation: string, data: object) => {
    return request({
        url: "promotion/productPromotion/" + operation,
        method: "post",
        data
    });
}

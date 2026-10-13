import request from "@/utils/request";
import { ProductGiftFilterResult, ProductGiftFormState } from "@/types/promotion/productGift.d";
// 获取活动赠品列表
export const getProductGiftList = (params: object) => {
    return request<ProductGiftFilterResult>({
        url: "promotion/productGift/list",
        method: "get",
        params,
    });
}
// 活动赠品列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/productGift/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 活动赠品列表页面删除项
export const delProductGift = (data: object) => {
    return request({
        url: "promotion/productGift/del",
        method: "post",
        data,
    });
}
// 活动赠品列表页面更新项
export const updateProductGiftFiled = (data: object) => {
    return request({
        url: "promotion/productGift/updateField",
        method: "post",
        data,
    });
}
// 获取活动赠品详情
export const getProductGift = (action: string, params: object) => {
    return request<ProductGiftFormState>({
        url: "promotion/productGift/" + action,
        method: "get",
        params
    });
}
// 更新活动赠品
export const updateProductGift = (operation: string, data: object) => {
    return request({
        url: "promotion/productGift/" + operation,
        method: "post",
        data
    });
}

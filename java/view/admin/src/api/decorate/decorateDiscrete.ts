// 零散的装修页面模块
import request from "@/utils/request";
import { FormResult } from "@/types/decorate/decorateDiscrete";
// 获取装修详情
export const getDecorateDiscrete = (params: object) => {
    return request<any>({
        url: "decorate/decorateDiscrete/detail",
        method: "get",
        params
    });
}
// 获取我的页面默认菜单
export const getMenberDecorateData = () => {
    return request<any>({
        url: "decorate/decorateDiscrete/memberDecorateData",
        method: "get"
    });
}
// 更新装修
export const updateDecorateDiscrete = (data: object) => {
    return request({
        url: "decorate/decorateDiscrete/update",
        method: "post",
        data
    });
}

import request from "@/utils/request";
import { LiveFilterResult, LiveFormState } from "@/types/promotion/live.d";
// 获取直播列表
export const getLiveList = (params: object) => {
    return request<LiveFilterResult>({
        url: "promotion/wechatLive/list",
        method: "get",
        params,
    });
}
// 获取直播详情
export const getLive = (action: string, params: object) => {
    return request<LiveFormState>({
        url: "promotion/wechatLive/" + action,
        method: "get",
        params
    });
}
// 直播列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/wechatLive/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 直播列表页面更新项
export const updateLiveFiled = (data: object) => {
    return request({
        url: "promotion/wechatLive/updateField",
        method: "post",
        data,
    });
}
// 更新直播列表
export const updateLive = (operation: string, data: object) => {
    return request({
        url: "promotion/wechatLive/" + operation,
        method: "post",
        data
    });
}
// api更新直播间
export const getLiveRefresh = () => {
    return request<LiveFormState>({
        url: "promotion/wechatLive/refresh",
        method: "get"
    });
}
import request from "@/utils/request";
import {PushLogFilterParams, PushLogFilterResult, PushLogFormState} from "@/types/user/pushLog.d"
// 获取商品分类列表
export const getPushLogList = (params: PushLogFilterParams) => {
    return request<PushLogFilterResult>({
        url: "user/pushLog/list",
        method: "get",
        params,
    });
}

// 删除
export const delPushLog= (data: object) => {
    return request({
        url: "user/pushLog/del",
        method: "post",
        data,
    });
}

export const recallPushLog= (data: object) => {
    return request({
        url: "user/pushLog/recall",
        method: "post",
        data,
    });
}
//获取分类详情
export const getPushLog  = (action: string, params: object) => {
    return request<PushLogFormState>({
        url: "user/pushLog/" + action,
        method: "get",
        params
    });
}

// 更新分类
export const updatePushLog = (operation: string, data: object) => {
    return request({
        url: "user/pushLog/" + operation,
        method: "post",
        data
    });
}

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "user/pushLog/batch",
        method: "post",
        data: {type, ...data},
    });
}

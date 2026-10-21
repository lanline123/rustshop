import request from "@/utils/request";
import type {MobileCatNavFilterParams, MobileCatNavFilterResult,MobileCatNavFormState} from "@/types/decorate/mobileCatNav.d";
// 获取移动端首页分类栏
export const getMobileCatNavList = (params: MobileCatNavFilterParams) => {
    return request<MobileCatNavFilterResult>({
        url: "decorate/mobileCatNav/list",
        method: "get",
        params,
    });
}

// PC导航栏页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "decorate/mobileCatNav/batch",
        method: "post",
        data: {type, ...data},
    });
}

// PC导航栏页面更新项
export const updateMobileCatNavFiled = (data: object) => {
    return request({
        url: "decorate/mobileCatNav/updateField",
        method: "post",
        data,
    });
}

export const delMobileCatNav = (data: object) => {
    return request({
        url: "decorate/mobileCatNav/del",
        method: "post",
        data,
    });
}

export  const getMobileCatNavInfo = (action: string, params: object) => {
    return request<MobileCatNavFormState>({
        url: "decorate/mobileCatNav/" + action,
        method: "get",
        params
    });
}


export const updateMobileCatNav = (operation: string, data: object) => {
    return request({
        url: "decorate/mobileCatNav/" + operation,
        method: "post",
        data
    });
}

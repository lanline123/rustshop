import request from "@/utils/request";
import type { RequestList, RequestInfo } from "@/types/store/storeTipManage"

//标签列表
export const tipList = (params: RequestList) => {
    return request<any>({
        url: "o2o/tip/list",
        method: "get",
        params
    });
}

//查询所有标签
export const selectAllTip = (params: RequestList) => {
    return request<any>({
        url: "o2o/tip/selectAllTip",
        method: "get",
        params
    });
}

//标签配置
export const tipConfig = () => {
    return request<any>({
        url: "o2o/tip/tipConfig",
        method: "get"
    });
}

//标签新增编辑
export const updateTip = (operation: string, data: object) => {
    return request<RequestInfo>({
        url: "o2o/tip/" + operation,
        method: "post",
        data,
    });
}

//标签详情
export const getTip = (action: string, params: object) => {
    return request<RequestInfo>({
        url: "o2o/tip/" + action,
        method: "get",
        params
    });
}


// 删除标签
export const delTip = (data: object) => {
    return request({
        url: "o2o/tip/del",
        method: "post",
        data,
    });
}



// 页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "o2o/tip/batch",
        method: "post",
        data: {type, ...data},
    });
}


// 标签列表页面更新项
export const updateTipFiled = (data: object) => {
    return request({
        url: "o2o/tip/updateField",
        method: "post",
        data,
    });
};
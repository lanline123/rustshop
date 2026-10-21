import request from "@/utils/request";
import { FilterResult, FormResult } from "@/types/decorate/pcDecorate";
// 获取示例模板列表
export const getPcDecorateList = (params: object) => {
    return request<FilterResult>({
        url: "decorate/mobileDecorate/list",
        method: "get",
        params,
    });
};
// 示例模板列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "decorate/mobileDecorate/batch",
        method: "post",
        data: {type, ...data},
    });
};
// 示例模板列表页面删除项
export const delPcDecorate = (data: object) => {
    return request({
        url: "decorate/mobileDecorate/del",
        method: "post",
        data,
    });
};
// 示例模板列表页面更新项
export const updatePcDecorateFiled = (data: object) => {
    return request({
        url: "decorate/mobileDecorate/updateField",
        method: "post",
        data,
    });
};
// 获取示例模板详情
export const getPcDecorate = (action: string, params: object) => {
    return request<FormResult>({
        url: "decorate/mobileDecorate/" + action,
        method: "get",
        params,
    });
};
// 更新示例模板
export const updatePcDecorate = (operation: string, data: object) => {
    return request({
        url: "decorate/mobileDecorate/" + operation,
        method: "post",
        data,
    });
};

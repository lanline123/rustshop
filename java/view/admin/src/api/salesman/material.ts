import request from "@/utils/request";
import type {materialFilterResult, materialFormResult} from "@/types/salesman/material.d";
// 获取素材管理列表
export const getmaterialList = (params: object) => {
    return request<materialFilterResult>({
        url: "salesman/material/list",
        method: "get",
        params,
    });
}
// 素材管理页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/material/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 素材管理页面删除项
export const delmaterial = (data: object) => {
    return request({
        url: "salesman/material/del",
        method: "post",
        data,
    });
}
// 素材管理页面更新项
export const updatematerialFiled = (data: object) => {
    return request({
        url: "salesman/material/updateField",
        method: "post",
        data,
    });
}
// 获取素材管理详情
export const getmaterial = (action: string, params: object) => {
    return request<any>({
        url: "salesman/material/" + action,
        method: "get",
        params
    });
}
// 更新素材管理
export const updatematerial = (operation: string, data: object) => {
    return request({
        url: "salesman/material/" + operation,
        method: "post",
        data
    });
}
// 获取素材管理配置项
export const getmaterialConfig = () => {
    return request({
        url: "salesman/material/config",
        method: "get"
    });
}
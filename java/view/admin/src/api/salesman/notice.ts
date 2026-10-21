import request from "@/utils/request";
import type {noticeFilterResult, noticeFormState} from "@/types/salesman/notice.d";
// 获取内容管理列表
export const getnoticeList = (params: object) => {
    return request<noticeFilterResult>({
        url: "salesman/content/list",
        method: "get",
        params,
    });
}
// 内容管理页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/content/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 内容管理页面删除项
export const delnotice = (data: object) => {
    return request({
        url: "salesman/content/del",
        method: "post",
        data,
    });
}
// 内容管理页面更新项
export const updatenoticeFiled = (data: object) => {
    return request({
        url: "salesman/content/updateField",
        method: "post",
        data,
    });
}
// 获取内容管理详情
export const getnotice = (action: string, params: object) => {
    return request<noticeFormState[]>({
        url: "salesman/content/" + action,
        method: "get",
        params
    });
}
// 更新内容管理
export const updatenotice = (operation: string, data: object) => {
    return request({
        url: "salesman/content/" + operation,
        method: "post",
        data
    });
}
// 获取内容管理配置项
export const getnoticeConfig = () => {
    return request({
        url: "salesman/content/config",
        method: "get"
    });
}
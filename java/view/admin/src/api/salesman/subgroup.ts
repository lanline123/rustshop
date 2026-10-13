import request from "@/utils/request";
import type {groupFilterResult, groupFormState} from "@/types/salesman/subgroup.d";
// 获取分组标签列表
export const getgroupList = (params: object) => {
    return request<groupFilterResult>({
        url: "salesman/group/list",
        method: "get",
        params,
    });
}
// 分组标签页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/group/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 分组标签页面删除项
export const delgroup = (data: object) => {
    return request({
        url: "salesman/group/del",
        method: "post",
        data,
    });
}
// 分组标签页面更新项
export const updategroupFiled = (data: object) => {
    return request({
        url: "salesman/group/updateField",
        method: "post",
        data,
    });
}
// 获取分组标签详情
export const getgroup = (action: string, params: object) => {
    return request<groupFormState[]>({
        url: "salesman/group/" + action,
        method: "get",
        params
    });
}
// 更新分组标签
export const updategroup = (operation: string, data: object) => {
    return request({
        url: "salesman/group/" + operation,
        method: "post",
        data
    });
}
// 获取分组标签配置项
export const getgroupConfig = () => {
    return request({
        url: "salesman/group/config",
        method: "get"
    });
}
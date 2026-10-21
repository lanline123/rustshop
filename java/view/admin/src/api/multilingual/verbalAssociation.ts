import request from "@/utils/request";
import type { LocalesRelationFilterResult, LocalesRelationFormState } from "@/types/multilingual/verbalAssociation.d";
// 获取地区语言列表
export const getLocalesRelationList = (params: object) => {
    return request<LocalesRelationFilterResult>({
        url: "lang/localesRelation/list",
        method: "get",
        params
    });
};
// 获取地区语言配置项
export const getLocalesRelationConfig = () => {
    return request<any>({
        url: "lang/localesRelation/config",
        method: "get"
    });
};
// 地区语言列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "lang/localesRelation/batch",
        method: "post",
        data: {type, ...data}
    });
};
// 地区语言列表页面删除项
export const delLocalesRelation = (data: object) => {
    return request({
        url: "lang/localesRelation/del",
        method: "post",
        data
    });
};
// 地区语言列表页面更新项
export const updateLocalesRelationFiled = (data: object) => {
    return request({
        url: "lang/localesRelation/updateField",
        method: "post",
        data
    });
};
// 获取地区语言详情
export const getLocalesRelation = (action: string, params: object) => {
    return request<LocalesRelationFormState>({
        url: "lang/localesRelation/" + action,
        method: "get",
        params
    });
};
// 更新地区语言
export const updateLocalesRelation = (operation: "create" | "update", data: object) => {
    return request({
        url: "lang/localesRelation/" + operation,
        method: "post",
        data
    });
};

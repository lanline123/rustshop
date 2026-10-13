import request from "@/utils/request";
import type { LocalesFilterResult, LocalesFormState } from "@/types/multilingual/languagesList.d";
// 获取地区语言列表
export const getLocalesList = (params: object) => {
    return request<LocalesFilterResult>({
        url: "lang/locales/list",
        method: "get",
        params
    });
};
// 地区语言列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "lang/locales/batch",
        method: "post",
        data: {type, ...data}
    });
};
// 地区语言列表页面删除项
export const delLocales = (data: object) => {
    return request({
        url: "lang/locales/del",
        method: "post",
        data
    });
};
// 地区语言列表页面更新项
export const updateLocalesFiled = (data: object) => {
    return request({
        url: "lang/locales/updateField",
        method: "post",
        data
    });
};
// 获取地区语言详情
export const getLocales = (action: string, params: object) => {
    return request<LocalesFormState>({
        url: "lang/locales/" + action,
        method: "get",
        params
    });
};
// 更新地区语言
export const updateLocales = (operation: "create" | "update", data: object) => {
    return request({
        url: "lang/locales/" + operation,
        method: "post",
        data
    });
};

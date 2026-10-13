import request from "@/utils/request";
import { ModulesType, EditResult } from "@/types/decorate/decorate.d";

/**
 * 加载草稿数据
 * @description 获取未完成的装修编辑草稿
 * @param id - 装修ID
 * @returns 草稿数据
 */
export const loadDraftData = (id: number) => {
    return request<any>({
        url: "decorate/decorate/loadDraftData",
        method: "get",
        params: {
            id,
        },
    });
};

/**
 * 保存草稿
 * @description 保存装修编辑的草稿数据
 * @param data - 草稿数据
 * @returns 保存结果
 */
export const saveDraft = (data: object) => {
    return request({
        url: "decorate/decorate/saveDraft",
        method: "post",
        data,
    });
};

/**
 * 获取装修详情
 * @description 获取页面装修的配置详情
 * @param params - 装修ID等参数
 * @returns 装修配置详情
 */
export const decorateDetail = (params: object) => {
    return request<EditResult>({
        url: "decorate/decorate/detail",
        method: "get",
        params,
    });
};

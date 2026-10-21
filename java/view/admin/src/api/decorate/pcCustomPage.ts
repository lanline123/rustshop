import request from "@/utils/request";
import type { FilterParams, FilterResult, PcCustomPageFormState, BindH5DecorateFormState, CancelBindingFormState } from "@/types/decorate/pcCustomPage";

export const getH5DecorateList = (params: FilterParams) => {
    return request<FilterResult>({
        url: "decorate/decorate/getH5DecorateList",
        method: "get",
        params
    });
};

export const getCustomDecorateList = (params: FilterParams) => {
    return request<FilterResult>({
        url: "decorate/decorate/getCustomDecorateList",
        method: "get",
        params
    });
};


// 更新示例模板
export const updatePcCustomDecorate = (data: PcCustomPageFormState) => {
    return request({
        url: "decorate/decorate/updateCustomDecorate",
        method: "post",
        data
    });
};

/**
 * 绑定H5页面
 * @param data 
 * @returns 
 */
export const bindingH5Decorate = (data: BindH5DecorateFormState) => {
    return request({
        url: "decorate/decorate/bindingH5Decorate",
        method: "post",
        data
    });
};


/**
 * 绑定自定义页面
 * @param data 
 * @returns 
 */
export const bindingCustomDecorate = (data: BindH5DecorateFormState) => {
    return request({
        url: "decorate/decorate/bindingCustomDecorate",
        method: "post",
        data
    });
};

/**
 * 取消绑定H5页面
 * @param data 
 * @returns 
 */
export const cancelBinding = (data: CancelBindingFormState) => {
    return request({
        url: "decorate/decorate/cancelBinding",
        method: "post",
        data
    });
};

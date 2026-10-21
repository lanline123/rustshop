import request from "@/utils/request";
import type {PcNavigationFilterParams, PcNavigationFilterResult, PcNavigationFormState, LinkFilterState,PcNavigationFilterState} from "@/types/decorate/pcNavigation";
// 获取PC导航栏列表
export const getPcNavigationList = (params: PcNavigationFilterParams) => {
    return request<PcNavigationFilterResult>({
        url: "decorate/pcNavigation/list",
        method: "get",
        params,
    });
}

// PC导航栏页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "decorate/pcNavigation/batch",
        method: "post",
        data: {type, ...data},
    });
}

// PC导航栏页面更新项
export const updatePcNavigationFiled = (data: object) => {
    return request({
        url: "decorate/pcNavigation/updateField",
        method: "post",
        data,
    });
}

export const delPcNavigation = (data: object) => {
    return request({
        url: "decorate/pcNavigation/del",
        method: "post",
        data,
    });
}

export  const getPcNavigation = (action: string, params: object) => {
    return request<PcNavigationFormState>({
        url: "decorate/pcNavigation/" + action,
        method: "get",
        params
    });
}

export  const getParentNav = (params: object) => {
    return request<PcNavigationFilterState[]>({
        url: "decorate/pcNavigation/getParentNav",
        method: "get",
        params
    });
}

export const updatePcNavigation = (operation: string, data: object) => {
    return request({
        url: "decorate/pcNavigation/" + operation,
        method: "post",
        data
    });
}

export const updatePcNavigationField = (data:object) => {
    return request({
        url: "decorate/pcNavigation/updateField",
        method: "post",
        data
    });
}

export const getSelectLinkList = () => {
    return request<LinkFilterState[]>({
        url: "decorate/pcNavigation/selectLink",
        method: "get"
    });
}

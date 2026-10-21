import request from "@/utils/request";
import type {
    LogisticsCompanyFilterParams,
    LogisticsCompanyFilterResult,
    LogisticsCompanyFormState,
    LogisticsCompanySyncResult
} from "@/types/setting/logisticsCompany";


export const getLogisticsCompanyList = (params: LogisticsCompanyFilterParams) => {
    return request<LogisticsCompanyFilterResult>({
        url: "setting/logisticsCompany/list",
        method: "get",
        params,
    });
};
export const getLogisticsCompanyAll = (params: object) => {
    return request<LogisticsCompanyFilterResult>({
        url: "setting/logisticsCompany/list",
        method: "get",
        params,
    });
};

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/logisticsCompany/batch",
        method: "post",
        data: {type, ...data},
    });
};

// 商品分类页面更新项
export const updateLogisticsCompanyFiled = (data: object) => {
    return request({
        url: "setting/logisticsCompany/updateField",
        method: "post",
        data,
    });
};

export const delLogisticsCompany = (data: object) => {
    return request({
        url: "setting/logisticsCompany/del",
        method: "post",
        data,
    });
};

export const getLogisticsCompany = (action: string, params: object) => {
    return request<LogisticsCompanyFormState>({
        url: "setting/logisticsCompany/" + action,
        method: "get",
        params
    });
};

export const updateLogisticsCompany = (operation: string, data: object) => {
    return request({
        url: "setting/logisticsCompany/" + operation,
        method: "post",
        data
    });
};
export const updateLogisticsCompanyField = (data: object) => {
    return request({
        url: "setting/logisticsCompany/updateField",
        method: "post",
        data
    });
};

// 同步快递鸟数据
export const syncKdniaoData = (data: object) => {
    return request<LogisticsCompanySyncResult>({
        url: "setting/logisticsCompany/syncKdn",
        method: "post",
        data
    });
};

// 同步快递100数据，overwriteName 同码时覆盖名称，defaultIsEnabled 新增记录默认启用
export const syncKuaidi100Data = (params: object) => {
    return request<LogisticsCompanySyncResult>({
        url: "setting/logisticsCompany/syncKuaidi100",
        method: "post",
        params
    });
};
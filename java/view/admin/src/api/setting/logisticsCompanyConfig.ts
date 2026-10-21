import request from "@/utils/request";
import type {
    LogisticsCompanyConfigFilterParams,
    LogisticsCompanyConfigFilterResult,
    LogisticsCompanyConfigFormState,
} from "@/types/setting/logisticsCompanyConfig";

export const getLogisticsCompanyConfigList = (params: LogisticsCompanyConfigFilterParams) => {
    return request<LogisticsCompanyConfigFilterResult>({
        url: "setting/logisticsCompanyConfig/list",
        method: "get",
        params,
    });
};

export const getLogisticsCompanyConfigAll = (params: object) => {
    return request<LogisticsCompanyConfigFilterResult>({
        url: "setting/logisticsCompanyConfig/list",
        method: "get",
        params,
    });
};

export const getLogisticsCompanyConfig = (action: string, params: object) => {
    return request<LogisticsCompanyConfigFormState>({
        url: "setting/logisticsCompanyConfig/" + action,
        method: "get",
        params,
    });
};

export const updateLogisticsCompanyConfig = (operation: string, data: object) => {
    return request({
        url: "setting/logisticsCompanyConfig/" + operation,
        method: "post",
        data,
    });
};

export const delLogisticsCompanyConfig = (data: object) => {
    return request({
        url: "setting/logisticsCompanyConfig/del",
        method: "post",
        data,
    });
};

export const setDefaultLogisticsCompanyConfig = (data: object) => {
    return request({
        url: "setting/logisticsCompanyConfig/setDefault",
        method: "post",
        params: data,
    });
};

export const updateLogisticsCompanyConfigField = (data: object) => {
    return request({
        url: "setting/logisticsCompanyConfig/updateField",
        method: "post",
        data,
    });
};

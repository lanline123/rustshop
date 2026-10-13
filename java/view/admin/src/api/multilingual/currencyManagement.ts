import request from "@/utils/request";
import type { CurrencyFilterResult, CurrencyFormState, ExchangeRateConfig, ExchangeRateSyncResult } from "@/types/multilingual/currencyManagement.d";
// 获取币种管理列表
export const getCurrencyList = (params: object) => {
    return request<CurrencyFilterResult>({
        url: "lang/currency/list",
        method: "get",
        params
    });
};
// 获取币种管理配置项
export const getCurrencyConfig = () => {
    return request<any>({
        url: "lang/currency/config",
        method: "get"
    });
};
// 币种管理列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "lang/currency/batch",
        method: "post",
        data: {type, ...data}
    });
};
// 币种管理列表页面删除项
export const delCurrency = (data: object) => {
    return request({
        url: "lang/currency/del",
        method: "post",
        data
    });
};
// 币种管理列表页面更新项
export const updateCurrencyFiled = (data: object) => {
    return request({
        url: "lang/currency/updateField",
        method: "post",
        data
    });
};
// 获取币种管理详情
export const getCurrency = (action: string, params: object) => {
    return request<CurrencyFormState>({
        url: "lang/currency/" + action,
        method: "get",
        params
    });
};
// 更新币种管理
export const updateCurrency = (operation: "create" | "update", data: object) => {
    return request({
        url: "lang/currency/" + operation,
        method: "post",
        data
    });
};
// 获取汇率自动更新配置（Open Exchange Rates，仅海外版）
export const getExchangeRateConfig = () => {
    return request<ExchangeRateConfig>({
        url: "lang/currency/exchangeRateConfig",
        method: "get"
    });
};
// 保存汇率自动更新配置
export const saveExchangeRateConfig = (data: object) => {
    return request({
        url: "lang/currency/saveExchangeRateConfig",
        method: "post",
        data
    });
};
// 立即更新汇率
export const syncExchangeRateNow = () => {
    return request<ExchangeRateSyncResult>({
        url: "lang/currency/syncExchangeRateNow",
        method: "post"
    });
};
// 翻译接口
export const updateTranslation = (data: object) => {
    return request<any>({
        url: "lang/currency/translation",
        method: "post",
        data
    });
};

// 翻译业务数据
export const updateCreateTranslation = (data: object) => {
    return request<any>({
        url: "lang/translations/createTranslations",
        method: "post",
        data
    });
};
// 业务翻译数据
export const getCreateTranslation = (data: object) => {
    return request<any>({
        url: "lang/translations/getTranslations",
        method: "post",
        data
    });
};

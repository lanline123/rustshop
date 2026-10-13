import request from "@/utils/request";
import type {
    MerchantApplicationListParams,
    MerchantApplicationListResult,
    MerchantApplicationItem,
    MerchantApplicationSubmitForm
} from "@/types/finance/merchantApplication.d";

// 进件分页列表（平台进件管理 / 进件审核 共用）
export const getMerchantApplicationList = (params: MerchantApplicationListParams) => {
    return request<MerchantApplicationListResult>({
        url: "finance/merchantApplication/list",
        method: "get",
        params
    });
};

// 平台端直接提交进件（进件管理：选择店铺直接提交微信）
export const submitMerchantApplication = (data: MerchantApplicationSubmitForm) => {
    return request<MerchantApplicationItem>({
        url: "finance/merchantApplication/submit",
        method: "post",
        data
    });
};

// 店铺/供应商端提交进件申请（存为待审核，不直接提交微信）
export const applyMerchantApplication = (data: MerchantApplicationSubmitForm) => {
    return request<MerchantApplicationItem>({
        url: "finance/merchantApplication/apply",
        method: "post",
        data
    });
};

// 店铺/供应商端获取当前主体最新进件申请（驱动步骤页）
// 传入 payChannel 时：优先该渠道 FINISH，没有成功单再退回该渠道最新一笔
export const getCurrentMerchantApplication = (params?: { payChannel?: string }) => {
    return request<MerchantApplicationItem>({
        url: "finance/merchantApplication/current",
        method: "get",
        params
    });
};

// 进件详情
export const getMerchantApplicationDetail = (params: { id: number | string }) => {
    return request<MerchantApplicationItem>({
        url: "finance/merchantApplication/detail",
        method: "get",
        params
    });
};

// 平台端审核通过（提交微信进件）
export const approveMerchantApplication = (data: { id: number | string }) => {
    return request({
        url: "finance/merchantApplication/approve",
        method: "post",
        data
    });
};

// 平台端审核拒绝
export const rejectMerchantApplication = (data: { id: number | string; rejectReason: string }) => {
    return request({
        url: "finance/merchantApplication/reject",
        method: "post",
        data
    });
};

// 刷新进件状态
export const refreshMerchantApplication = (params: { id: number | string }) => {
    return request({
        url: "finance/merchantApplication/refresh",
        method: "post",
        params
    });
};

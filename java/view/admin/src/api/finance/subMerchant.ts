import request from "@/utils/request";
import type { SubMerchantListParams, SubMerchantListResult, SubMerchantBindForm } from "@/types/finance/subMerchant.d";

// 子商户分页列表
export const getSubMerchantList = (params: SubMerchantListParams) => {
    return request<SubMerchantListResult>({
        url: "finance/subMerchant/list",
        method: "get",
        params
    });
};

// 手动绑定子商户
export const bindSubMerchant = (data: SubMerchantBindForm) => {
    return request({
        url: "finance/subMerchant/bind",
        method: "post",
        data
    });
};

// 解绑子商户
export const unbindSubMerchant = (params: { id: number | string }) => {
    return request({
        url: "finance/subMerchant/unbind",
        method: "post",
        params
    });
};

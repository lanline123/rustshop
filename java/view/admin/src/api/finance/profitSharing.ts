import request from "@/utils/request";
import type { ProfitSharingListParams, ProfitSharingListResult, ProfitSharingSubmitForm } from "@/types/finance/profitSharing.d";

// 分账单分页列表
export const getProfitSharingList = (params: ProfitSharingListParams) => {
    return request<ProfitSharingListResult>({
        url: "finance/profitSharing/list",
        method: "get",
        params
    });
};

// 批量发起分账
export const submitProfitSharingBatch = (data: ProfitSharingSubmitForm) => {
    return request({
        url: "finance/profitSharing/submitBatch",
        method: "post",
        data
    });
};

// 手动完结分账单
export const manualFinishProfitSharing = (params: { id: number | string }) => {
    return request({
        url: "finance/profitSharing/manualFinish",
        method: "post",
        params
    });
};

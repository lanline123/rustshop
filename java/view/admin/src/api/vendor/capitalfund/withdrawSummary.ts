import request from "@/utils/request";
import type { VendorWithdrawSummaryFilterParams, VendorWithdrawSummaryListResult } from "@/types/vendor/capitalfund/withdrawSummary";

// 供应商提现汇总列表
export const getVendorWithdrawSummaryList = (params: VendorWithdrawSummaryFilterParams) => {
    return request<VendorWithdrawSummaryListResult>({
        url: "vendor/vendorWithdraw/summaryList",
        method: "get",
        params
    });
};


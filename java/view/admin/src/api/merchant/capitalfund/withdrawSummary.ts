import request from "@/utils/request";
import type { ShopWithdrawSummaryFilterParams, ShopWithdrawSummaryListResult } from "@/types/merchant/capitalfund/withdrawSummary";

// 门店/店铺提现汇总列表
export const getShopWithdrawSummaryList = (params: ShopWithdrawSummaryFilterParams) => {
    return request<ShopWithdrawSummaryListResult>({
        url: "merchant/shopWithdraw/summaryList",
        method: "get",
        params
    });
};


import request from "@/utils/request";
import type { consumerRankingFilterParams, consumerRankingFilterState } from "@/types/panel/consumerRanking.d.ts";

// 会员消费排行
export const getUserConsumptionRanking = (params: consumerRankingFilterParams) => {
    return request<consumerRankingFilterState>({
        url: "panel/statisticsUser/userConsumptionRanking",
        method: "get",
        params
    });
};

export const exportUserConsumptionRanking = (params: consumerRankingFilterParams) => {
    return request({
        url: "panel/statisticsUser/userConsumptionRanking",
        method: "get",
        responseType: 'arraybuffer',
        params
    });
};

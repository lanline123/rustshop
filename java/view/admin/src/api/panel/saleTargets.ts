import request from "@/utils/request";
import { FilterResult } from "@/types/panel/saleTargets.d";

// 销售指标
export const getQuotaSales = () => {
    return request<FilterResult>({
        url: "panel/salesStatistics/salesIndicators",
        method: "get"
    });
};

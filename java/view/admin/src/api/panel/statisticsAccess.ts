import request from "@/utils/request";
import type { statisticsAccessFilterParams, FilterResult } from "@/types/panel/statisticsAccess.d.ts";

// 访问统计
export const getStatisticsAccess = (params: statisticsAccessFilterParams) => {
    return request<FilterResult>({
      url: "panel/statisticsAccess/accessStatistics",
      method: "get",
      params
    })
}
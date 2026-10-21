import request from "@/utils/request";
import type {CoreSummaryFormState, CoreTrendFormState,AnalysisFormResult, RankingFormResult} from "@/types/salesman/overview.d";
// 获取核心数据汇总
export const getOverviewCoreSummary = (params: object) => {
    return request<CoreSummaryFormState>({
        url: "salesman/overview/coreSummary",
        method: "get",
        params,
    });
}
// 获取核心指标趋势
export const getOverviewCoreTrend = (params: object) => {
    return request<CoreTrendFormState>({
        url: "salesman/overview/coreTrend",
        method: "get",
        params,
    });
}
// 获取分销员排行
export const getSalesmanRanking = (params: object) => {
    return request<RankingFormResult>({
        url: "salesman/salesman/ranking",
        method: "get",
        params,
    });
}
// 商品成交分析
export const getProductAnalysis = (params: object) => {
    return request<AnalysisFormResult>({
        url: "salesman/product/analysis",
        method: "get",
        params,
    });
}

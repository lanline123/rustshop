import request from "@/utils/request";
import { statisticsGoodsFilterParams, statisticsGoodsFilterState } from "@/types/panel/statisticsGoods";

// 销售排行列表
export const getGoodsTopSales = (data: statisticsGoodsFilterParams) => {
    return request<statisticsGoodsFilterState>({
        url: "panel/salesStatistics/salesRanking",
        method: "get",
        params: data
    });
};

// 销售排行列表导出
export const exportGoodsTopSales = (data: statisticsGoodsFilterParams) => {
    return request({
        url: "panel/salesStatistics/salesRanking",
        method: "get",
        responseType: 'arraybuffer',
        params: data
    });
};

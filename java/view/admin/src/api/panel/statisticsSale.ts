import request from "@/utils/request";
import type {
    statisticsSalesFilterParams,
    statisticsSalesFilterState,
    SaleDetaillistFilterParams,
    SaleDetaillistFilterState
} from "@/types/panel/statisticsSale";

//  销售明细
export const getSaleDetail = (data: statisticsSalesFilterParams) => {
    return request<statisticsSalesFilterState>({
        url: "panel/salesStatistics/salesDetail",
        method: "get",
        params: data
    });
};

// 销售商品明细
export const getSaleDetaillist = (data: SaleDetaillistFilterParams) => {
    return request<SaleDetaillistFilterState>({
        url: "panel/salesStatistics/salesProductDetail",
        method: "get",
        params: data
    });
};

// 销售商品明细导出
export const exportSaleDetaillis = (params: SaleDetaillistFilterParams) => {
    return request({
        url: "panel/salesStatistics/salesProductDetail",
        method: "get",
        responseType: 'arraybuffer',
        params
    });
};

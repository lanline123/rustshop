import request from "@/utils/request";
import type {
    InventoryFilterResult
} from "@/types/product/inventoryQuery.d";

// 获取库存列表
export const getInventoryList = (params: any) => {
    return request<InventoryFilterResult>({
        url: "product/stock/page",
        method: "get",
        params,
    });
};

// 商品库存调整
export const productStockAdjust = (data: any) => {
    return request({
        url: "product/stock/stockAdjust",
        method: "post",
        data
    })
}

// 库存调整详情
export const getStockAdjustDetail = (params: any) => {
    return request({
        url: "product/stock/adjustDetail",
        method: "get",
        params
    })
}

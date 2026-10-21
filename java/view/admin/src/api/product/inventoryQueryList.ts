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

// 单商品出入库明细
export const getInventoryDetail = (params: any) => {
    return request<any>({
        url: "product/stock/stockDetail",
        method: "get",
        params
    })
}

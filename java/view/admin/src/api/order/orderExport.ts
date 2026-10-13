import request from "@/utils/request";
import {ExportItemList, SaveExportItemFilterResult, ExportItemListFilterState} from "@/types/order/orderExport"
// 订单导出可选标签列表
export const getExportItemList = () => {
    return request<ExportItemList>({
        url: "order/order/getExportItemList",
        method: "get",
    });
}

// 订单导出保存标签列表
export const getExportItemInfo = () => {
    return request<ExportItemList>({
        url: "order/order/exportItemInfo",
        method: "get",
    });
}
// 订单导出保存的标签
export const saveExportItem = (data:SaveExportItemFilterResult) => {
    return request({
        url: "order/order/saveExportItem",
        method: "post",
        data,
    });
}
// 导出订单
export const getOrderExport = (params: object) => {
    return request({
        url: "order/order/orderExport",
        method: "get",
        responseType: "arraybuffer",
        params
    })
}
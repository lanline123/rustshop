import request from "@/utils/request";
import type {SalesmanOrderFilterResult, SalesmanOrderFormResult} from "@/types/salesman/performanceSettlement.d";
// 获取业绩结算列表
export const getSalesmanOrderList = (params: object) => {
    return request<SalesmanOrderFilterResult>({
        url: "salesman/order/list",
        method: "get",
        params,
    });
}
// 导出业绩结算列表
export const getSalesmanOrderExport = (params: object) => {
    return request({
        url: "salesman/order/list",
        method: "get",
        responseType: "arraybuffer",
        params
    })
}
// 获取结算方案设置
export const getSalesmanSettlementConfig = () => {
    return request<any>({
        url: "salesman/config/detail?code=salesmanSettlement",
        method: "get"
    });
}
// 结算方案保存配置
export const saveSalesmanSettlementConfig = (data: object) => {
    return request({
        url: "salesman/config/save?code=salesmanSettlement",
        method: "post",
        data,
    });
}
// 人工结算（标记已结算）
export const settlementSalesmanOrder = (data: { ids: (number | string)[] }) => {
    return request({
        url: "salesman/order/settlement",
        method: "post",
        data,
    });
}
// 业绩结算页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/product/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 业绩结算页面删除项
export const delSalesmanProduct = (data: object) => {
    return request({
        url: "salesman/product/del",
        method: "post",
        data,
    });
}
// 业绩结算页面更新项
export const updateSalesmanProductFiled = (data: object) => {
    return request({
        url: "salesman/product/updateField",
        method: "post",
        data,
    });
}
// 获取业绩结算详情
export const getSalesmanProduct = (action: string, params: object) => {
    return request<SalesmanOrderFormResult>({
        url: "salesman/product/" + action,
        method: "get",
        params
    });
}
// 更新业绩结算
export const updateSalesmanProduct = (operation: string, data: object) => {
    return request({
        url: "salesman/product/" + operation,
        method: "post",
        data
    });
}
// 获取业绩结算配置项
export const getSalesmanProductnConfig = () => {
    return request({
        url: "salesman/product/config",
        method: "get"
    });
}
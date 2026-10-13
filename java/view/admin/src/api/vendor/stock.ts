import request from "@/utils/request";
import type { InventoryFilterResult } from "@/types/product/inventoryQuery.d";
import type { ProductInventoryLogFilterParams, ProductInventoryLogFilterResult } from "@/types/product/productInventoryLog";

// 供应商端库存管理：所有接口固定走 vendor/stock 前缀，数据按当前供应商隔离

// 获取库存列表
export const getInventoryList = (params: any) => {
    return request<InventoryFilterResult>({
        url: "vendor/stock/page",
        method: "get",
        params,
    });
};

// 单商品出入库明细
export const getInventoryDetail = (params: any) => {
    return request<any>({
        url: "vendor/stock/stockDetail",
        method: "get",
        params,
    });
};

// 获取出入库单列表
export const getInventoryRecordList = (params: any) => {
    return request<any>({
        url: "vendor/stock/record/page",
        method: "get",
        params,
    });
};

// 出入库单详情
export const getRecordDetail = (params: any) => {
    return request({
        url: "vendor/stock/getRecordDetail",
        method: "get",
        params,
    });
};

// 商品入库
export const productStockIn = (data: any) => {
    return request({
        url: "vendor/stock/stockIn",
        method: "post",
        data,
    });
};

// 商品出库
export const productStockOut = (data: any) => {
    return request({
        url: "vendor/stock/stockOut",
        method: "post",
        data,
    });
};

// 商品库存调整
export const productStockAdjust = (data: any) => {
    return request({
        url: "vendor/stock/stockAdjust",
        method: "post",
        data,
    });
};

// 库存调整详情
export const getStockAdjustDetail = (params: any) => {
    return request({
        url: "vendor/stock/adjustDetail",
        method: "get",
        params,
    });
};

// 下载出入库导入模板
export const productStockTemplateSubmit = () => {
    return request({
        url: "vendor/stock/downloadTemplate",
        method: "get",
        responseType: "arraybuffer",
    });
};

// 批量导入出入库单
export const productStockImportSubmit = (data: object) => {
    return request({
        url: "vendor/stock/batchImport",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        data,
    });
};

// 获取导入失败的数据
export const getImportErrorData = (data: any) => {
    return request({
        url: "vendor/stock/getImportErrorData",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data",
        },
        responseType: "arraybuffer",
        data,
    });
};

// 获取经办人集合
export const getAddByList = () => {
    return request<string[]>({
        url: "vendor/stock/getAddByList",
        method: "get",
    });
};

// 获取库存日志列表
export const getProductInventoryLogList = (params: ProductInventoryLogFilterParams) => {
    return request<ProductInventoryLogFilterResult>({
        url: "vendor/productInventoryLog/list",
        method: "get",
        params,
    });
};

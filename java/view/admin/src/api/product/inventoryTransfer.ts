import request from "@/utils/request";
import type { InventoryFilterResult } from "@/types/product/inventoryTransfer";
import { computed } from "vue";
import { useInventoryStore } from "@/store/inventory";

const shopId: any = computed(() => useInventoryStore().outboundOrganizationId);

// 库存调拨列表
export const productStockAllocateRecordPage = (params: any) => {
    return request<InventoryFilterResult>({
        url: "product/stock/productStockAllocateRecordPage",
        method: "get",
        params
    });
};

// 获取组织列表
export const getOrgList = (params: any) => {
    return request({
        url: "product/stock/getOrgList",
        method: "get",
        params
    });
};

// 新建调拨单
export const createAllocate = (data: any) => {
    return request({
        url: "product/stock/createAllocate",
        method: "post",
        data
    });
};

// 调拨记录导出
export const exportAllocate = (data: any) => {
    return request({
        url: "product/stock/exportAllocate",
        method: "post",
        data,
        responseType: "arraybuffer"
    });
};

// 下载模板文件
export const getAllocateTemplate = () => {
    return request({
        url: "product/stock/getAllocateTemplate",
        method: "get",
        responseType: "arraybuffer"
    });
};

// 批量导入调拨商品
export const productStockAllocateImport = (data: any) => {
    return request({
        url: "product/stock/batchImportAllocate",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        data
    });
};

// 获取导入调拨商品失败Excel
export const getAllocateImportErrorData = (data: any) => {
    return request({
        url: "product/stock/getBatchImportErrorData",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        responseType: "arraybuffer",
        data
    });
};

// 获取经办人集合
export const getAddByList = (params: any) => {
    return request({
        url: "product/stock/getAddByList",
        method: "get",
        params
    });
};

// 调拨入库
export const stockInAllocate = (data: any) => {
    return request({
        url: "product/stock/stockInAllocate",
        method: "post",
        data
    });
};

// 调拨出库
export const stockOutAllocate = (data: any) => {
    return request({
        url: "product/stock/stockOutAllocate",
        method: "post",
        data
    });
};

// 调拨单作废
export const cancelAllocate = (data: any) => {
    return request({
        url: "product/stock/cancelAllocate",
        method: "post",
        data
    });
};

// 调拨单审核
export const auditAllocate = (params: any) => {
    return request({
        url: "product/stock/auditAllocate",
        method: "post",
        params
    });
};

// 调拨商品详情
export const productStockAllocateRecordDetail = (params: any) => {
    return request({
        url: "product/stock/productStockAllocateRecordDetail",
        method: "get",
        params
    });
};

// 库存调拨操作记录详情
export const productStockAllocateOperationDetail = (params: any) => {
    return request({
        url: "product/stock/productStockAllocateOperationDetail",
        method: "get",
        params
    });
};

// 获取可调拨商品列表
export const getAllocateProductStockPage = (params: any) => {
    return request({
        url: "product/stock/getAllocateProductStockPage",
        method: "get",
        params
    });
};

export const downloadAllocateTemplate = () => {
    return request({
        url: "product/stock/downloadAllocateTemplate",
        method: "get",
        responseType: "arraybuffer"
    });
};

export const batchImportAllocate = (params: any, data: any) => {
    return request({
        url: "product/stock/batchImportAllocate",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        params,
        data
    });
};

export const getAllocateProductErrorData = (params: any, data: any) => {
    return request({
        url: "product/stock/getAllocateProductErrorData",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        params,
        responseType: "arraybuffer",
        data
    });
};

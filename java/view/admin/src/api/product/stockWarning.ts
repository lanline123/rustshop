import request from "@/utils/request";
import type {
    StockWarningSetting,
    StockWarningSettingSaveParams,
    StockWarningFilterParams,
    StockWarningFilterResult,
    StockWarningSaveParams,
    StockWarningBatchSaveParams,
    StockWarningBatchSaveResult,
    StockWarningExportParams,
    StockWarningImportResult
} from "@/types/product/stockWarning.d";

// 「全部」在页面上用 -1 表达，后端约定不传即全部
const FILTER_ALL = -1;

const omitAll = (value?: number) => (value === undefined || value === FILTER_ALL ? undefined : value);

// 获取库存预警通知总开关
export const getStockWarningSetting = (params?: { shopId?: number }) => {
    return request<StockWarningSetting>({
        url: "product/stockWarning/setting",
        method: "get",
        params
    });
};

// 保存库存预警通知总开关
export const saveStockWarningSetting = (data: StockWarningSettingSaveParams) => {
    return request({
        url: "product/stockWarning/setting",
        method: "post",
        data
    });
};

// 获取库存预警列表
export const getStockWarningList = (params: StockWarningFilterParams) => {
    return request<StockWarningFilterResult>({
        url: "product/stockWarning/list",
        method: "get",
        params: {
            ...params,
            stockStatus: omitAll(params.stockStatus),
            warnEnabled: omitAll(params.warnEnabled)
        }
    });
};

// 保存单个商品/SKU 的预警规则
export const saveStockWarning = (data: StockWarningSaveParams) => {
    return request({
        url: "product/stockWarning/save",
        method: "post",
        data
    });
};

// 批量保存预警规则
export const batchSaveStockWarning = (data: StockWarningBatchSaveParams) => {
    return request<StockWarningBatchSaveResult>({
        url: "product/stockWarning/batchSave",
        method: "post",
        data
    });
};

// 下载库存预警导入模板，固定三列（规格编码 / 上限 / 下限），无入参
export const getStockWarningImportTemplate = () => {
    return request({
        url: "product/stockWarning/downloadTemplate",
        method: "get",
        responseType: "arraybuffer"
    });
};

// 导入预警规则，data 为含 file 字段的 FormData；行级失败在 failItems 里逐行返回
export const importStockWarning = (data: FormData) => {
    return request<StockWarningImportResult>({
        url: "product/stockWarning/import",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        data
    });
};

// 获取导入失败的数据，回传原文件换取带失败原因的 Excel
export const getStockWarningImportErrorData = (data: FormData) => {
    return request({
        url: "product/stockWarning/getBatchImportErrorData",
        method: "post",
        headers: {
            "Content-Type": "multipart/form-data"
        },
        responseType: "arraybuffer",
        data
    });
};

// 导出勾选行，ids 必填；后端只认 ids 与 shopId，列表筛选条件一律忽略
export const exportStockWarning = (params: StockWarningExportParams) => {
    return request({
        url: "product/stockWarning/export",
        method: "get",
        params,
        responseType: "arraybuffer"
    });
};

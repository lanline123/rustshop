import request from "@/utils/request";
import type {
    InventoryFilterResult
} from "@/types/product/goodsArePutIntoStorage";

// 获取入库列表
export const getInventoryRecordList = (params: any) => {
    return request<InventoryFilterResult>({
        url: "product/stock/record/page",
        method: "get",
        params,
    });
};

// 下载模板文件
export const productStockTemplateSubmit = () => {
    return request({
        url: "product/stock/downloadTemplate",
        method: "get",
        responseType: "arraybuffer",
    });
}

// 导入出入库单
export const productStockImportSubmit = (data: object) => {
    return request({
        url: "product/stock/batchImport",
        method: "post",
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        data
    })
}

// 获取导入失败的数据
export const getImportErrorData = (data: any) => {
    return request({
        url: "product/stock/getImportErrorData",
        method: "post",
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        responseType: "arraybuffer",
        data
    })
}

// 商品入库
export const productStockIn = (data: any) => {
    return request({
        url: "product/stock/stockIn",
        method: "post",
        data
    });
}

// 出入库详情
export const getRecordDetail = (params: any) => {
    return request({
        url: "product/stock/getRecordDetail",
        method: "get",
        params
    })
}

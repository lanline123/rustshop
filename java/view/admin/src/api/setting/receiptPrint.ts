import request from "@/utils/request";
import type { 
    PrintListParams, 
    PrintListResponse, 
    PrintDetailParams, 
    PrintDetailResponse, 
    PrintCreateParams, 
    PrintDeleteParams,
    PrintConfigParams,
    PrintUdateStatusParams
 } from "@/types/setting/receiptPrint";

//  获取打印机列表
export const getPrintList = (params: PrintListParams) => {
    return request<PrintListResponse>({
        url: "print/print/list",
        method: "get",
        params
    });
};

// 获取打印机详情
export const getPrintDetail = (params: PrintDetailParams) => {
    return request<PrintDetailResponse>({
        url: "print/print/detail",
        method: "get",
        params
    });
};

// 新增或更新打印机
export const printCreateAndUpdate= (operation: 'create' | 'update' ,data: PrintCreateParams) => {
    return request<any>({
        url: `print/print/${operation}`,
        method: "post",
        data
    });
};

// 更新打印机状态
export const printUpdateStatus= (data: PrintUdateStatusParams) => {
    return request<any>({
        url: "print/print/updateField",
        method: "post",
        data
    });
};

// 删除打印机
export const printDelete= (data: PrintDeleteParams) => {
    return request<any>({
        url: "print/print/del",
        method: "post",
        data
    });
};

// 获取打印机配置
export const getPrintConfig = (params: PrintConfigParams) => {
    return request<any>({
        url: "print/printConfig/getConfigsByPrintId",
        method: "get",
        params
    });
};

// 更新打印机配置
export const updatePrintConfig = (data: any) => {
    return request<any>({
        url: "print/printConfig/update",
        method: "post",
        data
    });
};

// 是否有可用打印机
export const hasEnabledPrint = () => {
    return request<any>({
        url: "print/print/hasEnabled",
        method: "post",
    });
};


// 打印订单
export const triggerPrint = (data: any) => {
    return request<any>({
        url: "print/printConfig/print",
        method: "post",
        data
    });
};



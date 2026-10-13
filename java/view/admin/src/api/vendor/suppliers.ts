import request from "@/utils/request";
import {SuppliersFilterResult, SuppliersFormState, SuppliersFilterParams} from "@/types/vendor/suppliers"
// 供应商列表
export const getSuppliersList = (params: SuppliersFilterParams) => {
    return request<SuppliersFilterResult>({
        url: "vendor/vendor/list",
        method: "get",
        params,
    });
}

//获取分类详情
export const getSuppliers  = (action: string, params: object) => {
    return request<SuppliersFormState>({
        url: "vendor/vendor/" + action,
        method: "get",
        params
    });
}

// 更新分类
export const updateSuppliers = (operation: string, data: object) => {
    return request({
        url: "vendor/vendor/" + operation,
        method: "post",
        data
    });
}
// 列表更新项
export const updateSuppliersField = (data:object) => {
    return request({
        url: "vendor/vendor/updateField",
        method: "post",
        data
    });
}
// 列表更新项
export const delSuppliers = (data:object) => {
    return request({
        url: "vendor/vendor/delVendor",
        method: "post",
        data
    });
}

// 创建供应商（关联需求 #8DE184 切换组织弹窗「新建供应商」）
export const createVendor = (data: object) => {
    return request<any>({
        url: "vendor/vendor/create",
        method: "post",
        data
    });
}

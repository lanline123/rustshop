import request from "@/utils/request";
import {SuppliersFilterResult, SuppliersFormState, SuppliersFilterParams} from "@/types/authority/suppliers"
// 获取商品分类列表
export const getSuppliersList = (params: SuppliersFilterParams) => {
    return request<SuppliersFilterResult>({
        url: "authority/suppliers/list",
        method: "get",
        params,
    });
}

// 删除
export const delSuppliers= (data: object) => {
    return request({
        url: "authority/suppliers/del",
        method: "post",
        data,
    });
}
//获取分类详情
export const getSuppliers  = (action: string, params: object) => {
    return request<SuppliersFormState[]>({
        url: "authority/suppliers/" + action,
        method: "get",
        params
    });
}

// 更新分类
export const updateSuppliers = (operation: string, data: object) => {
    return request({
        url: "authority/suppliers/" + operation,
        method: "post",
        data
    });
}

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "authority/suppliers/batch",
        method: "post",
        data: {type, ...data}
    });
}
// 列表更新项
export const updateSuppliersField = (data:object) => {
    return request({
        url: "authority/suppliers/updateField",
        method: "post",
        data
    });
}

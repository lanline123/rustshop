import request from "@/utils/request";
import type {productFilterResult, productFormState} from "@/types/salesman/product.d";
// 获取分销员列表列表
export const getSalesmanProductList = (params: object) => {
    return request<productFilterResult>({
        url: "salesman/product/list",
        method: "get",
        params,
    });
}
// 分销员列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "salesman/product/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 分销员列表页面删除项
export const delSalesmanProduct = (data: object) => {
    return request({
        url: "salesman/product/del",
        method: "post",
        data,
    });
}
// 分销员列表页面更新项
export const updateSalesmanProductFiled = (data: object) => {
    return request({
        url: "salesman/product/updateField",
        method: "post",
        data,
    });
}
// 获取分销员列表详情
export const getSalesmanProduct = (action: string, params: object) => {
    return request<productFormState>({
        url: "salesman/product/" + action,
        method: "get",
        params
    });
}
// 更新分销员列表
export const updateSalesmanProduct = (operation: string, data: object) => {
    return request({
        url: "salesman/product/" + operation,
        method: "post",
        data
    });
}
// 获取分销员列表配置项
export const getSalesmanProductnConfig = () => {
    return request<any>({
        url: "salesman/product/config",
        method: "get"
    });
}
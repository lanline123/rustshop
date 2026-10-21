import request from "@/utils/request";
import type { ProductServicesFilterParams, ProductServicesFilterResult, ProductServicesFormState} from "@/types/product/productServices";
// 获取商品服务列表
export const getProductServicesList = (params: ProductServicesFilterParams) => {
    return request<ProductServicesFilterResult>({
        url: "product/productServices/list",
        method: "get",
        params,
    });
}
// 商品批量编辑页非平台端商品服务列表
export const getProductBatchServicesList = (params: ProductServicesFilterParams) => {
    return request<ProductServicesFilterResult>({
        url: "product/productBatch/productService/list",
        method: "get",
        params,
    });
}
// 商品服务列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/productServices/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 商品服务列表页面删除项
export const delProductServices = (data: object) => {
    return request({
        url: "product/productServices/del",
        method: "post",
        data,
    });
}
// 商品服务列表页面更新项
export const updateProductServicesFiled = (data: object) => {
    return request({
        url: "product/productServices/updateField",
        method: "post",
        data,
    });
}
// 获取商品服务详情
export const getProductServices = (action: string, params: object) => {
    return request<ProductServicesFormState>({
        url: "product/productServices/" + action,
        method: "get",
        params
    });
}
// 更新商品服务
export const updateProductServices = (operation: string, data: object) => {
    return request({
        url: "product/productServices/" + operation,
        method: "post",
        data
    });
}

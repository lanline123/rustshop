import request from "@/utils/request";
import type {
    ProductAttributesTplFilterParams,
    ProductAttributesTplFilterResult,
    ProductAttributesTplFilterState,
    ProductAttributesTplFormState
} from "@/types/product/productAttributesTpl";
// 获取示例模板列表
export const getProductAttributesTplList = (params: ProductAttributesTplFilterParams) => {
    return request<ProductAttributesTplFilterResult>({
        url: "product/productAttributesTpl/list",
        method: "get",
        params,
    });
}

// 获取指定商品类目下的属性模板（商品新增/编辑页按类目筛选可选模板）
export const getProductAttributesTplListByCategory = (categoryId: number | string) => {
    return request<ProductAttributesTplFilterState[]>({
        url: "product/productAttributesTpl/listByCategory",
        method: "get",
        params: {categoryId},
    });
}

export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/productAttributesTpl/batch",
        method: "post",
        data: {type, ...data},
    });
}

export const delProductAttributesTpl = (data: object) => {
    return request({
        url: "product/productAttributesTpl/del",
        method: "post",
        data,
    });
}

export const updateProductAttributesTplFiled = (data: object) => {
    return request({
        url: "product/productAttributesTpl/updateField",
        method: "post",
        data,
    });
}

export const getProductAttributesTpl= (action: string, params: object) => {
    return request<ProductAttributesTplFormState>({
        url: "product/productAttributesTpl/" + action,
        method: "get",
        params
    });
}

export const updateProductAttributesTpl = (operation: string, data: object) => {
    return request({
        url: "product/productAttributesTpl/" + operation,
        method: "post",
        data
    });
}


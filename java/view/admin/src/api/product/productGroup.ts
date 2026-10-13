import request from "@/utils/request";
import {
    ProductGroupFilterParams,
    ProductGroupFilterResult,
    ProductGroupFilterState,
    ProductGroupFormState,
    ProductGroupSearchFilterResult
} from "@/types/product/productGroup";
import type { BrandSearchFilterResult } from "@/types/product/brand";

// 获取商品分类列表
export const getProductGroupList = (params: ProductGroupFilterParams) => {
    return request<ProductGroupFilterResult>({
        url: "product/productGroup/list",
        method: "get",
        params
    });
};

export const getProductGroupSearch = () => {
    return request<ProductGroupSearchFilterResult>({
        url: "product/productGroup/search",
        method: "get"
    });
};

// 删除
export const delProductGroup = (data: object) => {
    return request({
        url: "product/productGroup/del",
        method: "post",
        data
    });
};
//获取分类详情
export const getProductGroup = (action: string, params: object) => {
    return request<ProductGroupFormState>({
        url: "product/productGroup/" + action,
        method: "get",
        params
    });
};

// 更新分类
export const updateProductGroup = (operation: string, data: object) => {
    return request({
        url: "product/productGroup/" + operation,
        method: "post",
        data
    });
};

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "product/productGroup/batch",
        method: "post",
        data: {type, ...data}
    });
};

// 列表更新项
export const updateProductGroupField = (data: object) => {
    return request({
        url: "product/productGroup/updateField",
        method: "post",
        data
    });
};

// 获取商品分组列表
export const getProductGroupClientList = () => {
    return request<ProductGroupFilterState[]>({
        url: "product/productGroup/clientList",
        method: "get"
    });
};

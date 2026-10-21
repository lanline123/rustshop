import request from "@/utils/request";
import type {
    ProductStyleFilterParams,
    ProductStyleFilterResult,
    ProductStyleDetailVO,
    ProductStyleFormState,
    BrandProductCountVO
} from "@/types/promotion/productStyle";

export const getProductStyleList = (params: ProductStyleFilterParams) => {
    return request<ProductStyleFilterResult>({
        url: "promotion/productStyle/list",
        method: "get",
        params
    });
};

export const getProductStyleDetail = (params: { id: number }) => {
    return request<ProductStyleDetailVO>({
        url: "promotion/productStyle/detail",
        method: "get",
        params
    });
};

export const createProductStyle = (data: ProductStyleFormState) => {
    return request({
        url: "promotion/productStyle/create",
        method: "post",
        data
    });
};

export const updateProductStyle = (data: ProductStyleFormState) => {
    return request({
        url: "promotion/productStyle/update",
        method: "post",
        data
    });
};

export const updateProductStyleField = (data: { id: number; field: string; [key: string]: any }) => {
    return request({
        url: "promotion/productStyle/updateField",
        method: "post",
        data
    });
};

export const delProductStyle = (data: { id: number }) => {
    return request({
        url: "promotion/productStyle/del",
        method: "post",
        data
    });
};

export const batchSubmit = (type: string, data: { ids: number[] }) => {
    return request({
        url: "promotion/productStyle/batch",
        method: "post",
        data: { type, ...data }
    });
};

export const addProductsToStyle = (data: { styleId: number; productIds: number[] }) => {
    return request({
        url: "promotion/productStyle/addProducts",
        method: "post",
        data
    });
};

export const removeProductsFromStyle = (data: { styleId: number; productIds: number[] }) => {
    return request({
        url: "promotion/productStyle/removeProducts",
        method: "post",
        data
    });
};

export const getProductStyleBrandList = (params?: { styleId?: number }) => {
    return request<BrandProductCountVO[]>({
        url: "promotion/productStyle/brandList",
        method: "get",
        params
    });
};

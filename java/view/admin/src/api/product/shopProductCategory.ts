import request from "@/utils/request";
import type {ShopProductCategoryFilterParams, ShopProductCategoryFilterResult, ShopProductCategoryFormState, ShopProductCategoryFilterState} from "@/types/product/shopProductCategory.d.ts";
// 获取商品分类列表
export const getShopProductCategoryList = (params: ShopProductCategoryFilterParams) => {
    return request<ShopProductCategoryFilterResult>({
        url: "merchant/shopProductCategory/list",
        method: "get",
        params,
    });
}

export const getAllShopProductCategoryList = () => {
    return request<ShopProductCategoryFilterState[]>({
        url: "merchant/shopProductCategory/getAllCategory",
        method: "get",
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "merchant/shopProductCategory/batch",
        method: "post",
        data: {type, ...data},
    });
}

// 商品分类页面更新项
export const updateShopProductCategoryFiled = (data: object) => {
    return request({
        url: "merchant/shopProductCategory/updateField",
        method: "post",
        data,
    });
}

export const delShopProductCategory = (data: object) => {
    return request({
        url: "merchant/shopProductCategory/del",
        method: "post",
        data,
    });
}

export  const getShopProductCategory = (action: string, params: object) => {
    return request<ShopProductCategoryFormState>({
        url: "merchant/shopProductCategory/" + action,
        method: "get",
        params
    });
}

export const updateShopProductCategory = (operation: string, data: object) => {
    return request({
        url: "merchant/shopProductCategory/" + operation,
        method: "post",
        data
    });
}

export const moveShopProductCategory = ( data: object) => {
    return request({
        url: "merchant/shopProductCategory/moveCat",
        method: "post",
        data
    });
}

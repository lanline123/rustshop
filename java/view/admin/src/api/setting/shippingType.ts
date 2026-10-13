import type {ShippingTypeFilterParams, ShippingTypeFilterResult, ShippingTypeFormState} from "@/types/setting/shippingType.d";
import request from "@/utils/request";

export const getShippingTypeList = (params: ShippingTypeFilterParams) => {
    return request<ShippingTypeFilterResult>({
        url: "setting/shippingType/list",
        method: "get",
        params,
    });
}
export const getShippingTypeAll = (params: object) => {
    return request<ShippingTypeFilterResult>({
        url: "setting/shippingType/list",
        method: "get"
    });
}
// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/shippingType/batch",
        method: "post",
        data: {type, ...data},
    });
}


export const delShippingType = (data: object) => {
    return request({
        url: "setting/shippingType/del",
        method: "post",
        data,
    });
}

export const getShippingType = (action: string, params: object) => {
    return request<ShippingTypeFormState>({
        url: "setting/shippingType/" + action,
        method: "get",
        params
    });
}

export const updateShippingType = (operation: string, data: object) => {
    return request({
        url: "setting/shippingType/" + operation,
        method: "post",
        data
    });
}
export const updateShippingTypeField = (data: object) => {
    return request({
        url: "setting/shippingType/updateField",
        method: "post",
        data
    });
}

import type {ShippingTplFilterParams, ShippingTplFilterResult, ShippingTplFormState} from "@/types/setting/shippingTpl.d";
import request from "@/utils/request";

export const getShippingTplList = (params: ShippingTplFilterParams) => {
    return request<ShippingTplFilterResult>({
        url: "setting/shippingTpl/list",
        method: "get",
        params,
    });
}
export const getShippingTplAll = () => {
    return request<ShippingTplFilterResult>({
        url: "setting/shippingTpl/getAll",
        method: "get"
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/shippingTpl/batch",
        method: "post",
        data: {type, ...data},
    });
}


export const delShippingTpl = (data: object) => {
    return request({
        url: "setting/shippingTpl/del",
        method: "post",
        data,
    });
}

export const getShippingTplConfig = () => {
    return request<any>({
        url: "setting/shippingTpl/config",
        method: "get"
    });
}
export const getShippingTpl = (action: string, params: object) => {
    return request<ShippingTplFormState>({
        url: "setting/shippingTpl/" + action,
        method: "get",
        params
    });
}

export const updateShippingTpl = (operation: string, data: object) => {
    return request({
        url: "setting/shippingTpl/" + operation,
        method: "post",
        data
    });
}
export const updateShippingTplField = (data: object) => {
    return request({
        url: "setting/shippingTpl/updateField",
        method: "post",
        data
    });
}

// 设为默认运费模板
export const setDefault = (data: object) => {
    return request({
        url: "setting/shippingTpl/setDefault",
        method: "post",
        params: data
    });
}
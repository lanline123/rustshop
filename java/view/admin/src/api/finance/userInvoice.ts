import request from "@/utils/request";
import type {UserInvoiceFilterParams, UserInvoiceFilterResult, UserInvoiceFormState} from "@/types/finance/userInvoice.d"
// 获取商品分类列表
export const getUserInvoiceList = (params: UserInvoiceFilterParams) => {
    return request<UserInvoiceFilterResult>({
        url: "finance/userInvoice/list",
        method: "get",
        params,
    });
}

// 删除
export const delUserInvoice= (data: object) => {
    return request({
        url: "finance/userInvoice/del",
        method: "post",
        data,
    });
}

export const recallUserInvoice= (data: object) => {
    return request({
        url: "finance/userInvoice/recall",
        method: "post",
        data,
    });
}
//获取分类详情
export const getUserInvoice  = (action: string, params: object) => {
    return request<UserInvoiceFormState>({
        url: "finance/userInvoice/" + action,
        method: "get",
        params
    });
}

// 更新分类
export const updateUserInvoice = (operation: string, data: object) => {
    return request({
        url: "finance/userInvoice/" + operation,
        method: "post",
        data
    });
}

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "finance/userInvoice/batch",
        method: "post",
        data: {type, ...data},
    });
}

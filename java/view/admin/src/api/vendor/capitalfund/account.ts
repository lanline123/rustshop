import request from "@/utils/request";
import type {AccountFilterResult, AccountFormState, AccountTypeList} from "@/types/vendor/capitalfund/account.d";
// 获取商家账户列表
export const getAccountList = (params: object) => {
    return request<AccountFilterResult>({
        url: "vendor/vendorAccount/list",
        method: "get",
        params,
    });
}
// 获取商家账户配置
export const getAccountConfig = () => {
    return request<AccountTypeList[]>({
        url: "vendor/vendorAccount/config",
        method: "get"
    });
}

// 商家账户列表页面删除项
export const delAccount = (data: object) => {
    return request({
        url: "vendor/vendorAccount/del",
        method: "post",
        data,
    });
}
// 获取商家账户详情
export const getAccount = (action: string, params: object) => {
    return request<AccountFormState[]>({
        url: "vendor/vendorAccount/" + action,
        method: "get",
        params
    });
}
// 更新商家账户
export const updateAccount = (operation: string, data: object) => {
    return request({
        url: "vendor/vendorAccount/" + operation,
        method: "post",
        data
    });
}

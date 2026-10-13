import request from "@/utils/request";
import type {AccountFilterResult, AccountFormState, AccountTypeList} from "@/types/merchant/capitalfund/account.d";
// 获取商家账户列表
export const getAccountList = (params: object) => {
    return request<AccountFilterResult>({
        url: "merchant/account/list",
        method: "get",
        params,
    });
}
// 获取商家账户配置
export const getAccountConfig = () => {
    return request<AccountTypeList[]>({
        url: "merchant/account/config",
        method: "get"
    });
}

// 商家账户列表页面删除项
export const delAccount = (data: object) => {
    return request({
        url: "merchant/account/del",
        method: "post",
        data,
    });
}
// 获取商家账户详情
export const getAccount = (action: string, params: object) => {
    return request<AccountFormState[]>({
        url: "merchant/account/" + action,
        method: "get",
        params
    });
}
// 更新商家账户
export const updateAccount = (operation: string, data: object) => {
    return request({
        url: "merchant/account/" + operation,
        method: "post",
        data
    });
}

import request from "@/utils/request";
import type { AccountFilterResult, StaffFrom, AccountFormState} from "@/types/merchant/capitalfund/dashboard.d";
// 获取资金总览数据
export const getShopAccount = () => {
    return request<AccountFormState>({
        url: "merchant/shopAccount/index",
        method: "get"
    });
}

// 获取资产总览列表
export const getShopAccountList = (params?: object) => {
    return request<AccountFilterResult>({
        url: "merchant/shopAccount/logList",
        method: "get",
        params
    });
}

// 获取店铺协议
export const getShopAgreement = () => {
    return request<AccountFormState>({
        url: "merchant/shopAccount/applyShopAgreement",
        method: "get"
    });
}

// 获取员工信息展示
export const getStaffShow = () => {
    return request<StaffFrom>({
        url: "merchant/shop/staffShow",
        method: "get"
    });
}
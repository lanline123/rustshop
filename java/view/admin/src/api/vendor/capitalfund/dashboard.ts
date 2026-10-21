import request from "@/utils/request";
import type { AccountFilterResult, StaffFrom, AccountFormState} from "@/types/vendor/capitalfund/dashboard.d";
// 获取资金总览数据
export const getShopAccount = () => {
    return request<AccountFormState>({
        url: "vendor/vendorAccount/index",
        method: "get"
    });
}

// 获取资产总览列表
export const getShopAccountList = (params?: object) => {
    return request<AccountFilterResult>({
        url: "vendor/vendorAccount/logList",
        method: "get",
        params
    });
}

// 获取店铺协议
export const getShopAgreement = () => {
    return request<AccountFormState>({
        url: "vendor/vendorAccount/applyShopAgreement",
        method: "get"
    });
}


// 平台端获取资金总览数据
export const getAdminVendorAccount = () => {
    return request<AccountFormState>({
        url: "vendor/adminVendorAccount/index",
        method: "get"
    });
}


// 平台端获取资产总览列表
export const getAdminVendorLogList = (params?: object) => {
    return request<AccountFilterResult>({
        url: "vendor/adminVendorAccount/logList",
        method: "get",
        params
    });
}


// 供应商资金列表
export const getAdminVendorListFund = (params: object) => {
    return request<AccountFilterResult>({
        url: "vendor/adminVendorAccount/listFund",
        method: "get",
        params
    });
}

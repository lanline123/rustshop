import request from "@/utils/request";
import type { AdminUserFilterParams, AdminUserFilterResult, AdminUserFormState, VendorConfig } from "@/types/vendor/setting";
//获取供应商配置
export const vendorSettings = () => {
    return request({
        url: "setting/config/vendorSettings",
        method: "get"
    });
};

// 保存供应商配置
export const saveVendor = (data: object) => {
    return request({
        url: "setting/config/saveVendor",
        method: "post",
        data
    });
};

// 获取供应商管理员列表
export const getAdminVendorUserList = (params: AdminUserFilterParams) => {
    return request<AdminUserFilterResult>({
        url: "vendor/adminVendorUser/list",
        method: "get",
        params
    });
};

//获取详情
export const getAdminVendorUser = (action: string, params: object) => {
    return request<AdminUserFormState>({
        url: "vendor/adminVendorUser/" + action,
        method: "get",
        params
    });
};

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "vendor/adminVendorUser/batch",
        method: "post",
        data: { type, ...data }
    });
};

// 删除
export const delAdminUser = (data: object) => {
    return request({
        url: "vendor/adminVendorUser/del",
        method: "post",
        data
    });
};

// 更新列表
export const updateAdminUser = (operation: string, data: object) => {
    return request({
        url: "vendor/adminVendorUser/" + operation,
        method: "post",
        data
    });
};

//获取登录协议
export const getLoginProtocol = () => {
    return request<any>({
        url: "setting/config/getLoginProtocol",
        method: "get"
    });
};

//获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务
export const getLoginProtocolContent = (params: object) => {
    return request<any>({
        url: "setting/config/getLoginProtocolContent",
        method: "get",
        params
    });
};

// 保存登录协议
export const saveLoginProtocol = (data: object) => {
    return request({
        url: "setting/config/saveLoginProtocol",
        method: "post",
        data
    });
};

//获取供应商端供应商配置
export const vendorSetting = () => {
    return request<VendorConfig>({
        url: "vendor/vendor/currentDetail",
        method: "get"
    });
};
//保存供应商端供应商配置
export const saveVendorSetting = (data: object) => {
    return request({
        url: "vendor/vendor/setting",
        method: "post",
        data
    });
};
//修改供应商配置详情
export const saveVendorUpdateInfo = (data: object) => {
    return request({
        url: "vendor/vendor/updateInfo",
        method: "post",
        data
    });
};
// 获取员工信息展示
export const getVendorStaffShow = () => {
    return request<any>({
        url: "vendor/vendor/staffShow",
        method: "get"
    });
};

//店铺端获取供应商配置
export const vendorShopSettings = () => {
    return request({
        url: "merchant/shop/getVendorSetting",
        method: "get"
    });
};

// 店铺端保存供应商配置
export const saveShopVendor = (data: object) => {
    return request({
        url: "merchant/shop/updateVendorSetting",
        method: "post",
        data
    });
};

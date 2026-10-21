import type { accountManageResult, accountManageParams, accountManageFilterParams, mainAccountParams, pageAdminUserFilterParams, pageAdminUserResponse } from "@/types/authority/accountManage.d";
import request from "@/utils/request";

//根据店铺或供应商ID查询主账号信息
export const getMainAccount = (params: object) => {
    return request<accountManageParams>({
        url: "admin/adminAccount/getMainAccount",
        method: "get",
        params,
    });
}

//根据主账号ID和类型查询账号列表
export const getPageShopOrVendor = (params: accountManageFilterParams) => {
    return request<accountManageResult>({
        url: "admin/adminAccount/pageShopOrVendor",
        method: "get",
        params,
    });
}


// 根据主账号ID和店铺ID/供应商ID绑定账号
export const bindMainAccount = (data: mainAccountParams) => {
    return request({
        url: "admin/adminAccount/bindMainAccount",
        method: "post",
        data,
    });
}

//修改主账号密码
export const updateMainAccountPwd = (data: object) => {
    return request({
        url: "admin/adminAccount/updateMainAccountPwd",
        method: "post",
        data,
    });
}

//修改主账号信息
export const updateMainAccount = (data: object) => {
    return request({
        url: "admin/adminAccount/updateMainAccount",
        method: "post",
        data,
    });
}


//单独界面账号管理列表
export const getPageAdminUser = (params: pageAdminUserFilterParams) => {
    return request<pageAdminUserResponse>({
        url: "admin/adminAccount/pageAdminUser",
        method: "get",
        params,
    });
}

// 删除管理员账号（软删除）
export const deleteAdminUser = (params: { adminId: number }) => {
    return request({
        url: "admin/adminAccount/deleteAdminUser",
        method: "post",
        params
    });
}


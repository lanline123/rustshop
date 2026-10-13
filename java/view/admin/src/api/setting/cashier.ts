import type {PosFormState, PosFilterResult} from "@/types/setting/cashier.d";
import request from "@/utils/request";

//获取列表
export const getAdminUserPos = (params:Object) => {
    return request<PosFilterResult>({
        url: "pos/adminUserPos/list",
        method: "get",
        params,
    });
}

//获取详情
export const getAdminUserPosDetail = (params:Object) => {
    return request<PosFormState>({
        url: "pos/adminUserPos/detail",
        method: "get",
        params,
    });
}

//新增、编辑
export const updateAdminUserPos = (operation: string, data: object) => {
    return request({
        url: "pos/adminUserPos/" + operation,
        method: "post",
        data
    });
}

// 修改收银台员工用户
export const adminUserPosModifyUser = (data: object) => {
    return request({
        url: "pos/adminUserPos/modifyUser",
        method: "post",
        data
    });
}


//删除收银台员工
export const delAdminUserPos = (data: object) => {
    return request({
        url: "pos/adminUserPos/del",
        method: "post",
        data
    });
}


//批量删除收银台员工
export const batchSubmit = (data: object) => {
    return request({
        url: "pos/adminUserPos/batch",
        method: "post",
        data
    });
}

// 设备码与打印机绑定相关接口：后端就绪前由前端适配层承接，字段保持可选以兼容旧接口。
export const getCashierDeviceList = (params: Object) => request({ url: "pos/device/list", method: "get", params });
export const getCashierDeviceAll = () => request({ url: "pos/device/all", method: "get" });
export const getCashierDeviceDetail = (params: Object) => request({ url: "pos/device/detail", method: "get", params });
export const updateCashierDevice = (operation: string, data: object) => request({ url: `pos/device/${operation}`, method: "post", data });
export const updateCashierDeviceStatus = (data: object) => request({ url: "pos/device/updateField", method: "post", data });
export const deleteCashierDevice = (data: object) => request({ url: "pos/device/del", method: "post", data });

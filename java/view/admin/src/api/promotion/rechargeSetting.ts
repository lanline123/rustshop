import request from "@/utils/request";
import { RechargeSettingFilterResult, RechargeSettingFormState } from "@/types/promotion/rechargeSetting.d";
// 获取余额充值列表
export const getRechargeSettingList = (params: object) => {
    return request<RechargeSettingFilterResult>({
        url: "promotion/rechargeSetting/list",
        method: "get",
        params,
    });
}
// 余额充值列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "promotion/rechargeSetting/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 余额充值列表页面删除项
export const delRechargeSetting = (data: object) => {
    return request({
        url: "promotion/rechargeSetting/del",
        method: "post",
        data,
    });
}
// 余额充值列表页面更新项
export const updateRechargeSettingFiled = (data: object) => {
    return request({
        url: "promotion/rechargeSetting/updateField",
        method: "post",
        data,
    });
}
// 获取余额充值详情
export const getRechargeSetting = (action: string, params: object) => {
    return request<RechargeSettingFormState>({
        url: "promotion/rechargeSetting/" + action,
        method: "get",
        params
    });
}
// 更新余额充值
export const updateRechargeSetting = (operation: string, data: object) => {
    return request({
        url: "promotion/rechargeSetting/" + operation,
        method: "post",
        data
    });
}

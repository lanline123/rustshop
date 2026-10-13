import request from "@/utils/request";
import { SignInSettingFilterResult, SignInSettingFormState } from "@/types/promotion/signInSetting.d";
// 获取签到设置列表
export const getSignInSettingList = (params: object) => {
    return request<SignInSettingFilterResult>({
        url: "promotion/signInSetting/list",
        method: "get",
        params,
    });
}
// 获取签到设置详情
export const getSignInSetting = (action: string, params: object) => {
    return request<SignInSettingFormState>({
        url: "promotion/signInSetting/" + action,
        method: "get",
        params
    });
}
// 更新签到设置
export const updateSignInSetting = (operation: string, data: object) => {
    return request({
        url: "promotion/signInSetting/" + operation,
        method: "post",
        data
    });
}

// 列表行内更新字段（如排序）
export const updateSignInSettingField = (data: object) => {
    const params = data as { id: number; field: string; val: unknown };
    return request({
        url: "promotion/signInSetting/updateField",
        method: "post",
        data: {
            ...params,
            val: String(params.val ?? "")
        }
    });
};

export const delSignInSetting = (data: object) => request({
    url: "promotion/signInSetting/del",
    method: "post",
    data
});

export const batchSignInSetting = (type: string, data: object) => request({
    url: "promotion/signInSetting/batch",
    method: "post",
    data: { type, ...data }
});

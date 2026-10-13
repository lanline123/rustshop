import request from "@/utils/request";
import {UserCompanyFilterParams, UserCompanyFilterResult, UserCompanyFormState} from "@/types/user/userCertification.d"
// 获取会员列表
export const getUserCompanyList = (params: UserCompanyFilterParams) => {
    return request<UserCompanyFilterResult>({
        url: "user/userCompany/list",
        method: "get",
        params,
    });
}

// 删除
export const delUserCompany= (data: object) => {
    return request({
        url: "user/userCompany/del",
        method: "post",
        data,
    });
}

//获取会员详情
export const getUserCompany  = (action: string, params: object) => {
    return request<UserCompanyFormState>({
        url: "user/userCompany/" + action,
        method: "get",
        params
    });
}

// 更新用户信息
export const AuditUserCompany = (data: object) => {
    return request({
        url: "user/userCompany/audit",
        method: "post",
        data
    });
}

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "user/userCompany/batch",
        method: "post",
        data: {type, ...data},
    });
}
export const updateUserCompanyField = (data:object) => {
    return request({
        url: "user/userCompany/updateField",
        method: "post",
        data
    });
}

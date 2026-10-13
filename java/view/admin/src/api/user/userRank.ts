import request from "@/utils/request";
import {UserRankFilterParams, UserRankFormState} from "@/types/user/userRank.d"
import {UserFilterResult} from "@/types/user/levelManage.d"
// 获取商品分类列表
export const getUserRankList = (params: UserRankFilterParams) => {
    return request<UserFilterResult>({
        url: "user/userRank/list",
        method: "get",
        params,
    });
}

// 删除
export const delUserRank= (data: object) => {
    return request({
        url: "user/userRank/del",
        method: "post",
        data,
    });
}

export const recallUserRank= (data: object) => {
    return request({
        url: "user/userRank/recall",
        method: "post",
        data,
    });
}
//获取分类详情
export const getUserRank  = (action: string, params: object) => {
    return request<UserRankFormState>({
        url: "user/userRank/" + action,
        method: "get",
        params
    });
}

// 更新分类
export const updateUserRank = (operation: string, data: object) => {
    return request({
        url: "user/userRank/" + operation,
        method: "post",
        data
    });
}

// 批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "user/userRank/batch",
        method: "post",
        data: {type, ...data},
    });
}
export const updateUserRankField = (data:object) => {
    return request({
        url: "user/userRank/updateField",
        method: "post",
        data
    });
}

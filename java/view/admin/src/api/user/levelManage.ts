import request from "@/utils/request";
import {UserFilterParams, UserFilterResult, UserFormResult} from "@/types/user/levelManage.d"
// 获取会员等级列表
export const getUserRankList = (params: UserFilterParams) => {
    return request<UserFilterResult>({
        url: "user/userRank/list",
        method: "get",
        params,
    });
}
// 获取PRO会员等级列表
export const getUserRankListPro = (params: UserFilterParams) => {
    return request<UserFilterResult>({
        url: "user/userRank/listByPro",
        method: "get",
        params,
    });
}

//获取会员等级详情
export const getUserRank = (action: string) => {
    return request<any>({
        url: "user/userRank/" + action,
        method: "get"
    });
}

// 更新用户等级信息
export const updateUserRank = (operation: string, data: object) => {
    return request({
        url: "user/userRank/" + operation,
        method: "post",
        data
    });
}

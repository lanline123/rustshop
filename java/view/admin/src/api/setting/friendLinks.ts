import type {FriendLinksFilterParams, FriendLinksFilterResult,FriendLinksFormState} from "@/types/setting/friendLinks.d";
import request from "@/utils/request";

export const getFriendLinksList = (params: FriendLinksFilterParams) => {
    return request<FriendLinksFilterResult>({
        url: "setting/friendLinks/list",
        method: "get",
        params,
    });
}

export const getFriendLinksAll = () => {
    return request<FriendLinksFilterResult>({
        url: "setting/friendLinks/getAll",
        method: "get"
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/friendLinks/batch",
        method: "post",
        data: {type, ...data},
    });
}

export const delFriendLinks = (data: object) => {
    return request({
        url: "setting/friendLinks/del",
        method: "post",
        data,
    });
}

export  const getFriendLinks = (action: string, params: object) => {
    return request<FriendLinksFormState>({
        url: "setting/friendLinks/" + action,
        method: "get",
        params
    });
}

export const updateFriendLinks = (operation: string, data: object) => {
    return request({
        url: "setting/friendLinks/" + operation,
        method: "post",
        data
    });
}

export const updateFriendLinksField = (data:object) => {
    return request({
        url: "setting/friendLinks/updateField",
        method: "post",
        data
    });
}

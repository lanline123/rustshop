import request from "@/utils/request";
import type {AuthorityFilterResult, AuthorityFormState,AuthorityFilterParams} from "@/types/authority/authority";

// 获取商品分类列表
export const getAuthorityList = (params: AuthorityFilterParams) => {
    return request<AuthorityFilterResult>({
        url: "authority/authority/list",
        method: "get",
        params,
    });
}

export const getAllAuthority = (params?: object) => {
    return request<AuthorityFormState[]>({
        url: "authority/authority/getAllAuthority",
        method: "get",
        params,
    });
}

// 测试数据
export const getAllAuthorityData = (params?: object) => {
    return request<AuthorityFormState[]>({
        url: "authority/authority/getAuthority",
        method: "get",
        params,
    });
}

// 商品分类页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "authority/authority/batch",
        method: "post",
        data: {type, ...data}
    });
}

// 商品分类页面更新项
export const updateAuthorityFiled = (data: object) => {
    return request({
        url: "authority/authority/updateField",
        method: "post",
        data,
    });
}

export const delAuthority = (data: object) => {
    return request({
        url: "authority/authority/del",
        method: "post",
        data,
    });
}

export  const getAuthority = (action: string, params: object) => {
    return request<AuthorityFormState>({
        url: "authority/authority/" + action,
        method: "get",
        params
    });
}

export const updateAuthority = (operation: string, data: object) => {
    return request({
        url: "authority/authority/" + operation,
        method: "post",
        data
    });
}




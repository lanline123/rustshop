import request from "@/utils/request";
import type {
    CreditUserCreateBody,
    CreditUserDeleteBody,
    CreditUserDetail,
    CreditUserDetailQuery,
    CreditUserListPage,
    CreditUserListQuery,
    CreditUserUpdateFieldBody,
    CreditUserUpdateBody
} from "@/types/termPay/creditUser.d";

export function getCreditUserList(params: CreditUserListQuery) {
    return request<CreditUserListPage>({
        url: "/credit/user/list",
        method: "get",
        params
    });
}

export function getCreditUserDetail(params: CreditUserDetailQuery) {
    return request<CreditUserDetail>({
        url: "/credit/user/detail",
        method: "get",
        params
    });
}

export function createCreditUser(data: CreditUserCreateBody) {
    return request<null>({
        url: "/credit/user/create",
        method: "post",
        data
    });
}

export function updateCreditUser(data: CreditUserUpdateBody) {
    return request<null>({
        url: "/credit/user/update",
        method: "post",
        data
    });
}

export function deleteCreditUser(data: CreditUserDeleteBody) {
    return request<null>({
        url: "/credit/user/delete",
        method: "post",
        data
    });
}

/**
 * 授信会员启用/禁用（字段更新）
 * @description POST /adminapi/credit/user/updateField
 */
export function updateCreditUserField(data: CreditUserUpdateFieldBody) {
    return request<null>({
        url: "/credit/user/updateField",
        method: "post",
        data
    });
}

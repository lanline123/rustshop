import request from "@/utils/request";
import {
    UserTagFilterParams,
    UserTagFilterResult,
    UserTagDetailResult,
    TagItem,
    MemberTagCreateParams,
    MemberTagUpdateParams,
    MemberTagGroupCreateParams,
    MemberTagGroupUpdateParams,
    UserMemberTagItem,
    BindTagsParams,
    UnbindTagsParams,
    BatchBindTagsParams,
    BatchUnbindTagsParams
} from "@/types/user/userTags";

// ==================== 标签组管理 ====================

export const getMemberTagGroupList = (params: UserTagFilterParams) => {
    return request<UserTagFilterResult>({
        url: "user/memberTagGroup/list",
        method: "get",
        params
    });
};

export const getMemberTagGroupDetail = (params: { id: number }) => {
    return request<UserTagDetailResult>({
        url: "user/memberTagGroup/detail",
        method: "get",
        params
    });
};

export const createMemberTagGroup = (data: MemberTagGroupCreateParams) => {
    return request({
        url: "user/memberTagGroup/create",
        method: "post",
        data
    });
};

export const updateMemberTagGroup = (data: MemberTagGroupUpdateParams) => {
    return request({
        url: "user/memberTagGroup/update",
        method: "post",
        data
    });
};

export const delMemberTagGroup = (data: { id: number }) => {
    return request({
        url: "user/memberTagGroup/del",
        method: "post",
        data
    });
};

export const setTopMemberTagGroup = (data: { id: number }) => {
    return request({
        url: "user/memberTagGroup/setTop",
        method: "post",
        data
    });
};

export const cancelTopMemberTagGroup = (data: { id: number }) => {
    return request({
        url: "user/memberTagGroup/cancelTop",
        method: "post",
        data
    });
};

// ==================== 单标签管理 ====================

export const getMemberTagListByGroup = (params: { groupId: number }) => {
    return request<TagItem[]>({
        url: "user/memberTag/listByGroup",
        method: "get",
        params
    });
};

export const createMemberTag = (data: MemberTagCreateParams) => {
    return request({
        url: "user/memberTag/create",
        method: "post",
        data
    });
};

export const updateMemberTag = (data: MemberTagUpdateParams) => {
    return request({
        url: "user/memberTag/update",
        method: "post",
        data
    });
};

export const delMemberTag = (data: { id: number }) => {
    return request({
        url: "user/memberTag/del",
        method: "post",
        data
    });
};

// ==================== 用户标签关联管理 ====================

export const getUserMemberTagList = (params: { userId: number }) => {
    return request<UserMemberTagItem[]>({
        url: "user/userMemberTag/listByUser",
        method: "get",
        params
    });
};

export const bindUserTags = (data: BindTagsParams) => {
    return request({
        url: "user/userMemberTag/bindTags",
        method: "post",
        data
    });
};

export const unbindUserTags = (data: UnbindTagsParams) => {
    return request({
        url: "user/userMemberTag/unbindTags",
        method: "post",
        data
    });
};

export const batchBindUserTags = (data: BatchBindTagsParams) => {
    return request({
        url: "user/userMemberTag/batchBindTags",
        method: "post",
        data
    });
};

export const batchUnbindUserTags = (data: BatchUnbindTagsParams) => {
    return request({
        url: "user/userMemberTag/batchUnbindTags",
        method: "post",
        data
    });
};

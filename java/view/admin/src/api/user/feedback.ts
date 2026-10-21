import request from "@/utils/request";
import {FeedbackFilterParams, FeedbackFilterResult, FeedbackFormState} from "@/types/user/feedback"
// 获取会员留言列表
export const getFeedbackList = (params: FeedbackFilterParams) => {
    return request<FeedbackFilterResult>({
        url: "user/feedback/list",
        method: "get",
        params,
    });
}

// 会员留言删除
export const delFeedback= (data: object) => {
    return request({
        url: "user/feedback/del",
        method: "post",
        data,
    });
}

export const recallFeedback= (data: object) => {
    return request({
        url: "user/feedback/recall",
        method: "post",
        data,
    });
}
//获取会员留言详情
export const getFeedback  = (action: string, params: object) => {
    return request<FeedbackFormState>({
        url: "user/feedback/" + action,
        method: "get",
        params
    });
}

// 更新会员留言分类
export const updateFeedback = (operation: string, data: object) => {
    return request({
        url: "user/feedback/" + operation,
        method: "post",
        data
    });
}

// 批量操作会员留言
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "user/feedback/batch",
        method: "post",
        data: {type, ...data},
    });
}
export const updateFeedbackField = (data:object) => {
    return request({
        url: "user/feedback/updateField",
        method: "post",
        data
    });
}

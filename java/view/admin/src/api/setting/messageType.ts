import request from "@/utils/request";
import type {MessageTypeFilterParams, MessageTypeFilterResult, MessageTypeFormState} from "@/types/setting/messageType";
// 获取示例模板列表
export const getMessageTypeList = (params: MessageTypeFilterParams) => {
    return request<MessageTypeFilterResult>({
        url: "setting/messageType/list",
        method: "get",
        params,
    });
}
// 示例模板列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "setting/messageType/batch",
        method: "post",
        data: {type, ...data},
    });
}
// 示例模板列表页面删除项
export const delMessageType = (data: object) => {
    return request({
        url: "setting/messageType/del",
        method: "post",
        data,
    });
}
// 示例模板列表页面更新项
export const updateMessageTypeFiled = (data: object) => {
    return request({
        url: "setting/messageType/updateField",
        method: "post",
        data,
    });
}

export const miniProgramMessageTemplate = () => {
    return request({
        url: "setting/messageType/miniProgramMessageTemplate",
        method: "post"
    });
}

export const miniProgramMessageTemplateSync = () => {
    return request({
        url: "setting/messageType/miniProgramMessageTemplateSync",
        method: "post"
    });
}

export const wechatMessageTemplate = () => {
    return request({
        url: "setting/messageType/wechatMessageTemplate",
        method: "post"
    });
}

export const wechatMessageTemplateSync = () => {
    return request({
        url: "setting/messageType/wechatMessageTemplateSync",
        method: "post"
    });
}


// 获取示例模板详情
export const getMessageType = (action: string, params: object) => {
    return request<MessageTypeFormState[]>({
        url: "setting/messageType/" + action,
        method: "get",
        params
    });
}
// 更新示例模板
export const updateMessageType = (operation: string, data: object) => {
    return request({
        url: "setting/messageType/" + operation,
        method: "post",
        data
    });
}

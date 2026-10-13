import request from "@/utils/request";
import type { SearchInfoList, MessageList, MessageDetail } from "@/types/im/im.d";

/**
 * 获取会话列表
 * @description 获取客服会话列表数据
 * @param params - 查询参数
 * @returns 会话列表
 */
export const getConversation = (params: {}) => {
    return request<{records:SearchInfoList[]}>({
        prefix: "/im",
        url: "conversation/conversation/list",
        method: "get",
        params
    });
};
/**
 * 获取会话消息
 * @description 获取指定会话的消息列表
 * @param params - 会话ID等参数
 * @returns 消息列表
 */
export const getConversationMessage = (params: {}) => {
    return request<{records:MessageList[]}>({
        prefix: "/im",
        url: "conversation/message/list",
        method: "get",
        params
    });
};

/**
 * 获取客服消息播报记录列表（需求 #646738）
 * @description 语音播报「播报记录」抽屉客服消息 tab 专用：拉取当前登录客服/店铺收到的用户消息（type=1），
 *  与 SSE customerMsgCount 播报同口径（服务端固定 shopId=header、servantId=当前adminId、type=1、status=1），
 *  按 send_time 倒序分页。
 * @param params - 分页参数 { page, size }
 * @returns 消息记录分页
 */
export const getCustomerMsgReceivedList = (params: { page: number; size: number }) => {
    return request<{ records: MessageList[]; total: number }>({
        prefix: "/im",
        url: "conversation/message/receivedList",
        method: "get",
        params
    });
};

/**
 * 获取会员信息
 * @description 获取会话中会员的详细信息
 * @param params - 查询参数
 * @returns 会员信息
 */
export const getUserInfo = (params: {}) => {
    return request({
        prefix: "/im",
        url: "conversation/message/list",
        method: "get",
        params
    });
};
/**
 * 发送消息
 * @description 客服发送消息给用户
 * @param data - 消息内容
 * @returns 发送结果
 */
export const sendMessage = (data: {}) => {
    return request<any>({
        prefix: "/im",
        url: "conversation/message/send",
        method: "post",
        data
    });
};

/**
 * 设置消息已读
 * @description 标记会话消息为已读状态
 * @param data - 会话ID
 * @returns 操作结果
 */
export const setRead = (data: {}) => {
    return request({
        prefix: "/im",
        url: "conversation/message/setRead",
        method: "post",
        data
    });
};
/**
 * 删除会话
 * @description 删除指定会话记录
 * @param data - 会话ID(conversationId)
 * @returns 操作结果
 */
export const delConversation = (data: {}) => {
    return request({
        prefix: "/im",
        url: "conversation/conversation/del",
        method: "post",
        data
    });
};

/**
 * 更新会话字段
 * @description 更新会话字段
 * @param data - status
 * @returns 操作结果
 */
export const updateFieldConversation = (data: {}) => {
    return request({
        prefix: "/im",
        url: "conversation/conversation/updateField",
        method: "post",
        data
    });
};

/**
 * 获取会话详情
 * @description 获取会话的详细信息
 * @param params - 会话ID
 * @returns 会话详情
 */
export const getConversationDetail = (params: {}) => {
    return request<MessageDetail>({
        prefix: "/im",
        url: "conversation/conversation/detail",
        method: "get",
        params
    });
};

/**
 * 保存会话备注
 * @description 客服为会话添加备注信息
 * @param data - 备注内容
 * @returns 操作结果
 */
export const conversationSaveRemark = (data: object) => {
    return request({
        prefix: "/im",
        url: "conversation/conversation/saveRemark",
        method: "post",
        data
    });
};

/**
 * 创建会话
 * @description 客服主动发起新会话
 * @param data - 会话参数
 * @returns 新会话信息
 */
export const conversationCreate = (data: object) => {
    return request<MessageList>({
        prefix: "/im",
        url: "conversation/conversation/create",
        method: "post",
        data
    });
};


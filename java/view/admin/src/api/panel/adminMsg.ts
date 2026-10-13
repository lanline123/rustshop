import request from "@/utils/request";
import type {AdminMsgFilterParams, AdminMsgSearchFilterResult, AdminMsgMsgTypeFilterState} from "@/types/panel/adminMsg.d";
import type {VoiceBroadcastRecordListResult} from "@/types/voiceBroadcast.d";

// 获取消息消息类别
export const getAdminMsgType = () => {
    return request<AdminMsgMsgTypeFilterState[]>({
        url: "msg/adminMsg/msgTypeArr",
        method: "get",
    });
}

// 获取消息中心列表
export const getAdminMsgList = (params: AdminMsgFilterParams) => {
    return request<AdminMsgSearchFilterResult>({
        url: "msg/adminMsg/list",
        method: "get",
        params,
    });
}

// 语音播报内容列表（含商品摘要，供播报抽屉一次性渲染，避免逐条拉订单/售后详情）
export const getVoiceBroadcastRecordList = (params: AdminMsgFilterParams) => {
    return request<VoiceBroadcastRecordListResult>({
        url: "msg/adminMsg/broadcastList",
        method: "get",
        params,
    });
}

// 已读单条消息
export const getAdminMsgSetReaded = (data: object) => {
    return request({
        url: "msg/adminMsg/setReaded",
        method: "post",
        data,
    });
}

// 已读全部消息
export const getAdminMsgSetAllReaded = () => {
    return request({
        url: "msg/adminMsg/setAllReaded",
        method: "post"
    });
}

// 一键直达
export const getSearchMenu = (params:Object) => {
    return request({
        url: "panel/panel/searchMenu",
        method: "get",
        params
    });
}

/**
 * 语音播报「右上角弹窗提示」
 *
 * @description 需求 #646738 弹窗改造：开启「弹窗设置」后，收到播报增量不再直接弹出记录抽屉，
 *   改为在页面右上角弹出一条轻量提示（ant-design-vue notification）。
 *   用户点击提示后跳转到「语音播报内容」抽屉的对应类型 tab，并自动展开最新一条记录详情。
 *
 * 时序沿用「先播报再弹出」：由 SSEAdapter 在 speakable 时挂到 voiceBroadcaster.onceSpoken，
 *   在语音真正开始发声时才弹出提示；仅弹窗不发声时立即弹出。
 *
 * 文案不含条数：无论合并多少条增量，统一展示固定提示文案（与语音播报 MESSAGE_TEMPLATES 同口径）。
 */
import { notification } from "ant-design-vue";
import type { VoiceMsgType } from "@/types/voiceBroadcast.d";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";

/** 各类型提示标题（notification message） */
const POPUP_TITLE: Record<VoiceMsgType, string> = {
    newOrder: "新订单",
    paidOrder: "已付款订单",
    aftersale: "售后申请",
    cancelOrder: "订单取消",
    customerMsg: "客服消息"
};

/** 各类型提示正文（不含条数，与语音播报文案同口径） */
const POPUP_DESC: Record<VoiceMsgType, string> = {
    newOrder: "您有新的订单，请及时处理",
    paidOrder: "您有新的付款订单，请安排发货",
    aftersale: "您有新的售后申请，请及时处理",
    cancelOrder: "您有订单被取消，请注意",
    customerMsg: "您有新的客服消息"
};

/**
 * 弹出一条右上角提示
 *
 * @param type 播报类型；点击提示后定位到记录抽屉对应 tab 并展开首条详情
 */
export function showVoiceBroadcastPopup(type: VoiceMsgType): void {
    const key = `voice-broadcast-popup-${type}-${Date.now()}`;
    notification.open({
        key,
        message: POPUP_TITLE[type],
        description: POPUP_DESC[type],
        placement: "topRight",
        duration: 6,
        onClick: () => {
            // 点击提示：打开记录抽屉、定位对应 tab 并请求展开首条详情
            useVoiceBroadcastStore().requestRecordDetail(type);
            notification.close(key);
        }
    });
}

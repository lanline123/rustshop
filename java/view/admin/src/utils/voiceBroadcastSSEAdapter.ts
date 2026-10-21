/**
 * SSE → 语音播报增量分发器
 *
 * @description 接收 MsgCount.vue SSE 通道每次到达的 payload，
 *              与上一次 payload 做增量比较，按 5 类映射分发到 voiceBroadcaster。
 *
 *              依赖后端 v3 起所有播报计数单调递增（newOrder/cancelOrder 天然单调，
 *              paidOrder/aftersale 改单调口径，customerMsg 用单调字段 customerMsgCount）。
 *              缺字段时视为不存在，静默跳过，前端 UI 仍可见可切换开关。
 *
 * @see .ai-os/specs/admin-msg-tts.spec.md REQ-03
 */
import type {
    VoiceBroadcastSSEPayload,
    VoiceMsgType
} from "@/types/voiceBroadcast.d";
import { voiceBroadcaster } from "@/utils/voiceBroadcaster";
import { voiceBroadcastLeader } from "@/utils/voiceBroadcastLeader";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { showVoiceBroadcastPopup } from "@/utils/voiceBroadcastPopup";

/**
 * VoiceMsgType → SSE payload 字段名 映射（前端硬契约）
 *
 * v4 起 4 类业务消息改读 admin_msg 来源字段（*MsgCount），与消息中心同源、同过滤口径，
 * 使每次播报都对应一条消息中心可见的消息（事件级一致）。
 * 旧的 orderCount/paidOrderCount/aftersaleCount/cancelOrderCount 后端仍下发（红点/兼容），此处不再消费。
 */
const FIELD_MAP: Record<VoiceMsgType, keyof VoiceBroadcastSSEPayload> = {
    newOrder: "newOrderMsgCount",
    paidOrder: "paidOrderMsgCount",
    aftersale: "aftersaleMsgCount",
    cancelOrder: "cancelOrderMsgCount",
    // 客服播报仍走 IM 旁路的单调累计字段 customerMsgCount，与 UI 角标的 imMsgCount（未读数，可回落）解耦
    customerMsg: "customerMsgCount"
};

/**
 * VoiceMsgType → SSE payload「未读角标」字段名 映射（记录抽屉 tab 未读气泡用）
 *
 * 与播报字段（FIELD_MAP）区分：角标展示的是当前未读数（与消息中心红点同源）。
 * - 4 类业务消息复用 *MsgCount（自 lastOpenTime 起的未读，同消息中心口径）
 * - 客服消息用 imMsgCount（可随已读回落的未读数），而非单调累计的 customerMsgCount
 */
const BADGE_FIELD_MAP: Record<VoiceMsgType, keyof VoiceBroadcastSSEPayload> = {
    newOrder: "newOrderMsgCount",
    paidOrder: "paidOrderMsgCount",
    aftersale: "aftersaleMsgCount",
    cancelOrder: "cancelOrderMsgCount",
    customerMsg: "imMsgCount"
};

/**
 * 用当前帧 payload 刷新各类型 tab 的未读角标计数（记录抽屉气泡）
 *
 * 与播报增量解耦：每帧（含首帧 / 重连）都执行，只覆盖当前端允许且开关开启（tab 可见）的类型；
 * 字段缺失时视为 0。真正的未读展示 = 绝对计数 - 本地已读基线（见 store.recordUnread）。
 */
function syncRecordBadges(
    store: ReturnType<typeof useVoiceBroadcastStore>,
    payload: VoiceBroadcastSSEPayload
): void {
    const allowedTypes = store.allowedTypes;
    (Object.keys(BADGE_FIELD_MAP) as VoiceMsgType[]).forEach((type) => {
        if (!allowedTypes.includes(type)) return;
        if (!store[type]) return; // 该类型开关关闭：tab 不展示，无需维护角标
        const field = BADGE_FIELD_MAP[type];
        store.setRecordCount(type, Number(payload[field] ?? 0));
    });
}

/**
 * 比较两次 SSE payload，对增量 > 0 的类型按开关分发到播报队列
 *
 * @param prev 上一次 payload；首次或重连后传 null，所有字段 delta 视为 0
 * @param next 当前 payload
 */
export function dispatchVoiceBroadcast(
    prev: VoiceBroadcastSSEPayload | null,
    next: VoiceBroadcastSSEPayload | null | undefined
): void {
    if (!next) return;

    const store = useVoiceBroadcastStore();
    // 浏览器不支持时整个入口隐藏，播报/弹窗均无意义，直接返回
    if (!store.isSupported) return;

    // 未读角标：每帧（含首帧 / 重连）都用绝对未读数刷新，保证打开页面即可见、与后端一致
    syncRecordBadges(store, next);

    // prev=null（首次或重连）：只重建基线，不触发播报/弹窗
    if (prev === null) return;

    const allowedTypes = store.allowedTypes;
    /** 是否满足发声条件（未激活/全关时不发声，但仍可能弹窗） */
    const speakable = store.speakable;
    /**
     * 是否允许弹窗（需求 #646738）：有后端播报权限 + 开关开 + 当前 tab 为 leader。
     * 与语音一致仅 leader tab 触发，避免多 tab 同时弹出记录抽屉造成干扰。
     * 权限与项兜住无权限账号 localStorage 中的存量脏开关（REQ-17）。
     */
    const canPopup = store.hasPermission && store.popupEnabled && voiceBroadcastLeader.isLeader();
    /** 本帧所有有增量的类型（按 FIELD_MAP 顺序 = 语音 FIFO 播报顺序），用于弹窗定位 tab */
    const popupTypes: VoiceMsgType[] = [];

    (Object.keys(FIELD_MAP) as VoiceMsgType[]).forEach((type) => {
        // 当前端不允许的类型：跳过（纵深防御，兜住历史持久化的脏开关，防误播）
        if (!allowedTypes.includes(type)) return;
        if (!store[type]) return; // 该类型开关已关
        const field = FIELD_MAP[type];

        // 后端未补的字段：next 中不存在 → 静默跳过
        if (!(field in next)) return;

        const nextVal = Number(next[field] ?? 0);
        const prevVal = field in prev ? Number(prev[field] ?? 0) : 0;

        const delta = nextVal - prevVal;
        if (delta > 0) {
            // 发声（未激活/不支持时 voiceBroadcaster 内部静默降级）
            if (speakable) voiceBroadcaster.enqueue(type, delta);
            // 收集本帧所有增量类型供弹窗定位
            popupTypes.push(type);
        }
    });

    // 弹窗显示：本帧有新增量且开启弹窗时，在页面右上角弹出提示（不再直接弹出记录抽屉）。
    // 用户点击提示后跳转到记录抽屉对应类型 tab 并展开首条详情（见 voiceBroadcastPopup）。
    // 需求 #646738 时序修正：应「先播报再弹出」。
    //  - 会发声（speakable）时：给每个增量类型都挂 onceSpoken，提示随语音真正开始发声时逐条弹出；
    //  - 仅弹窗不发声（语音关、弹窗开）时：本帧每个增量类型立即各弹一条提示。
    if (canPopup && popupTypes.length > 0) {
        if (speakable) {
            popupTypes.forEach((type) => {
                voiceBroadcaster.onceSpoken(type, () => showVoiceBroadcastPopup(type));
            });
        } else {
            popupTypes.forEach((type) => showVoiceBroadcastPopup(type));
        }
    }
}

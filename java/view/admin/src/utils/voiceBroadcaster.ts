/**
 * 语音播报单例服务
 *
 * @description 维护 5 类消息的本地播报队列、合并窗口、最小间隔节流，
 *              依次驱动浏览器原生 SpeechSynthesis API 发声。
 *
 * 行为契约（见 .ai-os/specs/admin-msg-tts.spec.md REQ-04 / REQ-05 / REQ-12 / REQ-13）：
 *  - 同类 5s 内多次增量合并为一条固定提示文案（不含条数）
 *  - 同类两次播报最小间隔 5s（结束 → 开始锚点）
 *  - 多类同时到达 FIFO 排队、不并发不叠音
 *  - 后台 tab / 最小化时沿用相同节流策略，依赖浏览器原生能力发声（CR-20260615-173101）
 *  - 多 tab 选举：非 leader tab 在 enqueue 入口被守卫拦截，不消耗节流锚点（CR-20260615-173101）
 *  - 浏览器 Autoplay 兜底：首次 enqueue 时挂一次性 pointerdown/keydown 监听，
 *    用户任意手势后自动 speakRaw(' ') 重激活，治愈 F5 后 user activation 失效导致的 'not-allowed'
 *    （CR-20260616-100700-tts-autoplay-policy-fix）
 *  - 浏览器不支持 / 用户未激活 时静默降级
 *  - 缺字段或 store 关闭 由上层（adapter / store）拦截，本模块不感知
 */
import type { VoiceMsgType } from "@/types/voiceBroadcast.d";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { voiceBroadcastLeader } from "@/utils/voiceBroadcastLeader";

// CR-20260602-134700：同类合并窗口与最小间隔由 10s 调整为 5s
/** 同类合并窗口 ms */
const MERGE_WINDOW_MS = 5_000;
/** 同类两次播报最小间隔 ms（结束-开始锚点） */
const MIN_GAP_MS = 5_000;

/**
 * 5 类播报文案模板，仅提示不含条数
 * CR-20260616-092300：由内部常量改为 export，供设置抽屉「试听」按钮复用，
 *                    确保试听文案与真实推送文案单点同源
 */
export const MESSAGE_TEMPLATES: Record<VoiceMsgType, string> = {
    newOrder: "您有新的订单，请及时处理",
    paidOrder: "您有新的付款订单，请安排发货",
    aftersale: "您有新的售后申请，请及时处理",
    cancelOrder: "您有订单被取消，请注意",
    customerMsg: "您有新的客服消息"
};

/** 单类型的运行时状态 */
interface PerTypeState {
    pendingCount: number;
    flushTimer: ReturnType<typeof setTimeout> | null;
    lastSpokenEndAt: number;
}

class VoiceBroadcaster {
    private perType: Map<VoiceMsgType, PerTypeState> = new Map();
    private utteranceQueue: SpeechSynthesisUtterance[] = [];
    private speaking = false;
    /**
     * 一次性「已开始播报」回调（需求 #646738 弹窗时序修正）
     *
     * @description 供上层（SSEAdapter）把「弹出播报记录抽屉」挂到真正的发声事件上，
     *   实现「先播报再弹出」。key=类型，value=待触发回调数组（同帧同类型可能多次注册，取先进先出）。
     *   触发时机：对应 utterance 的 onstart（真正开始发声）；发声因开关关闭/不可发声被跳过或 onerror 时兜底触发，
     *   避免弹窗永远不出现。触发后即清空，保证一次注册只弹一次。
     */
    private spokenCallbacks: Map<VoiceMsgType, Array<() => void>> = new Map();
    /**
     * 是否已挂载 Autoplay 兜底首次手势监听（CR-20260616-100700-tts-autoplay-policy-fix）
     *
     * @description 浏览器 Autoplay Policy 要求 speechSynthesis 在 user activation 上下文中才能 speak。
     *   F5 刷新页面后 user activation 状态被清空、TipDismissed=1 又导致引导条不再显示，
     *   会出现 store.activated=true 但 speak 报 'not-allowed' 的状态。
     *   通过首次 enqueue 时挂一次性全局手势监听，任意一次用户手势触发 speakRaw(' ') 重激活。
     */
    private gestureArmed = false;

    /** 浏览器是否支持 Web Speech API */
    isSupported(): boolean {
        return typeof window !== "undefined" && "speechSynthesis" in window;
    }

    /** 入队一次增量 */
    enqueue(type: VoiceMsgType, count: number): void {
        if (!this.isSupported() || count <= 0) return;
        // CR-20260616-100700：lazy 装载 Autoplay 兜底监听
        this.armOnFirstGesture();
        // CR-20260615-173101：非 leader tab 直接丢弃增量，不消耗节流锚点
        // 避免 leader 切换后非 leader tab 的 lastSpokenEndAt 偏移导致体验异常
        if (!voiceBroadcastLeader.isLeader()) return;

        const state = this.getOrCreate(type);
        state.pendingCount += count;

        const now = Date.now();
        const sinceLastEnd = now - state.lastSpokenEndAt;
        const remainingGap = MIN_GAP_MS - sinceLastEnd;

        if (state.flushTimer !== null) {
            // 已有合并窗口在跑，等其结束统一 flush
            return;
        }

        const delay = remainingGap > 0
            ? Math.max(remainingGap, MERGE_WINDOW_MS)
            : MERGE_WINDOW_MS;

        state.flushTimer = setTimeout(() => this.flush(type), delay);
    }

    /**
     * 注册一次性「该类型已开始播报」回调（需求 #646738 弹窗时序修正）
     *
     * @description 与 enqueue 配套：上层在确认某类型会发声（speakable）后调用，
     *   把「弹出播报记录抽屉」延迟到该类型 utterance 真正开始发声时执行，实现「先播报再弹出」。
     *   回调只触发一次；若该类型在 flush 时因开关关闭/不可发声被跳过，则兜底同步触发（不阻塞弹窗）。
     */
    onceSpoken(type: VoiceMsgType, cb: () => void): void {
        const list = this.spokenCallbacks.get(type) ?? [];
        list.push(cb);
        this.spokenCallbacks.set(type, list);
    }

    /** 触发并清空某类型的一次性「已开始播报」回调 */
    private fireSpoken(type: VoiceMsgType): void {
        const list = this.spokenCallbacks.get(type);
        if (!list || list.length === 0) return;
        this.spokenCallbacks.delete(type);
        list.forEach((cb) => {
            try {
                cb();
            } catch {
                /* 回调异常静默降级，不影响播报链路 */
            }
        });
    }

    /**
     * 用空文本激活一次浏览器自动播放权限
     * 必须在用户手势回调中调用
     */
    speakRaw(text: string): void {
        if (!this.isSupported()) return;
        try {
            const u = new SpeechSynthesisUtterance(text);
            u.lang = "zh-CN";
            window.speechSynthesis.speak(u);
        } catch {
            /* 静默降级 */
        }
    }

    /**
     * 试听某类型文案（CR-20260616-092300）
     *
     * 旁路通道，完全绕过业务管控：
     *  - 不参与 leader 选举（仅在当前 tab 本地发声）
     *  - 不查 store 开关 / activated（试听不受 store 状态影响）
     *  - 不读写 perType 节流锚点（不污染真实推送的 5s 间隔）
     *  - 取消正在播放的 utterance 与本地队列，立即播报新一条（连续点击反馈最直接）
     *
     * 副作用：试听时若有真实推送 utterance 在播放，会被一并打断；
     *        但 setTimeout 中的 pendingCount 不丢失，窗口结束 flush 仍按原节流逻辑重新 speak。
     *
     * 由用户手势（点击试听按钮）触发，浏览器自动播放策略允许此时直接 speak，
     * 无需依赖顶部「立即开启」预激活。
     */
    previewSpeak(type: VoiceMsgType): void {
        if (!this.isSupported()) return;

        try {
            window.speechSynthesis.cancel();
        } catch {
            /* ignore */
        }
        this.utteranceQueue.length = 0;
        this.speaking = false;

        try {
            const u = new SpeechSynthesisUtterance(MESSAGE_TEMPLATES[type]);
            u.lang = "zh-CN";
            window.speechSynthesis.speak(u);
        } catch {
            /* 静默降级 */
        }
    }

    /** 卸载或重置时清空状态 */
    cancelAll(): void {
        if (this.isSupported()) {
            try {
                window.speechSynthesis.cancel();
            } catch {
                /* ignore */
            }
        }
        this.utteranceQueue.length = 0;
        this.speaking = false;
        this.perType.forEach((s) => {
            if (s.flushTimer !== null) clearTimeout(s.flushTimer);
            s.flushTimer = null;
            s.pendingCount = 0;
        });
        // 丢弃未触发的一次性弹窗回调（卸载/重置时不应再弹窗）
        this.spokenCallbacks.clear();
    }

    /** ===== 内部 ===== */

    private getOrCreate(type: VoiceMsgType): PerTypeState {
        let s = this.perType.get(type);
        if (!s) {
            s = { pendingCount: 0, flushTimer: null, lastSpokenEndAt: 0 };
            this.perType.set(type, s);
        }
        return s;
    }

    private flush(type: VoiceMsgType): void {
        const state = this.perType.get(type);
        if (!state) return;
        state.flushTimer = null;

        const count = state.pendingCount;
        state.pendingCount = 0;
        if (count <= 0) return;

        // CR-20260615-173101：后台 tab 沿用相同节流策略、照常发声（依赖浏览器原生能力）
        // 不再在 flush 时按 visibilityState 丢弃 pendingCount

        // 双开关检查：浏览器支持 + 用户已激活 + 该类型开关开
        const store = useVoiceBroadcastStore();
        if (!store.speakable || !store[type]) {
            state.lastSpokenEndAt = Date.now();
            // 不发声时兜底触发弹窗回调，避免「先播报再弹出」注册的弹窗永远不出现
            this.fireSpoken(type);
            return;
        }

        // 不再播报条数：无论合并多少条增量，统一播报固定提示文案
        const text = MESSAGE_TEMPLATES[type];

        const u = new SpeechSynthesisUtterance(text);
        u.lang = "zh-CN";
        // 真正开始发声时再弹窗，保证「先播报再弹出」（需求 #646738 时序修正）
        u.onstart = () => {
            this.fireSpoken(type);
        };
        u.onend = () => {
            const s2 = this.perType.get(type);
            if (s2) s2.lastSpokenEndAt = Date.now();
            this.speaking = false;
            this.drain();
        };
        u.onerror = () => {
            const s2 = this.perType.get(type);
            if (s2) s2.lastSpokenEndAt = Date.now();
            this.speaking = false;
            // onstart 未触发即报错时兜底弹窗，避免弹窗丢失
            this.fireSpoken(type);
            this.drain();
        };
        this.utteranceQueue.push(u);
        this.drain();
    }

    /**
     * Autoplay Policy 兜底：首次手势透明激活（CR-20260616-100700-tts-autoplay-policy-fix）
     *
     * @description 监听 document 上的 pointerdown / keydown，**任意一个触发后**：
     *   1) 同步调用 speakRaw(' ') —— 此时仍在 user gesture 调用栈内，浏览器授予 user activation
     *   2) 立即解绑两个监听器，避免后续重复激活
     *  capture=true 保证早于业务点击拿到事件；幂等：gestureArmed 防止重复挂载
     *
     *  特别说明：speakRaw(' ') 即使在 user activation 仍有效时调用也无副作用（浏览器静默忽略空文本）
     */
    private armOnFirstGesture(): void {
        if (this.gestureArmed) return;
        if (typeof document === "undefined") return;
        this.gestureArmed = true;
        const onFirstGesture = () => {
            document.removeEventListener("pointerdown", onFirstGesture, true);
            document.removeEventListener("keydown", onFirstGesture, true);
            this.speakRaw(" ");
        };
        document.addEventListener("pointerdown", onFirstGesture, true);
        document.addEventListener("keydown", onFirstGesture, true);
    }

    private drain(): void {
        if (this.speaking) return;
        const next = this.utteranceQueue.shift();
        if (!next) return;
        if (!this.isSupported()) return;
        try {
            this.speaking = true;
            window.speechSynthesis.speak(next);
        } catch {
            this.speaking = false;
        }
    }
}

/** 单例 */
export const voiceBroadcaster = new VoiceBroadcaster();

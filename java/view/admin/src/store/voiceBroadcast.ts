/**
 * 语音播报设置 store
 *
 * @description 管理 5 类一级业务消息的语音播报开关与浏览器权限激活状态
 * @persist localStorage（pinia-plugin-persist）
 * @see .ai-os/specs/admin-msg-tts.spec.md REQ-02 / REQ-07
 */
import { defineStore } from "pinia";
import type {
    VoiceBroadcastSettings,
    VoiceMsgType
} from "@/types/voiceBroadcast.d";
import { getAdminType } from "@/utils/storage";

/** 浏览器是否支持 Web Speech API */
function detectSupport(): boolean {
    return typeof window !== "undefined" && "speechSynthesis" in window;
}

/** 全部 5 类播报类型（未知端兜底用） */
const ALL_VOICE_TYPES: VoiceMsgType[] = [
    "newOrder",
    "paidOrder",
    "aftersale",
    "cancelOrder",
    "customerMsg"
];

/**
 * 端（adminType）→ 该端允许播报的消息类型集合
 *
 * 端判定复用项目既有标准 getAdminType()（localStorage adminType），与 menus.ts 等模块一致：
 * admin=平台 / shop=店铺 / store=门店 / vendor=供应商 / pos=收银（独立布局不引用本功能）。
 * 当前唯一差异：供应商端不播报「客服消息」。后续调整任一端只改此表一处。
 *
 * @see .ai-os/baseline-log/CR-20260611-160800-voice-broadcast-per-terminal-type.md
 */
const ALLOWED_TYPES_BY_ADMIN_TYPE: Record<string, VoiceMsgType[]> = {
    admin: ["newOrder", "paidOrder", "aftersale", "cancelOrder", "customerMsg"],
    shop: ["newOrder", "paidOrder", "aftersale", "cancelOrder", "customerMsg"],
    store: ["newOrder", "paidOrder", "aftersale", "cancelOrder", "customerMsg"],
    vendor: ["newOrder", "paidOrder", "aftersale", "cancelOrder"]
};

export const useVoiceBroadcastStore = defineStore("voiceBroadcast", {
    state: (): VoiceBroadcastSettings & {
        hasPermission: boolean;
        panelOpen: boolean;
        recordDrawerOpen: boolean;
        recordActiveType: VoiceMsgType | "";
        recordCount: Record<VoiceMsgType, number>;
        recordViewed: Record<VoiceMsgType, number>;
        recordDetailSignal: number;
        recordDetailType: VoiceMsgType | "";
    } => ({
        newOrder: false,
        paidOrder: false,
        aftersale: false,
        cancelOrder: false,
        customerMsg: false,
        activated: false,
        /** 弹窗显示开关（需求 #646738，持久化） */
        popupEnabled: false,
        /**
         * 后端「语音播报」权限（authoritySn=voiceBroadcastManage）（会话态，不持久化）
         *
         * 由 utils/menus.ts::getMenu() 在页面初始化拉取权限接口后回填。
         * 刻意不持久化：权限是服务端事实，必须每次整页加载重新取，
         * 避免账号被降权后本地缓存仍为 true 形成越权窗口。
         * 默认 false（fail-closed）：权限未就绪时一律按无权限处理。
         *
         * @see .ai-os/specs/admin-msg-tts.spec.md REQ-17
         */
        hasPermission: false,
        /** 设置抽屉是否打开（会话态，不持久化） */
        panelOpen: false,
        /** 播报记录抽屉是否打开（会话态，不持久化） */
        recordDrawerOpen: false,
        /** 播报记录抽屉当前激活的类型 tab（会话态） */
        recordActiveType: "",
        /**
         * 各类型 tab 的后端绝对未读计数（会话态，不持久化）
         *
         * 数据源为 SSE payload 每帧下发的绝对未读数（与消息中心红点同源）：
         *   newOrder/paidOrder/aftersale/cancelOrder → *MsgCount，customerMsg → imMsgCount。
         * 每帧覆盖刷新，保证与后端一致。
         */
        recordCount: {
            newOrder: 0,
            paidOrder: 0,
            aftersale: 0,
            cancelOrder: 0,
            customerMsg: 0
        },
        /**
         * 各类型 tab 的「已读基线」（会话态，不持久化）
         *
         * 用户点击/查看某 tab 时记下当时的 recordCount 为基线；
         * 气泡未读数 = max(0, recordCount - recordViewed)，实现「查看后置 0，新增量再冒泡」。
         */
        recordViewed: {
            newOrder: 0,
            paidOrder: 0,
            aftersale: 0,
            cancelOrder: 0,
            customerMsg: 0
        },
        /**
         * 「打开记录详情」信号（会话态，不持久化）
         *
         * 每次递增触发记录抽屉在对应 tab 记录加载完成后自动展开首条记录详情。
         * 用于右上角弹窗提示点击后「跳转对应 tab 并打开详情」（需求 #646738 弹窗改造）。
         */
        recordDetailSignal: 0,
        /** 「打开记录详情」目标类型（会话态，与 recordDetailSignal 配套） */
        recordDetailType: ""
    }),
    getters: {
        /** 当前端允许播报的类型集合（按 adminType 取，未知端兜底全 5 类） */
        allowedTypes(): VoiceMsgType[] {
            const type = getAdminType();
            return (type && ALLOWED_TYPES_BY_ADMIN_TYPE[type]) || ALL_VOICE_TYPES;
        },
        /** 是否任一开关打开（仅统计当前端允许的类型；与 activated 无关，供入口图标展示） */
        isAnyEnabled(state): boolean {
            return this.allowedTypes.some((type) => state[type]);
        },
        /** 浏览器是否支持 Web Speech API */
        isSupported(): boolean {
            return detectSupport();
        },
        /** 最终是否具备实际发声条件 = 有后端权限 + 支持 + 已激活 + 当前端任一允许类型开启 */
        speakable(state): boolean {
            return (
                state.hasPermission &&
                detectSupport() &&
                state.activated &&
                this.allowedTypes.some((type) => state[type])
            );
        },
        /**
         * 播报记录抽屉需要展示的类型 tab（需求 #646738）
         *
         * 规则：当前端允许（allowedTypes）且该类型播报开关为开时才展示对应 tab。
         * 例：客服消息开关关闭 → 记录抽屉不显示「客服消息」tab。
         */
        recordTabTypes(state): VoiceMsgType[] {
            return this.allowedTypes.filter((type) => state[type]);
        },
        /**
         * 各类型 tab 的未读气泡数（后端绝对未读 - 本地已读基线，下限 0）
         *
         * 打开页面即按后端未读数展示；用户查看某 tab 后基线抬到当前值，气泡归零，新增量再冒泡。
         */
        recordUnread(state): Record<VoiceMsgType, number> {
            const result = {} as Record<VoiceMsgType, number>;
            (Object.keys(state.recordCount) as VoiceMsgType[]).forEach((type) => {
                result[type] = Math.max(0, (state.recordCount[type] || 0) - (state.recordViewed[type] || 0));
            });
            return result;
        }
    },
    actions: {
        /**
         * 回填后端「语音播报」权限（REQ-17）
         *
         * 由 utils/menus.ts::getMenu() 在权限接口返回后调用；
         * 切店铺 / 切角色会重跑 getMenu()，权限随之即时重算。
         */
        setPermission(value: boolean) {
            this.hasPermission = value;
        },
        /** 通用单项开关切换（拦截无权限账号与当前端不允许的类型） */
        setEnabled(type: VoiceMsgType, value: boolean) {
            if (!this.hasPermission) return;
            if (!this.allowedTypes.includes(type)) return;
            this[type] = value;
        },
        /** 一次性把当前端允许的类型置为开启、其余置为关闭（保留"全开"语义，供未来"一键开启全部播报"复用） */
        enableAll() {
            if (!this.hasPermission) return;
            const allowed = new Set(this.allowedTypes);
            this.newOrder = allowed.has("newOrder");
            this.paidOrder = allowed.has("paidOrder");
            this.aftersale = allowed.has("aftersale");
            this.cancelOrder = allowed.has("cancelOrder");
            this.customerMsg = allowed.has("customerMsg");
        },
        /**
         * 按业务默认策略激活（首次引导条「立即开启」调用）
         *
         * @description 把当前端允许且应默认开启的类型置 true，其余（含 newOrder、cancelOrder）置 false。
         *   - newOrder：默认不开（用户首次激活后需要时手动到设置抽屉打开）
         *   - cancelOrder：默认不开（用户首次激活后需要时手动到设置抽屉打开）
         *   - paidOrder / aftersale / customerMsg：默认开（在端允许范围内）
         *
         * @see .ai-os/baseline-log/CR-20260615-173100-tts-default-newOrder-off.md
         */
        enableDefaults() {
            if (!this.hasPermission) return;
            const allowed = new Set(this.allowedTypes);
            this.newOrder = false;
            this.paidOrder = allowed.has("paidOrder");
            this.aftersale = allowed.has("aftersale");
            this.cancelOrder = false;
            this.customerMsg = allowed.has("customerMsg");
        },
        /** 标记用户已激活语音权限（通常由首次引导条「立即开启」调用） */
        markActivated() {
            this.activated = true;
        },
        /** 打开语音播报设置抽屉 */
        openSettingsPanel() {
            this.panelOpen = true;
        },
        /** 设置语音播报设置抽屉开关 */
        setSettingsPanelOpen(open: boolean) {
            this.panelOpen = open;
        },
        /** 设置「弹窗显示」开关（需求 #646738；无权限账号拦截，REQ-17） */
        setPopupEnabled(value: boolean) {
            if (!this.hasPermission) return;
            this.popupEnabled = value;
        },
        /** 设置播报记录抽屉开关 */
        setRecordDrawerOpen(open: boolean) {
            this.recordDrawerOpen = open;
        },
        /** 设置播报记录抽屉当前激活的类型 tab */
        setRecordActiveType(type: VoiceMsgType | "") {
            this.recordActiveType = type;
        },
        /**
         * 更新某类型 tab 的后端绝对未读计数（SSE 每帧下发时调用）
         *
         * 若后端计数回落到已读基线以下（如消息中心整体已读、startTime 变更），
         * 同步把基线下调，避免出现「负未读需要靠新增量才归零」的错乱。
         */
        setRecordCount(type: VoiceMsgType, value: number) {
            const v = Math.max(0, Number(value) || 0);
            this.recordCount[type] = v;
            if (this.recordViewed[type] > v) this.recordViewed[type] = v;
        },
        /** 把某类型 tab 标记为已读：已读基线抬到当前计数，气泡归零（用户点击 / 查看该 tab 时调用） */
        markRecordViewed(type: VoiceMsgType) {
            this.recordViewed[type] = this.recordCount[type] || 0;
        },
        /**
         * 打开播报记录抽屉（可指定定位到某类型 tab）
         *
         * @param type 需要定位的类型；不传则保持/回退到当前允许展示的首个 tab
         */
        openRecordDrawer(type?: VoiceMsgType) {
            const tabs = this.recordTabTypes;
            if (type && tabs.includes(type)) {
                this.recordActiveType = type;
            } else if (!this.recordActiveType || !tabs.includes(this.recordActiveType as VoiceMsgType)) {
                this.recordActiveType = tabs[0] ?? "";
            }
            this.recordDrawerOpen = true;
            // 打开即视为查看当前激活 tab，清零其未读气泡
            if (this.recordActiveType) this.markRecordViewed(this.recordActiveType as VoiceMsgType);
        },
        /**
         * 请求打开某类型的最新一条播报记录详情（右上角弹窗提示点击时调用，需求 #646738 弹窗改造）
         *
         * 先打开播报记录抽屉并定位到对应类型 tab，再抬升信号量；
         * 由记录抽屉在该 tab 记录加载完成后自动展开首条记录的详情弹窗。
         */
        requestRecordDetail(type: VoiceMsgType) {
            this.openRecordDrawer(type);
            this.recordDetailType = type;
            this.recordDetailSignal += 1;
        }
    },
    persist: {
        enabled: true,
        strategies: [
            {
                storage: localStorage,
                paths: [
                    "newOrder",
                    "paidOrder",
                    "aftersale",
                    "cancelOrder",
                    "customerMsg",
                    "activated",
                    "popupEnabled"
                ]
            }
        ]
    }
});

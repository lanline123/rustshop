<!--
/**
 * @file 语音播报内容（播报记录）抽屉
 * @description 需求 #646738：点击顶部喇叭入口打开本抽屉，展示各类语音播报的历史记录。
 *  - 右侧 600px 抽屉，标题「语音播报内容」，右上角齿轮 → 打开语音播报设置抽屉（原设置页转移至此）
 *  - 顶部按类型分 tab：新订单/已付款订单/售后申请/订单取消/客服消息
 *  - tab 按设置动态显隐：某类型播报开关关闭则不显示该 tab（且受当前端 allowedTypes 约束）
 *  - 记录来源复用消息中心 msg/adminMsg/list（按 msgType 过滤），滚动加载更多
 *  - 客服消息为 IM 旁路来源，admin_msg 无对应 msgType，暂以空态提示（待后端接入）
 * @see .agents VortFlow 任务【语音播报-前端/后端】需求 #646738
 */
-->
<template>
    <Drawer
        v-model:open="innerOpen"
        title="语音播报内容"
        placement="right"
        :width="600"
        :destroyOnClose="false"
        :bodyStyle="{ padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }"
    >
        <template #extra>
            <button type="button" class="vb-record-setting-btn" title="语音播报设置" aria-label="语音播报设置" @click="openSettings">
                <svg class="vb-record-setting-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <path
                        d="M469.333333 60.693333a85.333333 85.333333 0 0 1 85.333334 0l326.826666 188.714667a85.333333 85.333333 0 0 1 42.666667 73.898667v377.386666a85.333333 85.333333 0 0 1-42.666667 73.898667L554.666667 963.306667a85.333333 85.333333 0 0 1-85.333334 0L142.506667 774.592a85.333333 85.333333 0 0 1-42.666667-73.898667v-377.386666a85.333333 85.333333 0 0 1 42.666667-73.898667z m42.666667 73.898667L185.173333 323.306667v377.386666L512 889.408l326.826667-188.714667v-377.386666L512 134.592zM512 341.333333a170.666667 170.666667 0 1 1 0 341.333334 170.666667 170.666667 0 0 1 0-341.333334z m0 85.333334a85.333333 85.333333 0 1 0 0 170.666666 85.333333 85.333333 0 0 0 0-170.666666z"
                        fill="currentColor"
                    ></path>
                </svg>
            </button>
        </template>

        <div class="vb-record-drawer">
            <div v-if="tabs.length > 0" class="vb-record-tabs">
                <button
                    v-for="tab in tabs"
                    :key="tab.type"
                    type="button"
                    class="vb-record-tab"
                    :class="{ active: tab.type === activeType }"
                    @click="switchTab(tab.type)"
                >
                    {{ tab.label }}
                    <span v-if="unreadOf(tab.type) > 0" class="vb-record-tab-badge">{{ unreadText(tab.type) }}</span>
                </button>
            </div>

            <div ref="listRef" class="vb-record-list" @scroll="onScroll">
                <template v-if="tabs.length === 0">
                    <div class="vb-record-empty">
                        <img src="/src/style/images/common/empty-bg.png" alt="" />
                        <p>暂无开启的播报类型，请在设置中开启</p>
                    </div>
                </template>

                <template v-else>
                    <VoiceBroadcastRecordItem
                        v-for="(record, idx) in records"
                        :key="record.msgId"
                        :ref="(el: any) => setItemRef(el, idx)"
                        :item="record"
                        :voiceType="activeType as VoiceMsgType"
                    />

                    <div v-if="loading" class="vb-record-tip">加载中…</div>
                    <div v-else-if="records.length === 0" class="vb-record-empty">
                        <img src="/src/style/images/common/empty-bg.png" alt="" />
                        <p>当前暂无播报记录</p>
                    </div>
                    <div v-else-if="finished" class="vb-record-tip">没有更多了</div>
                </template>
            </div>
        </div>
    </Drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from "vue";
import { Drawer } from "ant-design-vue";
import { message } from "ant-design-vue";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { getVoiceBroadcastRecordList } from "@/api/panel/adminMsg";
import { getCustomerMsgReceivedList } from "@/api/im/im";
import type { MessageList } from "@/types/im/im.d";
import type { VoiceBroadcastRecord, VoiceMsgType } from "@/types/voiceBroadcast.d";
import VoiceBroadcastRecordItem from "./VoiceBroadcastRecordItem.vue";

const store = useVoiceBroadcastStore();

/** 播报类型 → 消息中心 admin_msg msgType（与 SSE 增量来源、DetailBtn 同源） */
const TYPE_MSG_TYPE: Record<VoiceMsgType, number | null> = {
    newOrder: 11,
    paidOrder: 12,
    aftersale: 32,
    cancelOrder: 31,
    // 客服消息为 IM 旁路来源，admin_msg 无对应 msgType（后端任务待接入）
    customerMsg: null
};

const TYPE_LABEL: Record<VoiceMsgType, string> = {
    newOrder: "新订单",
    paidOrder: "已付款订单",
    aftersale: "售后申请",
    cancelOrder: "订单取消",
    customerMsg: "客服消息"
};

const innerOpen = computed({
    get: () => store.recordDrawerOpen,
    set: (v: boolean) => store.setRecordDrawerOpen(v)
});

/** 需要展示的 tab（按设置开关 + 当前端允许类型） */
const tabs = computed(() => store.recordTabTypes.map((type) => ({ type, label: TYPE_LABEL[type] })));

const activeType = computed<VoiceMsgType | "">(() => store.recordActiveType);

/** 某类型 tab 的未读计数 */
const unreadOf = (type: VoiceMsgType) => store.recordUnread[type] || 0;

/** 未读气泡展示文案（超过 99 折叠为 99+） */
const unreadText = (type: VoiceMsgType) => {
    const n = unreadOf(type);
    return n > 99 ? "99+" : String(n);
};

const PAGE_SIZE = 10;
const records = ref<VoiceBroadcastRecord[]>([]);
const page = ref(1);
const total = ref(0);
const loading = ref(false);
const listRef = ref<HTMLElement | null>(null);

/** 记录卡片组件引用（用于程序化展开首条详情，需求 #646738 弹窗改造） */
const itemRefs = ref<Array<{ openDetail?: () => void } | null>>([]);
const setItemRef = (el: any, idx: number) => {
    itemRefs.value[idx] = el || null;
};

/** 是否有「打开首条详情」的待处理请求（右上角弹窗提示点击后置位） */
const pendingAutoDetail = ref(false);

/**
 * 消费待处理的「打开首条详情」请求
 *
 * 需在目标 tab 记录加载完成后调用：tab 已切到目标类型且非加载中时，
 * 展开首条（最新一条）记录详情；无论有无记录都清除待处理标记，避免请求悬挂。
 */
const tryOpenPendingDetail = () => {
    if (!pendingAutoDetail.value) return;
    if (loading.value) return;
    if (!store.recordDetailType || store.recordDetailType !== activeType.value) return;
    pendingAutoDetail.value = false;
    itemRefs.value[0]?.openDetail?.();
};

const finished = computed(() => records.value.length >= total.value && total.value > 0);

/** IM 消息类型 → 摘要文案（客服消息记录卡片用纯文本摘要，非会话消息一律折叠为提示） */
const summarizeImContent = (msg: MessageList): string => {
    const c = msg.content || ({} as MessageList["content"]);
    switch (msg.messageType) {
        case "text":
            return c.content || "[消息]";
        case "image":
            return "[图片]";
        case "sound":
            return "[语音]";
        case "video":
            return "[视频]";
        case "file":
            return "[文件]";
        case "custom":
            if (c.order) return "[订单消息]";
            if (c.product) return "[商品消息]";
            return msg.messageTypeText || "[消息]";
        default:
            return c.content || msg.messageTypeText || "[消息]";
    }
};

/** IM 用户消息 → 播报记录卡片结构（msgType=0 使其不落入订单/售后详情映射，改走会话跳转） */
const mapImMessage = (msg: MessageList): VoiceBroadcastRecord => {
    const userName = msg.user?.nickname || msg.user?.username || "客户";
    return {
        msgId: msg.id,
        msgType: 0,
        title: `${userName} 发来消息`,
        content: summarizeImContent(msg),
        sendTime: msg.sendTime,
        isReaded: msg.isRead,
        relatedData: { conversationId: msg.conversationId }
    };
};

/** 拉取当前 tab 记录（reset=true 重置到第 1 页） */
const loadRecords = async (reset = false) => {
    const type = activeType.value as VoiceMsgType;
    if (!type) {
        records.value = [];
        total.value = 0;
        return;
    }
    if (loading.value) return;
    if (!reset && records.value.length >= total.value && total.value > 0) return;

    if (reset) {
        page.value = 1;
        total.value = 0;
        records.value = [];
        itemRefs.value = [];
    }
    loading.value = true;
    try {
        if (type === "customerMsg") {
            // 客服消息走 IM 旁路接口（与 SSE customerMsgCount 同口径：当前客服/店铺收到的用户消息）
            const result = await getCustomerMsgReceivedList({ page: page.value, size: PAGE_SIZE });
            const list = (result?.records ?? []).map(mapImMessage);
            records.value = reset ? list : records.value.concat(list);
            total.value = result?.total ?? 0;
        } else {
            const msgType = TYPE_MSG_TYPE[type];
            // 语音播报专用列表：一次性带出当页所有商品摘要与金额，卡片无需再逐条拉详情
            const result = await getVoiceBroadcastRecordList({ page: page.value, size: PAGE_SIZE, msgType });
            const list = (result?.records ?? []) as VoiceBroadcastRecord[];
            records.value = reset ? list : records.value.concat(list);
            total.value = result?.total ?? 0;
        }
        page.value += 1;
    } catch (error: any) {
        message.error(error?.message || "加载播报记录失败");
    } finally {
        loading.value = false;
        // 记录渲染完成后，若有「打开首条详情」的待处理请求则消费
        nextTick(() => tryOpenPendingDetail());
    }
};

const switchTab = (type: VoiceMsgType) => {
    // 点击即视为查看：把该 tab 未读气泡归零
    const hadUnread = unreadOf(type) > 0;
    store.markRecordViewed(type);
    if (type === activeType.value) {
        // 当前 tab 有新增量时点击刷新，拉取最新记录
        if (hadUnread) loadRecords(true);
        return;
    }
    store.setRecordActiveType(type);
};

const openSettings = () => {
    store.openSettingsPanel();
};

/** 滚动到底部自动加载下一页 */
const onScroll = () => {
    const el = listRef.value;
    if (!el || loading.value) return;
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 40) {
        loadRecords(false);
    }
};

// 抽屉打开时：确保激活 tab 合法并加载
watch(
    () => store.recordDrawerOpen,
    (open) => {
        if (!open) return;
        const tabTypes = store.recordTabTypes;
        if (!activeType.value || !tabTypes.includes(activeType.value as VoiceMsgType)) {
            store.setRecordActiveType(tabTypes[0] ?? "");
        }
        // 打开即视为查看当前激活 tab，清零其未读气泡
        if (activeType.value) store.markRecordViewed(activeType.value as VoiceMsgType);
        loadRecords(true);
    }
);

// 切换 tab 时重置并加载；同时把列表滚回顶部
watch(activeType, () => {
    if (!store.recordDrawerOpen) return;
    // 激活 tab 变化视为查看，清零其未读气泡
    if (activeType.value) store.markRecordViewed(activeType.value as VoiceMsgType);
    nextTick(() => {
        if (listRef.value) listRef.value.scrollTop = 0;
    });
    loadRecords(true);
});

// 抽屉打开时，当前激活 tab 有新增量：自动刷新列表并保持已读（用户正在查看该 tab）
watch(
    () => (activeType.value ? store.recordCount[activeType.value as VoiceMsgType] : 0),
    (nextCount, prevCount) => {
        if (!store.recordDrawerOpen) return;
        if (!activeType.value) return;
        if (nextCount <= prevCount) return;
        // 正在查看的 tab：新记录直接进列表，不冒气泡
        store.markRecordViewed(activeType.value as VoiceMsgType);
        nextTick(() => {
            if (listRef.value) listRef.value.scrollTop = 0;
        });
        loadRecords(true);
    }
);

// 右上角弹窗提示点击：请求展开对应 tab 首条记录详情（需求 #646738 弹窗改造）
// requestRecordDetail 已同步打开抽屉并切到目标 tab；此处置位待处理标记，
// 由记录加载完成（loadRecords finally）后的 tryOpenPendingDetail 消费；
// 若目标 tab 记录已就绪（如抽屉已开在同一 tab），nextTick 立即尝试打开。
watch(
    () => store.recordDetailSignal,
    () => {
        if (!store.recordDetailType) return;
        pendingAutoDetail.value = true;
        nextTick(() => tryOpenPendingDetail());
    }
);
</script>

<style scoped lang="less">
.vb-record-setting-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    padding: 0;
    background: transparent;
    border: none;
    border-radius: 6px;
    color: #4e5969;
    cursor: pointer;
    font-size: 18px;
    transition:
        background 0.2s ease,
        color 0.2s ease;

    &:hover {
        background: #f2f3f5;
        color: var(--tig-primary, #3d7fff);
    }

    .vb-record-setting-svg {
        width: 18px;
        height: 18px;
        display: block;
    }
}

.vb-record-drawer {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
}

.vb-record-tabs {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0 16px;
    border-bottom: 1px solid #eef0f3;
    overflow-x: auto;
    flex-shrink: 0;

    .vb-record-tab {
        position: relative;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 12px 6px;
        background: transparent;
        border: none;
        font-size: 14px;
        line-height: 20px;
        color: #4e5969;
        white-space: nowrap;
        cursor: pointer;
        transition: color 0.2s ease;

        .vb-record-tab-badge {
            display: inline-block;
            min-width: 16px;
            height: 16px;
            padding: 0 4px;
            box-sizing: border-box;
            border-radius: 8px;
            background: #f53f3f;
            color: #fff;
            font-size: 11px;
            font-weight: 400;
            line-height: 16px;
            text-align: center;
        }

        &::after {
            content: "";
            position: absolute;
            left: 6px;
            right: 6px;
            bottom: -1px;
            height: 2px;
            border-radius: 2px;
            background: transparent;
            transition: background 0.2s ease;
        }

        &:hover {
            color: var(--tig-primary, #3d7fff);
        }

        &.active {
            color: var(--tig-primary, #3d7fff);
            font-weight: 600;
        }

        &.active::after {
            background: var(--tig-primary, #3d7fff);
        }
    }
}

.vb-record-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 0;

    &::-webkit-scrollbar {
        width: 6px;
    }

    &::-webkit-scrollbar-thumb {
        border-radius: 8px;
        background: rgba(0, 0, 0, 0.15);
    }
}

.vb-record-tip {
    text-align: center;
    font-size: 12px;
    color: #a8abb2;
    padding: 12px 0;
}

.vb-record-empty {
    width: 100%;
    text-align: center;
    font-size: 14px;
    padding: 80px 0 40px;
    color: #999;

    img {
        width: 150px;
    }

    p {
        margin-top: 12px;
    }
}
</style>

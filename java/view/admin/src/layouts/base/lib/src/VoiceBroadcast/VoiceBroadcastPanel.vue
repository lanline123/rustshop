<!--
/**
 * @file 语音播报设置抽屉
 * @description 右侧 440px 抽屉，5 项独立开关，扁平列表 + 彩色圆形图标
 *              切换立即生效并写 localStorage 持久化
 *              浏览器不支持 Web Speech API 时底部展示降级说明
 */
-->
<template>
    <!-- zIndex 只抬内容层；根节点（含遮罩）需 rootStyle，否则会被同级的播报记录抽屉盖住 -->
    <Drawer
        v-model:open="innerOpen"
        :title="$t('语音播报设置')"
        placement="right"
        :width="440"
        :destroyOnClose="false"
        :zIndex="DRAWER_NESTED_Z_INDEX"
        :rootStyle="{ zIndex: DRAWER_NESTED_Z_INDEX }"
        :bodyStyle="{ padding: '8px 20px 20px 20px' }"
    >
        <div class="voice-panel">
            <div v-if="store.hasPermission && !store.activated" class="activate-tip"> {{ $t('尚未激活浏览器语音权限，播报暂不会发声；您可先在此调整各类消息的播报开关，点击顶部「立即开启」后将按设置生效。') }} </div>
            <div v-for="item in visibleItems" :key="item.type" class="voice-row">
                <div class="voice-row-left">
                    <span class="voice-icon-circle" :style="{ background: item.color }">
                        <component :is="item.icon" />
                    </span>
                    <span class="voice-row-label">{{ $t('{label}语音播报', { label: item.label }) }}</span>
                    <button
                        v-if="store.isSupported && store.hasPermission"
                        type="button"
                        class="voice-preview-btn"
                        :title="$t('试听：{label}语音播报', { label: item.label })"
                        :aria-label="$t('试听：{label}语音播报', { label: item.label })"
                        @click.stop="onPreview(item.type)"
                    >
                        <SvgIcon name="setting-listening" width="16" height="16" />
                    </button>
                </div>
                <span class="voice-switch-wrap" :class="{ locked: !store.hasPermission }" @click="onLockedClick">
                    <el-switch
                        :model-value="store[item.type]"
                        :disabled="!store.hasPermission"
                        @change="(v: any) => setEnabled(item.type, !!v)"
                    />
                </span>
            </div>

            <div class="voice-divider"></div>

            <!-- 弹窗显示：开启后语音播报时同步弹出「播报记录」抽屉（需求 #646738） -->
            <div class="voice-row voice-row-popup">
                <div class="voice-row-left">
                    <span class="voice-row-label">{{ $t('弹窗显示') }}</span>
                </div>
                <span class="voice-switch-wrap" :class="{ locked: !store.hasPermission }" @click="onLockedClick">
                    <el-switch
                        :model-value="store.popupEnabled"
                        :disabled="!store.hasPermission"
                        @change="(v: any) => store.setPopupEnabled(!!v)"
                    />
                </span>
            </div>
            <div class="popup-note">{{ $t('开启后，语音播报同时在右上角弹出消息通知，点击可查看对应记录详情') }}</div>

            <div class="footer-note" v-if="!store.isSupported"> {{ $t('当前浏览器不支持语音合成，开关可保存但不会发声。建议使用最新版 Chrome / Edge。') }} </div>
        </div>
    </Drawer>
</template>

<script setup lang="ts">
import { computed, markRaw } from "vue";
import { Drawer, message } from "ant-design-vue";
import {
    ShoppingOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined,
    CloseCircleOutlined,
    CustomerServiceOutlined
} from "@ant-design/icons-vue";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { voiceBroadcaster } from "@/utils/voiceBroadcaster";
import { DRAWER_NESTED_Z_INDEX } from "@/utils/overlayZIndex";
import type { VoiceMsgType } from "@/types/voiceBroadcast.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ (e: "update:open", v: boolean): void }>();

const innerOpen = computed({
    get: () => props.open,
    set: (v: boolean) => emit("update:open", v)
});

const store = useVoiceBroadcastStore();

interface VoiceItem {
    type: VoiceMsgType;
    label: string;
    icon: any;
    color: string;
}

const items: VoiceItem[] = [
    { type: "newOrder", label: t('新订单'), icon: markRaw(ShoppingOutlined), color: "#14b8a6" },
    { type: "paidOrder", label: t('已付款订单'), icon: markRaw(CheckCircleOutlined), color: "#1456f0" },
    { type: "aftersale", label: t('售后申请'), icon: markRaw(ExclamationCircleOutlined), color: "#f5222d" },
    { type: "cancelOrder", label: t('订单取消'), icon: markRaw(CloseCircleOutlined), color: "#fa8c16" },
    { type: "customerMsg", label: t('客服消息'), icon: markRaw(CustomerServiceOutlined), color: "#06b6d4" }
];

// 仅展示当前端允许播报的类型开关（如供应商端不含「客服消息」）
const visibleItems = computed(() =>
    items.filter((item) => store.allowedTypes.includes(item.type))
);

/**
 * 无权限时点击开关区域的提示（REQ-17）
 *
 * 禁用态 el-switch 不会派发 change，提示只能挂在外层 wrapper 上；
 * el-switch 禁用态未设置 pointer-events: none，click 仍会冒泡到此处。
 */
const onLockedClick = () => {
    if (store.hasPermission) return;
    message.error(t("您没有语音播报权限，请联系管理员开通"));
};

const setEnabled = (type: VoiceMsgType, value: boolean) => {
    store.setEnabled(type, value);
};

/**
 * 试听按钮点击（CR-20260616-092300）
 *
 * 完全旁路业务管控：取消正在播放的 utterance + 立即播报固定文案。
 * 不受 store 开关 / activated / leader / 节流锚点影响，仅依赖浏览器是否支持 Web Speech API。
 */
const onPreview = (type: VoiceMsgType) => {
    voiceBroadcaster.previewSpeak(type);
};
</script>

<style scoped lang="less">
.voice-panel {
    .activate-tip {
        margin: 0 0 4px;
        padding: 10px 12px;
        background: #fff8e6;
        border: 1px solid #ffe7a3;
        border-radius: 6px;
        font-size: 12px;
        line-height: 18px;
        color: #ad7a00;
    }

    .voice-switch-wrap {
        display: inline-flex;
        align-items: center;
        flex-shrink: 0;

        &.locked {
            cursor: not-allowed;
        }
    }

    .voice-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 14px 0;

        .voice-row-left {
            display: flex;
            align-items: center;
            flex: 1;
            min-width: 0;
        }

        .voice-icon-circle {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            color: #fff;
            font-size: 16px;
            margin-right: 12px;
            flex-shrink: 0;

            :deep(svg) {
                width: 16px;
                height: 16px;
            }
        }

        .voice-row-label {
            font-size: 14px;
            color: #1f2329;
            line-height: 22px;
        }

        .voice-preview-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 28px;
            height: 28px;
            margin-left: 6px;
            padding: 0;
            background: transparent;
            border: 1px solid transparent;
            border-radius: 6px;
            color: #788d9b;
            cursor: pointer;
            flex-shrink: 0;
            transition:
                background 0.2s ease,
                color 0.2s ease,
                border-color 0.2s ease;

            &:hover {
                background: #f5f5f5;
                color: var(--tig-primary, #3d7fff);
            }

            &:active {
                background: #ececec;
            }

            &:focus-visible {
                outline: none;
                border-color: var(--tig-primary, #3d7fff);
            }

            :deep(svg) {
                width: 16px;
                height: 16px;
                display: block;
                pointer-events: none;
            }
        }
    }

    .voice-divider {
        height: 1px;
        margin: 6px 0;
        background: #eef0f3;
    }

    .voice-row-popup {
        padding-bottom: 4px;
    }

    .popup-note {
        margin: 0 0 4px;
        font-size: 12px;
        line-height: 18px;
        color: #86909c;
    }

    .footer-note {
        margin-top: 16px;
        padding: 10px 12px;
        background: #fff8e6;
        border: 1px solid #ffe7a3;
        border-radius: 6px;
        font-size: 12px;
        line-height: 18px;
        color: #ad7a00;
    }
}
</style>

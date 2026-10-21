<!--
/**
 * @file 语音播报首次引导条
 * @description 顶部 layout-top 内一行轻量提示，引导用户首次激活浏览器 Web Speech API 权限
 *
 *              显示条件（全为 true 才展示）：
 *               - 浏览器支持 Web Speech API
 *               - 用户已登录（useUserStore.userInfo.username 非空）
 *               - 未点过「立即开启」（localStorage.voiceBroadcastTipDismissed !== '1'）
 *               - 本次会话未点「暂不」
 *               - 用户尚未在抽屉内激活（store.activated === false）—— 已激活则无引导价值
 *
 *              「立即开启」：speakRaw(' ') 激活浏览器自动播放许可，
 *                          markActivated() + enableDefaults() + 打开设置抽屉 + 写 localStorage 永久 dismiss，Tip 消失
 *              「不再提醒」：不激活语音，仅写 localStorage 永久 dismiss，
 *                          除非清除本地缓存否则不再弹出引导条
 *              「暂不」：仅本次会话隐藏，不写 localStorage，下次刷新仍提示
 *                       （需求 #2C9427 "未授权则下次登录再次提示"）
 *
 * @see .ai-os/specs/admin-msg-tts.spec.md REQ-06
 * @see .ai-os/DESIGN.md 3.3
 * @see .ai-os/acceptance.yaml REQ-06
 */
-->
<template>
    <div v-if="visible" class="voice-broadcast-tip">
        <span class="voice-tip-icon">
            <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path
                    d="M602.72 526.83V313.48C602.72 200.69 511 109 398.26 109S193.8 200.69 193.8 313.48v213.35c0 103.68 77.56 189.57 177.79 202.68v64H220.46a26.67 26.67 0 0 0 0 53.34h355.59a26.67 26.67 0 1 0 0-53.34H424.92v-64c100.24-13.11 177.8-99 177.8-202.68zM247.13 313.48c0-83.34 67.79-151.13 151.13-151.13s151.12 67.79 151.12 151.13v160H247.13zM398.26 678c-83.34 0-151.13-67.79-151.13-151.13h302.25c0 83.3-67.78 151.13-151.12 151.13z"
                    fill="currentColor"
                ></path>
            </svg>
        </span>
        <span class="voice-tip-text">{{ $t('开启语音播报，新订单、售后等重要消息将自动语音提醒。') }}</span>
        <button class="voice-tip-btn voice-tip-btn-plain" @click="onDismissForever">{{ $t('不再提醒') }}</button>
        <button class="voice-tip-btn voice-tip-btn-primary" @click="onActivate">{{ $t('立即开启') }}</button>
        <button class="voice-tip-btn voice-tip-btn-plain" @click="onDismissOnce">{{ $t('暂不') }}</button>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useUserStore } from "@/store/user";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { voiceBroadcaster } from "@/utils/voiceBroadcaster";

const TIP_DISMISSED_KEY = "voiceBroadcastTipDismissed";

const userStore = useUserStore();
const voiceStore = useVoiceBroadcastStore();

/** 本次会话内是否已被「暂不」隐藏（不写 localStorage） */
const sessionDismissed = ref(false);

/** localStorage 中是否已永久 dismiss */
const persistDismissed = ref<boolean>(readPersistDismissed());

function readPersistDismissed(): boolean {
    try {
        return localStorage.getItem(TIP_DISMISSED_KEY) === "1";
    } catch {
        return false;
    }
}

const supported = computed(() => voiceStore.isSupported);
const loggedIn = computed(() => !!userStore.userInfo?.username);

const visible = computed(
    () =>
        supported.value &&
        loggedIn.value &&
        // 无 voiceBroadcastManage 权限时不引导开启，避免点了「立即开启」却全程无声（REQ-17）
        voiceStore.hasPermission &&
        !persistDismissed.value &&
        !sessionDismissed.value &&
        !voiceStore.activated
);

const onActivate = () => {
    voiceBroadcaster.speakRaw(" ");
    voiceStore.markActivated();
    // CR-20260615-173100：首次激活按业务默认策略开启（newOrder 不开，其余 4 类在端允许范围内开）
    voiceStore.enableDefaults();
    voiceStore.openSettingsPanel();
    try {
        localStorage.setItem(TIP_DISMISSED_KEY, "1");
        persistDismissed.value = true;
    } catch {
        /* localStorage 写入失败时静默；下次登录仍提示 */
    }
};

const onDismissOnce = () => {
    sessionDismissed.value = true;
};

/**
 * 「不再提醒」：永久关闭引导条但不激活语音权限。
 * 仅写 localStorage 永久 dismiss，除非用户清除本地缓存，否则不再弹出该提示栏。
 */
const onDismissForever = () => {
    try {
        localStorage.setItem(TIP_DISMISSED_KEY, "1");
        persistDismissed.value = true;
    } catch {
        // localStorage 写入失败时退化为本次会话隐藏，避免提示栏继续占位
        sessionDismissed.value = true;
    }
};
</script>

<style scoped lang="less">
.voice-broadcast-tip {
    display: flex;
    align-items: center;
    height: 36px;
    padding: 0 16px;
    background: #fff8e6;
    border-bottom: 1px solid #ffe7a3;
    font-size: 13px;
    line-height: 36px;
    color: #ad7a00;

    .voice-tip-icon {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 18px;
        height: 18px;
        margin-right: 8px;
        color: #faad14;
        flex-shrink: 0;

        svg {
            width: 100%;
            height: 100%;
            display: block;
        }
    }

    .voice-tip-text {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .voice-tip-btn {
        height: 26px;
        padding: 0 14px;
        margin-left: 8px;
        font-size: 12px;
        line-height: 24px;
        border-radius: 4px;
        border: 1px solid transparent;
        cursor: pointer;
        background: transparent;
        transition:
            background 0.2s ease,
            border-color 0.2s ease,
            color 0.2s ease;
        flex-shrink: 0;
    }

    .voice-tip-btn-primary {
        background: var(--tig-primary, #3d7fff);
        color: #fff;
        border-color: var(--tig-primary, #3d7fff);

        &:hover {
            opacity: 0.9;
        }
    }

    .voice-tip-btn-plain {
        color: #ad7a00;
        border-color: #ffd591;
        background: #fff;

        &:hover {
            background: #fffaf0;
        }
    }
}

@media only screen and (max-width: 767px) {
    .voice-broadcast-tip {
        padding: 0 10px;
        font-size: 12px;

        .voice-tip-text {
            font-size: 12px;
        }
    }
}
</style>

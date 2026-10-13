<!--
/**
 * @file 语音播报入口图标
 * @description 顶部栏喇叭/扬声器图标按钮，紧邻消息中心
 *              点击打开设置抽屉；全部开关关闭时显示静音样式
 *              浏览器不支持 Web Speech API 时整个入口隐藏
 * @features
 *  - 兼容默认 / topMenu 两种顶部栏布局
 *  - 入口图标使用内联 SVG（src/assets/setting/broadcast.svg），fill=currentColor 跟随父级 color
 */
-->
<template>
    <div v-if="supported" class="top-bar-item top-admin_msg voice-broadcast-entry" :class="themeInfo.layout">
        <a
            class="top-bar-btn voice-entry-btn"
            :class="[themeInfo.navTheme, { muted: !store.isAnyEnabled }]"
            @click="onClickEntry"
            href="javascript:;"
        >
            <span class="voice-icon">
                <svg
                    v-if="!store.isAnyEnabled"
                    class="voice-icon-svg"
                    viewBox="0 0 200 200"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                >
                    <path
                        d="M117.7,102.9V61.2c0-22-17.9-39.9-39.9-39.9S37.9,39.2,37.9,61.2v41.7c0,20.2,15.1,37,34.7,39.6V155H43.1c-2.9,0-5.2,2.3-5.2,5.2c0,2.9,2.3,5.2,5.2,5.2h69.5c2.9,0,5.2-2.3,5.2-5.2c0-2.9-2.3-5.2-5.2-5.2H83v-12.5C102.6,139.9,117.7,123.1,117.7,102.9L117.7,102.9z M48.3,61.2c0-16.3,13.2-29.5,29.5-29.5s29.5,13.2,29.5,29.5v31.2h-59V61.2z M77.8,132.4c-16.3,0-29.5-13.2-29.5-29.5h59C107.3,119.2,94.1,132.4,77.8,132.4z M132.5,74.7c2,2,5.3,2,7.4,0c7.4-7.5,7.4-19.5,0-27c-2.1-2-5.4-1.9-7.4,0.2c-1.9,2-1.9,5.1,0,7.1c3.4,3.4,3.4,8.9,0,12.3C130.5,69.4,130.5,72.6,132.5,74.7C132.5,74.7,132.5,74.7,132.5,74.7L132.5,74.7z"
                        fill="currentColor"
                    ></path>
                    <path
                        d="M154.6,33c-2-2.1-5.3-2.1-7.4-0.1c-2.1,2-2.1,5.3-0.1,7.4c0,0,0.1,0.1,0.1,0.1c11.5,11.5,11.5,30.2,0,41.8c-2,2.1-1.9,5.4,0.1,7.4c2,1.9,5.2,1.9,7.2,0C170.2,73.9,170.2,48.6,154.6,33z"
                        fill="currentColor"
                    ></path>
                    <path
                        d="M21.5,165.6c-1.5,0-2.9-0.6-3.9-1.9c-1.7-2.2-1.4-5.3,0.8-7L177.2,29.9c2.2-1.7,5.3-1.4,7,0.8c1.7,2.2,1.4,5.3-0.8,7L24.6,164.5C23.7,165.3,22.6,165.6,21.5,165.6z"
                        fill="currentColor"
                    ></path>
                </svg>
                <svg
                    v-else
                    class="voice-icon-svg"
                    viewBox="0 0 1024 1024"
                    xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                >
                    <path
                        d="M602.72 526.83V313.48C602.72 200.69 511 109 398.26 109S193.8 200.69 193.8 313.48v213.35c0 103.68 77.56 189.57 177.79 202.68v64H220.46a26.67 26.67 0 0 0 0 53.34h355.59a26.67 26.67 0 1 0 0-53.34H424.92v-64c100.24-13.11 177.8-99 177.8-202.68zM247.13 313.48c0-83.34 67.79-151.13 151.13-151.13s151.12 67.79 151.12 151.13v160H247.13zM398.26 678c-83.34 0-151.13-67.79-151.13-151.13h302.25c0 83.3-67.78 151.13-151.12 151.13zM678.62 382.6a26.85 26.85 0 0 0 37.78 0 97.87 97.87 0 0 0 0-138.24A26.64 26.64 0 0 0 678.73 282a44.52 44.52 0 0 1 0 62.89 26.47 26.47 0 0 0-0.11 37.68z"
                        fill="currentColor"
                    ></path>
                    <path
                        d="M791.74 168.91a26.64 26.64 0 1 0-37.67 37.67 151.32 151.32 0 0 1 0 213.8 26.68 26.68 0 0 0 37.78 37.67 204.57 204.57 0 0 0-0.11-289.14z m0 0"
                        fill="currentColor"
                    ></path>
                </svg>
            </span>
            <span v-if="themeInfo.layout !== 'topMenu'" class="voice-entry-text">{{ $t('播报') }}</span>
        </a>
        <VoiceBroadcastRecordDrawer />
        <VoiceBroadcastPanel v-model:open="panelOpen" />
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useVoiceBroadcastStore } from "@/store/voiceBroadcast";
import { useThemeStore } from "@/store/theme";
import VoiceBroadcastPanel from "./VoiceBroadcastPanel.vue";
import VoiceBroadcastRecordDrawer from "./VoiceBroadcastRecordDrawer.vue";

const store = useVoiceBroadcastStore();
const { themeInfo } = useThemeStore();

const supported = computed(() => store.isSupported);
const panelOpen = computed({
    get: () => store.panelOpen,
    set: (v: boolean) => store.setSettingsPanelOpen(v)
});

/**
 * 点击喇叭入口：打开「播报记录」抽屉（需求 #646738，原直接打开设置改为经记录抽屉右上角齿轮进入设置）
 */
const onClickEntry = () => {
    if (store.recordDrawerOpen) {
        store.setRecordDrawerOpen(false);
    } else {
        store.openRecordDrawer();
    }
};
</script>

<style scoped lang="less">
.voice-broadcast-entry {
    position: relative;
    display: flex;
    align-items: center;

    .voice-entry-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 40px;
        padding: 0 12px;
        font-size: 14px;
        color: #788d9b;
        border-radius: 5px;
        transition:
            background 0.2s ease 0s,
            color 0.2s ease 0s;

        &:hover {
            background: #f7f7f7;
        }

        .voice-icon {
            position: relative;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 22px;
            height: 22px;
        }

        .voice-icon-svg {
            width: 100%;
            height: 100%;
            display: block;
        }

        .voice-entry-text {
            margin-left: 4px;
        }

        /* 静音态：使用 broadcast-close.svg，灰度即可 */
        &.muted .voice-icon {
            color: #b6becc;
        }
    }
}

/* topMenu 模式下沿用纯图标无文字风格 */
.voice-broadcast-entry.topMenu {
    .voice-entry-btn {
        padding: 0 10px !important;
        color: var(--tig-menu-text-color);

        &:hover {
            background: #9c9c9c2e !important;
        }

        &.light {
            color: #788d9b;

            &:hover {
                background: #f7f7f7 !important;
            }
        }
    }
}

@media only screen and (max-width: 1200px) {
    .voice-broadcast-entry .voice-entry-text {
        display: none;
    }
    .voice-broadcast-entry .voice-entry-btn {
        padding: 0 8px;
    }
}

@media only screen and (max-width: 767px) {
    .voice-broadcast-entry .voice-entry-text {
        display: none;
    }
    .voice-broadcast-entry .voice-entry-btn {
        padding: 0 4px;
    }
}
</style>

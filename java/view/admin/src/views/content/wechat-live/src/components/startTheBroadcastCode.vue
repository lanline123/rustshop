<template>
    <el-dialog
        class="wechat-live-advertise-dialog"
        :append-to-body="true"
        :show-close="true"
        v-model="showDialog"
        width="400px"
        top="12vh"
        v-bind="$attrs"
        @close="handleClose"
        @closed="showDialog = false"
    >
        <div v-loading="loading" class="contentbox">
            <div v-if="liveCodeImage" class="code-container">
                <div class="code">
                    <el-image :src="liveCodeImage" fit="contain" :preview-src-list="[liveCodeImage]" preview-teleported />
                </div>
                <div class="title">{{ $t('小程序直播') }} <span class="bot"></span> {{ $t('主播端') }}</div>
                <div class="tip">{{ $t('微信扫码进入主播小程序') }}</div>
            </div>
            <div v-else class="code-empty">
                <el-empty :description="' '">
                    <div class="live-error-tip"> {{ $t('请检查是否拥有小程序直播资格，根据') }}<a
                            href="https://developers.weixin.qq.com/miniprogram/product/live/access-requirement.html"
                            target="_blank"
                            rel="noopener noreferrer"
                        >{{ $t('《微信小程序直播功能准入要求》') }}</a>{{ $t('查看是否达到要求') }} </div>
                </el-empty>
            </div>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { message } from "ant-design-vue";
import { computed } from "vue";
import type { PropType } from "vue";
import type { WechatLivePushUrlState } from "@/types/content/wechatLive.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    loading: {
        type: Boolean,
        default: false
    },
    roomName: {
        type: String,
        default: ""
    },
    pushInfo: {
        type: Object as PropType<WechatLivePushUrlState | null>,
        default: null
    }
});

const showDialog = defineModel("showDialog", {
    default: false
});

const handleClose = () => {
    showDialog.value = false;
};

const liveCodeImage = computed(() => {
    const liveCode = props.pushInfo?.liveCode?.trim?.() || "";
    if (!liveCode) return "";
    if (liveCode.startsWith("data:image")) {
        return liveCode;
    }
    return `data:image/png;base64,${liveCode}`;
});

const handleDownload = () => {
    if (!liveCodeImage.value) {
        message.warning(t('暂无可下载的开播码'));
        return;
    }
    try {
        const link = document.createElement("a");
        link.href = liveCodeImage.value;
        link.download = t('{roomName}开播码.png', { roomName: props.pushInfo?.roomName || props.roomName || t('直播间') });
        document.body.appendChild(link);
        link.click();
        link.remove();
    } catch (_error) {
        message.error(t('下载失败'));
    }
};
</script>

<style scoped lang="less">
.contentbox {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    margin: 40px 0 40px 0;
    text-align: center;
    font-size: 14px;

    .code {
        width: 200px;
        height: 200px;
        padding: 16px;
        border-radius: 8px;
        background: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;

        :deep(.el-image) {
            width: 100%;
            height: 100%;
        }

        :deep(.el-image__inner) {
            object-fit: contain;
        }
    }

    .code-empty {
        color: #909399;
        font-size: 14px;
    }

    .live-error-tip {
        color: #909399;
        font-size: 13px;
        line-height: 1.6;
        padding: 0 20px;

        a {
            color: var(--el-color-primary);
            text-decoration: none;
        }
    }

    .title {
        font-size: 16px;
        font-weight: 600;

        .bot {
            display: inline-block;
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background: #111111;
            line-height: 20px;
            margin: 0 2px;
        }
    }

    .copy {
        color: var(--tig-primary);
        cursor: pointer;
    }

    .tip {
        color: #9598a6;
        font-size: 12px;
    }
}

:deep(.el-dialog__title) {
    font-weight: bold;
}
</style>

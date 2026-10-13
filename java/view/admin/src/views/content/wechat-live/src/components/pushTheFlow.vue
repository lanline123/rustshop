<template>
    <el-dialog
        class="wechat-live-advertise-dialog"
        :append-to-body="true"
        :show-close="true"
        v-model="showDialog"
        width="540px"
        top="12vh"
        v-bind="$attrs"
        @close="handleClose"
        @closed="showDialog = false"
        header-class="dialog-header-no-padding"
    >
        <div v-loading="loading" class="contentbox">
            <div v-if="pushInfo?.pushUrl" class="push-url-container">
                <div class="title">{{ $t('推流地址') }}</div>
                <div class="desc">{{ $t('在第三方推流应用使用以下地址进行推流') }}</div>
                <div class="link">{{ pushInfo?.pushUrl || $t('暂无推流地址') }}</div>
                <a class="copy" @click="handleCopy(pushInfo?.pushUrl)">{{ $t('复制链接') }}</a>
                <div class="tip">{{ $t('此地址为当前直播间唯一推流地址，不要泄露给第三方。') }}</div>
            </div>
            <div v-else class="push-url-empty">
                <el-empty :description="' '">
                    <div class="live-error-tip"> {{ $t('请检查是否拥有小程序直播资格，根据') }}<a
                            href="https://developers.weixin.qq.com/miniprogram/product/live/access-requirement.html"
                            target="_blank"
                            rel="noopener noreferrer"
                            >{{ $t('《微信小程序直播功能准入要求》') }}</a
                        >{{ $t('查看是否达到要求') }} </div>
                </el-empty>
            </div>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { message } from "ant-design-vue";
import type { PropType } from "vue";
import type { WechatLivePushUrlState } from "@/types/content/wechatLive.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineProps({
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

const handleCopy = async (value?: string) => {
    if (!value) {
        message.warning(t('暂无可复制的推流地址'));
        return;
    }
    try {
        await navigator.clipboard.writeText(value);
        message.success(t('复制成功'));
    } catch (_error) {
        const textarea = document.createElement("textarea");
        textarea.value = value;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
        message.success(t('复制成功'));
    }
};
</script>

<style scoped lang="less">
.contentbox {
    display: flex;
    flex-direction: column;
    gap: 10px;
    text-align: center;
    margin: 80px 0 80px 0;
    font-size: 14px;

    .title {
        font-size: 16px;
    }

    .desc {
        color: #606266;
    }

    .link {
        padding: 12px;
        border-radius: 6px;
        background: #f5f7fa;
        color: #303133;
        line-height: 20px;
        word-break: break-all;
    }

    .copy {
        color: var(--tig-primary);
        cursor: pointer;
    }

    .tip {
        color: #9598a6;
        font-size: 12px;
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
}
</style>

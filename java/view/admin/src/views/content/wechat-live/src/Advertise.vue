<template>
    <div class="btn" @click="handleShow">
        <slot></slot>
    </div>
    <template v-if="show">
        <el-dialog
            class="sign-dialog"
            :append-to-body="true"
            :show-close="false"
            v-model="showDialog"
            width="900"
            top="12vh"
            v-bind="$attrs"
            @close="handleClose"
            @closed="handleClosed"
        >
            <template #header>
                <div class="header">
                    <div class="header-title">{{ $attrs.title || $t('推广') }}</div>
                    <div class="header-close" @click="showDialog = false">
                        <i class="iconfont-admin icon-cha1"></i>
                    </div>
                </div>
            </template>
            <a-spin :spinning="loading" style="width: 100%; height: 100%">
                <el-row class="orfanize-content">
                    <el-col :span="4">
                        <div class="tabs-row">
                            <div
                                v-for="item in tabs"
                                :key="item.value"
                                class="tabs"
                                :class="{ active: item.value === currentTab }"
                                @click="handleTabChange(item.value)"
                            >
                                {{ item.name }}
                            </div>
                        </div>
                    </el-col>
                    <el-col :span="20">
                        <div class="content">
                            <div class="poster-img-container">
                                <template v-if="posterUrl">
                                    <el-image class="poster-img" :src="posterUrl" fit="contain" />
                                    <div class="poster-enlarge" @click="showPreview = true">
                                        <svg width="1em" height="1em" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                            <g fill="none" fill-rule="evenodd">
                                                <path d="M0 0h16v16H0z"></path>
                                                <path
                                                    d="M7.333 2a5.333 5.333 0 014.109 8.734l2.578 2.58-.707.706-2.579-2.578A5.333 5.333 0 117.333 2zm0 1a4.333 4.333 0 100 8.667 4.333 4.333 0 000-8.667zm.5 2v1.833h1.834v1H7.833v1.834h-1V7.833H5v-1h1.833V5h1z"
                                                    fill="currentColor"
                                                ></path>
                                            </g>
                                        </svg>
                                    </div>
                                </template>
                                <div v-else class="poster-empty">
                                    <el-empty :description="$t('暂无海报素材')" />
                                </div>
                            </div>
                            <div class="poster-info">
                                <div class="poster-title poster-title-first">{{ $t('素材下载') }}</div>
                                <div class="poster-actions">
                                    <el-button bg class="buttonColor mr10" size="large" text type="primary" :disabled="!posterUrl" @click="handlePosterDownload"
                                        >{{ $t('下载海报') }}</el-button
                                    >
                                    <el-button bg class="buttonColor mr10" size="large" text type="primary" :disabled="!qrcodeUrl" @click="handleQRCode"
                                        >{{ $t('下载二维码') }}</el-button
                                    >
                                </div>
                                <div class="poster-title">{{ $t('微信小程序路径') }}</div>
                                <div class="poster-paths">
                                    <!--                                    <div class="poster-item poster-item-bordered">-->
                                    <!--                                        <span class="poster-item-text">适用于微信内会话/朋友圈等场景</span>-->
                                    <!--                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">-->
                                    <!--                                            <template #content>-->
                                    <!--                                                <div class="tooltip-content">-->
                                    <!--                                                    {{ shortLink || "-" }}-->
                                    <!--                                                </div>-->
                                    <!--                                            </template>-->
                                    <!--                                            <a class="btn-link" @click="handleCopy(shortLink, '微信短链接')">复制短链接</a>-->
                                    <!--                                        </el-tooltip>-->
                                    <!--                                    </div>-->
                                    <div class="poster-item">
                                        <span class="poster-item-text">{{ pagePath || "-" }}</span>
                                        <el-tooltip class="box-item" effect="light" placement="bottom" :show-after="300">
                                            <template #content>
                                                <div class="tooltip-content">
                                                    {{ pagePath || "-" }}
                                                </div>
                                            </template>
                                            <a class="btn-link" @click="handleCopy(pagePath, '小程序路径')">{{ $t('复制') }}</a>
                                        </el-tooltip>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </a-spin>
            <el-image-viewer v-if="showPreview && previewList.length" :url-list="previewList" :initial-index="0" show-progress @close="showPreview = false" />
        </el-dialog>
    </template>
</template>

<script setup lang="ts">
import { message } from "ant-design-vue";
import { computed, ref } from "vue";
import type { PropType } from "vue";
import { getWechatLivePromoteData } from "@/api/content/wechatLive";
import type { WechatLivePromoteState, WechatLiveRoomState } from "@/types/content/wechatLive.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineOptions({
    inheritAttrs: false
});

const props = defineProps({
    item: {
        type: Object as PropType<WechatLiveRoomState>,
        default: () => ({})
    }
});

const showPreview = ref(false);
const show = ref(false);
const showDialog = ref(false);
const loading = ref(false);
const promoteData = ref<WechatLivePromoteState | null>(null);
const tabs = ref([
    {
        name: t('微信小程序'),
        value: 1
    }
]);
const currentTab = ref(1);

const roomName = computed(() => {
    return promoteData.value?.roomName || props.item.name || props.item.wechatLiveTitle || t('直播间');
});

const posterUrl = computed(() => {
    return promoteData.value?.posterUrl || "";
});

const qrcodeUrl = computed(() => {
    return promoteData.value?.qrcodeUrl || "";
});

const shortLink = computed(() => {
    return promoteData.value?.wechatShortLink || "";
});

const pagePath = computed(() => {
    return promoteData.value?.pagePath || "";
});

const previewList = computed(() => {
    return posterUrl.value ? [posterUrl.value] : [];
});

const loadData = async () => {
    const roomId = Number(props.item.roomId || 0);
    if (!roomId) {
        message.warning(t('缺少直播间ID，无法获取推广信息'));
        return;
    }

    loading.value = true;
    try {
        promoteData.value = await getWechatLivePromoteData(roomId);
    } catch (error: any) {
        message.error(error?.message || t('获取推广信息失败'));
    } finally {
        loading.value = false;
    }
};

const handleShow = async () => {
    show.value = true;
    showDialog.value = true;
    await loadData();
};

const handleClose = () => {
    showDialog.value = false;
};

const handleClosed = () => {
    showPreview.value = false;
    show.value = false;
    promoteData.value = null;
};

const getFileBlob = async (url: string): Promise<Blob> => {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(t('文件下载失败'));
    }
    return await response.blob();
};

const triggerDownload = (url: string, filename: string) => {
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

const getFileExtension = (url: string, fallback = ".png") => {
    const matched = url.split("?")[0].match(/\.(png|jpg|jpeg|webp|gif)$/i);
    return matched ? `.${matched[1].toLowerCase()}` : fallback;
};

const buildFileName = (suffix: string, url: string, fallbackExt = ".png") => {
    return `${roomName.value}${suffix}${getFileExtension(url, fallbackExt)}`;
};

const downloadRemoteFile = async (url: string, filename: string) => {
    if (!url) {
        message.warning(t('暂无可下载素材'));
        return;
    }

    try {
        const blob = await getFileBlob(url);
        const blobUrl = URL.createObjectURL(blob);
        triggerDownload(blobUrl, filename);
        window.setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);
    } catch (_error) {
        triggerDownload(url, filename);
        message.warning(t('已尝试直接下载原始文件'));
    }
};

const handlePosterDownload = async () => {
    loading.value = true;
    try {
        await downloadRemoteFile(posterUrl.value, buildFileName(t('海报'), posterUrl.value));
    } finally {
        loading.value = false;
    }
};

const handleQRCode = async () => {
    loading.value = true;
    try {
        await downloadRemoteFile(qrcodeUrl.value, buildFileName(t('二维码'), qrcodeUrl.value));
    } finally {
        loading.value = false;
    }
};

const handleCopy = async (text?: string, label: string = t('内容')) => {
    if (!text) {
        message.warning(t('暂无可复制的{label}', { label }));
        return;
    }

    try {
        await navigator.clipboard.writeText(text);
        message.success(t('复制成功'));
    } catch (_error) {
        const textarea = document.createElement("textarea");
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
        message.success(t('复制成功'));
    }
};

const handleTabChange = (value: number) => {
    currentTab.value = value;
};
</script>

<style lang="less">
.el-overlay-dialog {
    .sign-dialog {
        padding: 20px 24px;
        border-radius: 4px;

        .el-dialog__header {
            padding: 0;
            border: none;
        }

        .el-form-item:last-child {
            margin-bottom: 0;
        }
    }

    .header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;

        .header-title {
            color: #1e2226;
            font-weight: 600;
            font-size: 16px;
            line-height: 24px;
        }

        .icon-cha1 {
            cursor: pointer;
            font-size: 12px;
            padding: 6px 0 6px 8px;
            font-weight: normal;
            color: #8a9099;
            transition: color 0.2s;

            &:hover {
                color: #1e2226;
            }
        }
    }

    .content {
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        height: 500px;
        padding: 20px 16px 16px;

        .poster-img-container {
            position: relative;
            flex-shrink: 0;
            margin-right: 20px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            max-height: 390px;
            width: 240px;
            background-color: #f8f9fb;
            border-radius: 6px;

            .poster-img {
                border-radius: 6px;
                width: 100%;
                height: 100%;
            }

            .poster-empty {
                display: flex;
                justify-content: center;
                align-items: center;
                width: 100%;
                height: 100%;
                min-height: 390px;
                padding: 12px;
            }

            .poster-enlarge {
                cursor: pointer;
                position: absolute;
                top: 0;
                right: 0;
                background-color: #1f2026;
                opacity: 0.7;
                padding: 3px;
                display: flex;
                justify-content: center;
                align-items: center;
                border-bottom-left-radius: 8px;
                border-top-right-radius: 6px;
                color: #fff;
                font-size: 23px;
                font-weight: 600;
            }
        }

        .poster-info {
            width: 100%;

            .poster-actions {
                display: flex;
                justify-content: flex-start;
                align-items: center;
                width: 100%;
            }

            .poster-title {
                color: #1f2026;
                font-weight: 600;
                margin: 16px 0 8px;
            }

            .poster-title-first {
                margin-top: 0;
            }

            .poster-paths {
                display: flex;
                flex-direction: column;
                justify-content: flex-start;
                align-items: flex-start;
                background-color: #f5f7fa;
                border-radius: 4px;
                padding: 0 12px;
                flex: 1;
            }

            .poster-item {
                display: flex;
                justify-content: flex-start;
                align-items: center;
                flex-wrap: nowrap;
                font-size: 14px;
                height: 47px;
                line-height: 47px;
                width: 100%;
            }

            .poster-item-bordered {
                border-bottom: 1px solid #e9ecf0;
            }

            .poster-item-text {
                flex-grow: 1;
                color: #666;
                min-width: 0;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                width: 350px;
            }
        }
    }
}

.orfanize-content {
    border: 1px solid #e4e4e4;

    .tabs-row {
        box-sizing: border-box;
        height: 100%;
        border-right: 1px solid #e4e4e4;
        padding-left: 10px;
        padding-top: 20px;

        .tabs {
            padding: 10px 15px;
            cursor: pointer;
        }

        .active {
            color: var(--tig-primary);
            background-color: var(--tig-item-active-bg);
            font-weight: 600;
        }
    }
}

.tooltip-content {
    max-width: 300px;
    padding: 5px 10px;
    word-break: break-all;
}
</style>

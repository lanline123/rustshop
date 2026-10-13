<template>
    <div v-if="item" class="material-preview">
        <div class="preview-main" :style="{ margin: isImage ? '0px' : '15px' }">
            <el-image v-if="isImage" fit="contain" :src="imageFormat((item as GalleryPicItem).picUrl || (item as GalleryPicItem).picThumb)" />
            <video v-else :src="imageFormat((item as VideoItem).videoUrl)" controls autoplay></video>
            <button v-if="hasPrev" class="preview-arrow left" @click="emit('callback', { type: 'prev' })"></button>
            <button v-if="hasNext" class="preview-arrow right" @click="emit('callback', { type: 'next' })"></button>
        </div>
        <div v-if="isImage" class="preview-info">
            <h3 class="info-title">{{ isImage ? $t('图片标题') : $t('视频标题') }}</h3>
            <p class="info-filename">{{ title }}</p>
            <h4 class="info-section-title">{{ $t('基本信息') }}</h4>
            <div class="info-list">
                <div class="info-row"><span class="info-label">{{ $t('分组：') }}</span><span class="info-value">{{ albumName || (isImage ? $t('全部图片') : $t('全部视频')) }}</span></div>
                <template v-if="isImage">
                    <div class="info-row"><span class="info-label">{{ $t('尺寸：') }}</span><span class="info-value">{{ sizeText }}</span></div>
                    <div class="info-row"><span class="info-label">{{ $t('大小：') }}</span><span class="info-value">{{ fileSizeText }}</span></div>
                </template>
                <template v-else>
                    <div v-if="(item as VideoItem).format" class="info-row"><span class="info-label">{{ $t('格式：') }}</span><span class="info-value">{{ (item as VideoItem).format?.toUpperCase() }}</span></div>
                    <div v-if="(item as VideoItem).duration" class="info-row"><span class="info-label">{{ $t('时长：') }}</span><span class="info-value">{{ (item as VideoItem).duration }}</span></div>
                </template>
                <div class="info-row"><span class="info-label">{{ $t('上传时间：') }}</span><span class="info-value">{{ createTimeText }}</span></div>
            </div>
            <div class="info-actions">
                <el-button type="primary" @click="download">{{ isImage ? $t('下载原图') : $t('下载原视频') }}</el-button>
                <el-button @click="openOriginal">{{ isImage ? $t('查看原图') : $t('查看原视频') }}</el-button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { downloadUrlAsFile } from "@/utils/download";
import { timestampToDate } from "@/utils/time";
import type { GalleryPicItem, VideoItem } from "@/types/setting/gallery.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    item: GalleryPicItem | VideoItem | null;
    activeTab: "image" | "video";
    hasPrev: boolean;
    hasNext: boolean;
    albumName?: string;
}>();
const emit = defineEmits(["callback"]);

const onFormSubmit = () => {};

defineExpose({ onFormSubmit });

const isImage = computed(() => props.activeTab === "image");

const formatDateValue = (value?: string | number | null): string => {
    if (value === undefined || value === null || value === "") return "-";
    if (/^\d+$/.test(String(value))) return timestampToDate(Number(value));
    return String(value);
};
const formatFileSize = (size?: number | string | null): string => {
    if (size === undefined || size === null || size === "" || size === 0 || size === "0") return "-";
    const bytes = typeof size === "string" ? parseFloat(size) : size;
    if (isNaN(bytes) || bytes <= 0) return typeof size === "string" ? size : "-";
    if (bytes < 1024) return `${bytes}B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
};

const sizeText = computed(() => {
    const pic = props.item as GalleryPicItem;
    return pic?.picWidth && pic?.picHeight ? `${pic.picWidth} x ${pic.picHeight}` : "-";
});
const fileSizeText = computed(() => formatFileSize((props.item as GalleryPicItem)?.picSize));
const createTimeText = computed(() => formatDateValue((props.item as any)?.createTime ?? (props.item as any)?.addTime));
const title = computed(() => ((props.item as GalleryPicItem)?.picName || (props.item as VideoItem)?.videoName || t('素材预览')) as string);
const materialUrl = computed(() => {
    if (!props.item) return "";
    return isImage.value ? imageFormat((props.item as GalleryPicItem).picUrl || (props.item as GalleryPicItem).picThumb) : imageFormat((props.item as VideoItem).videoUrl);
});

const download = async () => {
    if (!materialUrl.value) return;
    try {
        await downloadUrlAsFile(materialUrl.value, title.value);
    } catch (error: any) {
        message.error(error.message || t('下载失败，请检查资源跨域配置'));
    }
};

const openOriginal = () => {
    if (materialUrl.value) window.open(materialUrl.value, "_blank", "noopener,noreferrer");
};
</script>

<style scoped lang="less">
.material-preview {
    display: flex;
    gap: 24px;
    height: 500px;
}
.preview-main {
    flex: 1;
    min-width: 0;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #1a1a1a;
    border-radius: 6px;
    overflow: hidden;
    .el-image,
    video {
        max-width: 100%;
        max-height: 100%;
    }
}
.preview-info {
    width: 260px;
    flex-shrink: 0;
    overflow-y: auto;
}
.info-title {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 600;
    color: #333;
}
.info-filename {
    margin: 0 0 20px;
    color: #666;
    font-size: 13px;
    line-height: 1.5;
    word-break: break-all;
}
.info-section-title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: #333;
}
.info-list {
    margin-bottom: 24px;
}
.info-row {
    display: flex;
    padding: 5px 0;
    font-size: 13px;
    line-height: 1.6;
}
.info-label {
    color: #999;
    flex-shrink: 0;
}
.info-value {
    color: #333;
    word-break: break-all;
}
.info-actions {
    display: flex;
    gap: 12px;
}
.preview-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 60px;
    border: 0;
    background: rgba(255, 255, 255, 0.15);
    cursor: pointer;
    z-index: 2;
    transition: background 0.2s;
    &:hover {
        background: rgba(255, 255, 255, 0.35);
    }
    &::before {
        content: "";
        display: block;
        width: 14px;
        height: 14px;
        border-top: 2.5px solid #fff;
        border-right: 2.5px solid #fff;
    }
    &.left {
        left: 0;
        border-radius: 0 4px 4px 0;
        &::before {
            margin-left: 8px;
            transform: rotate(-135deg);
        }
    }
    &.right {
        right: 0;
        border-radius: 4px 0 0 4px;
        &::before {
            margin-left: 4px;
            transform: rotate(45deg);
        }
    }
}
</style>

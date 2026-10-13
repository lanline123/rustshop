<template>
    <div
        class="drag-upload-zone"
        @dragenter.prevent="handleDragEnter"
        @dragover.prevent
        @dragleave.prevent="handleDragLeave"
        @drop.prevent="handleDrop"
    >
        <slot />

        <transition name="drag-fade">
            <div v-if="isDragging" class="drag-overlay">
                <div class="drag-overlay-inner">
                    <el-icon class="drag-icon"><Upload /></el-icon>
                    <p class="drag-text">{{ $t('松开即可上传') }}</p>
                    <p class="drag-hint">{{ hintText }}</p>
                </div>
            </div>
        </transition>

        <transition name="drag-fade">
            <div v-if="uploading" class="upload-loading-overlay">
                <div class="upload-loading-inner">
                    <span class="upload-spinner"></span>
                    <p class="upload-loading-text">{{ uploadingText || $t('正在上传...') }}</p>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { message } from "ant-design-vue";
import { Upload } from "@element-plus/icons-vue";
import { useConfigStore } from "@/store/config";
import { isWithinImageUploadLimit } from "@/utils/imageUploadLimit";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    activeTab: "image" | "video";
    uploading?: boolean;
    uploadingText?: string;
}>();

const emit = defineEmits<{ filesDropped: [files: File[]] }>();

const configStore = useConfigStore();
const imageMaxSize = computed(() => configStore.imageUploadMaxSize);
const videoMaxSize = computed(() => {
    const value = Number(configStore.config.uploadMaxSize);
    return Number.isFinite(value) && value > 0 ? value : 100;
});

const hintText = computed(() =>
    props.activeTab === "image"
        ? t('支持 JPG/PNG/GIF/BMP/WebP，单文件不超过 {imageUploadMaxSize}MB', { imageUploadMaxSize: imageMaxSize.value })
        : t('支持 MP4，单文件不超过 {value}MB', { value: videoMaxSize.value })
);

const isDragging = ref(false);
let dragCounter = 0;

const IMAGE_TYPES = ["image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"];
const IMAGE_EXTS = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"];
const VIDEO_EXTS = [".mp4"];

const handleDragEnter = (e: DragEvent) => {
    dragCounter++;
    if (e.dataTransfer?.types.includes("Files")) {
        isDragging.value = true;
    }
};

const handleDragLeave = () => {
    dragCounter--;
    if (dragCounter <= 0) {
        isDragging.value = false;
        dragCounter = 0;
    }
};

const validateFile = (file: File): string | null => {
    const ext = "." + (file.name.split(".").pop()?.toLowerCase() || "");
    if (props.activeTab === "image") {
        if (!IMAGE_TYPES.includes(file.type) && !IMAGE_EXTS.includes(ext)) {
            return t('{name} 格式不支持，仅支持 JPG/PNG/GIF/BMP/WebP', { name: file.name });
        }
        if (!isWithinImageUploadLimit(file, imageMaxSize.value)) {
            return t('{name} 超过 {imageUploadMaxSize}MB 限制', { name: file.name, imageUploadMaxSize: imageMaxSize.value });
        }
    } else {
        if (file.type !== "video/mp4" && !VIDEO_EXTS.includes(ext)) {
            return t('{name} 格式不支持，仅支持 MP4', { name: file.name });
        }
        if (file.size / 1024 / 1024 >= videoMaxSize.value) {
            return t('{name} 超过 {value}MB 限制', { name: file.name, value: videoMaxSize.value });
        }
    }
    return null;
};

const handleDrop = (e: DragEvent) => {
    isDragging.value = false;
    dragCounter = 0;

    const files = Array.from(e.dataTransfer?.files || []);
    if (!files.length) return;

    const validFiles: File[] = [];
    for (const file of files) {
        const error = validateFile(file);
        if (error) {
            message.warning(error);
        } else {
            validFiles.push(file);
        }
    }

    if (validFiles.length) emit("filesDropped", validFiles);
};
</script>

<style scoped lang="less">
.drag-upload-zone {
    position: relative;
    min-height: 200px;
}
.drag-overlay {
    position: absolute;
    inset: 8px;
    z-index: 100;
    background: rgba(21, 91, 212, 0.06);
    border: 2px dashed var(--tig-primary, #155bd4);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    .drag-overlay-inner {
        text-align: center;
    }
    .drag-icon {
        font-size: 48px;
        color: var(--tig-primary, #155bd4);
    }
    .drag-text {
        margin-top: 12px;
        font-size: 16px;
        font-weight: 500;
        color: var(--tig-primary, #155bd4);
    }
    .drag-hint {
        margin-top: 4px;
        font-size: 13px;
        color: #999;
    }
}
.upload-loading-overlay {
    position: absolute;
    inset: 8px;
    z-index: 101;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    .upload-loading-inner {
        text-align: center;
    }
    .upload-spinner {
        display: inline-block;
        width: 36px;
        height: 36px;
        border: 3px solid rgba(21, 91, 212, 0.2);
        border-top-color: var(--tig-primary, #155bd4);
        border-radius: 50%;
        animation: drag-upload-spin 0.8s linear infinite;
    }
    .upload-loading-text {
        margin-top: 12px;
        font-size: 14px;
        font-weight: 500;
        color: var(--tig-primary, #155bd4);
    }
}
@keyframes drag-upload-spin {
    to {
        transform: rotate(360deg);
    }
}
.drag-fade-enter-active,
.drag-fade-leave-active {
    transition: opacity 0.2s;
}
.drag-fade-enter-from,
.drag-fade-leave-to {
    opacity: 0;
}
</style>

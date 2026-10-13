<template>
    <el-form label-width="90px" class="upload-image-form">
        <el-form-item :label="$t('上传至分组')">
            <div class="gallery-select-row">
                <el-tree-select v-model="galleryId" :data="treeOptions" check-strictly :render-after-expand="false" class="upload-gallery-select" />
            </div>
        </el-form-item>
        <el-form-item :label="$t('上传图片')">
            <el-upload
                ref="uploadRef"
                name="file"
                action="#"
                :multiple="true"
                accept="image/*"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileChange"
                class="material-upload-dragger"
            >
                <div class="upload-dragger-content">
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    <div class="upload-text">{{ $t('点击或将文件拖拽到这里上传') }}</div>
                    <div v-if="fileList.length" class="upload-count">{{ $t('({length}张)', { length: fileList.length }) }}</div>
                </div>
            </el-upload>
            <div>
                <div v-if="fileList.length" class="file-preview-list">
                    <div v-for="(file, index) in fileList" :key="file.uid" class="file-preview-item">
                        <img :src="getThumbUrl(file)" class="file-thumb" />
                        <span class="file-status-tag">{{ $t('待上传') }}</span>
                        <el-icon class="file-remove" @click.stop="removeFile(index)"><Close /></el-icon>
                    </div>
                </div>
                <div class="upload-notice">
                    <div>{{ $t('注意： 1. 图片上传支持bmp、png、jpeg、jpg、gif、svg、webp格式；') }}</div>
                    <div style="padding-left: 42px">{{ $t('2. 最大支持{imageUploadMaxSize}M的图片；', { imageUploadMaxSize }) }}</div>
                </div>
            </div>
        </el-form-item>
    </el-form>
    <DialogForm
        v-model:visible="addGroupVisible"
        path="content/material-center/components/AlbumFormDialog"
        :title="$t('新建分组')"
        width="420px"
        :okText="$t('确定')"
        :params="{ mode: 'add', albumType: 'image', albumId: undefined, parentId: 0, initialName: '' }"
        @okCallback="handleGroupAdded"
    />
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { Upload, Close } from "@element-plus/icons-vue";
import type { UploadFile } from "element-plus";
import { requestUrl } from "@/utils/request";
import { DialogForm } from "@/components/dialog";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";
import { useConfigStore } from "@/store/config";
import { filterFilesByImageUploadLimit, isWithinImageUploadLimit } from "@/utils/imageUploadLimit";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{ treeData: GalleryTreeNode[]; currentAlbumId: GalleryNodeId }>();
const emit = defineEmits(["submitCallback", "callback"]);
const configStore = useConfigStore();
const imageUploadMaxSize = computed(() => configStore.imageUploadMaxSize);
const galleryId = ref<GalleryNodeId>(0);
const fileList = ref<UploadFile[]>([]);
const addGroupVisible = ref(false);
const thumbCache = new Map<number | string, string>();

watch(
    () => props.currentAlbumId,
    (val) => (galleryId.value = val || 0),
    { immediate: true }
);

const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({
        label: node.galleryName || node.name,
        value: node.galleryId || node.id,
        children: node.children?.length ? mapTree(node.children) : undefined
    }));
const treeOptions = computed(() => [{ label: t('全部图片'), value: 0 }, ...mapTree(props.treeData)]);

const getThumbUrl = (file: UploadFile): string => {
    if (file.url) return file.url;
    const cached = thumbCache.get(file.uid);
    if (cached) return cached;
    if (file.raw) {
        const url = URL.createObjectURL(file.raw);
        thumbCache.set(file.uid, url);
        return url;
    }
    return "";
};

const handleFileChange = (file: UploadFile) => {
    if (file.status !== "ready" || !file.raw) {
        return;
    }
    if (!isWithinImageUploadLimit(file.raw, imageUploadMaxSize.value)) {
        message.warning(t('{name} 超过 {imageUploadMaxSize}MB 限制', { name: file.name, imageUploadMaxSize: imageUploadMaxSize.value }));
        return;
    }
    const exists = fileList.value.some((f) => f.uid === file.uid);
    if (!exists) fileList.value.push(file);
};

const removeFile = (index: number) => {
    fileList.value.splice(index, 1);
};

const handleGroupAdded = () => {
    emit("callback", { type: "reloadTree" });
};

const onFormSubmit = async () => {
    const uploadItems = fileList.value.filter((file): file is UploadFile & { raw: File } => Boolean(file.raw));
    const { accepted } = filterFilesByImageUploadLimit(
        uploadItems.map((file) => file.raw),
        imageUploadMaxSize.value
    );
    const acceptedFiles = new Set(accepted);
    const validUploadItems = uploadItems.filter((file) => acceptedFiles.has(file.raw));

    if (!validUploadItems.length) {
        return;
    }
    const url = requestUrl.prefix + "/setting/galleryPic/uploadImg?galleryId=" + toApiGalleryId(galleryId.value);
    const headers = requestUrl.headers();

    await Promise.all(
        validUploadItems.map(async (file) => {
            const formData = new FormData();
            formData.append("file", file.raw);
            try {
                const resp = await fetch(url, { method: "POST", headers, body: formData });
                const json = await resp.json();
                if (json?.code && json.code !== 0) {
                    message.error(json.message || t('{name} 上传失败', { name: file.name }));
                }
            } catch {
                message.error(t('{name} 上传失败', { name: file.name }));
            }
        })
    );

    emit("submitCallback");
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.gallery-select-row {
    display: flex;
    align-items: center;
    gap: 12px;
}
.upload-gallery-select {
    width: 460px;
}
.add-group-link {
    color: var(--tig-primary);
    font-size: 14px;
    cursor: pointer;
    white-space: nowrap;
    &:hover {
        text-decoration: underline;
    }
}
.material-upload-dragger {
    width: 100%;
    :deep(.el-upload) {
        width: 100%;
    }
    :deep(.el-upload-dragger) {
        width: 100%;
        height: 150px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fafbfc;
        border-color: #dcdfe6;
    }
}
.upload-dragger-content {
    text-align: center;
}
.upload-icon {
    color: #c0c4cc;
    font-size: 28px;
}
.upload-text {
    margin-top: 12px;
    font-size: 14px;
    color: #606266;
}
.upload-count {
    margin-top: 4px;
    font-size: 13px;
    color: #909399;
}
.file-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 16px;
}
.file-preview-item {
    position: relative;
    width: 104px;
    height: 104px;
    border-radius: 4px;
    overflow: hidden;
    .file-thumb {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }
    .file-status-tag {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        text-align: center;
        font-size: 12px;
        color: #fff;
        background: rgba(0, 0, 0, 0.5);
        line-height: 22px;
    }
    .file-remove {
        position: absolute;
        top: 4px;
        right: 4px;
        width: 18px;
        height: 18px;
        background: rgba(0, 0, 0, 0.4);
        border-radius: 50%;
        color: #fff;
        font-size: 10px;
        cursor: pointer;
        display: none;
        align-items: center;
        justify-content: center;
    }
    &:hover .file-remove {
        display: flex;
    }
}
.upload-notice {
    margin-top: 16px;
    color: #999;
    font-size: 13px;
    line-height: 24px;
}
</style>

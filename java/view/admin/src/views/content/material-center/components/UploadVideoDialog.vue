<template>
    <div class="upload-video-form">
        <el-form ref="formRef" :model="formState" label-width="110px">
            <el-form-item prop="videoUrl" :label="$t('本地视频')" :rules="[{ required: true, message: '请上传本地视频!' }]">
                <div>
                    <div v-if="formState.videoUrl" class="video-preview">
                        <div class="iconfont icon-cha1 btn-remove" @click="deleteVideo"></div>
                        <video :src="imageFormat(formState.videoUrl)" controls class="preview-video"></video>
                    </div>
                    <div v-else class="upload-video-box">
                        <Upload
                            ref="uploadRef"
                            name="file"
                            v-model:file-list="fileList"
                            :action="requestUrl.prefix + '/setting/galleryVideoInfo/uploadVideo'"
                            :headers="requestUrl.headers()"
                            :show-upload-list="showUploadList"
                            :before-upload="beforeUpload"
                            :progress="progress"
                            accept=".mp4,video/*"
                            @change="handleChange"
                        >
                            <div class="gallery-add-item">
                                <el-icon size="30" color="#9e9fa1"><Plus /></el-icon>
                                <span class="upload-hint">{{ $t('可拖拽文件上传') }}</span>
                            </div>
                        </Upload>
                    </div>
                    <div class="extra mt10">{{ $t('视频大小不超过{uploadMaxSize}MB，时长5秒-5分钟以内，建议时长15-190秒，支持的视频文件类型 .mp4。', { uploadMaxSize }) }}</div>
                </div>
            </el-form-item>
            <el-form-item prop="videoName" :label="$t('视频名称')" :rules="[{ required: true, validator: validateName }]">
                <el-input v-model="formState.videoName" :placeholder="$t('20个字以内')" :maxlength="20" class="form-input" />
            </el-form-item>
            <el-form-item prop="galleryId" :label="$t('所在视频分组')">
                <div class="gallery-select-row">
                    <el-cascader v-model="formState.galleryId" :options="cascaderOptions" :props="cascaderProps" :placeholder="$t('请选择视频分组')" class="gallery-cascader" />
                    <div class="gallery-actions">
                        <el-button link type="primary" @click="addGroupVisible = true">{{ $t('添加分组') }}</el-button>
                        <span class="divider">|</span>
                        <el-button link type="primary" :loading="galleryLoading" @click="loadGallery">{{ $t('刷新') }}</el-button>
                    </div>
                </div>
            </el-form-item>
            <el-form-item prop="videoCover" :label="$t('视频封面')">
                <div>
                    <FormAddGallery v-model:photo="formState.videoCover" />
                    <div class="extra">{{ $t('建议尺寸：800*800px，支持.jpg，.gif，.png格式，大小不超过{imageUploadMaxSize}MB。如果不添加封面，系统会默认截取视频的第一个画面作为封面。', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
                </div>
            </el-form-item>
        </el-form>

        <DialogForm
            v-model:visible="addGroupVisible"
            path="content/material-center/components/AlbumFormDialog"
            :title="$t('新建分组')"
            width="420px"
            :okText="$t('确定')"
            :params="{ mode: 'add', albumType: 'video', albumId: undefined, parentId: 0, initialName: '' }"
            @okCallback="loadGallery"
        />
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { message, Upload } from "ant-design-vue";
import type { UploadChangeParam, UploadProps } from "ant-design-vue";
import { Plus } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { FormAddGallery } from "@/components/gallery";
import { requestUrl } from "@/utils/request";
import { imageFormat } from "@/utils/format";
import { formatDuration } from "@/utils/time";
import { getAllCategory, updateVideoInfo } from "@/api/setting/gallery";
import { useConfigStore } from "@/store/config";
import { toApiGalleryId } from "@/utils/galleryTree";
import type { GalleryNodeId, GalleryTreeNode, VideoDetail } from "@/types/setting/gallery.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{ treeData: GalleryTreeNode[]; currentAlbumId: GalleryNodeId }>();
const emit = defineEmits(["submitCallback"]);

const configStore = useConfigStore();
configStore.updateConfig();
const uploadMaxSize = configStore.config.uploadMaxSize;

const formRef = ref();
const uploadRef = ref<InstanceType<typeof Upload>>();
const fileList = ref<any[]>([]);
const showUploadList = ref(true);
const options = ref<any[]>([]);
const cascaderOptions = computed(() => [{ id: 0, name: t('全部视频') }, ...options.value]);
const galleryLoading = ref(false);
const addGroupVisible = ref(false);

const cascaderProps = {
    label: "name",
    value: "id",
    children: "children",
    checkStrictly: true,
    multiple: false,
    emitPath: false
};

const progress: UploadProps["progress"] = {
    strokeWidth: 5,
    format: (percent) => `${parseFloat((percent ?? "0").toString())}%`
};

const formState = ref<VideoDetail>({
    videoUrl: "",
    videoName: "",
    videoCover: "",
    galleryId: 0,
    format: "",
    duration: "",
    size: ""
});

const validateName = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('视频名称不能为空')));
    } else if (value.length > 20) {
        callback(new Error(t('视频名称不能超过20个字')));
    } else {
        callback();
    }
};

const findCascaderPath = (nodes: any[], targetId: GalleryNodeId, path: GalleryNodeId[] = []): GalleryNodeId[] | null => {
    for (const node of nodes) {
        const currentPath = [...path, node.id];
        if (String(node.id) === String(targetId)) return currentPath;
        if (node.children?.length) {
            const found = findCascaderPath(node.children, targetId, currentPath);
            if (found) return found;
        }
    }
    return null;
};

const loadGallery = async () => {
    galleryLoading.value = true;
    try {
        const result = await getAllCategory();
        options.value = result || [];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            galleryLoading.value = false;
        }, 200);
    }
};

const supportedTypes = { ".mp4": "video/mp4" };

const beforeUpload = (file: any) => {
    showUploadList.value = true;
    const fileExt = "." + file.name.split(".").pop().toLowerCase();
    if (!Object.keys(supportedTypes).includes(fileExt)) {
        message.error(t('不支持 {fileExt} 格式的视频文件', { fileExt }));
        return false;
    }
    const isLtSize = file.size / 1024 / 1024 < Number(uploadMaxSize);
    if (!isLtSize) {
        message.error(t('只能上传{uploadMaxSize}M以内的视频', { uploadMaxSize }));
    }
    useVideoInfo(file);
    return isLtSize;
};

const useVideoInfo = (file: any) => {
    const videoUrl = URL.createObjectURL(file);
    const video = document.createElement("video");
    video.src = videoUrl;
    video.onloadedmetadata = () => {
        formState.value.duration = formatDuration(video.duration);
        video.currentTime = 0.1;
    };
    video.onerror = () => {
        message.error(t('视频文件损坏或无法读取'));
        URL.revokeObjectURL(videoUrl);
    };
};

const handleChange = (info: UploadChangeParam) => {
    if (!info.file.status) {
        showUploadList.value = false;
        fileList.value = [];
    }
    if (info.file.status === "done") {
        const response: any = info.file.response;
        if (!response || response.code !== 0) {
            info.file.status = "error";
            message.error(response?.message || t('视频上传失败'));
        } else {
            fileList.value = [info.file];
            formState.value.size = info.file && info.file.size ? `${(info.file.size / 1024 / 1024).toFixed(2)}MB` : "";
            Object.assign(formState.value, response.data);
            formRef.value?.clearValidate?.("videoUrl");
            message.success(t('视频上传成功！'));
            showUploadList.value = false;
        }
    } else if (info.file.status === "error") {
        message.error(t('{name} 视频上传失败！', { name: info.file.name }));
    }
};

const deleteVideo = () => {
    formState.value.videoUrl = "";
    formState.value.videoName = "";
    formState.value.duration = "";
    fileList.value = [];
    showUploadList.value = true;
};

const formSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    let galleryId = formState.value.galleryId;
    if (!Array.isArray(galleryId)) galleryId = [toApiGalleryId(galleryId)];
    if (!galleryId.length) galleryId = [0];
    confirmLoading.value = true;
    try {
        await updateVideoInfo("create", {
            ...formState.value,
            galleryId,
            source: "upload"
        });
        message.success(t('操作成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('上传视频失败'));
    } finally {
        confirmLoading.value = false;
    }
};

onMounted(async () => {
    await loadGallery();
    const cur = toApiGalleryId(props.currentAlbumId);
    if (cur !== 0 && findCascaderPath(options.value, cur)) {
        formState.value.galleryId = cur;
    }
});

const onFormSubmit = () => {
    formSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.upload-video-form {
    padding: 8px 0;
}
.form-input {
    width: 100%;
    max-width: 360px;
}
.upload-video-box {
    width: 400px;
    max-width: 100%;
}
.gallery-add-item {
    width: 280px;
    max-width: 100%;
    height: 160px;
    background-color: #f7f8fa;
    border: 1px dashed #d9d9d9;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.2s;
    &:hover {
        border-color: var(--tig-primary, #155bd4);
    }
    .upload-hint {
        margin-top: 8px;
        font-size: 12px;
        color: #999;
    }
}
.video-preview {
    width: 280px;
    max-width: 100%;
    height: 160px;
    border: 1px solid #ddd;
    padding: 5px;
    position: relative;
    .preview-video {
        width: 100%;
        height: 100%;
    }
    .btn-remove {
        position: absolute;
        top: -10px;
        right: -10px;
        background-color: #7b7b7b;
        color: #fff;
        width: 20px;
        height: 20px;
        line-height: 22px;
        text-align: center;
        border-radius: 50%;
        cursor: pointer;
        font-size: 14px;
    }
}
.gallery-select-row {
    display: flex;
    align-items: center;
    width: 100%;
}
.gallery-cascader {
    width: 220px;
}
.gallery-actions {
    display: flex;
    align-items: center;
    margin-left: 10px;
    white-space: nowrap;
    .divider {
        margin: 0 8px;
        color: #ddd;
    }
}
.extra {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
    line-height: 1.6;
}
.mt10 {
    margin-top: 10px;
}
@media only screen and (max-width: 767px) {
    .upload-video-box,
    .gallery-add-item,
    .video-preview {
        width: 100%;
    }
    .gallery-select-row {
        flex-direction: column;
        align-items: flex-start;
    }
    .gallery-cascader {
        width: 100%;
    }
    .gallery-actions {
        margin-left: 0;
        margin-top: 8px;
    }
}
</style>

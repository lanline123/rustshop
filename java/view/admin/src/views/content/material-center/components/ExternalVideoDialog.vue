<template>
    <el-form ref="formRef" :model="formState" :rules="rules" label-width="110px" class="external-video-form">
        <el-form-item :label="$t('上传至分组')">
            <el-tree-select v-model="formState.galleryId" :data="treeOptions" check-strictly :render-after-expand="false" class="upload-gallery-select" />
        </el-form-item>
        <el-form-item :label="$t('外部视频地址')" prop="videoUrl">
            <TigInput v-model="formState.videoUrl" :placeholder="$t('请输入视频文件的网络地址')" clearable style="width: 360px" />
            <div class="input-tip">{{ $t('需要填写视频文件的网络地址，格式支持 mp4/flv/webm/avi；不能填视频网站播放页这种"页面地址"') }}</div>
        </el-form-item>
        <el-form-item :label="$t('视频名称')" prop="videoName">
            <TigInput v-model="formState.videoName" :placeholder="$t('20个字以内')" :maxlength="20" style="width: 360px" />
        </el-form-item>
        <el-form-item :label="$t('视频封面')">
            <FormAddGallery v-model:photo="formState.videoCover" />
            <div class="input-tip">{{ $t('建议尺寸：800*800px，支持 jpg/gif/png 格式，大小不超过 {imageUploadMaxSize}MB', { imageUploadMaxSize: configStore.imageUploadMaxSize }) }}</div>
        </el-form-item>
    </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { updateVideoInfo } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const configStore = useConfigStore();
const props = defineProps<{ treeData: GalleryTreeNode[]; currentAlbumId: GalleryNodeId }>();
const emit = defineEmits(["submitCallback"]);

const formRef = ref();
const formState = ref({
    galleryId: 0 as GalleryNodeId,
    videoUrl: "",
    videoName: "",
    videoCover: ""
});

const validateHttpUrl = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (value && !/^https?:\/\//i.test(value.trim())) {
        callback(new Error("请输入以 http:// 或 https:// 开头的网络地址"));
        return;
    }
    callback();
};

const rules = {
    videoUrl: [
        { required: true, message: t('请输入视频文件的网络地址'), trigger: "blur" },
        { validator: validateHttpUrl, trigger: "blur" }
    ],
    videoName: [{ required: true, message: t('请输入视频名称'), trigger: "blur" }]
};

watch(() => props.currentAlbumId, (val) => (formState.value.galleryId = val || 0), { immediate: true });

const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({ label: node.galleryName || node.name, value: node.galleryId || node.id, children: node.children?.length ? mapTree(node.children) : undefined }));
const treeOptions = computed(() => [{ label: t('全部视频'), value: 0 }, ...mapTree(props.treeData)]);

const onFormSubmit = async () => {
    await formRef.value.validate();
    try {
        await updateVideoInfo("create", {
            videoUrl: formState.value.videoUrl.trim(),
            videoName: formState.value.videoName,
            galleryId: [toApiGalleryId(formState.value.galleryId)],
            videoCover: formState.value.videoCover,
            source: "external",
            duration: "",
            size: "",
            format: ""
        });
        message.success(t('添加外部视频成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('添加外部视频失败'));
    }
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.external-video-form {
    padding: 32px 0px 28px 0px;
}
.upload-gallery-select {
    width: 360px;
}
.input-tip {
    margin-top: 8px;
    color: #999;
    font-size: 13px;
    line-height: 20px;
}
</style>

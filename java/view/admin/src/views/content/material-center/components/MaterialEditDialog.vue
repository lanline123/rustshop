<template>
    <el-form label-width="90px">
        <el-form-item :label="activeTab === 'image' ? $t('图片名称') : $t('视频名称')">
            <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('所属分组')">
            <el-tree-select v-model="form.galleryId" :data="treeOptions" check-strictly :render-after-expand="false" :placeholder="$t('请选择分组')" />
        </el-form-item>
        <template v-if="activeTab === 'video'">
            <el-form-item :label="$t('视频封面')">
                <FormAddGallery v-model:photo="cover" />
            </el-form-item>
            <el-form-item :label="$t('简介')">
                <el-input v-model="form.description" type="textarea" />
            </el-form-item>
        </template>
    </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { getGalleryPicDetail, getGalleryVideoInfoDetail, transferGalleryPic, updateGalleryPicField, updateVideoInfo } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{
    activeTab: "image" | "video";
    itemId?: GalleryNodeId;
    treeData: GalleryTreeNode[];
}>();
const emit = defineEmits(["submitCallback"]);
const loading = ref(false);
const original = ref<Record<string, any>>({});
const cover = ref<string>("");
const form = ref({ name: "", galleryId: 0 as GalleryNodeId, description: "" });

const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({
        label: node.galleryName || node.name,
        value: node.galleryId || node.id,
        children: node.children?.length ? mapTree(node.children) : undefined
    }));
const treeOptions = computed(() => [
    { label: props.activeTab === "image" ? t('全部图片') : t('全部视频'), value: 0 },
    ...mapTree(props.treeData)
]);

watch(() => props.itemId, async () => {
    if (!props.itemId) return;
    loading.value = true;
    try {
        const detail = props.activeTab === "image" ? await getGalleryPicDetail({ id: props.itemId }) : await getGalleryVideoInfoDetail({ id: props.itemId });
        original.value = { ...detail };
        form.value = {
            name: detail.picName || detail.videoName || "",
            galleryId: detail.galleryId || 0,
            description: detail.description || ""
        };
        cover.value = detail.videoCover || "";
    } catch (error: any) {
        message.error(error.message || t('加载详情失败'));
    } finally {
        loading.value = false;
    }
}, { immediate: true });

const submit = async () => {
    if (!props.itemId) return;
    confirmLoading.value = true;
    try {
        if (props.activeTab === "image") {
            await updateGalleryPicField({ id: props.itemId, field: "picName", val: form.value.name });
            const targetGalleryId = toApiGalleryId(form.value.galleryId);
            if (String(targetGalleryId) !== String(original.value.galleryId ?? 0)) {
                await transferGalleryPic({ ids: [props.itemId], targetGalleryId, type: "image" });
            }
        } else {
            await updateVideoInfo("update", {
                ...original.value,
                id: props.itemId,
                videoName: form.value.name,
                galleryId: toApiGalleryId(form.value.galleryId),
                videoCover: cover.value,
                description: form.value.description
            });
        }
        message.success(t('保存成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('保存失败'));
    } finally {
        confirmLoading.value = false;
    }
};

const onFormSubmit = () => {
    submit();
};

defineExpose({ onFormSubmit });
</script>

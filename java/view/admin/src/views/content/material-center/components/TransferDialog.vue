<template>
    <div>
        <p class="mb10">{{ $t('已选择{selectedCount}个素材，请选择目标分组：', { selectedCount }) }}</p>
        <el-tree-select v-model="targetId" :data="treeOptions" check-strictly :render-after-expand="false" style="width: 100%" />
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { transferGalleryPic } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{ treeData: GalleryTreeNode[]; selectedIds: GalleryNodeId[]; selectedCount: number; albumType: "image" | "video"; currentAlbumId?: GalleryNodeId }>();
const emit = defineEmits(["submitCallback"]);
const targetId = ref<GalleryNodeId>(0);

watch(() => props.selectedIds, () => (targetId.value = 0));

const isCurrentAlbum = (id: GalleryNodeId) => props.currentAlbumId !== undefined && String(id) === String(props.currentAlbumId);
const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => {
        const value = node.galleryId || node.id;
        return {
            label: node.galleryName || node.name,
            value,
            disabled: isCurrentAlbum(value),
            children: node.children?.length ? mapTree(node.children) : undefined
        };
    });
const treeOptions = computed(() => [
    { label: props.albumType === "image" ? t('全部图片') : t('全部视频'), value: 0, disabled: isCurrentAlbum(0) },
    ...mapTree(props.treeData)
]);

const submit = async () => {
    confirmLoading.value = true;
    try {
        await transferGalleryPic({ ids: props.selectedIds, targetGalleryId: toApiGalleryId(targetId.value), type: props.albumType });
        message.success(t('移动成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('移动失败'));
    } finally {
        confirmLoading.value = false;
    }
};

const onFormSubmit = () => {
    submit();
};

defineExpose({ onFormSubmit });
</script>

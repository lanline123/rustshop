<template>
    <el-form label-width="86px" class="group-manage-form">
        <el-form-item :label="$t('素材转移')">
            <div class="transfer-row">
                <span>{{ $t('将') }}</span>
                <el-tree-select v-model="sourceId" :data="treeOptions" check-strictly :render-after-expand="false" :placeholder="$t('请选择')" />
                <span>{{ $t('移动至') }}</span>
                <el-tree-select v-model="targetId" :data="treeOptions" check-strictly :render-after-expand="false" :placeholder="$t('请选择')" />
            </div>
        </el-form-item>
        <el-form-item :label="$t('转移方式')">
            <el-radio-group v-model="transferMode" class="transfer-mode-group">
                <el-radio label="move_and_delete">
                    <div class="transfer-mode-title">{{ $t('移动素材并删除分组') }}</div>
                    <div class="transfer-mode-desc">{{ $t('原分组及其下级分组将从原目录被删除') }}</div>
                </el-radio>
                <el-radio label="move_and_keep">
                    <div class="transfer-mode-title">{{ $t('移动素材，保留原分组') }}</div>
                    <div class="transfer-mode-desc">{{ $t('原分组及其下级分组的所有素材将被直接合并，并移动至新分组目录下') }}</div>
                </el-radio>
            </el-radio-group>
        </el-form-item>
    </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { moveGallery } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{ treeData: GalleryTreeNode[]; albumType: "image" | "video" }>();
const emit = defineEmits(["submitCallback"]);
const sourceId = ref<GalleryNodeId>();
const targetId = ref<GalleryNodeId>();
const transferMode = ref("move_and_delete");

watch(
    () => props.treeData,
    () => {
        sourceId.value = undefined;
        targetId.value = undefined;
        transferMode.value = "move_and_delete";
    }
);

const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({
        label: node.galleryName || node.name,
        value: node.galleryId || node.id,
        children: node.children?.length ? mapTree(node.children) : undefined
    }));
const treeOptions = computed(() => [
    { label: props.albumType === "image" ? t('全部图片') : t('全部视频'), value: 0 },
    ...mapTree(props.treeData)
]);

const submit = async () => {
    if (sourceId.value === undefined || targetId.value === undefined) {
        message.warning(t('请选择需要转移的分组和目标分组'));
        return;
    }
    if (String(sourceId.value) === String(targetId.value)) {
        message.warning(t('转移分组和目标分组不能相同'));
        return;
    }
    confirmLoading.value = true;
    try {
        await moveGallery({
            sourceId: toApiGalleryId(sourceId.value),
            targetId: toApiGalleryId(targetId.value),
            mode: transferMode.value,
            type: props.albumType
        });
        message.success(t('分组转移成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('分组转移失败'));
    } finally {
        confirmLoading.value = false;
    }
};

const onFormSubmit = () => {
    submit();
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.group-manage-form {
    padding: 18px 0 80px;
}
.transfer-row {
    display: flex;
    align-items: center;
    gap: 12px;

    :deep(.el-select),
    :deep(.el-tree-select) {
        width: 220px;
    }
}
.transfer-mode-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;

    :deep(.el-radio) {
        height: auto;
        align-items: flex-start;
        margin-right: 0;
    }

    :deep(.el-radio__input) {
        margin-top: 2px;
    }
}
.transfer-mode-title {
    color: #303133;
    line-height: 20px;
}
.transfer-mode-desc {
    margin-top: 12px;
    color: #999;
    line-height: 20px;
}
</style>

<template>
    <div class="album-delete-content">
        <div class="album-delete-tip">{{ $t('即将对当前分组进行删除') }}</div>
        <div class="album-delete-label">{{ $t('请选择删除策略') }}</div>
        <el-radio-group v-model="deleteMode" class="album-delete-options">
            <el-radio label="only_group">
                <span class="option-title">{{ $t('仅删除分组(推荐)') }}</span>
                <span class="option-desc">{{ $t('仅删除分组，组内的素材会移出分组，仍可在全部列表中找到') }}</span>
            </el-radio>
            <el-radio label="group_and_files">
                <span class="option-title">{{ $t('删除分组及组内素材') }}</span>
                <span class="option-desc">{{ $t('分组及组内素材将会一并删除，删除不会对已使用图片的业务产生影响。') }}</span>
            </el-radio>
        </el-radio-group>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { message } from "ant-design-vue";
import { delGalleryField, delGalleryVideoInfo } from "@/api/setting/gallery";
import type { GalleryNodeId } from "@/types/setting/gallery.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{ albumId?: GalleryNodeId; albumName?: string; albumType: "image" | "video" }>();
const emit = defineEmits(["submitCallback"]);
const deleteMode = ref("only_group");

const submit = async () => {
    if (!props.albumId) return;
    confirmLoading.value = true;
    try {
        const data = { id: props.albumId, deleteMode: deleteMode.value };
        props.albumType === "image" ? await delGalleryField(data) : await delGalleryVideoInfo(data);
        message.success(t('删除成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('删除失败'));
    } finally {
        confirmLoading.value = false;
    }
};

const onFormSubmit = () => {
    submit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less">
.album-delete-content {
    padding: 4px 0 2px;
}

.album-delete-tip {
    color: #1f2329;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
}

.album-delete-label {
    margin-top: 18px;
    color: #9aa0a6;
    font-size: 13px;
    line-height: 20px;
}

.album-delete-options {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    width: 100%;
    margin-top: 14px;

    .el-radio {
        height: auto;
        margin-right: 0;
        align-items: flex-start;
        white-space: normal;
    }

    .el-radio__input {
        margin-top: 3px;
    }

    .el-radio__label {
        display: flex;
        flex-direction: column;
        gap: 4px;
        white-space: normal;
    }

    .option-title {
        color: #1f2329;
        font-size: 14px;
        font-weight: 500;
        line-height: 22px;
    }

    .option-desc {
        color: #9aa0a6;
        font-size: 13px;
        line-height: 20px;
    }
}
</style>

<template>
    <div class="album-form-dialog-content">
        <el-form label-width="80px" @submit.prevent>
            <el-form-item>
                <template #label> {{ $t('分组名称') }} <span style="color: #f56c6c">*</span>
                </template>
                <el-input v-model="name" maxlength="20" show-word-limit :placeholder="$t('请输入分组名称')" @keyup.enter="submit" />
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { updateGalleryInfo, updateGalleryVideoInfo } from "@/api/setting/gallery";
import type { GalleryNodeId } from "@/types/setting/gallery.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const confirmLoading = defineModel<boolean>("confirmLoading", { default: false });
const props = defineProps<{
    mode: "add" | "rename";
    albumType: "image" | "video";
    albumId?: GalleryNodeId;
    parentId?: GalleryNodeId;
    initialName?: string;
}>();
const emit = defineEmits(["submitCallback"]);
const name = ref("");

watch(
    () => props.initialName,
    () => {
        name.value = props.initialName || "";
    }
);

onMounted(() => {
    name.value = props.initialName || "";
});

const submit = async () => {
    if (!name.value.trim()) {
        message.warning(t('请输入分组名称'));
        return;
    }
    confirmLoading.value = true;
    try {
        if (props.albumType === "image") {
            await updateGalleryInfo(props.mode === "add" ? "create" : "update", {
                id: props.albumId,
                parentId: props.parentId || 0,
                galleryName: name.value.trim()
            });
        } else {
            await updateGalleryVideoInfo(props.mode === "add" ? "create" : "update", {
                id: props.albumId,
                parentId: props.parentId || 0,
                name: name.value.trim()
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
<style scoped lang="scss">
.album-form-dialog-content {
    padding: 30px 10px;
}
</style>

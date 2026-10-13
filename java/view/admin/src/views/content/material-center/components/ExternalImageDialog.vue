<template>
    <el-form ref="formRef" :model="formState" :rules="rules" label-width="90px" class="external-image-form">
        <el-form-item :label="$t('所在分组')">
            <div class="gallery-select-row">
                <el-tree-select v-model="formState.galleryId" :data="treeOptions" check-strictly :render-after-expand="false" class="upload-gallery-select" />
            </div>
        </el-form-item>
        <el-form-item :label="$t('外部链接')" prop="picUrl">
            <TigInput v-model="formState.picUrl" :placeholder="$t('请输入图片的网络地址')" clearable style="width: 360px" />
            <div class="input-tip">{{ $t('需要填写单张图片的网络地址，格式支持 bmp/jpg/png/gif，不支持 webp/svg；不能填百度图片搜索结果页这种"页面地址"') }}</div>
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
import { DialogForm } from "@/components/dialog";
import { createExternalImage } from "@/api/setting/gallery";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { toApiGalleryId } from "@/utils/galleryTree";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{ treeData: GalleryTreeNode[]; currentAlbumId: GalleryNodeId }>();
const emit = defineEmits(["submitCallback", "callback"]);

const formRef = ref();
const addGroupVisible = ref(false);
const formState = ref({
    galleryId: 0 as GalleryNodeId,
    picUrl: ""
});

const validateHttpUrl = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (value && !/^https?:\/\//i.test(value.trim())) {
        callback(new Error("请输入以 http:// 或 https:// 开头的网络地址"));
        return;
    }
    callback();
};

const rules = {
    picUrl: [
        { required: true, message: t('请输入图片的网络地址'), trigger: "blur" },
        { validator: validateHttpUrl, trigger: "blur" }
    ]
};

watch(() => props.currentAlbumId, (val) => (formState.value.galleryId = val || 0), { immediate: true });

const mapTree = (nodes: GalleryTreeNode[]): any[] =>
    nodes.map((node) => ({ label: node.galleryName || node.name, value: node.galleryId || node.id, children: node.children?.length ? mapTree(node.children) : undefined }));
const treeOptions = computed(() => [{ label: t('全部图片'), value: 0 }, ...mapTree(props.treeData)]);

const handleGroupAdded = () => {
    emit("callback", { type: "reloadTree" });
};

const extractFileName = (url: string): string => {
    try {
        const pathname = new URL(url).pathname;
        const name = pathname.split("/").pop() || "";
        return name || t('外部图片');
    } catch {
        return t('外部图片');
    }
};

const onFormSubmit = async () => {
    await formRef.value.validate();
    const url = formState.value.picUrl.trim();
    if (!url) {
        message.warning(t('请输入图片的网络地址'));
        return;
    }
    try {
        await createExternalImage({
            picUrl: url,
            picThumb: url,
            picName: extractFileName(url),
            galleryId: toApiGalleryId(formState.value.galleryId),
            source: "external"
        });
        message.success(t('添加外部图片成功'));
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message || t('添加外部图片失败'));
    }
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
    width: 360px;
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
.input-tip {
    margin-top: 8px;
    color: #999;
    font-size: 13px;
    line-height: 20px;
}
</style>

<template>
    <div class="flex flex-align-center">
        <div class="avatar-type">
            <template v-if="!loading">
                <!-- 空态：点击打开相册 -->
                <DialogForm v-if="!avatar" type="gallery" class="avatar-trigger" @okCallback="onGallerySelect" :params="{ isMultiple: false }">
                    <!-- 空态样式参照 FormAddGallery “添加图片” -->
                    <div
                        class="flex avatar-empty"
                        style="flex-direction: column; height: 80px; width: 80px; align-items: center; justify-content: center; line-height: 22px"
                    >
                        <i class="iconfont icon-tianjiatupian"></i>
                        <span>{{ $t('添加图片') }}</span>
                    </div>
                </DialogForm>

                <!-- 回显态：展示图片 + 编辑/删除 -->
                <div v-else class="avatar-trigger have-image">
                    <!-- 用 key 强制刷新，避免同路径图片缓存导致“值变了但图片不更新” -->
                    <el-image class="avatar-image" :key="avatarSrc" :src="avatarSrc" fit="cover" />
                    <div class="bar">
                        <!-- 自定义头像：编辑打开相册 -->
                        <DialogForm
                            v-if="!isDefAvatar"
                            type="gallery"
                            class="bar-btn"
                            @okCallback="onGallerySelect"
                            :params="{ isMultiple: false }"
                        >
                            <span class="iconfont icon-bianji1"></span>
                        </DialogForm>
                        <!-- 系统头像：编辑打开系统头像弹窗 -->
                        <span v-else class="iconfont icon-bianji1 bar-btn" @click="openDefaultAvatar"></span>
                    </div>
                    <!-- 删除按钮：样式参照相册组件 -->
                    <p class="remove-btn" @click.stop>
                        <i class="iconfont icon-cha" @click="removeAvatar"></i>
                    </p>
                </div>
            </template>

            <!-- 仅用于弹窗，不展示 DefaultAvatar 自带触发UI -->
            <DefaultAvatar ref="defaultAvatarRef" v-if="!loading" v-model:avatar="avatar" :hideTrigger="true" />
        </div>
        <div class="extra">{{ $t('选择') }}<span class="tips" @click="openDefaultAvatar">{{ $t('系统头像') }}</span></div>
    </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { DefaultAvatar, FormAddGallery } from "@/components/gallery";
import { DialogForm } from "@/components/dialog";
import { avatarFormat } from "@/utils/format";
import { extractContent } from "@/utils/util";

const avatarType = defineModel<any>("avatarType", { type: Boolean, default: true });
const avatar = defineModel<any>("avatar", { type: String, default: "" });
const props = defineProps({
    loading: {
        type: Boolean,
        default: true
    }
});

const defaultAvatarRef = ref<any>(null);
const openDefaultAvatar = () => {
    defaultAvatarRef.value?.show?.();
};

const avatarKind = computed(() => extractContent(String(avatar.value || "")));
const isDefAvatar = computed(() => avatarKind.value === "def");
const avatarSrc = computed(() => avatarFormat(String(avatar.value || "")));

const onGallerySelect = (result: any) => {
    avatar.value = result?.[0]?.picUrl || "";
};

const removeAvatar = () => {
    avatar.value = "";
};

watch(
    () => avatar.value,
    (val) => {
        const t = extractContent(String(val || ""));
        if (t === "def") avatarType.value = false;
        if (t === "one") avatarType.value = true;
    },
    { immediate: true }
);
</script>
<style scoped lang="less">
.avatar-type {
    display: flex;
    flex-direction: row;
    gap: 10px;
    align-items: center;
    margin-right: 20px;
}

.tips {
    color: #1456f0;
    margin-right: 2px;
    cursor: pointer;
}

/* DialogForm 内部实际渲染元素是 .dialog-link，需要用 :deep 才能命中（scoped） */
:deep(.dialog-link.avatar-trigger),
.avatar-trigger {
    width: 80px;
    height: 80px;
    border: 1px dashed #ddd;
    background: #f5f6f9;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
}
:deep(.dialog-link.avatar-trigger:hover),
.avatar-trigger:hover {
    border-color: #155bd4;
}
.avatar-image {
    width: 80px;
    height: 80px;
}
.avatar-empty {
    color: #999;
    font-size: 12px;
}
.avatar-empty > i {
    font-size: 20px;
    color: #999;
}

.have-image {
    border-style: solid;
    background: #fff;
}

.remove-btn {
    position: absolute;
    top: -10px;
    right: -10px;
    background-color: #f5f7fa;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    text-align: center;
    line-height: 18px;
    display: none;
    i {
        font-size: 12px;
        cursor: pointer;
    }
}
.have-image:hover .remove-btn {
    display: block;
}

.bar {
    display: none;
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 20px;
    line-height: 20px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    padding: 0 10px;
    box-sizing: border-box;
}
.have-image:hover .bar {
    display: block;
}
.bar-btn {
    display: inline-block;
    color: #fff;
    cursor: pointer;
}
</style>

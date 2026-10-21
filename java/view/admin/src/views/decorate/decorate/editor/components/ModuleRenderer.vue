<template>
    <component
        v-if="previewComp"
        :is="previewComp"
        v-model:module="props.module.module"
        :module-index="props.module.moduleIndex"
        :decorate-id="decorateId"
    ></component>
</template>
<script lang="ts" setup>
import { shallowRef, watch, type Component } from "vue";
import { getPreviewComponent } from "../config/module-registry";
import type { DecorateModuleBase } from "@/types/decorate/editorModule";

// 统一以 v-model:module 接口渲染：legacy 旧 View.vue 的 defineModel 与
// 新 Preview.vue 的 props.module 均兼容（mutate 的是 store 内同一对象引用）
const props = defineProps<{
    module: DecorateModuleBase;
    decorateId?: string | number;
}>();

const previewComp = shallowRef<Component | null>(null);

watch(
    () => [props.module.type, props.module.url],
    async () => {
        previewComp.value = await getPreviewComponent(props.module.type, props.module.url);
        if (!previewComp.value) {
            console.warn(`[ModuleRenderer] 未注册的模块类型: ${props.module.type}@${props.module.url}`);
        }
    },
    { immediate: true }
);
</script>

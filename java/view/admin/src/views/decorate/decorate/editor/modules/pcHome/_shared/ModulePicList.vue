<template>
    <PicList v-model:photos="photos" v-bind="$attrs" />
</template>

<script setup lang="ts">
import { watch } from "vue";
import { PicList } from "@/components/decorate";

defineOptions({ name: "ModulePicList", inheritAttrs: false });

const photos = defineModel<unknown[]>("photos", { default: () => [] });

const emit = defineEmits<{
    (e: "change", value: unknown[]): void;
}>();

// 仅在列表「内容」真正变化时抛 change（用户增删改/拖拽排序/选链接），交给 Settings 同步 store。
// 外部回写（store→localData→prop，Settings 常 map 出同内容的新数组）即便引用变化，只要内容不变就跳过，
// 否则会与回写互相触发死循环。按内容判断同时覆盖「原地 mutate」与「整体重新赋值」两种 PicList 改动方式。
let lastSnapshot = JSON.stringify(photos.value ?? []);
watch(
    photos,
    (val) => {
        const snapshot = JSON.stringify(val ?? []);
        if (snapshot === lastSnapshot) return;
        lastSnapshot = snapshot;
        emit("change", val ?? []);
    },
    { deep: true }
);
</script>

<template>
    <up-loading-page :font-size="fontSize" :loading="modelValue" :image="image" :bg-color="bgColor" :loading-text="$t(loadingText)" :loading-mode="mode" />
</template>

<script setup lang="ts">
const props = defineProps({
    modelValue: {
        type: Boolean,
        default: true,
        required: true
    },
    loadingText: {
        type: String,
        default: "加载中"
    },
    mode: {
        type: String,
        default: "circle"
    },
    image: {
        type: String,
        default: ""
    },
    fontSize: {
        type: String,
        default: "16"
    },
    bgColor: {
        type: String,
        default: "#ffffff"
    }
});
</script>

<style lang="scss" scoped></style>

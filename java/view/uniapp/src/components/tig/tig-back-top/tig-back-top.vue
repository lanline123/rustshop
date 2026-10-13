<template>
    <u-back-top
        :mode="mode"
        :scroll-top="scrollTop"
        :icon-style="iconStyle"
        :bottom="styleFormat.bottom"
        :right="styleFormat.right"
        :duration="duration"
        :z-index="zIndex"
        :custom-style="customStyle"
    />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useTabbarStore } from "@/store/tabbar";
import { useConfigStore } from "@/store/config";
const tabbarStore = useTabbarStore();
const configStore = useConfigStore();
const props = defineProps({
    right: {
        type: Number,
        default: 15
    },
    bottom: {
        type: Number,
        default: 20
    },
    duration: {
        type: Number,
        default: 500
    },
    scrollTop: {
        type: [Number, String],
        default: 0
    },
    zIndex: {
        type: [Number, String],
        default: 999
    },
    mode: {
        type: String,
        default: "circle"
    },
    iconStyle: {
        type: Object,
        default: () => {
            return {
                fontSize: "32rpx",
                color: "#333"
            };
        }
    },
    customStyle: {
        type: Object,
        default: () => {
            return {
                backgroundColor: "#fff"
            };
        }
    }
});
const styleFormat = computed(() => {
    return {
        right: props.right + "rpx",
        bottom: props.bottom + tabbarStore.tabbarHeightNum + configStore.safeAreaInsets.bottom + "px"
    };
});
</script>

<style lang="scss" scoped></style>

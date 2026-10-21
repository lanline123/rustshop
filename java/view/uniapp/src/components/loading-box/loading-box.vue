<template>
    <view v-if="page > 1 && modelValue" class="loading-box">
        <view class="bottomLoading"><image lazy-load class="loading" :src="staticResource('common/loading.gif')" /></view>
    </view>
</template>

<script setup lang="ts">
import { staticResource } from "@/utils";
import { computed } from "vue";

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false,
        required: true
    },
    page: {
        type: Number,
        default: 1,
        required: true
    },
    length: {
        type: Number,
        default: 0,
        required: true
    },
    color: {
        type: String,
        default: ""
    }
});
const emit = defineEmits(["update:modelValue"]);
const textColor = computed(() => {
    return props.color;
});
</script>

<style lang="scss" scoped>
/* 触底加载 */
.loading-box {
    display: flex;
    justify-content: center;
    height: 60rpx;
    align-items: center;
    // padding: 20rpx 0 25rpx 0;
}
.loading-text {
    color: v-bind(textColor);
}
</style>

<template>
    <tig-popup v-model:show="show" :z-index="999" :show-close="false" background-color="#fff">
        <view class="picker-box">
            <slot />
            <picker-view indicator-style="height: 50px;" :value="innerIndex" class="picker-view" @change="changeHandler">
                <picker-view-column v-for="(col, colIndex) in columns" :key="colIndex">
                    <view v-for="(item, index) in col" :key="index" class="item">{{ item }}</view>
                </picker-view-column>
            </picker-view>
            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button class="btn" :custom-style="{ 'background-color': '#ff720d' }" @click="$emit('confirm')"> {{ $t("确定") }} </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
/**
 * 日期选择弹窗组件
 * - 选择开始和结束日期
 * - 弹窗形式展示日历
 */
import { computed, ref, watch } from "vue";
import type { PropType } from "vue";
const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    columns: {
        type: Array,
        default: () => []
    },
    defaultIndex: {
        type: Array as PropType<number[]>,
        default: () => [0, 0, 0]
    }
});
const emit = defineEmits(["update:modelValue", "change", "confirm"]);
const show = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});
const innerIndex = ref([0, 0, 0]);
watch(
    () => props.defaultIndex,
    (newVal) => {
        if (newVal.length > 0) {
            innerIndex.value = JSON.parse(JSON.stringify(newVal));
        }
    },
    { immediate: true }
);

const changeHandler = (e: any) => {
    emit("change", {
        values: props.columns.map((item: any, index) => item[e.detail.value[index]]),
        indexs: e.detail.value
    });
};
</script>

<style lang="scss" scoped>
.picker-box {
    height: 50vh;
    width: 100%;

    .picker-view {
        height: 400rpx;
        margin-top: 20rpx;
    }
    .item {
        line-height: 100rpx;
        text-align: center;
        font-size: 30rpx;
    }
}

.btn-box {
    padding: 25rpx;
}
</style>

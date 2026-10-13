<template>
    <view class="datetime-item" :class="{ disabled }" @tap="handleTap">
        <text class="iconfont-h5 icon-riqi" />
        <template v-if="type === 1">
            <view class="datetime-item-text" style="text-align: left">{{ modelValue ? modelValue : $t("选择日期") }}</view>
        </template>
        <template v-else-if="type === 2">
            <view class="datetime-item-text">{{ modelValue && modelValue[0] ? modelValue[0] : $t("开始日期") }}</view>
            -
            <view class="datetime-item-text">{{ modelValue && modelValue[1] ? modelValue[1] : $t("结束日期") }}</view>
        </template>
    </view>
</template>

<script setup lang="ts">
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif

const props = defineProps({
    modelValue: {
        type: [Array, String],
        default: ""
    },
    type: {
        type: Number,
        default: 1
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(["click"]);

const handleTap = () => {
    if (props.disabled) return;
    emit("click");
};
</script>

<style lang="scss" scoped>
.datetime-item {
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    background-color: #fff;
    color: #666;
    font-size: 14px;
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    border-radius: 4px;
    border: 1px solid #e5e5e5;
    column-gap: 2px;

    &.disabled {
        opacity: 0.5;
        cursor: default;
        touch-action: none;
    }

    .icon-riqi {
        color: rgb(192, 196, 204);
        font-size: 19px;
        padding-left: 6px;
    }

    .datetime-item-text {
        width: auto;
        height: 35px;
        padding-left: 5px;
        position: relative;
        flex: 1;
        line-height: 35px;
        font-size: 14px;
        overflow: hidden;
        text-align: center;
        min-width: 0;
    }
}
</style>

<template>
    <view class="tag">
        <slot>
            {{ $t("已过期") }}
        </slot>
    </view>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.tag {
    transform: translate(-50%, -50%) rotate(-20deg);
    width: 120rpx;
    height: 120rpx;
    line-height: 120rpx;
    text-align: center;
    border: 1.5px solid #818181;
    background-color: white;
    border-radius: 50%;
    color: #818181;
    font-weight: 500;
    font-size: 16px;
}
</style>

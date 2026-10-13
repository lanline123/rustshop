<template>
    <view>
        <view class="preview-box">
            <view>
                {{ $t("预览") }}：&emsp;
                <text>{{ $t("移动端") }}</text>
            </view>
        </view>
        <view class="placeholder" />
    </view>
</template>

<script setup lang="ts">
/**
 * 预览提示组件
 * - 装修预览模式下显示提示信息
 */
</script>

<style lang="scss" scoped>
.preview-box {
    height: 100rpx;
    display: flex;
    align-items: center;
    background-color: #fff;
    border-bottom: 1px solid #ebeef5;
    position: fixed;
    width: 100%;
    z-index: 999;
    left: 0;
    top: 0;
    padding-left: 20rpx;
    text {
        position: relative;
        display: inline-block;
        font-size: 28rpx;
        &::after {
            content: "";
            position: absolute;
            bottom: -10rpx;
            left: 0;
            width: 100%;
            height: 4rpx;
            background-color: #155bd4;
        }
    }
}

.placeholder {
    width: 100%;
    height: 0;
    padding-top: 100rpx;
}
</style>

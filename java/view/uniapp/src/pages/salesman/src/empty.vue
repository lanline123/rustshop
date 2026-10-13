<template>
    <view>
        <view class="van-empty">
            <view v-if="emptyImgUrl" class="van-empty__image" :style="styles">
                <image :src="emptyImgUrl" style="height: 100%; width: 100%" />
            </view>
            <view class="van-empty__description">{{ $t(emptyTxt) }}</view>
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 空状态组件
 * - 展示列表为空时的占位图
 * - 可自定义图片和文案
 */
import { staticResource } from "@/utils";
defineProps({
    emptyImgUrl: {
        type: String,
        default: staticResource("salesman/no_order.png")
    },
    emptyTxt: {
        type: String,
        default: "暂无数据"
    },
    styles: {
        type: Object,
        default: () => {}
    }
});
</script>

<style lang="scss" scoped>
.van-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    padding: 64rpx 0;
    padding-top: 150px;
}
.van-empty__image {
    width: 150rpx;
    height: 150rpx;
    img {
        width: 100%;
        height: 100%;
    }
}
.van-empty__description {
    margin-top: 32rpx;
    padding: 0 120rpx;
    color: #969799;
    font-size: 28rpx;
    line-height: 40rpx;
}
</style>

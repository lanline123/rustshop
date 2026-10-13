<template>
    <view class="service-card-box" :class="{ 'service-card-box--separated': separated }">
        <view class="service-card" @click="show = true">
            <view v-for="item in serviceList" :key="item.productServiceId" class="service-card-item">
                <text class="item-text line1">{{ item.productServiceName }}</text>
                <text class="iconfont-h5 icon-a-gengduo20" />
            </view>
        </view>
    </view>

    <tig-popup v-model:show="show" :show-close="false" background-color="#fff">
        <view class="service-popup">
            <view class="popup-title-box">
                <view class="title">{{ $t("服务保障") }}</view>
                <view class="close-icon" @click="show = false">
                    <image :src="staticResource('order/close.png')" />
                </view>
            </view>

            <scroll-view scroll-y class="popup-content">
                <view v-for="item in serviceList" :key="item.productServiceId" class="content-item">
                    <view class="item-title">{{ item.productServiceName }}</view>
                    <view v-if="item.productServiceDesc" class="item-content">{{ item.productServiceDesc }}</view>
                </view>
            </scroll-view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import type { ServiceList } from "@/types/product/product";
import { staticResource } from "@/utils";

interface Props {
    serviceList?: ServiceList[];
    separated?: boolean;
}

withDefaults(defineProps<Props>(), {
    serviceList: () => [],
    separated: false
});

const show = ref(false);
</script>

<style lang="scss" scoped>
.service-card-box {
    padding: 18rpx 28rpx 28rpx;
    background-color: var(--product-content-bg, #fff);

    &.service-card-box--separated {
        margin-top: 20rpx;
    }

    .service-card {
        padding: 7rpx 21rpx;
        overflow: hidden;
        background: #f7f8fc;
        border-radius: 14rpx;

        .service-card-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 21rpx 0;
            border-bottom: 2rpx solid #e0e1e5;

            &:last-child {
                border-bottom: none;
            }

            .item-text {
                flex: 1;
                width: 0;
                color: #1a1a1a;
                font-size: 24.5rpx;
                line-height: 32rpx;
            }

            .icon-a-gengduo20 {
                display: flex;
                align-items: center;
                margin-top: 5rpx;
                color: #888b94;
                font-size: 16rpx;
            }
        }
    }
}

.service-popup {
    overflow: hidden;
    border-radius: 14rpx 14rpx 0 0;

    .popup-content {
        box-sizing: border-box;
        max-height: 70vh;
        padding: 14rpx 28rpx 28rpx;

        .content-item {
            padding: 21rpx;
            margin-bottom: 21rpx;
            background-color: #f5f6fa;
            border-radius: 14rpx;

            &:last-child {
                margin-bottom: 0;
            }

            .item-title {
                color: #269636;
                font-size: 24.5rpx;
                font-weight: 700;
                line-height: 29rpx;
                text-align: left;
            }

            .item-content {
                margin-top: 11rpx;
                color: #505259;
                font-size: 24.5rpx;
                font-weight: 400;
                line-height: 29rpx;
                text-align: left;
                white-space: normal;
                word-break: break-word;
            }
        }
    }
}

.popup-title-box {
    display: flex;
    justify-content: center;
    padding: 35rpx 28rpx 28rpx;
    background-color: #fff;

    .title {
        display: flex;
        flex: 1;
        justify-content: center;
        font-size: 32rpx;
        font-weight: 700;
        line-height: 42rpx;
    }

    .close-icon {
        display: flex;
        align-items: center;

        image {
            width: 35rpx;
            height: 35rpx;
        }
    }
}
</style>

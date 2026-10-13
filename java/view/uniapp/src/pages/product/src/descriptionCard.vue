<template>
    <view class="description-card-box" :class="{ 'description-card-box--overseas': overseas }">
        <view class="description-card">
            <view class="description-card-title" @click="showMore = !showMore">
                <view class="title-item"> {{ $t("商品详情") }}</view>
                <text class="iconfont-h5 icon-a-gengduo20" :class="{ actvie: showMore }" />
            </view>
            <view v-show="showMore" class="description-content">
                <template v-if="isPro() && productType === 4">
                    <template v-if="isBuy === 1 && paidContent">
                        <template v-for="(item, index) in paidContent" :key="index">
                            <view v-if="item.type == 'pic'" class="desc-pic-item">
                                <image lazy-load :src="imageFormat(item?.pic || '')" class="slide-image" mode="widthFix" />
                            </view>
                            <rich-text v-else-if="item.type == 'text'" class="desc-text-item" :nodes="formatRichText(item.html)" />
                        </template>
                    </template>
                    <template v-else>
                        <image class="img" :src="staticResource('product/paid_content_bg.png')" mode="widthFix" />
                    </template>
                </template>

                <template v-for="(item, index) in descArr" :key="index">
                    <view v-if="item.type == 'pic'" class="desc-pic-item">
                        <image lazy-load :src="imageFormat(item?.pic || '')" class="slide-image" mode="widthFix" />
                    </view>
                    <rich-text v-else-if="item.type == 'text'" class="desc-text-item" :nodes="formatRichText(item.html)" />
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 商品详情卡片
 * - 标题栏点击展开/收起
 * - 富文本 / 图片渲染详情内容
 */
import { imageFormat } from "@/utils/format";
import { isPro, staticResource, formatRichText } from "@/utils";
import type { PropType } from "vue";

defineProps({
    descArr: {
        type: Array as PropType<Array<any>>,
        default: () => []
    },
    productId: {
        type: Number,
        default: 0
    },
    productType: {
        type: Number,
        default: 1
    },
    isBuy: {
        type: Number,
        default: 0
    },
    paidContent: {
        type: [Array, String],
        default: () => []
    },
    overseas: {
        type: Boolean,
        default: false
    }
});

const showMore = defineModel("showMore", { type: Boolean, default: false });
</script>

<style lang="scss" scoped>
.description-card-box {
    margin-bottom: 14rpx;

    &.description-card-box--overseas {
        margin-bottom: 0;
        padding-bottom: 28rpx;
        background: var(--product-page-bg, #f5f5f5);

        .description-card {
            border-top: 14rpx solid var(--product-page-bg, #f5f5f5);
            border-radius: 0;
        }
    }

    .description-card {
        background-color: var(--product-content-bg, #fff);

        .description-card-title {
            display: flex;
            width: 100%;
            overflow: hidden;
            font-size: 24.5rpx;
            line-height: 42rpx;
            padding: 28rpx;

            .title-item {
                color: #000;
                display: flex;
                align-items: center;
                flex: 1;
                font-weight: 700;
                font-size: 28rpx;
            }

            .icon-a-gengduo20 {
                font-size: 16rpx;
                color: rgb(136, 139, 148);
                display: flex;
                align-items: center;
                transform: rotate(90deg);

                &.actvie {
                    transform: rotate(-90deg);
                }
            }
        }

        .description-content {
            padding: 0 28rpx 28rpx;
            color: #9b9c9f;
            font-size: 24rpx;

            .img {
                width: 100%;
            }

            .desc-text-item {
                width: 100%;
                overflow: hidden;
                display: block;
                height: auto;
            }

            .desc-pic-item {
                .slide-image {
                    display: block;
                    width: 100%;
                }
            }
        }
    }
}
</style>

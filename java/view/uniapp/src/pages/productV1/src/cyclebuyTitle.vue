<template>
    <view class="product-group-title-box">
        <view class="product-group-title flex justify-between align-center" :style="titleStyle">
            <view class="activity-overlay" />
            <view class="group-price-box">
                <view class="title-top-price">
                    <view class="price-box">
                        <view class="price">
                            <format-price
                                :font-style="{ fontWeight: 'bold', fontSize: '48rpx' }"
                                :decimals-style="{
                                    fontSize: '32rpx',
                                    fontWeight: 'bold'
                                }"
                                :currency-style="{
                                    fontSize: '28rpx',
                                    fontWeight: 'bold'
                                }"
                                :price-data="productPrice"
                            />
                        </view>
                    </view>
                    <view class="title-top-market_price flex align-center">
                        <view class="originalprice-box">
                            <view>
                                <format-price :is-bottom="false" :price-data="productOriginPrice" :font-style="{ 'text-decoration': 'line-through' }" />
                            </view>
                        </view>
                        <view class="discountStock">
                            <view> {{ $t("已售") }}{{ productInfo.virtualSales }}</view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 周期购标题组件
 * - 显示周期购商品的价格区间
 * - 展示周期购专属活动标签
 */
import type { ProductItem } from "@/types/product/product";
import { computed } from "vue";
import { staticResource } from "@/utils";

interface Props {
    productInfo: ProductItem;
    productOriginPrice: string | number;
    productPrice: string | number;
    activityBgImage?: string;
}

const props = defineProps<Props>();
/**
 * 背景图样式
 */
const titleStyle = computed(() => {
    const url = props.activityBgImage || staticResource("groupon/titleBg.png");
    return {
        backgroundImage: `url(${url})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat"
    };
});
</script>

<style lang="scss" scoped>
.product-group-title-box {
    padding-bottom: 20rpx;
}
.product-group-title {
    position: relative;
    min-height: 140rpx;
    padding: 20rpx 20rpx 35rpx 25rpx;
    background-color: red;

    .activity-overlay {
        position: absolute;
        inset: 0;
        z-index: 0;
    }

    .group-price-box {
        position: relative;
        z-index: 1;
    }

    .title-top-price {
        .num-group {
            color: #fff;
            font-size: 28rpx;

            .separator {
                padding: 0 16rpx;
            }
        }

        .price-box {
            display: flex;
        }

        .price {
            color: #fff;
            font-size: 48rpx;
            display: flex;
            align-items: center;
            margin-top: 8rpx;
        }

        .title-top-market_price {
            font-size: 24rpx;

            .originalprice-box {
                display: flex;
                font-size: 24rpx;
                color: #fff;
            }

            .discountStock {
                color: #fff;
                position: relative;
                padding: 0 18rpx;
                margin-left: 18rpx;

                &::before {
                    position: absolute;
                    content: "";
                    left: 0;
                    top: 0;
                    width: 2rpx;
                    height: 100%;
                    background-color: #fff;
                }
            }
        }
    }
}
</style>

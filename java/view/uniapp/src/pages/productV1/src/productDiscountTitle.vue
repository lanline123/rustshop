<template>
    <view class="product-group-title-box">
        <view class="product-group-title flex justify-between align-center" :style="bgStyle">
            <view class="activity-overlay" />
            <view class="group-price-box">
                <view class="title-top-price">
                    <view class="price-box">
                        <view class="price">
                            <format-price
                                :font-style="{ fontSize: '48rpx', fontWeight: 'bold' }"
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
                        <view v-if="!showB2bText" class="tag-text-box">
                            <view class="tag-text">
                                {{ $t("直降") }}
                                <format-price :is-bottom="false" :font-style="{ fontWeight: 'bold' }" :price-data="discountValue.discountPrice" />
                            </view>
                        </view>
                    </view>

                    <view class="title-top-market_price flex align-center">
                        <view v-if="productOriginPrice && Number(productOriginPrice) > Number(productPrice)" class="originalprice-box">
                            {{ $t("价格") }}
                            <view>
                                <format-price :is-bottom="false" :price-data="productOriginPrice" :font-style="{ 'text-decoration': 'line-through' }" />
                            </view>
                        </view>
                        <view class="discountStock">
                            <view> {{ $t("剩余") }} {{ productStock }}</view>
                        </view>
                    </view>
                </view>
            </view>
            <view class="group-time-box">
                <view class="type-box">
                    <view class="type-text">{{ $t("限时折扣") }}</view>
                </view>
                <view class="countdown-box flex align-center">
                    <view class="title-top-time">{{ $t("距结束还剩") }}</view>
                    <tig-countdown background-color="rgba(0, 0, 0, .06)" :end-time="discountValue.discountEndTime" :font-size="11" color="#fff" />
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import type { ProductItem } from "@/types/product/product";
import { computed } from "vue";
import { staticResource } from "@/utils";
import { isShowB2bPriceText } from "@/utils/b2bPrice";

interface Props {
    productInfo: ProductItem;
    productPrice: string | number;
    productOriginPrice: string | number;
    discountValue: AnyObject;
    productStock: number;
    activityBgImage?: string;
}

const props = defineProps<Props>();

const showB2bText = computed(() => isShowB2bPriceText());

const bgStyle = computed(() => {
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

    .activity-overlay {
        position: absolute;
        inset: 0;
        z-index: 0;
    }

    .group-price-box,
    .group-time-box {
        position: relative;
        z-index: 1;
    }

    .title-top-price {
        .price-box {
            display: flex;

            .tag-text-box {
                display: inline-flex;
                align-items: flex-end;
                padding-left: 15rpx;
                position: relative;
                top: -8rpx;

                .tag-text {
                    background-color: #fff;
                    font-size: 20rpx;
                    color: var(--main-bg);
                    padding: 2rpx 6rpx;
                    border-radius: 25rpx;
                }
            }
        }

        .price {
            color: #fff;
            font-size: 48rpx;
            display: flex;
            align-items: center;
        }
        .title-top-market_price {
            .originalprice-box {
                display: flex;
                font-size: 18rpx;
                color: #fff;
                opacity: 0.7;
            }
            .discountStock {
                color: #fff;
                position: relative;
                font-size: 10px;
                padding: 0 6px;
                margin-left: 6px;
                &::before {
                    position: absolute;
                    content: "";
                    left: 0;
                    top: 4px;
                    width: 0.5px;
                    height: 8px;
                    opacity: 0.5;
                    background-color: #fff;
                }
            }
        }
    }
    .group-time-box {
        align-self: flex-start;
        padding-top: 14rpx;

        .title-top-time {
            font-size: 11px;
            color: #fff;
            padding-right: 8rpx;
        }
        .type-box {
            display: flex;
            justify-content: end;
            font-size: 24rpx;
            color: #fff;
            padding-bottom: 8rpx;
            position: relative;
            top: -5rpx;

            .type-text {
                font-style: italic;
                font-weight: bold;
            }
        }
    }
}
</style>

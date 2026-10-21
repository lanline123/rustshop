<template>
    <view class="product-group-title-box">
        <view class="product-group-title flex justify-between align-center" :style="bgStyle">
            <view class="activity-overlay" />
            <view class="group-price-box">
                <template v-if="presaleInfo.saleMode === 2">
                    <view class="deposit-amount flex align-center">
                        <text>{{ $t("预售价") }}</text>
                        <view class="deposit-amount-separator">
                            <text>.</text>
                        </view>
                        <text>{{ $t("定金") }}</text>
                        <view class="deposit-amount-price">
                            <format-price :price-data="presaleInfo.depositAmount" />
                        </view>
                    </view>
                </template>

                <view class="presale-price-row flex align-baseline">
                    <template v-if="presaleInfo.saleMode === 1">
                        <text class="presale-label">{{ $t("预售价") }}</text>
                    </template>
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
            <view v-if="configStore.showSelledCount" class="group-time-box">
                <view class="sold-box">
                    <text>{{ $t("已售") }}{{ soldCount }}{{ $t("件") }}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import type { ProductItem } from "@/types/product/product";
import type { PresaleInfo } from "@/types/presale/presale";
import { staticResource } from "@/utils";
import { computed } from "vue";
import { useConfigStore } from "@/store/config";

const configStore = useConfigStore();

interface Props {
    productInfo: ProductItem;
    productPrice: string | number;
    presaleInfo: PresaleInfo;
    soldCount?: number;
    activityBgImage?: string;
}

const props = withDefaults(defineProps<Props>(), {
    soldCount: 0
});

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

    .deposit-amount {
        color: #fff;
        font-size: 26rpx;
        font-weight: 500;
        margin-bottom: 10rpx;

        .deposit-amount-separator {
            width: 20rpx;
            display: flex;
            align-items: center;
            justify-content: center;

            text {
                font-size: 26rpx;
                font-weight: 500;
                color: #fff;
                line-height: 1;
                position: relative;
                top: -0.3em;
            }
        }

        .deposit-amount-price {
            position: relative;
            top: 2rpx;
        }
    }

    .presale-price-row {
        flex: 1;
        gap: 16rpx;
        color: #fff;

        .presale-label {
            font-size: 28rpx;
            font-weight: 500;
            line-height: 1.2;
        }
    }

    .group-time-box {
        align-self: flex-start;
        padding-top: 14rpx;
    }

    .sold-box {
        color: #fff;
        font-size: 26rpx;
        white-space: nowrap;
    }
}
</style>

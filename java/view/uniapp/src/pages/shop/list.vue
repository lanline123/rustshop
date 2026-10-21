<template>
    <tig-layout :title="$t('店铺街')">
        <template v-if="!isLoading && total > 0">
            <view class="shop-list">
                <template v-for="item in data" :key="item.shopId">
                    <view class="item-card">
                        <view class="item-info">
                            <view class="info-content">
                                <image class="info-img" :src="imageFormat(item.shopLogo)" />
                                <view class="info-text">
                                    <view class="info-title line1">{{ item.shopTitle }}</view>
                                    <view class="info-desc">
                                        <view class="desc-listingcount">{{ $t("商品数") }} {{ item.listingCount }}</view>
                                        <view class="desc-collectcount">{{ $t("收藏数") }} {{ item.collectCount }}</view>
                                    </view>
                                </view>
                            </view>
                            <view class="info-btn" @click="redirect({ url: `/pages/shop/index?shopId=${item.shopId}` })">{{ $t("进店") }}</view>
                        </view>
                        <template v-if="item.listingProduct.length > 0">
                            <view class="item-product">
                                <template v-for="subItem in item.listingProduct.slice(0, 3)" :key="subItem.productId">
                                    <view class="product-item" @click="redirect({ url: `/pages/product/index?id=${subItem.productId}` })">
                                        <image class="product-img" mode="widthFix" :src="productImage(subItem)" />
                                    </view>
                                </template>
                            </view>
                        </template>
                    </view>
                </template>
            </view>
        </template>
    </tig-layout>
</template>

<script setup lang="ts">
import { getShopList } from "@/api/shop/shop";
import type { shopListItem } from "@/types/shop/shop";
import { useList } from "@/hooks/index";
import { imageFormat, productImage } from "@/utils/format";
import { redirect } from "@/utils";

const { data, isLoading, total } = useList<shopListItem>(getShopList, {
    params: {
        page: 1,
        size: 10
    },
    path: {
        dataKey: "records"
    },
    immediate: true
});
</script>

<style lang="scss" scoped>
.shop-list {
    padding: 20rpx 20rpx 0;
    display: flex;
    flex-direction: column;
    row-gap: 20rpx;
    .item-card {
        background-color: #fff;
        border-radius: 30rpx;
        padding: 20rpx 20rpx;

        .item-info {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .info-content {
                display: flex;
                align-items: center;
                column-gap: 10rpx;

                .info-img {
                    width: 100rpx;
                    height: 100rpx;
                    border-radius: 50%;
                    overflow: hidden;
                }

                .info-text {
                    .info-title {
                        font-size: 32rpx;
                        color: #333;
                        font-weight: bold;
                    }

                    .info-desc {
                        padding-top: 10rpx;
                        display: flex;
                        color: #999;
                        font-size: 24rpx;
                        display: flex;
                        align-items: center;
                        column-gap: 15rpx;
                    }
                }
            }

            .info-btn {
                width: 120rpx;
                height: 58rpx;
                line-height: 56rpx;
                text-align: center;
                border: 1px solid var(--general);
                color: var(--general);
                border-radius: 10rpx;
                font-weight: bold;
                font-size: 28rpx;
            }
        }

        .item-product {
            display: flex;
            column-gap: 20rpx;
            padding-top: 25rpx;

            .product-item {
                flex: 1;
                border-radius: 20rpx;
                overflow: hidden;
                box-shadow: 0 0 2rpx 0 rgba(0, 0, 0, 0.1);
                .product-img {
                    width: 100%;
                }
            }
        }
    }
}
</style>

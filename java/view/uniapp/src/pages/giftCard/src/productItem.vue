<template>
    <view class="product-item" @click="toProductPage(product.productId)">
        <view class="product-img">
            <tig-image class="img" :src="pickPic(product)" mode="aspectFill" />
            <image v-if="productStyle?.borderImage" class="product-style-border" :src="productStyle.borderImage" mode="scaleToFill" />
        </view>
        <view class="product-info">
            <view class="product-name line1">{{ product.productName }}</view>
            <view v-if="skuDesc" class="product-sku-box">
                <view class="product-sku line1">{{ skuDesc }}</view>
            </view>
            <view class="product-price">
                <format-price
                    :style="{ fontSize: '36rpx', color: '#A9781B' }"
                    :currency-style="{
                        fontSize: '24rpx'
                    }"
                    :decimals-style="{
                        fontSize: '24rpx'
                    }"
                    :price-data="product.skuId ? product.skuPrice : product.productPrice"
                />
                <view class="product-num">x1</view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { pickPic } from "@/utils/format";
import type { GiftCardProduct } from "@/types/giftCard/giftCard";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";
import { redirect } from "@/utils";
import { computed, withDefaults } from "vue";

const props = withDefaults(
    defineProps<{
        product: GiftCardProduct;
        productStyle?: ProductActiveStyleVO | null;
    }>(),
    { productStyle: null }
);

const skuDesc = computed(() => {
    if (!props.product.skuData || !props.product.skuData.length) {
        return "";
    }
    try {
        const skuInfo = JSON.parse(props.product.skuData);
        return skuInfo
            .map((item: any) => {
                return item.name + ": " + item.value;
            })
            .join(" ");
    } catch {
        return "";
    }
});

const toProductPage = (id: number) => {
    redirect({
        url: `/pages/product/index?id=${id}`
    });
};
</script>

<style lang="scss" scoped>
.product-item {
    display: flex;
    gap: 30rpx;
    .product-img {
        position: relative;
        flex-shrink: 0;
        border-radius: 8rpx;
        width: 170rpx;
        height: 170rpx;
        overflow: hidden;
        background: #f2f2f2;
        .product-style-border {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            z-index: 5;
            pointer-events: none;
        }
        .img {
            text-align: center;
            display: flex;
            justify-content: center;
        }
    }
    .product-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-width: 0;
        .product-name {
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 1;
            font-size: 32rpx;
            color: #262626;
        }
        .product-sku-box {
            display: flex;
            .product-sku {
                font-size: 24rpx;
                line-height: 24rpx;
                color: #8c8c8c;
                padding: 10rpx 32rpx;
                background: #f5f5f5;
                border-radius: 22rpx;
                overflow: hidden;
                flex-grow: 0;
                max-width: 80%;
            }
        }
    }
    .product-price {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        .product-num {
            font-size: 28rpx;
            color: #262626;
            font-weight: bold;
        }
    }
}
</style>

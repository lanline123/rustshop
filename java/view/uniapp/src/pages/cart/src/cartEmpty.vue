<template>
    <view class="cart-empty-box">
        <view class="cart-empty-content">
            <view class="empty-img">
                <image :src="staticResource('cart/cart-empty.png')" />
            </view>
            <view class="empty-text">{{ $t("还没有商品？") }}</view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 购物车空状态组件
 * - 显示购物车为空的提示图标和文字
 */
import { staticResource } from "@/utils";
</script>

<style lang="scss" scoped>
.cart-empty-box {
    background-color: #fff;
    flex: 1;
    height: 100%;
    min-height: 440rpx;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    display: flex;

    .cart-empty-content {
        .empty-img {
            height: 88rpx;
            width: 88rpx;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        image {
            width: 70rpx;
            height: 56rpx;
        }

        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24.5rpx;
        font-weight: 400;
        line-height: 32rpx;
        text-align: center;
        color: #c2c4cc;
        flex-direction: row;
    }
}
</style>

<template>
    <view class="content-item" @click="handleToProduct">
        <view class="store-card-item-content-left">
            <tig-image class="store-card-item-img" :src="pickPic(product)" />
        </view>
        <view class="store-card-item-content-right">
            <view class="store-card-item-content-right-title line1">{{ product.productName }}</view>

            <view v-if="isOverseas() || hasSkuText(product.skuData)" class="sku-card-box">
                <text v-if="hasSkuText(product.skuData)">{{ getSkuText(product.skuData) }}</text>
            </view>

            <template v-if="!isOverseas() && activityTags.length > 0">
                <view class="activity-box">
                    <template v-for="(activityItem, activityIndex) in activityTags" :key="activityIndex">
                        <activityCard font-size="22rpx" :activity-info="activityItem" card-type="sub" />
                    </template>
                </view>
            </template>

            <view class="right-price-box">
                <view class="store-card-item-content-right-price">
                    <format-price :price-data="product.price" :integer-style="{ fontSize: '18px', lineHeight: '24px' }" />
                </view>
                <view class="store-card-item-content-right-num"> x{{ product.quantity }}</view>
            </view>

            <template v-if="product.originPrice && product.originPrice !== product.price">
                <view class="discount-box">
                    <format-price
                        :font-style="{
                            fontWeight: '400',
                            textDecoration: 'line-through',
                            fontSize: '12px'
                        }"
                        :is-bottom="false"
                        :price-data="product.originPrice"
                    />
                    <view v-if="isOverseas()" class="discount-text">{{ getDiscount(product) }}% off</view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 结算页商品条目组件
 * - 展示商品图片、名称、规格、活动标、价格与数量
 * - 点击跳转商品详情页
 */
import { computed } from "vue";
import activityCard from "@/components/masonry/activityCard.vue";
import { getSkuText, hasSkuText, isOverseas } from "@/utils";
import { pickPic } from "@/utils/format";
import type { Cart } from "@/types/order/check";

const props = defineProps<{
    product: Cart;
}>();

/** 只展示活动类型 1（促销）与 6（优惠），与店铺卡原逻辑一致 */
const activityTags = computed<any[]>(() => {
    const list = (props.product.activityInfo as any[]) || [];
    if (!Array.isArray(list)) return [];
    return list.filter((item) => item.type === 1 || item.type === 6);
});

/** 划线价折扣百分比，跨境展示用 */
const getDiscount = (product: Cart) => {
    const origin = Number(product.originPrice);
    const price = Number(product.price);
    if (!origin || origin <= 0) return 0;
    return Math.floor(((origin - price) / origin) * 100);
};

const handleToProduct = () => {
    uni.navigateTo({
        url: `/pages/product/index?id=${props.product.productId}`
    });
};
</script>

<style lang="scss" scoped>
.content-item {
    display: flex;
    align-items: center;
    color: #1a1a1a;
    width: 100%;
}

.store-card-item-content-left {
    width: 155rpx;
    height: 155rpx;
    border-radius: 7rpx;
    overflow: hidden;
    flex-shrink: 0;

    .store-card-item-img {
        width: inherit;
        height: inherit;
    }
}

.store-card-item-content-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 14rpx 0 14rpx 14rpx;
    overflow: hidden;

    .store-card-item-content-right-title {
        font-weight: normal;
        font-size: 24.5rpx;
        width: 100%;
    }

    .sku-card-box {
        display: flex;
        font-size: 24.5rpx;
        margin-top: 14rpx;
        margin-bottom: 14rpx;
        color: #888b94;
    }

    .activity-box {
        margin-bottom: 10rpx;
    }

    .right-price-box {
        width: 100%;
        display: flex;
        justify-content: space-between;
    }

    .store-card-item-content-right-price {
        font-size: 21rpx;
        display: inline-block;
        margin-right: 10rpx;
    }

    .store-card-item-content-right-num {
        display: flex;
        align-items: center;
        font-size: 22rpx;
        color: #969799;
    }

    .discount-box {
        color: #888b94;
        display: flex;
        align-items: center;
        font-size: 12px;

        .discount-text {
            font-weight: 600;
            padding-left: 4px;
        }
    }
}
</style>

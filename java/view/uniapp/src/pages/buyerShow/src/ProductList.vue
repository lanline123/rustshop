<template>
    <template v-if="total">
        <view class="productList">
            <view class="total">{{ selectedCount }}/{{ total }}</view>
            <up-list :height="productListHeight" @scrolltolower="onScrolltolower">
                <template v-for="(item, index) in products" :key="index">
                    <view :class="[{ active: item.isChecked }, 'productInfo']">
                        <SoldOut :show-outsale="item.storeProductStock === 0" :item="item" stock-field="storeProductStock" @click="toProductDetail(item)">
                            <tig-lazy-image :src="pickPic(item)" class="image" mode="aspectFit" />
                            <!--                        <view class="image" :style="{ 'background-image': `url(${item.picUrl})` }" />-->
                        </SoldOut>
                        <view class="content">
                            <view class="title">{{ item.productName }}</view>
                            <format-price class="price" :price-data="item.productPrice" :font-style="{ fontSize: '28rpx' }" />
                        </view>
                        <tig-checkbox v-model:checked="item.isChecked" size="20" class="check-item" :disabled="item.isDisabled" />
                    </view>
                </template>
            </up-list>
        </view>
    </template>
</template>

<script setup lang="ts">
/**
 * 商品选择列表组件
 * - 展示用户已购商品列表
 * - 多选关联到买家秀
 */
import { computed, defineEmits, defineProps } from "vue";
import TigCheckbox from "@/components/tig/tig-checkbox/tig-checkbox.vue";
import SoldOut from "@/pages/buyerShow/buyerShowCompnents/SoldOut.vue";
import { redirect, staticResource } from "@/utils";
import { pickPic } from "@/utils/format";
import TigLazyImage from "@/components/tig/tig-lazy-image/tig-lazy-image.vue";

const products = defineModel<any>("products");
const total = defineModel<number>("total", {
    default: 0
});
const emit = defineEmits(["scrolltolower"]);
const onScrolltolower = () => {
    emit("scrolltolower");
};

defineProps({
    productListHeight: {
        type: Number,
        default: 0
    }
});

// 计算已选中数量
const selectedCount = computed(() => {
    return products.value ? products.value.filter((item: any) => item.isChecked).length : 0;
});

const toProductDetail = (item: any) => {
    redirect({
        url: "/pages/product/index",
        param: {
            id: item.productId
        }
    });
};
</script>

<style scoped lang="scss">
.productList {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    position: relative;
    .total {
        position: absolute;
        right: 0;
        top: -40rpx;
        font-weight: 500;
        font-size: 28rpx;
        color: #9a9a9a;
    }
    .productInfo {
        height: 242rpx;
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 17rpx 22rpx;
        box-sizing: border-box;
        background: #fafafa;
        border-radius: 8rpx;
        &.active {
            background: #fff5f3;
        }
        .image {
            flex-shrink: 0;
            width: 200rpx;
            height: 200rpx;
            background-size: cover;
            background-position: center;
        }
        .content {
            margin: 0 28rpx 0 10rpx;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 100%;
            flex: 1;
            .title {
                font-weight: bold;
                font-size: 30rpx;
                color: #2a2a2a;
                line-height: 45rpx;
                // 两行文本溢出省略号
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
                overflow: hidden;
                text-overflow: ellipsis;
                word-wrap: break-word;
                word-break: break-word;
            }
            .price {
                font-weight: bold;
                font-size: 24rpx;
                color: var(--price);
                line-height: 56rpx;
            }
        }
    }
}

:deep(.u-form-item__body__left) {
    font-weight: bold;
    font-size: 32rpx;
    color: #262626;
}
</style>

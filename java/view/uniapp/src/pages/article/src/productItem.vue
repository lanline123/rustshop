<template>
    <view class="productList">
        <template v-for="(product, index) in productList" :key="index">
            <view class="product-item">
                <view class="content-item" @click="handleLink(product.productId)">
                    <view class="product-item-left">
                        <tig-image class="product-item-img" :src="pickPic(product)" />
                        <image
                            v-if="styleMap[product.productId]?.borderImage"
                            class="product-style-border"
                            :src="styleMap[product.productId].borderImage"
                            mode="scaleToFill"
                        />
                    </view>
                    <view class="product-item-right">
                        <view class="product-item-title line2">{{ product.productName }}</view>
                        <view class="product-item-price-box">
                            <view class="product-item-price">
                                <format-price
                                    :font-style="{ fontWeight: 'bold', fontSize: '28rpx' }"
                                    :decimals-style="{
                                        fontSize: '20rpx',
                                        fontWeight: 'bold'
                                    }"
                                    :currency-style="{
                                        fontSize: '20rpx',
                                        fontWeight: 'bold'
                                    }"
                                    :price-data="product.productPrice"
                                />
                                <template v-if="product.marketPrice && product.marketPrice !== product.productPrice">
                                    <format-price
                                        :font-style="{
                                            fontWeight: '400',
                                            textDecoration: 'line-through',
                                            fontSize: '22rpx',
                                            color: '#969799',
                                            position: 'relative',
                                            left: '10rpx'
                                        }"
                                        :is-bottom="false"
                                        :price-data="product.marketPrice"
                                    />
                                </template>
                            </view>
                            <view class="product-item-num">x{{ product.quantity || 1 }}</view>
                        </view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script setup lang="ts">
import { pickPic } from "@/utils/format";
/**
 * 文章关联商品组件
 * - 展示文章中关联的商品卡片
 * - 显示商品图片、名称、价格
 */
import { ref, watch, onMounted } from "vue";
import { getProductDetail } from "@/api/product/product";
import type { ProductItem } from "@/types/product/product.d";
import { useProductStyle } from "@/hooks/useProductStyle";

interface ProductWithQuantity extends ProductItem {
    quantity?: number;
}

const props = defineProps({
    productIds: {
        type: Array,
        default: () => []
    }
});

const productList = ref<ProductWithQuantity[]>([]);
const { styleMap } = useProductStyle(productList);
const loading = ref(false);

// 获取商品详情
const fetchProductDetails = async () => {
    if (!props.productIds || props.productIds.length === 0) {
        productList.value = [];
        return;
    }

    loading.value = true;
    try {
        const promises = props.productIds.map(async (item: any) => {
            // 如果 productIds 是对象数组，包含 id 和 quantity
            if (typeof item === "object" && item.id) {
                try {
                    const result = await getProductDetail(item.id);
                    return {
                        ...result.item,
                        quantity: item.quantity || 1
                    };
                } catch (error) {
                    console.error(`获取商品 ${item.id} 详情失败:`, error);
                    return null;
                }
            }
            // 如果 productIds 只是 ID 数组
            else if (typeof item === "number" || typeof item === "string") {
                try {
                    const result = await getProductDetail(Number(item));
                    return {
                        ...result.item,
                        quantity: 1
                    };
                } catch (error) {
                    console.error(`获取商品 ${item} 详情失败:`, error);
                    return null;
                }
            }
            return null;
        });

        const results = await Promise.all(promises);
        productList.value = results.filter((item) => item !== null) as ProductWithQuantity[];
    } catch (error) {
        console.error("获取商品列表失败:", error);
    } finally {
        loading.value = false;
    }
};

// 跳转到商品详情页
const handleLink = (productId: number) => {
    if (!productId) return;
    uni.navigateTo({
        url: `/pages/product/index?id=${productId}`
    });
};

// 监听 productIds 变化
watch(
    () => props.productIds,
    () => {
        fetchProductDetails();
    },
    { deep: true, immediate: true }
);

onMounted(() => {
    fetchProductDetails();
});
</script>

<style scoped lang="scss">
.productList {
    margin-top: 15rpx;
}
.product-item {
    padding: 20rpx 0;
    border-bottom: 1px solid rgba(217, 217, 217, 0.56);
    &:last-child {
        border-bottom: none;
    }

    .content-item {
        display: flex;
    }

    .product-item-left {
        position: relative;
        width: 165rpx;
        height: 165rpx;
        //border-radius: 15rpx;
        overflow: hidden;
        //box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.04);

        .product-item-img {
            width: inherit;
            height: inherit;
        }
    }

    .product-item-right {
        width: calc(100% - 165rpx);
        padding-left: 18rpx;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .product-item-title {
            font-weight: normal;
            font-size: 28rpx;
            width: 100%;
            color: #333;
            margin-bottom: 10rpx;
        }

        .product-item-price-box {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: auto;
        }

        .product-item-price {
            display: flex;
            align-items: center;
            flex: 1;
        }

        .product-item-num {
            display: flex;
            align-items: center;
            font-size: 22rpx;
            color: #969799;
            margin-left: 20rpx;
        }
    }
}

.product-style-border {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    z-index: 5;
    pointer-events: none;
}

.line2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>

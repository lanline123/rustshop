<template>
    <view class="item-li" @click="toPage(item)">
        <view class="photo">
            <tig-image :src="pickPic(item)" mode="widthFix" />
            <view class="product-status-box">
                <view v-if="shouldShowSoldOut(item) || item.productStatus == 0" class="outsale">
                    {{ shouldShowSoldOut(item) ? $t("已售罄") : $t("已下架") }}
                </view>
            </view>
            <image v-if="productStyle?.borderImage" class="product-style-border" :src="productStyle.borderImage" mode="scaleToFill" />
        </view>
        <view class="info">
            <view class="detail">
                {{ item.productName }}
            </view>
            <view class="action">
                <template v-if="isWholesaleCardComputed">
                    <view class="pricenum wholesale-pricenum">
                        <text class="wholesale-label">{{ $t("批发价") }}</text>
                        <format-price
                            :font-style="{ fontSize: '32rpx' }"
                            :currency-style="{ fontSize: '22rpx', position: 'relative', top: '3rpx' }"
                            :price-data="item.firstWholesalePrice"
                        />
                        <text class="wholesale-suffix">{{ $t("起") }}</text>
                    </view>
                    <view class="wholesale-cta" @click.stop="openWholesaleSpec">{{ $t("选规格") }}</view>
                </template>
                <template v-else>
                    <view class="pricenum">
                        <format-price
                            :font-style="{ fontSize: '32rpx' }"
                            :currency-style="{ fontSize: '22rpx', position: 'relative', top: '3rpx' }"
                            :price-data="item.productPrice"
                        />
                    </view>
                    <buy :id="item.productId" :disabled="item.productStock == 0" @callback="getCallback">
                        <view class="buy_btn"><text class="iconfont-h5 icon-gouwuche3 buy_btn_icon" /></view>
                    </buy>
                </template>
            </view>
            <block v-if="item.shop">
                <view class="shop" @click.stop="handleToShop(item.shop.shopId)">
                    <view class="shop-name">{{ item.shop.shopTitle }}</view>
                    <view class="shop-btn">{{ $t("进店") }} <text class="iconfont-h5 icon-youjiantou" /></view>
                </view>
            </block>
        </view>
    </view>
    <WholesaleListSpec
        ref="wholesaleSpecRef"
        v-model="wholesaleProductId"
        @load-end="onWholesaleLoadEnd"
        @close="onWholesaleClose"
        @add-success="getCallback"
    />
</template>

<script lang="ts" setup>
import { computed, ref, type PropType } from "vue";
import { pickPic } from "@/utils/format";
import buy from "@/components/product/buy.vue";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
import WholesaleListSpec from "@/components/wholesale/WholesaleListSpec.vue";
import { useProductCardBuy } from "@/hooks/useProductCardBuy";

const props = defineProps({
    item: {
        type: Object as PropType<any>,
        default: () => ({})
    },
    productStyle: {
        type: Object as PropType<ProductActiveStyleVO | null>,
        default: null
    }
});
const emit = defineEmits(["callback"]);
const getCallback = () => {
    emit("callback");
};

const toPage = (item: any) => {
    let id = item.productId;
    uni.navigateTo({ url: "/pages/product/index?id=" + id });
};
const handleToShop = (id: number) => {
    uni.navigateTo({
        url: `/pages/shop/index?shopId=${id}`
    });
};

/**
 * 批发卡片判定：firstWholesalePrice 存在即走批发态（卡片展示批发价 + 选规格唤起 WholesaleListSpec）
 */
const { isWholesaleCard, cardProductId } = useProductCardBuy();
const isWholesaleCardComputed = computed(() => isWholesaleCard(props.item));
const wholesaleProductId = ref(0);
const wholesaleSpecRef = ref<InstanceType<typeof WholesaleListSpec>>();

const openWholesaleSpec = () => {
    wholesaleProductId.value = cardProductId(props.item);
};

const onWholesaleLoadEnd = (payload: { wholesale: boolean }) => {
    if (payload.wholesale) {
        wholesaleSpecRef.value?.handleShowPopup();
    } else {
        toPage(props.item);
    }
};

const onWholesaleClose = () => {
    wholesaleProductId.value = 0;
};
</script>
<style lang="scss" scoped>
.item-li {
    width: 100%;
    margin-bottom: 15rpx;
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
}
.item-li .photo {
    padding: 20rpx;
    text-align: center;
    position: relative;
}
.item-li .photo image {
    width: 100%;
}
.item-li .photo .product-style-border {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    z-index: 5;
    pointer-events: none;
}
.item-li .info {
    background: #ffffff;
}

.item-li .info .detail {
    margin: 0 20rpx 10rpx;
    display: block;
    line-height: 40rpx;
    height: 80rpx;
    overflow: hidden;
    font-size: 26rpx;
    display: block;
    font-weight: bold;
    color: #2a3145;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}

.item-li .info .detail .name {
    line-height: 40rpx;
    overflow: hidden;
    font-size: 26rpx;
    display: block;
    font-weight: bold;
    color: #2a3145;
    text-overflow: ellipsis;
    display: -webkit-box;
    display: -moz-box;
    -webkit-line-clamp: 2;
    -moz-line-clamp: 2;
    -webkit-box-orient: vertical;
    -moz-box-orient: vertical;
}
.item-li .info .shop {
    height: 50rpx;
    font-size: 26rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16rpx 10rpx;

    .shop-name {
        width: 70%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .shop-btn {
        display: flex;
        align-items: center;
        justify-content: center;

        .icon-youjiantou {
            font-size: 32rpx;
        }
    }
}
.item-li .info .action {
    padding: 0 16rpx 10rpx;
    display: flex;
    justify-content: space-between;
}

.item-li .info .price {
    font-weight: bold;
    display: inline-block;
    font-size: 30rpx;
}
.item-li .info .price text {
    font-size: 20rpx;
}

.item-li .buy_btn {
    margin-right: 10rpx;
    margin-top: 6rpx;
    color: var(--general);
    display: flex;
    align-items: center;
    justify-content: center;
    .buy_btn_icon {
        font-size: 46rpx;
    }
}
.item-li .buy_btn image {
    width: 100%;
    height: 100%;
}

.list-cart .buy_btn {
    display: none;
}
.list-overseas {
    background: #fff;
    -webkit-box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.05);
    box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.05);
}

.product-status-box {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;

    .outsale {
        width: 50%;
        height: 50%;
        line-height: 180rpx;
        background: rgba(0, 0, 0, 0.4);
        font-size: 38rpx;
        color: #ffffff;
        letter-spacing: 0;
        border-radius: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}

.pricenum {
    line-height: 56rpx;
    color: var(--general);
}

/* @vort-if common.wholesale */
.wholesale-pricenum {
    display: flex;
    align-items: baseline;
    flex-wrap: nowrap;
    overflow: hidden;
}
.wholesale-label {
    font-size: 20rpx;
    color: var(--general);
    background: rgba(252, 65, 65, 0.08);
    padding: 2rpx 8rpx;
    border-radius: 4rpx;
    margin-right: 8rpx;
    line-height: 1.2;
    flex-shrink: 0;
}
.wholesale-suffix {
    font-size: 22rpx;
    color: var(--general);
    margin-left: 4rpx;
}
.wholesale-cta {
    margin-right: 10rpx;
    margin-top: 6rpx;
    padding: 8rpx 20rpx;
    color: #fff;
    background: var(--general, #ff3700);
    font-size: 24rpx;
    border-radius: 30rpx;
    line-height: 1.2;
}
/* @vort-endif */
</style>

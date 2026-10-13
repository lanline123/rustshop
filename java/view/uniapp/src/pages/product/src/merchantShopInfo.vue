<template>
    <view class="merchant-shop-card" :class="`merchant-shop-card--${variant}`">
        <view class="merchant-shop-card__title" @click="handleToShop">
            <view class="merchant-shop-card__logo">
                <tig-image :src="imageFormat(shopInfo.shopLogo)" mode="aspectFit" />
            </view>
            <view class="merchant-shop-card__name line1">{{ shopInfo.shopTitle }}</view>
        </view>
        <view class="merchant-shop-card__actions">
            <view class="merchant-shop-card__button" @click="handleShopCollection">{{ collectText }}</view>
            <view class="merchant-shop-card__button" @click="handleToShop">
                <text class="iconfont-h5 icon-dianpu" />
                {{ $t("进店逛逛") }}
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { ShopDetailItem } from "@/types/shop/shop";
import { shopCollection } from "@/api/shop/shop";
import { imageFormat } from "@/utils/format";
import { redirect } from "@/utils";

const { t } = useI18n();

interface Props {
    shopInfo: ShopDetailItem;
    variant?: "vortmall" | "o2o" | "overseas";
}

const props = withDefaults(defineProps<Props>(), {
    variant: "o2o"
});
const emit = defineEmits<{
    refreshShopDetail: [shopId: number];
}>();

const collectText = computed(() => (props.shopInfo.collectShop ? t("取消收藏") : t("收藏店铺")));

const toggleCollection = async () => {
    try {
        await shopCollection({ shopId: props.shopInfo.shopId });
        uni.showToast({
            title: props.shopInfo.collectShop ? t("操作成功") : t("收藏成功")
        });
        emit("refreshShopDetail", props.shopInfo.shopId);
    } catch (error) {
        console.error(error);
    }
};

const handleShopCollection = () => {
    if (!props.shopInfo.collectShop) {
        toggleCollection();
        return;
    }

    uni.showModal({
        title: t("提示"),
        content: t("确认要取消收藏店铺吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: (res) => {
            if (res.confirm) toggleCollection();
        }
    });
};

const handleToShop = () => {
    if (!Number.isInteger(props.shopInfo.shopId) || props.shopInfo.shopId < 0) return;
    redirect({
        url: `/pages/shop/index?shopId=${props.shopInfo.shopId}`
    });
};
</script>

<style lang="scss" scoped>
.merchant-shop-card {
    margin-top: 14rpx;
    padding: 24rpx 28rpx;
    background-color: var(--product-content-bg, #fff);

    &--o2o {
        margin-top: 20rpx;
        border-radius: 20rpx;
    }

    &__title {
        display: flex;
        align-items: center;
        min-width: 0;
    }

    &__logo {
        flex-shrink: 0;
        width: 62rpx;
        height: 62rpx;
        margin-right: 20rpx;
        overflow: hidden;
        border-radius: 50%;
    }

    &__name {
        flex: 1;
        min-width: 0;
        color: #1a1a1a;
        font-size: 28rpx;
        font-weight: 600;
    }

    &__actions {
        display: flex;
        gap: 32rpx;
        margin-top: 24rpx;
    }

    &__button {
        display: flex;
        flex: 1;
        align-items: center;
        justify-content: center;
        height: 62rpx;
        border: 1px solid #d7d7d7;
        border-radius: 10rpx;
        color: #686868;
        font-size: 24rpx;

        .icon-dianpu {
            margin-right: 8rpx;
        }
    }
}
</style>

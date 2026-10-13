<template>
    <view
        class="shop-info-box"
        :class="{
            'shop-info-box--vortmall': variant === 'vortmall' || variant === 'vortmall-activity',
            'shop-info-box--vortmall-activity': variant === 'vortmall-activity',
            'shop-info-box--overseas': variant === 'overseas'
        }"
        @click="handleToShop"
    >
        <view class="shop-info-title">
            <view class="shop-info-title-left">
                <view class="shop-logo" :style="{ backgroundImage: `url(${imageFormat(showStoreInfo.shopLogo)})` }" />
            </view>
            <view class="shop-info-title-right line1">{{ showStoreInfo.shopTitle }}</view>
        </view>

        <view class="shop-info-btn" @click.stop="handleStoreList">
            <view>{{ $t("切换门店") }}</view>
            <view class="right-arrows"><text class="iconfont-h5 icon-gengduo1" style="font-size: 22rpx; color: #bfbfbf" /></view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 商品门店信息组件
 * - 显示门店名称、地址、距离
 * - 支持点击跳转门店详情
 * - 显示门店营业状态
 */
import type { StoreDetailData } from "@/types/store/store";
import { redirect } from "@/utils";
import { imageFormat } from "@/utils/format";
import { useRegionStore } from "@/store/region";
import { useConfigStore } from "@/store/config";
import { computed } from "vue";

const { t } = useI18n();
interface Props {
    storeInfo: StoreDetailData;
    productId?: number;
    shopId?: number;
    formType: boolean;
    variant?: "default" | "vortmall" | "vortmall-activity" | "overseas";
}
const props = withDefaults(defineProps<Props>(), {
    productId: undefined,
    shopId: undefined,
    variant: "default"
});

const configStore = useConfigStore();

const showStoreInfo = computed(() => {
    return {
        shopTitle: props.storeInfo?.shopTitle || t("商城自营"),
        shopLogo: props.storeInfo?.shopLogo || configStore.baseInfo.shopLogo
    };
});

const handleToShop = () => {
    if (!props.storeInfo?.shopId) return;

    redirect({
        url: `/pages/store/index?id=${props.storeInfo.shopId}`
    });
};

const handleStoreList = () => {
    if (!props.productId) return;

    const regionStore = useRegionStore();

    // formType 为 true 时（通过带 shopId 的链接直接进入商详），用 redirectTo 替换当前商详页，
    // 让门店列表选店后重新打开商详；不直接操作 history.back，避免 PC iframe 套壳下退出整个 H5 应用导致跳转失效
    redirect({
        url: `/pages/storeList/index?productId=${props.productId}&regionId=${regionStore.currentRegion.regionId}&latitude=${regionStore.currentLocationInfo.latitude}&longitude=${regionStore.currentLocationInfo.longitude}&shopId=${props.shopId ?? ""}`,
        mode: props.formType ? "redirectTo" : "navigateTo"
    });
};
</script>

<style lang="scss" scoped>
.shop-info-box {
    background-color: var(--product-content-bg, #fff);
    padding: 0.625rem;
    border-radius: 0.625rem;
    margin-top: 20rpx;
    display: flex;
    align-items: center;
    column-gap: 20rpx;

    .shop-info-title {
        display: flex;
        align-items: center;
        flex: 1;
        overflow: hidden;

        .shop-info-title-left {
            width: 62rpx;
            height: 62rpx;
            overflow: hidden;
            border-radius: 50%;
            margin-right: 20rpx;
            position: relative;

            .shop-logo {
                width: 100%;
                height: 100%;
                position: absolute;
                background-position: center center;
                background-size: contain;
                background-repeat: no-repeat;
            }
        }

        .shop-info-title-right {
            font-size: 26rpx;
            flex: 1;
            overflow: hidden;
        }
    }

    .shop-info-btn {
        display: flex;
        font-size: 26rpx;
        align-items: center;
        justify-content: flex-end;

        .right-arrows {
            position: relative;
            top: 0;
        }
    }

    &.shop-info-box--vortmall {
        margin: 20rpx 0 0;
        // A1 门店卡与评价/详情卡共用 28rpx 内容边界，避免 logo 和切换门店贴到屏幕边缘。
        padding: 22rpx 28rpx;
        border-radius: 0;
        column-gap: 16rpx;

        .shop-info-title {
            .shop-info-title-left {
                width: 52rpx;
                height: 52rpx;
                margin-right: 16rpx;
                border-radius: 10rpx;
                background: #f5f5f5;
            }

            .shop-info-title-right {
                color: #1a1a1a;
                font-size: 25rpx;
            }
        }

        .shop-info-btn {
            color: #1a1a1a;
            font-size: 25rpx;

            .right-arrows {
                margin-left: 8rpx;
            }
        }
    }

    /* A1 活动态的门店入口位于标题卡片外，需要自行补齐页面水平内边距。 */
    &.shop-info-box--vortmall-activity {
        padding-left: 28rpx;
        padding-right: 28rpx;
    }

    &.shop-info-box--overseas {
        margin: 14rpx 0 0;
        padding: 20rpx 28rpx;
        border-radius: 0;
    }
}
</style>

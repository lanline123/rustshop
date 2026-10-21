<template>
    <view class="shop-info-box" @click="handleToShop">
        <view class="shop-info-title">
            <view class="shop-info-title-left">
                <view class="shop-logo" :style="{ backgroundImage: `url(${imageFormat(showStoreInfo.shopLogo)})` }" />
            </view>
            <view class="shop-info-title-right line1">{{ showStoreInfo.shopTitle }}</view>
        </view>

        <view class="shop-info-btn" @click.stop="handleStoreList">
            <view class="">{{ formType ? $t("进入门店") : $t("切换门店") }}</view>
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
}
const props = defineProps<Props>();

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
    background-color: #fff;
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
            top: 3rpx;
        }
    }
}
</style>

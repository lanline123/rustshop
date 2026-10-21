<template>
    <tig-layout :title="$t('关于')" :border-bottom="true">
        <view class="container">
            <view class="version-info">
                <view class="logo">
                    <image class="image" mode="widthFix" :src="imageFormat(configStore.baseInfo.shopLogo)"></image>
                </view>
                <view class="version-code">{{ configStore.version }}</view>
                <view class="update-tip">{{ $t("当前已是最新版本") }}</view>
                <!-- <up-loading-icon v-if="isLoading" size="22" mode="circle" /> -->
            </view>
            <view class="copyrigth-info-wrap">
                <view class="row-info">{{ $t(configStore.shopIcpNo) }}</view>
                <view class="row-info">{{ $t(configStore.shopCompanyTxt) }}</view>
                <view class="row-info">{{ $t(configStore.baseInfo.shopTitle) }} {{ $t("版权所有") }}</view>
            </view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 关于页面
 * - 展示应用Logo、版本号
 * - 显示ICP备案号、公司信息、版权声明
 */
import { ref } from "vue";
import { useConfigStore } from "@/store/config";
import { imageFormat } from "@/utils/format";

const configStore = useConfigStore();
</script>

<style lang="scss" scoped>
page {
    background: #fff;
}
.container {
    display: flex;
    flex-direction: column;
    height: calc(100vh - var(--nav-height));
    background: #fff;
    .version-info {
        margin: 200rpx auto 0 auto;
        width: fit-content;
        .logo {
            width: 300rpx;
            height: 100rpx;
            .image {
                width: 100%;
                height: 100%;
            }
        }
        .version-code {
            margin-top: 20rpx;
            text-align: center;
            color: #343434;
            font-size: 24rpx;
        }
        .update-tip {
            margin-top: 20rpx;
            text-align: center;
            color: #9c9c9c;
            font-size: 24rpx;
        }
    }
    .copyrigth-info-wrap {
        display: flex;
        flex-direction: column;
        row-gap: 10rpx;
        align-items: center;
        margin-top: auto;
        margin-bottom: 60rpx;
        .row-info {
            color: #9c9c9c;
            font-size: 24rpx;
        }
    }
}
</style>

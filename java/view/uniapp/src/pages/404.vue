<template>
    <tig-layout>
        <view class="container-404">
            <view class="content-box">
                <image :src="staticResource('common/404.png')" mode="widthFix" />
            </view>
            <view class="text-box">{{ $t("很抱歉，您访问的页面不存在") }}</view>
            <view class="btn-404-box">
                <tig-button style="width: 50%; height: 100%" plain @click="handleLink('pre')">{{ $t("返回上一页") }}</tig-button>
                <tig-button style="width: 50%; height: 100%" @click="handleLink('home')">{{ $t("返回首页") }}</tig-button>
            </view>
        </view>
    </tig-layout>
</template>

<script setup lang="ts">
import { redirect, staticResource } from "@/utils";
const handleLink = (type: string) => {
    if (type === "home") {
        redirect({ url: "/pages/index/index" });
    } else {
        // #ifdef H5
        history.back();
        // #endif

        // #ifndef H5
        uni.navigateBack();
        // #endif
    }
};
</script>

<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.container-404 {
    padding: 20rpx;
    padding-top: 50rpx;
}
.content-box {
    display: flex;
    justify-content: center;
    width: 100%;
}

.btn-404-box {
    height: 110rpx;
    padding: 20rpx 10rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20rpx;
    column-gap: 10rpx;
}
.text-box {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #999;
    text-align: center;
}
</style>

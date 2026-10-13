<template>
    <view class="coupon-search">
        <up-search v-model="keyword" shape="square" :action-text="$t('取消')" @search="handleCustom" @custom="handleCustom" />
        <view v-if="searchHistory.length" class="search_init_box">
            <view class="search_history_box">
                <view class="title">
                    {{ $t("搜索历史") }}
                    <view class="clear_history" @click.stop="clearHistory"><text class="iconfont-h5 icon-shanchu" /></view>
                </view>
                <view class="list acea-row">
                    <block v-for="(item, index) in searchHistory" :key="index">
                        <view class="item" @click="searchKeywords(item)">{{ item }}</view>
                    </block>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 优惠券搜索页
 * - 搜索优惠券
 * - 显示搜索结果列表
 */
import { onLoad } from "@dcloudio/uni-app";

import { ref } from "vue";
const keyword = ref("");

const handleCustom = () => {
    if (keyword.value) {
        searchHistory.value.unshift(keyword.value);
        searchHistory.value = [...new Set(searchHistory.value)];
        if (searchHistory.value.length > 10) {
            searchHistory.value.splice(10, searchHistory.value.length - 10);
        }
        uni.setStorageSync("couponSearchHistory", searchHistory.value);
        uni.navigateBack({
            success: () => {
                uni.$emit("coupon-search-return", keyword.value);
            }
        });
    } else {
        uni.navigateBack({
            success: () => {
                uni.$emit("coupon-search-return", "");
            }
        });
    }
};

const searchHistory = ref<any[]>([]);

onLoad((options: any) => {
    if (uni.getStorageSync("couponSearchHistory")) {
        searchHistory.value = uni.getStorageSync("couponSearchHistory");
    }
});
const clearHistory = () => {
    searchHistory.value = [];
    uni.removeStorageSync("couponSearchHistory");
};

const searchKeywords = (item: any) => {
    uni.navigateBack({
        success: () => {
            uni.$emit("coupon-search-return", item);
        }
    });
};
</script>

<style>
page {
    background-color: #fff;
}
</style>
<style lang="scss" scoped>
.coupon-search {
    padding: 20rpx;
}

.search_init_box .title {
    font-size: 24rpx;
    color: #333;
    margin: 50rpx 30rpx 25rpx 30rpx;
    font-weight: bold;
    position: relative;
}
.search_init_box .list {
    padding-left: 10rpx;
}
.search_init_box .list .item {
    font-size: 24rpx;
    color: #666;
    padding: 0 28rpx;
    height: 50rpx;
    border-radius: 30rpx;
    line-height: 50rpx;
    margin: 0 0 20rpx 20rpx;
    background: #f6f6f6;
}

.clear_history {
    position: absolute;
    right: 0;
    top: 0;
    font-weight: normal;
    font-size: 26rpx;
    padding: 20rpx;
    top: -20rpx;
}
</style>

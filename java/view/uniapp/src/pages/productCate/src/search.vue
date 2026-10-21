<template>
    <view class="search-box">
        <view v-if="needMenu" class="btn-box" @click="$emit('menu')">
            <text class="iconfont-h5 icon-shouye" />
        </view>
        <view class="search-content" @click="$emit('search')">
            <text class="iconfont-h5 icon-sousuo1" />
            <text class="placeholder-text">{{ $t(placeholder) }}</text>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 搜索框组件
 * - 分类页顶部搜索入口
 * - 点击跳转搜索页
 */
const props = defineProps({
    placeholder: {
        type: String,
        default: "搜索商品"
    },
    needMenu: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["menu", "search"]);
</script>

<style lang="scss" scoped>
.search-box {
    width: 100%;
    height: 90rpx;
    padding: 0 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-bottom: 20rpx;
    border-bottom: 1px solid #f8f8f8;
    .btn-box {
        width: 90rpx;
        height: 70rpx;
        background-color: #f8f8f8;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        .icon-shouye {
            font-size: 34rpx;
        }
    }

    .search-content {
        width: 100%;
        height: 100%;
        background-color: #f8f8f8;
        border-radius: 100rpx;
        display: flex;
        align-items: center;
        padding: 0 30rpx;

        .placeholder-text {
            font-size: 26rpx;
            color: #999999;
            margin-left: 10rpx;
        }

        .icon-sousuo1 {
            font-size: 30rpx;
        }
    }
}
</style>

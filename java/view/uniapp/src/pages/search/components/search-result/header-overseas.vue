<template>
    <!-- 海外版搜索头（layoutStyle=2）：白底黑描边方角框 -->
    <view class="header-overseas">
        <view class="header acea-row row-center-wrapper">
            <view class="back-icon" @click.stop="previousPage">
                <text class="iconfont-h5 icon-fanhui" />
            </view>
            <view class="row-between-wrapper input" @click.stop="emit('toSearch')">
                <view v-if="!keyword" class="txt">{{ $t("请输入搜索内容") }}</view>
                <scroll-view v-else class="keyword-scroll" scroll-x :scroll-left="scrollLeft" show-scrollbar="false">
                    <view class="keyword-tag">
                        <text class="keyword-text">{{ keyword }}</text>
                    </view>
                </scroll-view>
                <text class="iconfont-h5 icon-a-34 search-icon" />
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 海外版搜索头
 * 由结果页外层 fixed 容器吸顶，组件内不再单独 fixed。
 * 根节点不用 .header，避免被父级遗留样式再次钉成 fixed。
 */
defineProps<{
    keyword: string;
    scrollLeft: number;
}>();

const emit = defineEmits<{
    (e: "toSearch"): void;
}>();

const previousPage = () => {
    const pages = getCurrentPages();
    if (pages.length > 1) {
        uni.navigateBack();
        return;
    }
    uni.switchTab({
        url: "/pages/index/index"
    });
};
</script>

<style lang="scss" scoped>
.header-overseas {
    width: 100%;
    background-color: var(--search-item-bg, #fff);

    .header {
        width: 100%;
        height: 99rpx;
        background-color: var(--search-item-bg, #fff);
        border-bottom: 1rpx solid #f5f5f5;
        padding: 0 var(--search-item-padding-lr, 32rpx);
        box-sizing: border-box;
        display: flex;
        align-items: center;
    }

    .back-icon {
        flex-shrink: 0;
        margin: 0 26rpx 0 5rpx;
        display: flex;
        align-items: center;

        .icon-fanhui {
            font-size: 28rpx;
            color: #1a1a1a;
        }
    }

    .input {
        flex: 1;
        width: 0;
        height: 68rpx;
        background-color: #fff;
        padding: 0 20rpx 0 24rpx;
        color: #1a1a1a;
        border: 1px solid #1a1a1a;
        border-radius: 8rpx;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .txt {
            color: #999;
            font-size: 26rpx;
        }

        .keyword-scroll {
            flex: 1;
            width: 0;
            height: 100%;
            margin-right: 12rpx;
            display: flex;
            align-items: center;
        }

        .keyword-tag {
            width: max-content;
            color: #1a1a1a;
            display: inline-flex;
            align-items: center;
            justify-content: flex-start;
            height: 100%;

            .keyword-text {
                white-space: nowrap;
                border-radius: 8rpx;
                padding: 4rpx 16rpx;
                background-color: #f2f3f5;
                font-size: 26rpx;
            }
        }

        .search-icon {
            flex-shrink: 0;
            font-size: 35rpx;
            color: #555;
        }
    }
}
</style>

<template>
    <view :class="'module-ad-con module-search_bar'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <view class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <view class="searchBar-warp">
                <view class="searchBar-con">
                    <view class="searchBar-item">
                        <view class="item-content">
                            <view class="item-search-con">
                                <view
                                    class="item-search-form"
                                    :style="allFormat.itemBackgroundColor + allFormat.itemBorderRadius + allFormat.itemHeight + allFormat.textColor"
                                    @click="search"
                                >
                                    <view class="iconfont-h5 icon-sousuo" />
                                    <view class="item-search-input">{{ $t(module?.text) }}</view>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { formatFrame } from "@/components/modules";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame);
});
const allFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color: ${props.module?.frame?.itemBackgroundColor};`,
        itemHeight: `height: ${props.module?.frame?.itemHeight}px;`,
        itemBorderRadius: `border-radius: ${props.module?.frame?.itemRadius}px;`,
        textColor: `color: ${props.module?.frame?.textColor};`
    };
});

const search = () => {
    uni.navigateTo({
        url: "/pages/search/index"
    });
};
</script>
<style>
.searchBar-warp .item-search-con .item-search-form {
    background: #fff;
    height: 60rpx;
    display: flex;
    align-items: center;
    padding: 0;
    color: #555555;
}
.searchBar-warp .item-search-con .item-search-form .iconfont-h5 {
    margin-left: 20rpx;
}

.icon-sousuo {
    font-size: 32rpx;
}
.searchBar-warp .item-search-con .item-search-form .item-search-input {
    margin-left: 10rpx;
}
</style>

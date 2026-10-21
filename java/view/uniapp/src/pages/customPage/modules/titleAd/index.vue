<template>
    <view
        :class="'module-ad-con  module-title_ad  ad-title_align__' + module?.title?.titleAlign"
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <view class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <view class="title-ad-warp">
                <view class="title-ad-con">
                    <view class="title-ad-item">
                        <view class="item-content" style="">
                            <view :class="'image-ad-title ad-title_style__' + module?.title?.titleStyle" style="">
                                <view class="image-ad-title-l" style="">
                                    <view class="image-ad-title-h" :style="allFormat.titleColor">{{ $t(module.title.titleText) }}</view>
                                    <view class="image-ad-title-d" :style="allFormat.descColor">{{ $t(module.title.descText) }}</view>
                                </view>
                                <block v-if="module.title.showMore == 1">
                                    <view class="image-ad-title-r">
                                        <navigator class="more-link" :url="module.title.moreLink[0]" :style="allFormat.moreColor">
                                            <view class="flex align-center">
                                                <text>{{ $t("查看更多") }}</text>
                                                <text class="iconfont-h5 icon-youjiantou" />
                                            </view>
                                        </navigator>
                                    </view>
                                </block>
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
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        descColor: `color: ${props.module?.title?.descColor};`,
        moreColor: `color: ${props.module?.title?.moreColor};`,
        titleColor: `color: ${props.module?.title?.titleColor};font-weight: ${props.module?.title?.titleWeight || "normal"};`
    };
});
</script>
<style>
.title-ad-warp .title-ad-item {
    padding: 0;
    min-height: 40rpx;
}
.title-ad-warp .title-ad-item .item-name-con {
    display: flex;
    flex-wrap: nowrap;
}
.title-ad-warp .title-ad-item .item-name {
    line-height: 40rpx;
    font-size: 32rpx;
    font-weight: bold;
    color: #323233;
    word-break: break-all;
}
.title-ad-warp .title-ad-item .item-desc {
    line-height: 36rpx;
    padding-top: 16rpx;
    color: #8c8c8c;
    font-size: 24rpx;
    word-break: break-all;
}
.ad-title_align__2 .title-ad-warp .image-ad-title {
    justify-content: center;
}
.ad-title_align__2 .title-ad-warp .image-ad-title .image-ad-title-r {
    position: absolute;
    right: 0;
}

.icon-youjiantou {
    font-size: 32rpx;
}
</style>

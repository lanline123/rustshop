<template>
    <view :class="'module-ad-con module-notice'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <view class="notice-ad-warp">
            <view class="notice-con" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
                <view class="notice-item">
                    <view class="item-content">
                        <view class="item-notice-con" :style="itemFormat.itemBackgroundColor + itemFormat.itemHeight + itemFormat.itemRadius">
                            <view class="item-notice-ico">
                                <tig-image :src="module.icoPic.picUrl" mode="widthFix" />
                            </view>
                            <view class="item-notice-list">
                                <template v-if="module.nociceEffect === 'scroll'">
                                    <view class="item-notice-input scroll-animation" :style="itemFormat.textColor">
                                        <template v-for="(item, index) in list" :key="index">
                                            <view class="headlines-item">{{ $t(item) }}</view>
                                        </template>
                                    </view>
                                </template>
                                <template v-if="module.nociceEffect === 'still'">
                                    <view class="item-notice-input" :style="itemFormat.textColor">
                                        <view class="headlines-item">{{ $t(module?.text) }}</view>
                                    </view>
                                </template>
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
import { formatFrame, copyArray } from "@/components/modules";

const props = defineProps({
    module: {
        type: Object,
        default: () => {}
    }
});

const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});

const itemFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color:${props.module?.frame?.itemBackgroundColor};`,
        itemRadius: `border-radius:${props.module?.frame?.itemRadius}px;`,
        itemHeight: `height:${props.module?.frame?.itemHeight}px;`,
        textColor: `color:${props.module?.frame?.textColor};`
    };
});

// 滚动时复制文本列表
const list = computed(() => {
    if (props.module?.text) {
        return copyArray(props.module.text, 3);
    }
    return [];
});
</script>
<style>
.notice-ad-warp .item-notice-con {
    background: #fff;
    height: 40rpx;
    display: flex;
    align-items: center;
    padding: 10rpx 0;
    color: #555555;
}
.notice-ad-warp .item-notice-con .item-notice-ico {
    margin-left: 20rpx;
    width: 32rpx;
    height: 32rpx;
}
.notice-ad-warp .item-notice-con .item-notice-ico image {
    width: 100%;
    height: 100%;
}
.notice-ad-warp .item-notice-con .item-notice-list {
    margin-left: 16rpx;
    flex: 1;
    overflow: hidden;
    position: relative;
}

.notice-ad-warp .item-notice-con .item-notice-input {
    display: inline-flex;
    align-items: center;
    color: #555555;
    white-space: nowrap;
}

.notice-ad-warp .item-notice-con .item-notice-input.scroll-animation {
    animation: marquee-scroll 13s linear infinite;
}

.notice-ad-warp .item-notice-con .headlines-item {
    display: inline-block;
    padding-right: 30rpx;
}

@keyframes marquee-scroll {
    0% {
        transform: translateX(0);
    }
    100% {
        transform: translateX(-50%);
    }
}
</style>

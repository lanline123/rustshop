<template>
    <view
        class="module-ad-con module-notice-a3"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundColor +
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.paddingTopBottom +
                commonStyle.moduleContentStyle.paddingLeftRight +
                wrapStyle
            "
        >
            <view v-if="noticeList.length > 0" class="notice-a3-wrap">
                <view v-if="Number(module.showHeader) === 1 && module.headerImage?.picUrl" class="notice-a3-header">
                    <image :src="imageFormat(module.headerImage.picUrl)" mode="widthFix" :style="headerImgStyle" />
                    <view class="notice-a3-divider" />
                </view>
                <view class="notice-a3-body" :style="bodyStyle">
                    <template v-if="noticeList.length === 1">
                        <view class="notice-a3-item" @click="onClickItem(noticeList[0])">
                            <text v-if="Number(module.showPrefix) === 1 && noticeList[0].prefix" class="notice-a3-prefix">{{ $t(noticeList[0].prefix) }}</text>
                            <text class="notice-a3-text" :style="textStyle">{{ $t(noticeList[0].title) }}</text>
                        </view>
                    </template>
                    <template v-else>
                        <swiper class="notice-a3-swiper" vertical circular autoplay :interval="3000" :duration="500" :indicator-dots="false">
                            <swiper-item v-for="(item, index) in noticeList" :key="index">
                                <view class="notice-a3-item" @click="onClickItem(item)">
                                    <text v-if="Number(module.showPrefix) === 1 && item.prefix" class="notice-a3-prefix">{{ $t(item.prefix) }}</text>
                                    <text class="notice-a3-text" :style="textStyle">{{ $t(item.title) }}</text>
                                </view>
                            </swiper-item>
                        </swiper>
                    </template>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect } from "@/utils";

const props = defineProps({
    module: { type: Object, default: () => ({}) }
});

const commonStyle = computed(() => formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}));

const noticeList = computed(() => props.module.noticeList || []);

const onClickItem = (item: any) => {
    const url = urlFormat(item.link);
    if (url) redirect({ url });
};

const wrapStyle = computed(() => {
    if (Number(props.module.showBorder) === 1 && props.module.borderColor) {
        return `border: 1px solid ${props.module.borderColor};`;
    }
    return "";
});

const bodyStyle = computed(() => {
    if (props.module.contentBgColor) {
        return `background-color: ${props.module.contentBgColor};`;
    }
    return "";
});

const headerImgStyle = computed(() => {
    return `width: ${props.module.headerImageWidth || 48}px; height: auto;`;
});

const textStyle = computed(() => {
    return `color: ${props.module.textColor || "#666666"};`;
});
</script>

<style lang="scss" scoped>
.module-notice-a3 {
    min-height: auto !important;

    .module-ad-content {
        overflow: hidden;
    }
}

.notice-a3-wrap {
    display: flex;
    align-items: center;
    overflow: hidden;
}

.notice-a3-header {
    display: flex;
    align-items: center;
    margin-right: 16rpx;
    max-height: 40rpx;
    overflow: hidden;

    image {
        height: auto;
        display: block;
    }
}

.notice-a3-divider {
    width: 1px;
    height: 24rpx;
    background-color: #e2e8f0;
    margin-left: 16rpx;
    margin-right: 4rpx;
    flex-shrink: 0;
}

.notice-a3-body {
    flex: 1;
    overflow: hidden;
}

.notice-a3-swiper {
    width: 100%;
    height: 60rpx;
}

.notice-a3-item {
    height: 60rpx;
    display: flex;
    align-items: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 26rpx;
}

.notice-a3-prefix {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #de872a;
    border-radius: 4rpx;
    font-size: 24rpx;
    margin-right: 8rpx;
    flex-shrink: 0;
    font-weight: 500;
    width: 72rpx;
    height: 40rpx;
    line-height: 36rpx;
    text-align: center;
    overflow: hidden;
    background-color: #fff0cf;
    color: #de872a;
}

.notice-a3-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>

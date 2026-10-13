<template>
    <view
        class="module-ad-con module-category-rec-a1"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundColor +
            commonStyle.moduleStyle.backgroundImage +
            commonStyle.moduleStyle.backgroundSize
        "
    >
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view v-if="module.picList && module.picList.length > 0" class="category-rec-a1-list" :style="`padding-left:${itemGap}px;`">
                <view
                    v-for="(pic, index) in module.picList"
                    :key="index"
                    class="category-rec-a1-item"
                    :style="{ flexBasis: '25%', paddingRight: `${itemGap}px` }"
                    @click="urlFormat(pic.picLink) && redirect({ url: urlFormat(pic.picLink) })"
                >
                    <view class="item-inner" :style="`border-radius:${module.picRadius ?? 0}px;`">
                        <view class="item-bg" :style="`background-image:url(${imageFormat(pic.picUrl)});background-size:${module.picFillType || 'cover'};`" />
                        <view class="item-content">
                            <text
                                v-if="module.subTitle?.showTitle"
                                class="item-title"
                                :style="commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize + commonStyle.moduleSubTitle.textBlod"
                                >{{ $t(pic.picTitle || "") }}</text
                            >
                            <text class="item-desc" :style="`color:${module.subTitleColor || '#999999'};`">{{ $t(pic.picDesc || "") }}</text>
                        </view>
                    </view>
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

const commonStyle = computed(() => formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {}));

const itemGap = 4;
</script>

<style lang="scss" scoped>
.module-category-rec-a1 {
    .module-ad-content {
        position: relative;
    }

    .category-rec-a1-list {
        display: flex;
        flex-wrap: nowrap;
    }

    .category-rec-a1-item {
        min-width: 0;
        box-sizing: border-box;
    }

    .item-inner {
        position: relative;
        overflow: hidden;
    }

    .item-bg {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        background-position: center;
        background-repeat: no-repeat;
        background-size: contain;
    }

    .item-content {
        position: relative;
        z-index: 1;
        display: flex;
        flex-direction: column;
        padding: 20rpx;
    }

    .item-title {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 24rpx;
        font-weight: 700;
        color: #333;
        height: 40rpx;
        line-height: 40rpx;
    }

    .item-desc {
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 22rpx;
        color: #666;
        height: 32rpx;
        line-height: 32rpx;
    }
}
</style>

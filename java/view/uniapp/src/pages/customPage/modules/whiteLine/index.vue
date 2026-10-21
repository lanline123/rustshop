<template>
    <view :class="'module-ad-con  module-white_line ad-style__' + module.lineType + ' ad-line_style__' + module.lineStyle">
        <view class="white-line-warp" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
            <view class="white-line-con" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
                <view class="white-line-item">
                    <view class="item-content">
                        <view class="item-line" />
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
</script>
<style lang="scss" scoped>
/*线条*/
.white-line-warp {
    .white-line-con {
        padding-top: 30rpx;
        padding-bottom: 30rpx;

        .white-line-item {
            height: 2rpx;
            width: 100%;
            border-bottom: 2rpx solid #e5e5e5;
        }
    }
}
.ad-line_style__2 .white-line-warp .white-line-con .white-line-item {
    border-bottom: 2rpx dashed #e5e5e5;
}
.ad-line_style__3 .white-line-warp .white-line-con .white-line-item {
    border-bottom: 2rpx dotted #e5e5e5;
}
.ad-style__1 .white-line-warp .white-line-con .white-line-item {
    border-bottom: none;
}
</style>

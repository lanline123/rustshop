<template>
    <template v-if="module.showTitle == 1">
        <view :class="'image-ad-title ad-title_style__' + module.titleStyle" :style="titleFormat.titleBackground + titleFormat.titleRadius">
            <view class="image-ad-title-l">
                <block v-if="titleBackgroundImage">
                    <view class="title-pic-box">
                        <image class="title-pic" :src="titleBackgroundImage" />
                    </view>
                </block>
                <view class="image-ad-title-h" :style="'color: ' + module.titleColor">{{ $t(module.titleText ?? "") }}</view>
                <view class="image-ad-title-d" :style="'color: ' + module.descColor">{{ $t(module.descText ?? "") }}</view>
            </view>
            <block v-if="module.showMore == 1">
                <view class="image-ad-title-r">
                    <view class="more-link" :style="'color: ' + module.moreColor" @click="module.moreLink && redirect({ url: urlFormat(module.moreLink) })">
                        <text>{{ $t("查看更多") }}</text>
                        <text class="iconfont-h5 icon-youjiantou" />
                    </view>
                </view>
            </block>
        </view>
    </template>
</template>
<script lang="ts" setup>
import type { ModuleTitleType } from "@/types/decorate/decorate.d.ts";
import { computed } from "vue";
import { redirect } from "@/utils/index";
import { urlFormat, productImage } from "@/utils/format";
// 在modules加入要使用的模块
const props = defineProps<{
    module: ModuleTitleType;
}>();
const titleBackgroundImage = computed(() => productImage(props.module?.titleBackgroundPic));
const titleFormat = computed(() => {
    let backgroundText = props.module.titleBackground2
        ? props.module.titleBackground2
            ? `background:linear-gradient(to right, ${props.module.titleBackground}, ${props.module.titleBackground2});`
            : ""
        : props.module.titleBackground
          ? "background-color:" + props.module.titleBackground + ";"
          : "";
    return {
        titleBackground: backgroundText,
        titleRadius: "border-radius:" + props.module.titleRadius + "px " + props.module.titleRadius + "px " + "0 0;"
    };
});
</script>
<style lang="scss" scoped>
.title-pic-box {
    display: flex;
    align-items: center;
    justify-content: center;
    padding-right: 10rpx;
    .title-pic {
        width: 30rpx;
        height: 30rpx;
    }
}

.icon-youjiantou {
    font-size: 34rpx;
}
</style>

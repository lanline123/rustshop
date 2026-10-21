<template>
    <view
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
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
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.backgroundColor
            "
        >
            <view class="imagetext">
                <view
                    class="image-box"
                    :style="`border: ${module.picBorder}px solid ${module.picBorderColor}; border-radius: ${module.picRadius}px`"
                    @click="urlFormat(module.picUrl.picLink) && redirect({ url: urlFormat(module.picUrl.picLink) })"
                >
                    <image mode="widthFix" class="img" :src="imageFormat(module.picUrl?.picUrl)" alt="" />
                </view>
                <view class="intro-box">
                    <view
                        class="title"
                        :style="`color: ${module.titColor}; text-align:  ${module.titTextAlignment}; margin-bottom: ${module.titMarginBottom}px`"
                    >
                        {{ $t(module.title) }}
                    </view>
                    <view class="intro" :style="`color: ${module.descColor}; text-align: ${module.descTextAlignment}`">
                        {{ $t(module.description) }}
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>
<script setup lang="ts">
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect } from "@/utils";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});
</script>
<style lang="scss" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.image-box {
    overflow: hidden;
    .img {
        width: 100%;
        display: block;
    }
}

.intro-box {
    padding: 15px 10px 5px;
    .title {
        font-size: 20px;
        font-weight: 700;
    }
    .intro {
        font-size: 14px;
        line-height: 24px;
        width: 100%;
        overflow: hidden;
        white-space: pre-wrap;
    }
}
</style>

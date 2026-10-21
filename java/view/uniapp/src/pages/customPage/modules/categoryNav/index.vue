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
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <view :class="'category-nav decorate-slide-box ' + module.listStyleType">
                <template v-for="(item, index) in module.categoryList" :key="index">
                    <view class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle" @click="handleLink(item.picLink)">
                        <view class="item-box" :style="`padding:${module.categoryPadding}px;`">
                            <view class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle" />
                            <view
                                v-if="module.subTitle?.showTitle"
                                class="category-nav-item-text line1"
                                :style="commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize"
                            >
                                {{ $t(item.picTitle ?? "") }}
                            </view>
                        </view>
                    </view>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat } from "@/utils/format";
import { redirect } from "@/utils";
import { useAnimation } from "@/hooks/useDecorate";
import { imageFormat } from "@/utils/format";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const { AnimationFormat, show, transitionDuration } = useAnimation(".category-nav", props.module.animation);

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});
const itemStyle = computed(() => {
    let str = `flex-grow:0;flex-basis:${props.module.rowNum ? 100 / props.module.rowNum : 25}%;`;
    if (props.module.listStyleType === "slide") {
        str += `flex-shrink: 0;`;
    }
    return str;
});
const imgStyle = computed(() => {
    return `padding-top:${props.module.picPercent}%;width:${props.module.picPercent}%;border-radius:${props.module.radius}px;background-size:${props.module.picFillType};`;
});

const handleLink = (data: any) => {
    if (!urlFormat(data)) return;
    console.log(data, 999);
    if (data.path === "article") {
        redirect({
            url: urlFormat(data) + "&notBottom=1"
        });
    } else {
        redirect({
            url: urlFormat(data)
        });
    }
};
</script>

<style lang="scss" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    overflow: hidden;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.list {
    display: flex;
    flex-wrap: wrap;
}

.slide {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: scroll;
}
.category-nav-item {
    overflow: hidden;
}
.item-box {
    display: block;
    overflow: hidden;
    width: 100%;
}
.category-nav-item-img {
    width: 100%;
    height: 0;
    background-repeat: no-repeat;
    background-position: center;
    margin: 0 auto;
}
.category-nav-item-text {
    text-align: center;
    display: block;
    height: 20px;
    line-height: 20px;
    margin-top: 3px;
    font-size: 14px;
    width: inherit;
}
</style>

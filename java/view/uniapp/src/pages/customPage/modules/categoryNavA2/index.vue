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
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.paddingBottom +
                commonStyle.moduleContentStyle.paddingTop +
                commonStyle.moduleContentStyle.paddingLeftRight
            "
        >
            <view class="category-nav">
                <scroll-view scroll-x="true" class="scroll-box" @scroll="handleScoll">
                    <view :class="' decorate-slide-box ' + module.listStyleType" :style="`width:${flexWidth}px`">
                        <template v-for="(item, index) in module.categoryList" :key="index">
                            <view class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle" @click="handleLink(item.picLink)">
                                <view class="item-box" :style="`padding:${module.categoryPadding}px;`">
                                    <view class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle" />
                                    <view
                                        v-if="module.subTitle?.showTitle"
                                        class="category-nav-item-text line1"
                                        :style="
                                            commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize
                                        "
                                    >
                                        {{ $t(item.picTitle ?? "") }}
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>
                </scroll-view>
            </view>
            <template v-if="module.listStyleType != 'list' && module.indicatorBar == 1">
                <view class="line-box">
                    <view :style="lineStyle.baseStyle" class="line">
                        <view :style="lineStyle.onStyle" />
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref, onMounted, onUnmounted } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
import { useAnimation } from "@/hooks/useDecorate";
import { imageFormat } from "@/utils/format";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const instance = getCurrentInstance();
const { AnimationFormat, show, transitionDuration } = useAnimation(".category-nav", props.module.animation);

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});
const imgStyle = computed(() => {
    return `padding-top:${props.module.picPercent}%;width:${props.module.picPercent}%;border-radius:${props.module.radius}px;background-size:${props.module.picFillType};`;
});

const boxWidth = ref(0);
const itemWidth = computed(() => {
    let num = 0;
    if (props.module.rowNum === "custom") {
        num = Number((boxWidth.value * props.module.rowPicPercent * 0.01).toFixed(2));
    } else {
        num = Number((boxWidth.value / props.module.rowNum).toFixed(2));
    }
    return num;
});
const itemStyle = computed(() => {
    let str = `flex-shrink: 0;`;
    if (itemWidth.value > 0) {
        str += `width:${itemWidth.value}px;`;
    }
    return str;
});

const lineStyle = computed(() => {
    return {
        baseStyle: `margin: 0 auto;width:${props.module.barWidth}%;height:${props.module.barHeight}px;border-radius: 4px;background: ${props.module.barColor};overflow: hidden;`,
        onStyle: `width:${props.module.barWidthOn}%;height:${props.module.barHeight}px;border-radius: 10px;background:${props.module.barColorOn};transition: 0.1s;position: relative;left:${onlinePosition.value}%;`
    };
});

const flexWidth = computed(() => {
    let num = 0;
    switch (props.module.listStyleType) {
        case "slide2":
            num = itemWidth.value * Math.ceil(props.module.categoryList.length / 2);
            return num;
        case "slide":
            num = itemWidth.value * props.module.categoryList.length;
            return num;
        default:
            return `auto`;
    }
});

const onlinePosition = ref(0);
const getOnlinePosition = (scrollLeft: number) => {
    if (typeof flexWidth.value === "number" && flexWidth.value > 0) {
        let num = Number((scrollLeft / (flexWidth.value - boxWidth.value)).toFixed(2)) * 100;
        if (num < 100 - props.module.barWidthOn) {
            onlinePosition.value = num;
        } else {
            onlinePosition.value = 100 - props.module.barWidthOn;
        }
    }
};
const handleScoll = (e: any) => {
    getOnlinePosition(e.detail.scrollLeft);
};

onMounted(async () => {
    getBoxWidth();
});

const getBoxWidth = async () => {
    const res = await getElementRect(".category-nav", instance);
    boxWidth.value = res?.width || 0;
};

const windowResizeCallback = () => {
    getBoxWidth();
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});

const handleLink = (data: any) => {
    if (!urlFormat(data)) return;
    redirect({
        url: urlFormat(data)
    });
};
</script>

<style lang="scss" scoped>
.scroll-box {
    width: 100%;
}
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
.slide2 {
    display: flex;
    flex-wrap: wrap;
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

.line-box {
    padding: 5px 0 10px;
}
</style>

<template>
    <div
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
        <div
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
            <div class="category-nav" ref="itemBoxRef">
                <div :class="' decorate-slide-box ' + module.listStyleType" :style="flexWidth">
                    <template v-for="(item, index) in module.categoryList" :key="index">
                        <div class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle">
                            <div class="item-box" :style="`padding:${module.categoryPadding}px;`">
                                <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                                <p
                                    v-if="module.subTitle?.showTitle"
                                    class="category-nav-item-text"
                                    :style="
                                        commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize
                                    "
                                >
                                    {{ item.picTitle }}
                                </p>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
            <template v-if="module.listStyleType !== 'list' && module.indicatorBar === 1">
                <div class="line-box">
                    <div :style="lineStyle.baseStyle" class="line">
                        <div :style="lineStyle.onStyle"></div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, watchEffect } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { categoryNavA2Defaults } from "./index";
import type { CategoryNavA2Module } from "./types";

const props = defineProps<{ module: CategoryNavA2Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, categoryNavA2Defaults);

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const AnimationFormat = computed(() => {
    return `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;`;
});
const transitionDuration = (index: number) => {
    return `transition-duration:0s;`;
};

const imgStyle = computed(() => {
    return `padding-top:${module.picPercent}%;width:${module.picPercent}%;border-radius:${module.radius}px;background-size:${module.picFillType};`;
});

const boxWidth = ref(0);
const itemWidth = computed(() => {
    let num = 0;
    if (module.rowNum === "custom") {
        num = Number((boxWidth.value * module.rowPicPercent * 0.01).toFixed(2));
    } else {
        num = Number((boxWidth.value / module.rowNum).toFixed(2));
    }
    return num;
});
const itemStyle = computed(() => {
    let str = `flex-shrink: 0;`;
    str += `width:${itemWidth.value}px;`;

    return str;
});

const lineStyle = computed(() => {
    return {
        baseStyle: `margin: 0 auto;width:${module.barWidth}%;height:${module.barHeight}px;border-radius: 4px;background: ${module.barColor};overflow: hidden;`,
        onStyle: `width:${module.barWidthOn}%;height:${module.barHeight}px;border-radius: 10px;background:${module.barColorOn};transition: 0.3s;position: relative;left: 0%;`
    };
});

const flexWidth = computed(() => {
    switch (module.listStyleType) {
        case "slide2":
            return `width: ${itemWidth.value * Math.ceil(module.categoryList.length / 2)}px`;
        case "slide":
            return `width: ${itemWidth.value * module.categoryList.length}px`;
        default:
            return `width: auto`;
    }
});
const itemBoxRef = ref();
onMounted(() => {
    // console.log("clientWidth", itemBoxRef.value.clientWidth);
    boxWidth.value = itemBoxRef.value.clientWidth;
});
watchEffect(() => {
    if (module.moduleStyle.boxMargin > 0 || module.contentStyle.paddingLeftRight > 0) {
        boxWidth.value = itemBoxRef.value?.clientWidth;
    }
    // if(module.value.contentStyle.padding > 0) {
    //     boxWidth.value = itemBoxRef.value?.clientWidth;
    // }
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
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
.category-nav {
    overflow: hidden;
}
.category-nav-item {
    overflow: hidden;
}
.item-box {
    display: block;
    text-align: center;
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
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 3px;
    font-size: 14px;
    width: 100%;
}

.line-box {
    padding: 5px 0 10px;
    .line {
        box-sizing: content-box;
    }
}
</style>

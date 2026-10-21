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
                                <div class="img-box" :style="imgBoxStyle">
                                    <div class="img-box-bg" :style="{ backgroundColor: module.imageBackgroundColor }"></div>
                                    <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                                </div>
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
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, watchEffect } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { categoryNavA1Defaults } from "./index";
import type { CategoryNavA1Module } from "./types";

const props = defineProps<{ module: CategoryNavA1Module }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, categoryNavA1Defaults);

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const AnimationFormat = computed(() => {
    return `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;`;
});
const transitionDuration = (index: number) => {
    return `transition-duration:0s;`;
};

const imgBoxStyle = computed(() => {
    return `padding-top:${module.picPercent}%;width:${module.picPercent}%;border-radius:${module.radius}px;`;
});

const imgStyle = computed(() => {
    return `border-radius:${module.radius}px;background-size:${module.picFillType};`;
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
    boxWidth.value = itemBoxRef.value.clientWidth;
});
watchEffect(() => {
    if (module.moduleStyle.boxMargin > 0 || module.contentStyle.paddingLeftRight > 0) {
        boxWidth.value = itemBoxRef.value?.clientWidth;
    }
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

    .img-box {
        position: relative;
        width: 100%;
        height: 0;
        overflow: hidden;
        margin: 0 auto;

        .img-box-bg {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
        }
    }
}

.category-nav-item-img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-repeat: no-repeat;
    background-position: center;
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

.line {
    box-sizing: content-box;
}
</style>

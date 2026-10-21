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
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div :class="'category-nav decorate-slide-box ' + module.listStyleType">
                <template v-for="(item, index) in module.categoryList" :key="index">
                    <div class="category-nav-item" :style="AnimationFormat + transitionDuration(index) + itemStyle">
                        <div class="item-box" :style="`padding:${module.categoryPadding}px;`">
                            <div class="category-nav-item-img" :style="`background-image:url(${imageFormat(item.picUrl)});` + imgStyle"></div>
                            <p
                                v-if="module.subTitle?.showTitle"
                                class="category-nav-item-text"
                                :style="commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor + commonStyle.moduleSubTitle.textFontSize"
                            >
                                {{ item.picTitle }}
                            </p>
                        </div>
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { categoryNavDefaults } from "./index";
import type { CategoryNavModule } from "./types";

const props = defineProps<{ module: CategoryNavModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, categoryNavDefaults);

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const AnimationFormat = computed(() => {
    return `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;`;
});
const transitionDuration = (index: number) => {
    return `transition-duration:0s;`;
};
const itemStyle = computed(() => {
    let str = `flex-grow:0;flex-basis:${module.rowNum ? 100 / module.rowNum : 25}%;`;
    if (module.listStyleType === "slide") {
        str += `flex-shrink: 0;`;
    }
    return str;
});
const imgStyle = computed(() => {
    return `padding-top:${module.picPercent}%;width:${module.picPercent}%;border-radius:${module.radius}px;background-size:${module.picFillType};`;
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
.category-nav-item {
    overflow: hidden;
}
.item-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
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
    white-space: nowrap;
    text-align: center;
    height: 20px;
    line-height: 20px;
    margin-top: 3px;
    font-size: 14px;
    width: 100%;
}
</style>

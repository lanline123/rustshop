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
            <div class="slidelarge-image" ref="slidelargeRef">
                <div :style="titleStyle.titleBox" v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'top' && module.moduleTitle?.titleText">
                    <div :style="titleStyle.title">{{ module.moduleTitle?.titleText }}</div>
                    <div :style="titleStyle.subTitle">{{ module.moduleTitle?.subTitleText }}</div>
                </div>
                <div :style="`margin-top: ${module.picMarginTop}%; width: 100%; z-index: 1; overflow: hidden;`">
                    <div :style="slidelargeBoxStyle">
                        <div :class="[{ 'slidelarge-image-box': module.slidingEffect === 'auto' }]" :style="slidelargeBoxStyle + slidelargeAnimationStyle">
                            <template v-for="(item, index) in list" :key="index">
                                <div :style="`width: ${itemWidth}px; flex-shrink: 0`">
                                    <div :style="itemStyle">
                                        <div :style="imgStyle + `background-image: url(${imageFormat(item.picUrl)});`"></div>
                                        <div
                                            :style="
                                                textStyle.picTextBox +
                                                `${
                                                    module.picTitleDesc.fontPosition === 'internal'
                                                        ? 'position: absolute;left: 0px;right: 0px;bottom: 0px;padding: 10px 15px;'
                                                        : ''
                                                }` +
                                                `${module.picTitleDesc.fontPosition !== 'internal' ? `margin: ${module.picTitleDesc.marginTop}px ${module.picTitleDesc.marginLeft}px ${module.picTitleDesc.marginBottom}px;` : ''}`
                                            "
                                            v-if="module.picTitleDesc?.show && (item.picTitle || item.picDesc)"
                                        >
                                            <div :style="textStyle.picTextTitle" v-if="item.picTitle">{{ item.picTitle }}</div>
                                            <div :style="textStyle.picTextDesc" v-if="item.picDesc">{{ item.picDesc }}</div>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>
                <div :style="titleStyle.titleBox" v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'bottom'">
                    <div :style="titleStyle.title">{{ module.moduleTitle?.titleText }}</div>
                    <div :style="titleStyle.subTitle">{{ module.moduleTitle?.subTitleText }}</div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, watchEffect } from "vue";
import { mergeDefaultModule, formatCommonStyle, copyArray } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { slideLargeImageDefaults } from "./index";
import type { SlideLargeImageModule } from "./types";
const props = defineProps<{ module: SlideLargeImageModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, slideLargeImageDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const list = computed(() => {
    if (module.picList) {
        return copyArray(module.picList, 5);
    }
    return [];
});
const slidelargeBoxStyle = computed(() => {
    return `width: ${itemWidth.value * module.picList?.length}px; display: flex; animation-duration: ${module.slidingSpeed}s; position: relative; top: 0px; left: 0px`;
});
const slidelargeAnimationStyle = computed(() => {
    return `transform: translateX(${0}px);animation-play-state:  ${module.slidingEffect === "manual" ? "paused" : "running"};`;
});
const itemStyle = computed(() => {
    return `margin-right: ${module.picMargin}px; background: none; border-radius: ${module.picRadius}px; position: relative; overflow: hidden`;
});
const imgStyle = computed(() => {
    return `width: 100%;height: 0px;padding-top: ${imgratio.value * 100}%;border-bottom-left-radius: 0px;border-bottom-right-radius: 0px;background-size: cover;background-repeat: no-repeat;background-position: center center;`;
});

const textStyle = computed(() => {
    return {
        picTextBox: `background: ${module.picTitleDesc?.backgroundColor ? module.picTitleDesc.backgroundColor : "none"};text-align: ${module.picTitleDesc?.textAlignment};`,
        picTextTitle: `color: ${module.picTitleDesc?.titleColor}; font-size: ${module.picTitleDesc?.titleFontSize}px; font-weight: ${module.picTitleDesc?.titleFontWeight}; overflow: hidden`,
        picTextDesc: `margin-top: 6px; font-size: ${module.picTitleDesc?.descFontSize}px;font-weight: ${module.picTitleDesc?.descFontWeight};color: ${module.picTitleDesc?.descColor};overflow: hidden;`
    };
});

const titleStyle = computed(() => {
    return {
        titleBox: `text-align: ${module.moduleTitle?.textAlignment}; margin: ${module.moduleTitle?.titleMarginTop}px ${module.moduleTitle?.titleMarginLeft}px ${module.moduleTitle?.titleMarginBottom}px;`,
        title: `font-size: ${module.moduleTitle?.titleFontSize}px; font-weight: ${module.moduleTitle?.titleFontWeight};color: ${module.moduleTitle?.titleColor};`,
        subTitle: `font-size: ${module.moduleTitle?.subFontTitleSize}px; margin-top: 8px; font-weight: ${module.moduleTitle?.subFontTitleWeight};color: ${module.moduleTitle?.subFontTitleColor};`
    };
});

const imgratio = ref(0);
const getImgInfo = (url: any) => {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = imageFormat(url);
        img.onload = () => {
            resolve({
                width: img.width,
                height: img.height
            });
        };
        img.onerror = () => {
            reject();
        };
    });
};
const getImgratio = async (url: any) => {
    try {
        const res: any = await getImgInfo(url);
        imgratio.value = Number((res.height / res.width).toFixed(3));
    } catch (error) {
        console.error(error);
    }
};
const slidelargeRef = ref();
const boxWidth = ref(0);
const itemWidth = computed(() => {
    return boxWidth.value / module.rowNum;
});
onMounted(async () => {
    await getImgratio(module.picList[0].picUrl);
    // console.log("imgratio", imgratio.value);
    boxWidth.value = slidelargeRef.value.clientWidth;
});

watchEffect(() => {
    getImgratio(module.picList[0].picUrl);
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
.slidelarge-image {
    width: 100%;
    overflow: hidden;
}
@keyframes left {
    0% {
        left: -200%;
    }
    100% {
        left: -300%;
    }
}
.slidelarge-image-box {
    animation: left 5000ms linear infinite running;
}
</style>

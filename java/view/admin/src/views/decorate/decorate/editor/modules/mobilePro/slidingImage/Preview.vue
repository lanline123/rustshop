<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
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
            <div class="slidingImage" ref="slidingImageRef">
                <template v-if="!loading">
                    <ul class="slidingImage-box" :style="`transition-duration: ${duration}; transform: translateX(-${translateX}px)`">
                        <template v-for="(item, index) in list" :key="index">
                            <li class="item-box" :style="`width: ${itemWidth}px`">
                                <div
                                    class="item"
                                    :style="
                                        itemStyle +
                                        `${module.backgroundGradientType === 'purity' ? `background-color:${item.gradientColorA};` : `background-image:linear-gradient(${gradientMap[module.backgroundGradientType as GradientType]},${item.gradientColorA},${item.gradientColorA});`}transform: scale(${isEvenOrOdd(index) === isEvenOrOdd(number) ? 1 : module.picReducePercent});`
                                    "
                                >
                                    <div class="fox-title">
                                        <div class="img" :style="imgStyle + `background-image:url(${imageFormat(item.picUrl)})`"></div>
                                        <div
                                            v-if="module.showDesc"
                                            class="desc"
                                            :style="
                                                `color: ${module.descColor};` +
                                                `${module.backgroundGradientType === 'purity' ? `background-color:${item.gradientColorA};` : `background-image:linear-gradient(${gradientMap[module.backgroundGradientType as GradientType]},${item.gradientColorA},${item.gradientColorA});`}`
                                            "
                                        >
                                            {{ item.picDesc }}
                                        </div>
                                    </div>
                                    <div
                                        class="title"
                                        v-if="module.subTitle?.showTitle"
                                        :style="
                                            commonStyle.moduleSubTitle.textFontSize +
                                            commonStyle.moduleSubTitle.textBlod +
                                            commonStyle.moduleSubTitle.textColor
                                        "
                                    >
                                        {{ item.picTitle }}
                                    </div>
                                </div>
                            </li>
                        </template>
                    </ul>
                </template>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { GradientType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, formatCommonStyle, gradientMap, copyArray } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { slidingImageDefaults } from "./index";
import type { SlidingImageModule } from "./types";

const props = defineProps<{ module: SlidingImageModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, slidingImageDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle, {}, module.subTitle ?? {});
});
const list = computed(() => {
    if (module.picList) {
        return copyArray(module.picList, 3);
    }
    return [];
});
const itemStyle = computed(() => {
    return `margin-right: ${module.picMargin}px;padding: ${module.picPadding}px;border-radius: ${module.picRadius}px;`;
});
const imgStyle = computed(() => {
    return `padding-top: 87.5%;border-radius: ${module.picRadius}px;background-size: ${module.picFillType};`;
});

const isEvenOrOdd = (num: number) => {
    if (num % 2 === 0) {
        return true;
    }
};
const translateX = ref(0);
const duration = ref("500ms");
const slidingImageRef = ref();
const boxWidth = ref(0);
const itemWidth = computed(() => {
    return boxWidth.value * module.picWidth * 0.01;
});
const number = ref(0);
let timer: any;

const startSliding = () => {
    if (timer) clearInterval(timer);
    timer = setInterval(() => {
        number.value++;
        if (number.value === module.picList.length) {
            translateX.value += itemWidth.value;
            setTimeout(() => {
                duration.value = "0ms";
                number.value = 0;
                translateX.value = boxWidth.value;
            }, 500);
        } else {
            if (duration.value === "0ms") {
                duration.value = "500ms";
            }
            translateX.value += itemWidth.value;
        }
    }, module.interval * 1000);
    // }, 1000);
};
const stopSliding = () => {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
};
const loading = ref(true);
onMounted(() => {
    boxWidth.value = slidingImageRef.value.clientWidth;
    translateX.value = boxWidth.value;
    loading.value = false;
});
onUnmounted(() => {
    stopSliding();
});
watch(
    () => module.autoPlay,
    (val) => {
        if (val) {
            startSliding();
        } else {
            stopSliding();
        }
    },
    {
        immediate: true
    }
);
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
.slidingImage {
    overflow: hidden;
}
.slidingImage-box {
    display: flex;
    transition-duration: 0ms;
    transition-timing-function: linear;
}
.item-box {
    flex-shrink: 0;
}
.item {
    padding: 3px;
    text-align: center;
    overflow: hidden;
    display: block;
    transition-duration: 0.5s;
    transition-timing-function: cubic-bezier(0.52, 1.64, 0.37, 0.66);
}
.fox-title {
    position: relative;
    display: flex;
    justify-content: center;
    .img {
        width: 100%;
        height: 0;
        background-repeat: no-repeat;
        background-position: center;
        background-size: cover;
    }
    .desc {
        overflow: hidden;
        padding: 3px 8px;
        position: absolute;
        bottom: 2px;
        border-radius: 15px;
        font-size: 12px;
        transform: scale(0.9);
    }
}
.title {
    overflow: hidden;
    margin: 6px 0 3px 0;
}
</style>

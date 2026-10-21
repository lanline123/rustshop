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
import { ref, computed, Ref, onMounted, onUnmounted, watch } from "vue";
import { ModuleType, ModuleSlidingImageType, GradientType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultSubTitle, defaultModuleStyle, formatCommonStyle, gradientMap, copyArray } from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleSlidingImageType>("module") as Ref<ModuleType & ModuleSlidingImageType>;
const picList = [
    {
        picId: 1417,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094u3csMxzo4Wzksk1uTw.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094u3csMxzo4Wzksk1uTw.jpg",
        picName: "FvsG9vzOZj0aUGNdgE0VrqzDqpu",
        gradientColorA: "rgba(235, 152, 115, 1)",
        gradientColorB: "rgba(234, 92, 88, 1)",
        picTitle: t('每日特价'),
        picDesc: t('2件5折')
    },
    {
        picId: 1418,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17312930944dZgpDLjByAzYyejYt.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17312930944dZgpDLjByAzYyejYt.jpg",
        picName: "Fru1GfeNP4JIvRLqI61o8NT36ZIe",
        gradientColorA: "rgba(243, 115, 130, 1)",
        gradientColorB: "rgba(229, 65, 116, 1)",
        picTitle: t('精选好货'),
        picDesc: t('9.9秒杀')
    },
    {
        picId: 1420,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094BFUdJ8FAzQ6sHI9cs8.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094BFUdJ8FAzQ6sHI9cs8.jpg",
        picName: "Fmkw5_XrRUmaNYs1jk2858je58Qi",
        gradientColorA: "rgba(219, 123, 248, 1)",
        gradientColorB: "rgba(154, 39, 241, 1)",
        picTitle: t('加量不加价'),
        picDesc: t('最高加一倍')
    },
    {
        picId: 1419,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094bHw28wIxfKlXorX1Rs.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094bHw28wIxfKlXorX1Rs.jpg",
        picName: "FglfJu8K11jusC9gUKjkZU3mZ3eE",
        gradientColorA: "rgba(116, 214, 172, 1)",
        gradientColorB: "rgba(98, 193, 96, 1)",
        picTitle: t('下单即发货'),
        picDesc: t('满49包邮')
    }
];
const defaultModule = ref({
    picList: picList, // 图片列表
    backgroundGradientType: "diagonal", // 背景渐变类型 "upDown": "上下","leftRight": "左右","diagonal": "斜向","purity": "单色"
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picWidth: 25, // 图片宽度
    picMargin: 0, // 图片间距
    picPadding: 4, // 图片内边距
    picRadius: 15, // 图片圆角
    picReducePercent: 0.85, // 图片缩小比例
    subTitle: defaultSubTitle, //标题文字
    showDesc: 1, // 是否显示描述 0:不显示, 1:显示
    descColor: "#ffffff", // 描述文字颜色
    moduleStyle: defaultModuleStyle, // 模块样式
    autoPlay: 1, //自动轮播
    interval: 5 // 轮播间隔 单位秒
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle, {}, module.value.subTitle ?? {});
});
const list = computed(() => {
    if (module.value.picList) {
        return copyArray(module.value.picList, 3);
    }
    return [];
});
const itemStyle = computed(() => {
    return `margin-right: ${module.value.picMargin}px;padding: ${module.value.picPadding}px;border-radius: ${module.value.picRadius}px;`;
});
const imgStyle = computed(() => {
    return `padding-top: 87.5%;border-radius: ${module.value.picRadius}px;background-size: ${module.value.picFillType};`;
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
    return boxWidth.value * module.value.picWidth * 0.01;
});
const number = ref(0);
let timer: any;

const startSliding = () => {
    if (timer) clearInterval(timer);
    timer = setInterval(() => {
        number.value++;
        if (number.value === module.value.picList.length) {
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
    }, module.value.interval * 1000);
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
    () => module.value.autoPlay,
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

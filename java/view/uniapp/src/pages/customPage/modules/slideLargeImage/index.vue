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
            <view ref="slidelargeRef" class="slidelarge-image">
                <view v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'top'" :style="titleStyle.titleBox">
                    <view :style="titleStyle.title">{{ module.moduleTitle?.titleText }}</view>
                    <view :style="titleStyle.subTitle">{{ module.moduleTitle?.subTitleText }}</view>
                </view>
                <view
                    :style="`margin-top: ${module.picMarginTop}%; width: 100%; z-index: 1; overflow: hidden;`"
                    @touchstart="touchstart"
                    @touchend="touchend"
                    @touchmove="touchmove"
                >
                    <view :style="slidelargeBoxStyle">
                        <view :class="[{ 'slidelarge-image-box': module.slidingEffect === 'auto' }]" :style="slidelargeBoxStyle + slidelargeAnimationStyle">
                            <template v-for="(item, index) in list" :key="index">
                                <view
                                    :style="`width: ${itemWidth}px; flex-shrink: 0`"
                                    @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })"
                                >
                                    <view :style="itemStyle">
                                        <view :style="imgStyle + `background-image: url(${imageFormat(item.picUrl)});`" />
                                        <view
                                            v-if="module.picTitleDesc?.show == 1"
                                            :style="
                                                textStyle.picTextBox +
                                                `${
                                                    module.picTitleDesc.fontPosition === 'internal'
                                                        ? 'position: absolute;left: 0px;right: 0px;bottom: 0px;padding: 10px 15px;'
                                                        : ''
                                                }` +
                                                `${
                                                    module.picTitleDesc.fontPosition !== 'internal'
                                                        ? `margin: ${module.picTitleDesc.marginTop}px ${module.picTitleDesc.marginLeft}px ${module.picTitleDesc.marginBottom}px;`
                                                        : ''
                                                }`
                                            "
                                        >
                                            <view :style="textStyle.picTextTitle">{{ $t(item.picTitle ?? "") }}</view>
                                            <view :style="textStyle.picTextDesc">{{ $t(item.picDesc ?? "") }}</view>
                                        </view>
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>
                </view>
                <view v-if="module.moduleTitle && module.moduleTitle.titlePosition === 'bottom'" :style="titleStyle.titleBox">
                    <view :style="titleStyle.title">{{ $t(module.moduleTitle?.titleText) }}</view>
                    <view :style="titleStyle.subTitle">{{ $t(module.moduleTitle?.subTitleText) }}</view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, onUnmounted, reactive, ref } from "vue";
import { formatCommonStyle, copyArray } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
import { useImgInfo } from "@/hooks/useDecorate";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const list = computed(() => {
    if (props.module.picList) {
        return copyArray(props.module.picList, 5);
    }
    return [];
});
const { imgratio, updateImgInfo } = useImgInfo(props.module.picList[0].picUrl);
const commonStyle = computed(() => {
    return formatCommonStyle(
        props.module.moduleStyle ?? {
            boxMarginTop: 5,
            boxMarginBottom: 0,
            boxMargin: 10,
            backgroundColor: "",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover"
        },
        props.module.contentStyle ?? {
            paddingTop: 0,
            paddingBottom: 0,
            paddingLeftRight: 0,
            padding: 0,
            boxRadiusTop: 0,
            boxRadiusBottom: 0,
            gradientType: "upDown",
            gradientColorA: "#ffffff",
            gradientColorB: "#ffffff",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover",
            boxShadow: 0
        },
        props.module.subTitle ?? {}
    );
});
const slidelargeBoxStyle = computed(() => {
    return `width: ${itemWidth.value * props.module.picList?.length}px; display: flex; animation-duration: ${
        props.module.slidingSpeed
    }s; position: relative; top: 0px; left: 0px;`;
});
const slidelargeAnimationStyle = computed(() => {
    return `transform: translateX(${touchInfo.translateX}px);animation-play-state:  ${
        props.module.slidingEffect === "manual" || touchInfo.touch ? "paused" : "running"
    };`;
});
const itemStyle = computed(() => {
    return `margin-right: ${props.module.picMargin}px; background: none; border-radius: ${props.module.picRadius}px; position: relative; overflow: hidden;`;
});
const imgStyle = computed(() => {
    return `width: 100%;height: 0px;padding-top: ${
        imgratio.value * 100
    }%;border-bottom-left-radius: 0px;border-bottom-right-radius: 0px;background-size: cover;background-repeat: no-repeat;background-position: center center;`;
});
const textStyle = computed(() => {
    return {
        picTextBox: `background: ${props.module.picTitleDesc?.backgroundColor ? props.module.picTitleDesc.backgroundColor : "none"};text-align: ${
            props.module.picTitleDesc?.textAlignment
        };`,
        picTextTitle: `color: ${props.module.picTitleDesc?.titleColor}; font-size: ${props.module.picTitleDesc?.titleFontSize}px; font-weight: ${props.module.picTitleDesc?.titleFontWeight}; overflow: hidden`,
        picTextDesc: `margin-top: 6px; font-size: ${props.module.picTitleDesc?.descFontSize}px;font-weight: ${props.module.picTitleDesc?.descFontWeight};color: ${props.module.picTitleDesc?.descColor};overflow: hidden;`
    };
});
const titleStyle = computed(() => {
    return {
        titleBox: `text-align: ${props.module.moduleTitle?.textAlignment}; margin: ${props.module.moduleTitle?.titleMarginTop}px ${props.module.moduleTitle?.titleMarginLeft}px ${props.module.moduleTitle?.titleMarginBottom}px;`,
        title: `font-size: ${props.module.moduleTitle?.titleFontSize}px; font-weight: ${props.module.moduleTitle?.titleFontWeight};color: ${props.module.moduleTitle?.titleColor};`,
        subTitle: `font-size: ${props.module.moduleTitle?.subFontTitleSize}px; margin-top: 8px; font-weight: ${props.module.moduleTitle?.subFontTitleWeight};color: ${props.module.moduleTitle?.subFontTitleColor};`
    };
});
const boxWidth = ref(0);
const itemWidth = computed(() => {
    return boxWidth.value / props.module.rowNum;
});
const instance = getCurrentInstance();
const getBoxWidth = async () => {
    const res = await getElementRect(".slidelarge-image", instance);
    boxWidth.value = res?.width ?? 0;
};
onMounted(async () => {
    getBoxWidth();
});
const windowResizeCallback = () => {
    updateImgInfo();
    getBoxWidth();
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});

const touchInfo = reactive({
    touch: false,
    startX: 0,
    endX: 0,
    translateX: 0
});
const touchstart = (e: any) => {
    touchInfo.touch = true;
    touchInfo.startX = e.touches[0].clientX;
};

const touchmove = (e: any) => {
    // console.log("touchmove", e.touches[0].clientX);
    const moveNum = Number((e.touches[0].clientX - touchInfo.startX).toFixed(2));
    if (Math.abs(moveNum) > itemWidth.value * props.module.picList?.length) {
        touchInfo.translateX = moveNum % (itemWidth.value * props.module.picList?.length);
    } else {
        touchInfo.translateX = moveNum;
    }
};

const touchend = (e: any) => {
    touchInfo.touch = false;
};

const handleLink = (data: any) => {
    if (!urlFormat(data)) return;
    redirect({
        url: urlFormat(data)
    });
};
</script>

<style lang="scss" scoped>
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

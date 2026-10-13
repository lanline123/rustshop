<template>
    <view
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
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
            <view class="slidingImage">
                <template v-if="!loading">
                    <view
                        class="slidingImage-box"
                        :style="`transition-duration: ${duration}; transform: translateX(-${translateX}px)`"
                        @click="next()"
                        @touchstart="touchstart"
                        @touchend="touchend"
                    >
                        <template v-for="(item, index) in list" :key="index">
                            <view
                                class="item-box"
                                :style="`width: ${itemWidth}px`"
                                @click="urlFormat(item.picLink) && redirect({ url: urlFormat(item.picLink) })"
                            >
                                <view
                                    class="item"
                                    :style="
                                        itemStyle +
                                        `${
                                            module.backgroundGradientType === 'purity'
                                                ? `background-color:${item?.gradientColorA};`
                                                : `background-image:linear-gradient(${gradientMap[module.backgroundGradientType]},${item?.gradientColorA},${
                                                      item?.gradientColorA
                                                  });`
                                        }transform: scale(${isEvenOrOdd(index) === isEvenOrOdd(number) ? 1 : module.picReducePercent});`
                                    "
                                >
                                    <view class="fox-title">
                                        <view class="img" :style="imgStyle + `background-image:url(${imageFormat(item.picUrl)})`" />
                                        <view
                                            v-if="module.showDesc == 1"
                                            class="desc line1"
                                            :style="
                                                `color: ${module.descColor};` +
                                                `${
                                                    module.backgroundGradientType === 'purity'
                                                        ? `background-color:${item?.gradientColorA};`
                                                        : `background-image:linear-gradient(${gradientMap[module.backgroundGradientType]},${
                                                              item?.gradientColorA
                                                          },${item?.gradientColorA});`
                                                }`
                                            "
                                        >
                                            {{ $t(item.picDesc) }}
                                        </view>
                                    </view>
                                    <view
                                        v-if="module.subTitle?.showTitle"
                                        class="title line2"
                                        :style="
                                            commonStyle.moduleSubTitle.textFontSize + commonStyle.moduleSubTitle.textBlod + commonStyle.moduleSubTitle.textColor
                                        "
                                    >
                                        {{ $t(item.picTitle ?? "") }}
                                    </view>
                                </view>
                            </view>
                        </template>
                    </view>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, ref, onMounted, onUnmounted, reactive } from "vue";
import { formatCommonStyle, gradientMap, copyArray } from "@/components/modules";
import { urlFormat } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
import { imageFormat } from "@/utils/format";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle, {}, props.module.subTitle);
});
const list = computed(() => {
    if (props.module.picList) {
        return copyArray(props.module.picList, 3);
    }
    return [];
});
const itemStyle = computed(() => {
    return `margin-right: ${props.module.picMargin}px;padding: ${props.module.picPadding}px;border-radius: ${props.module.picRadius}px;`;
});
const imgStyle = computed(() => {
    return `padding-top: 87.5%;border-radius: ${props.module.picRadius}px;background-size: ${props.module.picFillType};`;
});
const isEvenOrOdd = (num: number) => {
    if (Math.abs(num) % 2 === 0) {
        return true;
    }
};

const translateX = ref(0);
const duration = ref("500ms");
const boxWidth = ref(0);
const itemWidth = computed(() => {
    return boxWidth.value * props.module.picWidth * 0.01;
});
const number = ref(0);
let timer: any;

const next = () => {
    number.value++;
    if (number.value === props.module.picList.length) {
        translateX.value += itemWidth.value;
        setTimeout(() => {
            duration.value = "0ms";
            number.value = 0;
            translateX.value = boxWidth.value;
        }, 450);
    } else {
        if (duration.value === "0ms") {
            duration.value = "500ms";
        }
        translateX.value += itemWidth.value;
    }
    // console.log('next', number.value)
};

const prev = () => {
    number.value--;
    if (Math.abs(number.value) === props.module.picList.length) {
        translateX.value -= itemWidth.value;
        setTimeout(() => {
            duration.value = "0ms";
            number.value = 0;
            translateX.value = boxWidth.value;
        }, 450);
    } else {
        if (duration.value === "0ms") {
            duration.value = "500ms";
        }
        translateX.value -= itemWidth.value;
    }
    // console.log('prev', number.value)
};

const touchInfo = reactive({
    startX: 0, // 触摸起始位置的X坐标
    moveX: 0, // 移动时的X坐标
    endX: 0, // 触摸结束位置的X坐标
    disX: 0 // 水平方向的距离
});

const touchstart = (e: any) => {
    touchInfo.startX = e.touches[0].pageX;
    stopSliding();
};
const touchend = (e: any) => {
    // 记录触摸结束的位置
    touchInfo.endX = e.changedTouches[0].pageX;
    // 计算水平位移距离
    touchInfo.disX = touchInfo.startX - touchInfo.endX;
    // 判断左右滑动
    if (Math.abs(touchInfo.disX) > 20) {
        // 设置滑动阈值
        if (touchInfo.disX > 0) {
            next();
            // console.log("向左滑动");
        } else {
            prev();
            // console.log("向右滑动");
        }
    } else {
        // 如果位移距离小于阈值，则不认为是滑动
        // console.log("未达到滑动条件");
    }
    // 重置起始位置
    touchInfo.startX = 0;
    touchInfo.endX = 0;
    touchInfo.disX = 0;
    startSliding();
};

const startSliding = () => {
    if (timer) clearInterval(timer);
    timer = setInterval(() => {
        next();
    }, props.module.interval * 1000);
    // }, 50000);
};
const stopSliding = () => {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
};
const intance = getCurrentInstance();
const loading = ref(true);
const getInfo = async () => {
    const res = await getElementRect(".slidingImage", intance);
    if (res) {
        boxWidth.value = res.width;
        translateX.value = res.width;
        loading.value = false;
        if (props.module.autoPlay == 1) {
            startSliding();
        }
    }
};
onMounted(async () => {
    getInfo();
});
const windowResizeCallback = () => {
    getInfo();
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
    stopSliding();
});

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
        width: 100%;
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

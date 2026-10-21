<template>
    <view
        v-if="props.module.picList && props.module.picList.length > 0"
        :class="
            'module-ad-con module-ad-con-' +
            ' module-image_nav ad-nav_type__' +
            module.navType +
            ' ad-nav_style__' +
            module.navStyle +
            ' ad-row_num__' +
            module.rowNum +
            ' ad-col_num__' +
            module.colNum +
            ' ad-pic_page_type__' +
            module.picPageType +
            ' ad-radio_style__' +
            module.radioStyle
        "
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <view class="module-ad-content" :style="frameFormat.innerPadding + frameFormat.backgroundColor + frameFormat.boxRadius">
            <template v-if="module.navType == 2">
                <div class="image-ad-warp">
                    <div class="image-ad-con">
                        <tig-swiper
                            v-model="swiperList"
                            :class="{ 'uni-swiper-dot-box': module.picPageType == 3 }"
                            :mode="mode"
                            :color="module.picPageType == 3 ? '#fff' : ''"
                            :select-color="selectColor"
                            :height="`${swiperHeight}px`"
                        >
                            <template #default="{ item }">
                                <div class="swiper-main" :style="{ 'flex-wrap': module.colNum > 1 ? 'wrap' : 'nowrap' }">
                                    <div
                                        v-for="(subItem, index) in item"
                                        :key="index"
                                        class="swiper-item"
                                        :style="`flex-grow: 0; flex-basis: ${100 / module.rowNum}%;padding:${module.imgPadding}px;`"
                                        @click="subItem.picLink && redirect({ url: urlFormat(subItem.picLink) })"
                                    >
                                        <div class="swiper-item-box" :style="`background-image: url(${imageFormat(subItem.picUrl)})`" />
                                        <div v-if="subItem.picTitle && module.navStyle !== 2" class="imagenav-item-text">
                                            {{ $t(subItem.picTitle) }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </tig-swiper>
                    </div>
                </div>
            </template>
            <template v-else>
                <div class="imagenav-main">
                    <div
                        v-for="(item, index) in module.picList"
                        :key="index"
                        class="imagenav-main-item"
                        @click="item.picLink && redirect({ url: urlFormat(item.picLink) })"
                    >
                        <view class="item-img-a">
                            <template v-if="module.navStyle == 2 || module.navStyle == 1">
                                <image
                                    :class="{ 'img-height': module.navStyle === 2 }"
                                    class="imagenav-item-img"
                                    :src="imageFormat(item.picUrl)"
                                    mode="widthFix"
                                />
                            </template>
                        </view>
                        <view class="item-text-a">
                            <template v-if="item.picTitle && module.navStyle != 2">
                                <div class="imagenav-item-text line1">{{ $t(item.picTitle ?? "") }}</div>
                            </template>
                        </view>
                    </div>
                </div>
            </template>
        </view>
    </view>
</template>

<script lang="ts" setup>
import { ref, computed, watchEffect, onUnmounted, getCurrentInstance, onMounted, nextTick } from "vue";
import { formatFrame } from "@/components/modules";
import { imageFormat, urlFormat } from "@/utils/format";
import { redirect, getElementRect } from "@/utils";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const { frame } = props.module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const swiperList = ref<any[]>([]);
const swiperPageColor = computed(() => {
    return props.module.swiperPageColor || "#333";
});
const getSwiperList = () => {
    if (props.module.navType == 1) return;
    swiperList.value = [];
    let itemNum;

    if (props.module.rowNum && props.module.colNum) {
        itemNum = props.module.rowNum * props.module.colNum;
    } else {
        itemNum = 0;
    }

    let swiperListNum = Math.ceil(props.module.picList.length / itemNum);
    for (let index = 0; index < swiperListNum; index++) {
        swiperList.value.push(props.module.picList.slice(index * itemNum, (index + 1) * itemNum));
    }
};
watchEffect(() => {
    getSwiperList();
});

const selectColor = computed(() => {
    return props.module.swiperPageColor || "#333";
});

const mode = computed(() => {
    return props.module.picPageType == 1 ? "default" : "dot";
});

const instance = getCurrentInstance();

const swiperHeight = ref(150);

const gteSwiperHeight = async () => {
    try {
        const res = await getElementRect(".swiper-main", instance);
        if (res) {
            swiperHeight.value = res.height;
        }
    } catch (error) {
        console.error(error);
    }
};

onMounted(() => {
    nextTick(() => {
        gteSwiperHeight();
    });
});
const windowResizeCallback = (res: any) => {
    gteSwiperHeight();
};
uni.onWindowResize(windowResizeCallback);
onUnmounted(() => {
    uni.offWindowResize(windowResizeCallback);
});
</script>
<style lang="scss" scoped>
:deep(.swiper-pagination-bullet-active) {
    background: v-bind("swiperPageColor") !important;
}
.imagenav-main {
    display: grid;
    grid-template-columns: repeat(v-bind("module.rowNum"), 1fr);
    grid-template-rows: repeat(v-bind("module.colNum"), 1fr);
    grid-gap: v-bind("module.imgPadding + 'px'");
    box-sizing: border-box;

    .imagenav-main-item {
        margin-top: 8px;
        margin-bottom: 2px;
        padding-bottom: 22px;
        position: relative;

        .imagenav-item-img {
            width: 100%;
            height: inherit;
        }
        .img-height {
            height: inherit;
        }

        .imagenav-item-text {
            text-align: center;
            font-size: 24rpx;
            height: 30rpx;
        }
    }
}

.swiper-main {
    height: auto;
    width: 100%;
    display: flex;
    padding-bottom: 15px;
    overflow: hidden;

    .swiper-item {
        overflow: hidden;
        box-sizing: border-box;

        .swiper-item-box {
            width: 100%;
            padding-bottom: 100%;
            background-repeat: no-repeat;
            background-position: center;
            background-size: cover;
        }

        .imagenav-item-text {
            text-align: center;
            font-size: 26rpx;
        }
    }
}

.ad-nav_type__2.ad-pic_page_type__1 :deep(.swiper-pagination-bullet) {
    width: 24rpx;
    border-radius: 0;
    height: 4rpx;
    margin: 0 2rpx;
}
.item-text-a {
    position: absolute;
    width: 100%;
    bottom: 6rpx;
    display: flex;
    align-items: end;
    justify-content: center;
}
</style>

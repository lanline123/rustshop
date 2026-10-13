<template>
    <view class="product-pic-con">
        <template v-if="list.length > 0">
            <view class="product-bg">
                <swiper :indicator-dots="false" :circular="true" interval="3000" duration="500" :style="swiperStyle" @change="swiperChange">
                    <template v-for="(item, index) in list" :key="index">
                        <swiper-item>
                            <view class="swiper-item-box">
                                <template v-if="!item.videoUrl">
                                    <image
                                        :src="imageFormat(item.picLarge || item.picUrl || '')"
                                        :mode="imageDisplayMode"
                                        class="slide-image"
                                        @click="swiperImagePreview(index)"
                                    />
                                </template>
                                <template v-else>
                                    <view class="video-box">
                                        <view v-show="!showVideo" class="cover-box" :class="{ 'mp-force-hidden': showVideo }" @click="handlePlayVideo">
                                            <image
                                                :src="imageFormat(item.videoCover ? item.videoCover : item.picLarge || item.picUrl || '')"
                                                :mode="imageDisplayMode"
                                                class="slide-image"
                                            />
                                            <view class="video-icon" />
                                        </view>
                                        <video
                                            v-show="showVideo"
                                            id="productVideo"
                                            class="product-video"
                                            :src="imageFormat(item.videoUrl)"
                                            :autoplay="false"
                                            :enable-progress-gesture="false"
                                            controls
                                            :loop="false"
                                            @pause="handlevideoPause"
                                            @error="handlevideoError"
                                            @ended="handlevideoEnded"
                                            @fullscreenchange="handleFullscreenChange"
                                        />
                                    </view>
                                </template>
                                <template v-if="isOutSale">
                                    <view class="product-status-box">
                                        <view class="outsale">{{ $t("已售罄") }}</view>
                                    </view>
                                </template>
                            </view>
                        </swiper-item>
                    </template>
                </swiper>
                <view v-show="list.length > 1 && !showVideo" class="swiper-indicator-box" :class="{ 'mp-force-hidden': !(list.length > 1 && !showVideo) }">
                    <template v-for="item in list.length" :key="item">
                        <view class="swiper-indicator-item" :class="{ active: currentPic === item }" />
                    </template>
                </view>
            </view>
        </template>
    </view>
</template>

<script lang="ts" setup>
/**
 * 商品图片轮播组件（风格一 装修版）
 * - 主图比例 / 圆角由后台装修字段驱动（imageRatio / imageRadiusStyle）
 * - 圆点指示器、内边距留白，与标准风视觉一致
 * - 售罄判断、视频播放沿用门店风主图的业务逻辑，保证行为一致
 */
import { computed, onMounted, ref, getCurrentInstance, watch } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource } from "@/utils";
import { shouldShowSoldOut } from "@/utils/productSoldOut";
import type { PicList, ProductItem, VideoList } from "@/types/product/product";

type AnyObject = Record<string, any>;

interface Iprops {
    picList?: PicList[];
    productInfo?: ProductItem;
    videoList?: VideoList[];
    seckillValue?: AnyObject;
    grouponValue?: AnyObject;
    productStock?: number;
    // 父页面详情接口完成后置为 true；用于避免首次进入因默认库存=0而误显示“已售罄”
    detailLoaded?: boolean;
    isSubCard?: boolean;
    /** 主图比例（装修字段） */
    imageRatio?: "4:3" | "4:5" | "1:1";
    /** 轮播图左右边距（px） */
    imageMargin?: number | string;
    /** 主图显示模式：1-完整显示，2-铺满裁切 */
    imageMode?: number | string;
    /** 主图圆角（装修字段） */
    imageRadiusStyle?: "square" | "round" | "large";
}

const props = withDefaults(defineProps<Iprops>(), {
    picList: () => [] as PicList[],
    productInfo: () => ({}) as ProductItem,
    videoList: () => [] as VideoList[],
    seckillValue: () => ({}),
    grouponValue: () => ({}),
    productStock: 0,
    detailLoaded: false,
    isSubCard: false,
    imageRatio: "1:1",
    imageMargin: 16,
    imageMode: 1,
    imageRadiusStyle: "square"
});

const isOutSale = computed(() => {
    // 仅在详情接口已完成时才判断，避免首次进入（默认库存为 0）误显示
    if (!props.detailLoaded) return false;
    return shouldShowSoldOut(
        {
            ...props.productInfo,
            currentStock: props.productStock
        },
        {
            stockField: "currentStock",
            isSubCard: props.isSubCard
        }
    );
});

const currentPic = ref(1);

const videoData = computed(() => {
    return props.videoList[0] ? props.videoList[0] : ({} as VideoList);
});

const list = computed(() => {
    return videoData.value.videoUrl
        ? [
              {
                  ...videoData.value,
                  picLarge: props.picList[0]?.picLarge || ""
              },
              ...props.picList
          ]
        : props.picList;
});

// ===== 主图比例 / 圆角映射（装修字段驱动）=====
const imageRatioMap: Record<NonNullable<Iprops["imageRatio"]>, number> = {
    "4:3": 3 / 4,
    "4:5": 5 / 4,
    "1:1": 1
};

const imageRadiusMap: Record<NonNullable<Iprops["imageRadiusStyle"]>, string> = {
    square: "0",
    round: "20rpx",
    large: "36rpx"
};

const imageMarginPx = computed(() => {
    const value = Number(props.imageMargin);
    return Number.isFinite(value) ? Math.max(0, value) : 16;
});
const imageMarginCss = computed(() => `${imageMarginPx.value}px`);

const swiperStyle = computed(() => {
    const windowWidth = uni.getSystemInfoSync().windowWidth || 0;
    const contentWidth = Math.max(windowWidth - imageMarginPx.value * 2, 0);
    const ratio = imageRatioMap[props.imageRatio] || imageRatioMap["1:1"];
    return {
        height: `${Math.round(contentWidth * ratio)}px`
    };
});

const imageRadiusValue = computed(() => {
    return imageRadiusMap[props.imageRadiusStyle] || imageRadiusMap.square;
});

const imageDisplayMode = computed(() => (Number(props.imageMode) === 2 ? "aspectFill" : "aspectFit"));

const swiperChange = (e: any) => {
    currentPic.value = e.detail.current + 1;
    showVideo.value = false;
    videoContext?.pause();
};

const swiperImagePreview = (index: number) => {
    const images = list.value.map((item) => imageFormat(item.picUrl || ""));
    uni.previewImage({
        current: images[index],
        urls: images,
        indicator: "number",
        loop: true
    });
};

const instance = getCurrentInstance();
let videoContext: any;
const showVideo = ref(false);
// 记录视频是否处于全屏态：全屏时若直接隐藏 video 元素会导致全屏层无法回收、页面卡死
const isFullScreen = ref(false);

const handlePlayVideo = () => {
    showVideo.value = true;
    videoContext?.play();
};

const handleFullscreenChange = (e: any) => {
    const fullScreen = !!e?.detail?.fullScreen;
    isFullScreen.value = fullScreen;
    if (!fullScreen && pendingHideVideo) {
        pendingHideVideo = false;
        showVideo.value = false;
    }
};

const handlevideoError = (e: Event) => {
    console.log("video error", e);
};

// 标记"待退出全屏后再隐藏视频"，避免在全屏状态下抽掉 video 元素导致卡死
let pendingHideVideo = false;

const hideVideoSafely = () => {
    if (isFullScreen.value) {
        pendingHideVideo = true;
        videoContext?.exitFullScreen?.();
    } else {
        showVideo.value = false;
    }
};

const handlevideoEnded = () => {
    hideVideoSafely();
};

const handlevideoPause = () => {
    // #ifdef APP-PLUS
    hideVideoSafely();
    // #endif
};

watch(
    () => videoData.value.videoUrl,
    (videoUrl) => {
        if (!videoUrl) {
            showVideo.value = false;
            videoContext?.pause();
        }
    }
);

onMounted(() => {
    videoContext = uni.createVideoContext("productVideo", instance);
});

const playImg = computed(() => {
    return `url("${staticResource("product/video-playing.png")}")`;
});
</script>

<style lang="scss" scoped>
.product-pic-con {
    padding: 6px v-bind(imageMarginCss) 12px;
    background-color: var(--product-content-bg, #fff);
}

.product-bg {
    width: 100%;
    position: relative;
    background: rgba(0, 0, 0, 0.02);
    border-radius: v-bind(imageRadiusValue);
    overflow: hidden;

    .video-box {
        width: 100%;
        height: 100%;
        position: relative;
        border-radius: inherit;
        overflow: hidden;

        .product-video {
            position: absolute;
            inset: 0;
            width: 100%;
            height: 100%;
            border-radius: inherit;
        }

        .cover-box {
            position: absolute;
            inset: 0;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: inherit;
            overflow: hidden;

            .video-icon {
                position: absolute;
                top: 50%;
                left: 50%;
                width: 110rpx;
                height: 110rpx;
                transform: translate(-50%, -50%);
                background-image: v-bind(playImg);
                background-size: cover;
            }
        }
    }

    swiper {
        width: 100%;
        padding-bottom: 21rpx;
        box-sizing: border-box;
        border-radius: inherit;
        overflow: hidden;
    }

    .swiper-item-box {
        position: relative;
        height: 100%;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: inherit;
        overflow: hidden;
        background: rgba(0, 0, 0, 0.02);

        .product-status-box {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            display: flex;
            justify-content: center;
            align-items: center;
            width: 320rpx;
            height: 320rpx;
            background: rgba(0, 0, 0, 0.4);
            color: #fff;
            border-radius: 50%;
            font-size: 40rpx;

            .outsale {
                letter-spacing: 0;
            }
        }
    }

    .slide-image {
        width: 100%;
        height: 100%;
        border-radius: inherit;
    }

    .swiper-indicator-box {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        z-index: 1;
        display: flex;
        align-items: center;
        justify-content: center;

        .swiper-indicator-item {
            margin: 0 7rpx;
            width: 11rpx;
            height: 11rpx;
            border-radius: 50%;
            background-color: #e7e7eb;

            &.active {
                width: 28rpx;
                transition: all 0.1s;
                border-radius: 11rpx;
                background-color: #888b94;
            }
        }
    }
}

:deep(.uni-swiper-wrapper) {
    border-radius: v-bind(imageRadiusValue);
}

// #ifdef MP-WEIXIN
/* 微信端 hidden 的优先级会被 display:flex 覆盖，显式 class 保证视频封面和指示器互斥。 */
.mp-force-hidden {
    display: none !important;
}
// #endif
</style>

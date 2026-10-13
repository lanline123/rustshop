<template>
    <view class="product-pic-con">
        <view class="nav-bg" />
        <template v-if="list.length > 0">
            <view class="product-bg">
                <swiper :indicator-dots="false" :circular="true" interval="3000" duration="500" @change="swiperChange">
                    <template v-for="(item, index) in list" :key="index">
                        <swiper-item>
                            <template v-if="!item.videoUrl">
                                <view class="slide-image-box">
                                    <image :src="imageFormat(item.picLarge || '')" class="slide-image" mode="aspectFit" @click="swiperImagePreview(index)" />
                                </view>
                            </template>
                            <template v-else>
                                <view class="video-box">
                                    <view v-show="!showVideo" class="cover-box" @click="handlePlayVideo">
                                        <view class="slide-image-box">
                                            <image
                                                :src="imageFormat(item.videoCover ? item.videoCover : item.picLarge || '')"
                                                mode="heightFix"
                                                class="slide-image"
                                            />
                                        </view>

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
                        </swiper-item>
                    </template>
                </swiper>
                <view v-show="list.length > 1 && !showVideo" class="pages">
                    <text>{{ currentPic }}</text>
                    /{{ list.length || 1 }}
                </view>

                <template v-if="isOutSale">
                    <view class="outsale">{{ $t("已售罄") }}</view>
                </template>
            </view>
        </template>
    </view>
</template>

<script lang="ts" setup>
/**
 * 商品图片轮播组件
 * - 主图轮播展示商品图片和视频
 * - 支持图片预览、视频播放
 * - 显示当前图片索引指示器
 */
import { computed, onMounted, ref, getCurrentInstance } from "vue";
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
}

const props = withDefaults(defineProps<Iprops>(), {
    picList: () => [] as PicList[],
    productInfo: () => ({}) as ProductItem,
    videoList: () => [] as VideoList[],
    seckillValue: () => ({}),
    grouponValue: () => ({}),
    productStock: 0,
    detailLoaded: false,
    isSubCard: false
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
    // 退出全屏时，如果视频已播放结束，再执行隐藏
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
        // 仍在全屏：先退出全屏，等 fullscreenchange 回调里再隐藏
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

onMounted(() => {
    videoContext = uni.createVideoContext("productVideo", instance);
});

const playImg = computed(() => {
    return `url("${staticResource("product/video-playing.png")}")`;
});
</script>

<style lang="scss" scoped>
.product-pic-con {
    position: relative;
    .nav-bg {
        position: absolute;
        left: 0;
        top: 0;
        z-index: 9;
        width: 100%;
        height: 150rpx;
        background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent);
    }
}
.product-bg {
    position: relative;
    width: 100%;
    height: 750rpx;
    position: relative;
    background: #fff;

    .outsale {
        position: absolute;
        top: 50%;
        left: 50%;
        width: 50%;
        height: 50%;
        line-height: 180rpx;
        transform: translate(-50%, -50%);
        background: rgba(0, 0, 0, 0.4);
        font-size: 48rpx;
        color: #ffffff;
        letter-spacing: 0;
        border-radius: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .video-box {
        width: 100%;
        height: 100%;

        .product-video {
            width: 100%;
            height: 100%;
        }

        .cover-box {
            width: 100%;
            height: 100%;
            position: relative;

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
        height: 100%;
        position: relative;
    }

    .slide-image-box {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .slide-image {
        width: 100%;
        height: 100%;
    }

    .pages {
        visibility: visible;
        transition: all 500ms ease-in-out 0s;
        position: absolute;
        background-color: rgba(0, 0, 0, 0.2);
        height: 40rpx;
        padding: 0 40rpx 0 30rpx;
        border-radius: 40rpx 0 0 40rpx;
        right: 0;
        bottom: 30rpx;
        line-height: 40rpx;
        font-size: 20rpx;
        color: #fff;
    }
}

.product-bg .pages text {
    font-weight: bold;
    font-size: 30rpx;
}
.product-bg .slide-navigator .slide-image {
    width: 100%;
    height: 100%;
    border-radius: 15rpx;
}
.product-bg .wx-swiper-dot {
    width: 20rpx;
    height: 5rpx;
    border-radius: 3rpx;
}
.product-bg .wx-swiper-dots.wx-swiper-dots-horizontal {
    margin-bottom: 5rpx;
}

.product-pic-con {
    height: 750rpx;
    width: 750rpx;
    position: relative;
    background: #f2f2f2;
}
</style>

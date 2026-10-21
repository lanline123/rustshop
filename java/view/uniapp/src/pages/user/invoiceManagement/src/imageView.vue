<template>
    <tig-layout :title="`${title}/${fileList.length}`">
        <view class="container" :style="`height: calc(100vh - ${configStore.navHeight + 120}rpx)`">
            <view class="image-view">
                <swiper class="swiper" :indicator-dots="false" :circular="true" @animationfinish="handleFinish">
                    <template v-for="(item, index) in fileList" :key="index">
                        <swiper-item>
                            <view class="image-bg" :style="{ backgroundImage: `url(${imageFormat(item.picUrl)})` }" @click="previewImage(index)" />
                        </swiper-item>
                    </template>
                </swiper>
            </view>
            <tig-fixed-placeholder background-color="#fff">
                <view class="btn-box">
                    <tig-button @click="handleDownload"> {{ $t("下载图片") }} </tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-layout>
</template>

<script setup lang="tsx">
import { computed, ref } from "vue";
import type { InvoiceAttachment } from "@/types/user/order";
import { onLoad } from "@dcloudio/uni-app";
import { useConfigStore } from "@/store/config";
import { downloadImage } from "@/utils";
import { imageFormat } from "@/utils/format";

const configStore = useConfigStore();
const title = ref(1);
const current = ref(0);
const fileList = ref<InvoiceAttachment[]>([]);

const handleFinish = (e: any) => {
    title.value = e.detail.current + 1;
    current.value = e.detail.current;
};

const previewImage = (current: number) => {
    uni.previewImage({
        current,
        urls: fileList.value.map((e) => imageFormat(e.picUrl)),
        indicator: "number",
        loop: true
    });
};

const handleDownload = () => {
    // #ifdef H5
    downloadImage(fileList.value[current.value]?.picUrl);
    //#endif

    //#ifdef MP-WEIXIN
    fileList.value.forEach((item) => {
        downloadImage(item.picUrl);
    });
    //#endif
    //#ifdef APP-PLUS
    fileList.value.forEach((item) => {
        downloadImage(item.picUrl);
    });
    //#endif
};

onLoad((options: any) => {
    if (options && options.fileList) {
        fileList.value = JSON.parse(decodeURIComponent(options.fileList));
    }
});
</script>

<style lang="scss" scoped>
.container {
    display: flex;
    align-items: center;
    width: 100%;
    .image-view {
        width: 100%;
        height: 100%;
        .swiper {
            width: 100%;
            height: 100%;
            .image-bg {
                width: 100%;
                height: 100%;
                background-size: contain;
                background-position: center;
                background-repeat: no-repeat;
            }
        }
    }
    .btn-box {
        padding: 25rpx;
    }
}

.scroll-view {
    display: flex;
    overflow-x: auto;
    u-image {
        background: #f4f8ff;
        & + u-image {
            margin-left: 20rpx;
        }
    }
    .empty {
        margin: 0 auto;
        &-img {
            width: 258rpx;
            height: 138rpx;
        }
        &-text {
            margin-top: 20rpx;
            font-weight: 700;
            font-size: 28rpx;
            color: #666666;
            line-height: 44rpx;
            text-align: center;
        }
    }
}
</style>

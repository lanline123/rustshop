<template>
    <view v-if="show" class="splash-ad">
        <image v-if="adType === 'image'" :src="imageFormat(adUrl)" class="ad-img" mode="aspectFill" @load="onLoaded" @click="handleAdClick" />
        <video
            v-else-if="adType === 'video'"
            :src="imageFormat(adUrl)"
            class="ad-video"
            object-fit="cover"
            :autoplay="true"
            :controls="false"
            :show-center-play-btn="false"
            :show-play-btn="false"
            :show-fullscreen-btn="false"
            :enable-progress-gesture="false"
            :muted="true"
            :loop="true"
            @ended="closeAd"
            @loadedmetadata="onLoaded"
            @click="handleAdClick"
        >
            <cover-view v-if="loaded" class="close-btn" @click.stop="closeAd">{{ countdown }} {{ $t("跳过") }}</cover-view>
        </video>
        <view v-if="adType !== 'video' && loaded" class="close-btn" @click.stop="closeAd">{{ countdown }} {{ $t("跳过") }}</view>
    </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { urlFormat, imageFormat } from "@/utils/format";

const props = defineProps<{
    adType: "image" | "video";
    adUrl: string;
    duration?: number;
    redirectType?: string;
    redirectUrl?: string;
}>();

const show = ref(true);
const loaded = ref(false);
const countdown = ref(props.duration || 3);
let timer: any = null;
const emit = defineEmits(["close"]);

const closeAd = () => {
    show.value = false;
    clearInterval(timer); // 销毁倒计时
    // if (props.redirectType == "1") {
    //     uni.navigateTo({
    //         url: urlFormat(props.redirectUrl || "")
    //     });
    // }
    emit("close");
};
const handleAdClick = () => {
    console.log("123132132");
    // const redirect = JSON.parse(props.redirectUrl || "{}");
    if (props.redirectType == "1") {
        uni.navigateTo({
            url: urlFormat(props.redirectUrl || "")
        });
        clearInterval(timer); // 销毁倒计时
        emit("close");
    }
};

const onLoaded = () => {
    loaded.value = true;
    timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
            closeAd();
        }
    }, 1000);
};
</script>
<style lang="scss">
.close-btn {
    position: absolute;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    padding: 10rpx 24rpx;
    border-radius: 30rpx;
    font-size: 28rpx;
    line-height: 40rpx;
    z-index: 100001;
    pointer-events: auto;
}
/* #ifdef MP-WEIXIN */
.close-btn {
    top: 103rpx;
    left: 15rpx;
}
/* #endif */

/* #ifdef H5 */
.close-btn {
    top: 40rpx;
    right: 40rpx;
}
/* #endif */

/* #ifdef APP-PLUS */
.close-btn {
    top: 80rpx;
    right: 40rpx;
}
/* #endif */

.splash-ad {
    position: fixed;
    z-index: 99999;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: #000;
    display: flex;
    align-items: center;
    justify-content: center;
}

.ad-img,
.ad-video {
    position: relative;
    width: 100vw;
    height: 100vh;
    object-fit: cover;
    display: block;
}
</style>

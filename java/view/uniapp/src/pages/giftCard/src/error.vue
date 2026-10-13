<template>
    <view class="error-page">
        <view class="error-content">
            <view class="error-image" />
            <view class="error-text">{{ $t(message) }}</view>
            <view class="error-btn-box">
                <view theme class="error-btn" @click="handleClick">{{ $t("进店逛逛") }}</view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { redirect, staticResource } from "@/utils";
import { computed } from "vue";

defineProps({
    message: {
        type: String,
        default: "页面失效"
    }
});
const errorImage = computed(() => {
    return `url(${staticResource("product/noDetail.png")})`;
});

const handleClick = () => {
    redirect({ url: "/pages/index/index" });
    console.log("Redirecting to home page");
};
</script>

<style lang="scss" scoped>
.error-content {
    padding: 48rpx 0 200rpx;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;

    .error-image {
        display: inline-block;
        width: 181rpx;
        height: 171rpx;
        margin-bottom: 48rpx;
        background-image: v-bind(errorImage);
        background-repeat: no-repeat;
        background-size: cover;
        background-position: 50%;
    }

    .error-btn-box {
        display: flex;
        justify-content: center;
        font-size: 24.5rpx;
        .error-btn {
            height: 53rpx;
            line-height: 53rpx;
            padding: 0 20rpx;
            background-color: var(--main-bg);
            color: var(--main-text);
        }
    }

    .error-text {
        margin: 0 0 6.66667vw;
        color: #646464;
    }
}
</style>

<template>
    <view>
        <tig-layout>
            <view class="plr-40">
                <view class="h1_tit">{{ $t("找回密码") }}</view>
                <block v-if="step === 1">
                    <stepOne v-model:step="step" v-model:mobile-key="mobileKey" />
                </block>
                <up-transition :show="step === 2" :mode="'fade-left'">
                    <stepTow v-model:step="step" v-model:mobile-key="mobileKey" />
                </up-transition>
            </view>
        </tig-layout>
    </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import stepOne from "./src/stepOne.vue";
import stepTow from "./src/stepTow.vue";
const step = ref(1);
const mobileKey = ref("");
</script>

<style>
page {
    background: #fff;
}
</style>
<style lang="scss" scoped>
.btn2-css3 {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    padding: 0;
    font-size: 36rpx;
    font-weight: normal;
}
.placeholder {
    font-size: 28rpx;
    font-weight: normal;
}
.plr-40 {
    padding: 0 40rpx;
}
.h1_tit {
    color: #333;
    font-size: 36rpx;
    margin-bottom: 40rpx;
    margin-top: 150rpx;
    margin-bottom: 100rpx;
    font-weight: bold;
}

.item {
    margin-bottom: 70rpx;
    position: relative;
    .text {
        border: 0;
        border-radius: 0;
        background-color: transparent;
        padding-left: 0rpx;
        height: 88rpx;
        line-height: normal;
        font-size: 28rpx;
        color: #252525;
        padding: 0;
        font-weight: normal;
        border-bottom: 1rpx solid #eee;
    }
    .get-mobile {
        background-color: transparent;
        padding-left: 0rpx;
        height: 88rpx;
        line-height: normal;
        font-size: 28rpx;
        color: #252525;
        padding: 0;
        font-weight: normal;
        position: absolute;
        top: 0;
        width: 100%;
        z-index: 88;
    }
    .parent {
        position: relative;
    }

    &.mobile_code_text {
        width: 100%;
        /* padding-right: 200rpx; */
        align-items: center;
        display: flex;
    }
}
</style>

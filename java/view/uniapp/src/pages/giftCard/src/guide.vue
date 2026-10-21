<template>
    <view v-if="modelValue" class="guide-box" @click="emit('update:modelValue', false)">
        <uni-transition ref="ani" custom-class="transition" :mode-class="['fade', 'slide-top']" :styles="styles" :show="modelValue">
            <view class="guide-content">
                <view class="guide-img-box">
                    <image class="guide-img" :src="staticResource('common/share_arrow.png')" />
                </view>
                <view class="guide-text"> {{ $t("立即分享给好友吧") }} </view>
                <view> {{ $t("点击屏幕右上角将本页面分享给好友") }} </view>
            </view>
        </uni-transition>
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";
const configStore = useConfigStore();
defineProps({
    modelValue: {
        type: Boolean,
        default: false
    }
});
const emit = defineEmits(["update:modelValue"]);

const styles = ref({
    justifyContent: "center",
    alignItems: "center",
    flexDirection: "column",
    width: "100vw",
    height: "auto"
});

const iconInfo = computed(() => {
    return configStore.menuButtonInfo;
});
</script>

<style lang="scss" scoped>
.guide-box {
    width: 100vw;
    height: 100vh;
    position: fixed;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 88;
    top: 0;
    left: 0;

    .guide-content {
        color: #fff;
        font-size: 28rpx;
        text-align: center;
        display: flex;
        flex-direction: column;
        align-items: center;
        position: fixed;
        width: 100%;
        top: 20rpx;
        // #ifdef MP-WEIXIN
        top: v-bind("iconInfo.bottom + 5 + 'px'");
        // #endif

        .guide-text {
            font-weight: bold;
            padding-bottom: 20rpx;
            font-size: 30rpx;
        }
    }

    .guide-img-box {
        display: flex;
        flex-direction: row;
        padding-right: 50rpx;
        padding-bottom: 20rpx;
        width: 100%;
        // #ifdef MP-WEIXIN
        padding-right: v-bind("(iconInfo.width - 15) + 'px'");
        // #endif
        justify-content: flex-end;
        .guide-img {
            width: 166rpx;
            height: 296rpx;
        }
    }
}
</style>

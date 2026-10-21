<template>
    <view class="animation-box" :class="{ animation: showAnimation }" :style="{ ...styleVariable }">
        <view class="animation-content" @animationend="handleAnimationEnd" />
    </view>
</template>

<script setup lang="ts">
/**
 * 加入购物车动画组件
 * - 商品图片飞入购物车抛物线动画
 * - CSS动画实现平滑过渡效果
 */
import { watchEffect } from "vue";

interface Props {
    animationPosition: { top: number; left: number };
    styleVariable: {
        "--y": string;
        "--x": string;
    };
}
const props = defineProps<Props>();
const showAnimation = defineModel({ default: false });
let timeout: ReturnType<typeof setTimeout>;

// #ifdef MP-WEIXIN
watchEffect(() => {
    if (showAnimation.value) {
        timeout = setTimeout(() => {
            showAnimation.value = false;
            clearTimeout(timeout);
        }, 290);
    }
});
//  #endif

const handleAnimationEnd = () => {
    // #ifdef MP-WEIXIN
    return;
    // #endif
    showAnimation.value = false;
};
</script>

<style lang="scss" scoped>
@keyframes moveY {
    to {
        transform: translateY(var(--y));
    }
}
@keyframes moveX {
    to {
        transform: translateX(var(--x));
    }
}

.animation-box {
    position: fixed;
    width: 22rpx;
    height: 22rpx;
    border-radius: 50%;
    z-index: 9999;
    display: none;
}
.animation {
    display: block;
    top: v-bind("props.animationPosition.top + 'px'");
    left: v-bind("props.animationPosition.left + 'px'");
    animation: moveY 0.3s cubic-bezier(0.5, -4, 1, 1);

    .animation-content {
        width: 100%;
        height: 100%;
        background: var(--general);
        border-radius: 50%;
        animation: moveX 0.3s linear;
    }
}
</style>

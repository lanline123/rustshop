<template>
    <view class="item-img-a">
        <view class="item-img-bg" :style="`background-image: url(${imageFormat(src ?? '')});`" />
        <tig-image class="item-img" mode="widthFix" :src="src" />
    </view>
</template>

<script setup lang="ts">
import { imageFormat } from "@/utils/format";

defineProps({
    src: String
});
</script>

<style lang="scss" scoped>
.item-img-a {
    height: 100%;
    width: 100%;
    position: relative;
    // 小程序端父组件选择器无法跨组件生效，圆角通过继承的 CSS 变量在组件内部应用
    border-radius: var(--img-radius, 0px);
    overflow: hidden;

    .item-img-bg {
        position: absolute;
        top: 0;
        left: 0;
        background-position: center;
        background-size: cover;
        height: 100%;
        width: 100%;
        overflow: hidden;
        // z-index: 9;
    }
}

.item-img {
    width: 100%;
    opacity: 0;
    // position: relative;
    // z-index: -1;
}
</style>

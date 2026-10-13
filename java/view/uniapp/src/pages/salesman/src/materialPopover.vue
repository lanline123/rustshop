<template>
    <view class="material-popover">
        <view class="popover-masking" :class="{ show }" @click="handleClick" />
        <view v-if="show" class="popover-content-box" :class="{ show }">
            <view class="popover-content" :class="{ enter: show, leave: !show }" @click="handleClick">
                <view class="triangle" />
                <slot name="content" :show="show" />
            </view>
        </view>

        <view @click="show = !show">
            <slot />
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 素材操作弹出菜单组件
 * - 提供素材的更多操作选项
 */
import { ref } from "vue";
const show = ref(false);
const handleClick = () => {
    show.value = !show.value;
};
</script>

<style lang="scss" scoped>
.material-popover {
    position: relative;

    .popover-masking {
        position: fixed;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        display: none;
    }

    .popover-content-box {
        position: absolute;
        top: 50%;
        left: -420rpx;
        display: block;

        .popover-content {
            box-shadow: 0 4rpx 24rpx rgba(50, 50, 51, 0.12);
            background-color: #323233;
            height: 74rpx;
            line-height: 74rpx;
            color: #fff;
            font-size: 26rpx;
            transform-origin: 100% 50%;
            transition: all 0.15s ease-out;
            &.enter {
                transform: translateY(-50%) translateX(0) scale(1);
                opacity: 1;
            }

            &.leave {
                transform: translateY(-50%) translateX(20%) scale(0.8);
                opacity: 0.3;
            }
            .triangle {
                width: 0;
                height: 0;
                border: 12rpx solid transparent;
                border-left: 14rpx solid #323233;
                position: absolute;
                right: -24rpx;
                top: 26rpx;
            }
        }
    }

    .show {
        display: block !important;
    }
}
</style>

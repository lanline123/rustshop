<template>
    <view class="tig-navbar">
        <view class="navbar-box" :style="`background-color: ${module?.titleBackgroundColor}`">
            <view class="navbar">
                <view class="navbar-content">
                    <view class="left">
                        <template v-if="showLeft">
                            <slot name="left">
                                <view class="iconfont icon-houtui" @click="handleIcon" />
                            </slot>
                        </template>
                    </view>

                    <view class="middle">
                        <slot name="title">
                            <view class="title line1" :style="`color: ${module?.titleColor};`">{{ $t(module?.title) }}</view>
                        </slot>
                    </view>

                    <view class="right">
                        <slot name="right" />
                    </view>
                </view>
            </view>
        </view>
        <view class="placeholder" />
    </view>
</template>

<script setup lang="ts">
defineProps({
    title: {
        type: String,
        default: ""
    },
    background: {
        type: String,
        default: "#fff"
    },
    showLeft: {
        type: Boolean,
        default: false
    },
    module: {
        type: Object,
        default: () => ({})
    }
});

const handleIcon = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    uni.navigateBack();
    // #endif
};
</script>

<style lang="scss" scoped>
.tig-navbar {
    .navbar-box {
        // position: fixed;
        // left: 0;
        // top: 0;
        width: 100%;
        height: var(--nav-height);
        z-index: 888;
        background-color: #fff;

        .navbar {
            position: relative;
            height: 100%;

            .navbar-content {
                position: absolute;
                height: 100rpx;
                bottom: 0;
                left: 0;
                display: flex;
                align-items: center;
                width: 100%;

                .left {
                    width: 100rpx;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    .icon-houtui {
                        font-size: 40rpx;
                    }
                }
                .middle {
                    flex: 1;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    color: #000;
                    overflow: hidden;
                    .title {
                        font-size: 32rpx;
                    }
                }

                .right {
                    padding-right: 16rpx;
                    width: 100rpx;
                }
            }
        }
    }
}

.placeholder {
    // width: 100%;
    // height: 0;
    // padding-top: var(--nav-height);
}
</style>

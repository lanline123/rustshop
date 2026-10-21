<template>
    <view class="im-navbar">
        <view class="navbar-box" :style="{ background: background }">
            <view class="navbar">
                <view class="navbar-content" :style="contentStyle">
                    <!-- 左侧返回按钮 -->
                    <view class="left" @click="handleBack">
                        <text class="iconfont icon-houtui" :style="{ color: iconColor }" />
                    </view>

                    <!-- 中间标题 -->
                    <view class="middle">
                        <text class="title line1" :style="{ color: titleColor }">{{ displayTitle }}</text>
                    </view>

                    <!-- 右侧全部已读 -->
                    <view class="right" :style="rightStyle" @click="handleReadAll">
                        <view class="read-all">
                            <image class="clear-icon" :src="staticResource('im/clear.png')" mode="aspectFill" />
                            <text class="clear-text">{{ $t("全部已读") }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
        <!-- 占位元素，防止内容被导航栏遮挡 -->
        <view class="placeholder" :style="{ paddingTop: navHeight }" />
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 聊天导航栏组件
 * - 聊天页面顶部导航
 * - 显示对方名称和在线状态
 */
import { computed } from "vue";
import { useConfigStore } from "@/store/config";
import { staticResource } from "@/utils";

const { t } = useI18n();
const configStore = useConfigStore();

const props = defineProps({
    title: {
        type: String,
        default: ""
    },
    background: {
        type: String,
        default: "#f6f6f6"
    },
    titleColor: {
        type: String,
        default: "#333"
    },
    iconColor: {
        type: String,
        default: "#333"
    }
});

const displayTitle = computed(() => props.title || t("消息"));

const emit = defineEmits(["readAll", "update:modelValue"]);

// 小程序胶囊按钮信息
const menuButtonInfo = computed(() => configStore.menuButtonInfo);

// 计算安全区域顶部距离
const safeTop = computed(() => {
    // #ifdef MP-WEIXIN
    return menuButtonInfo.value.top;
    // #endif
    // #ifndef MP-WEIXIN
    return configStore.saveTop + 10;
    // #endif
});

// 导航栏总高度
const navHeight = computed(() => {
    // #ifdef MP-WEIXIN
    // 小程序：胶囊顶部 + 胶囊高度 + 底部间距
    return `${menuButtonInfo.value.top + menuButtonInfo.value.height + 8}px`;
    // #endif
    // #ifndef MP-WEIXIN
    // H5/App：安全区域 + 内容高度
    return `${configStore.saveTop + 50}px`;
    // #endif
});

// 内容区域样式
const contentStyle = computed(() => {
    // #ifdef MP-WEIXIN
    return {
        top: `${menuButtonInfo.value.top}px`,
        height: `${menuButtonInfo.value.height}px`
    };
    // #endif
    // #ifndef MP-WEIXIN
    return {
        top: `${safeTop.value}px`,
        height: "44px"
    };
    // #endif
});

// 右侧按钮样式（避开小程序胶囊按钮）
const rightStyle = computed(() => {
    // #ifdef MP-WEIXIN
    return {
        marginRight: `${menuButtonInfo.value.width + 16}px`
    };
    // #endif
    // #ifndef MP-WEIXIN
    return {
        marginRight: "16rpx"
    };
    // #endif
});

// 返回事件
const handleBack = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    const pageList = getCurrentPages();
    if (pageList.length > 1) {
        uni.navigateBack();
    } else {
        uni.switchTab({ url: "/pages/index/index" });
    }
    // #endif
};

// 全部已读事件
const handleReadAll = () => {
    emit("readAll");
};
</script>

<style lang="scss" scoped>
.im-navbar {
    .navbar-box {
        position: fixed;
        left: 0;
        top: 0;
        width: 100%;
        z-index: 200;

        .navbar {
            position: relative;
            width: 100%;

            .navbar-content {
                position: absolute;
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
                    flex-shrink: 0;

                    .icon-houtui {
                        font-size: 38rpx;
                        font-weight: bold;
                    }
                }

                .middle {
                    flex: 1;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    overflow: hidden;
                    padding: 0 20rpx;

                    .title {
                        font-size: 32rpx;
                        font-weight: 500;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                }

                .right {
                    flex-shrink: 0;
                    display: flex;
                    align-items: center;
                    justify-content: flex-end;

                    .read-all {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        padding: 0 10rpx;

                        .clear-icon {
                            width: 28rpx;
                            height: 28rpx;
                            margin-bottom: 4rpx;
                        }

                        .clear-text {
                            font-size: 20rpx;
                            color: #666;
                            white-space: nowrap;
                        }
                    }
                }
            }
        }
    }

    .placeholder {
        width: 100%;
        height: 0;
    }
}
</style>

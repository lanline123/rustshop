<template>
    <view class="tabbar-box">
        <view class="tabbar-container">
            <view class="tabbar">
                <template v-for="(item, index) in tabbarStore.tabbarList" :key="index">
                    <view class="tabbar-item" @click="handleTabbar(item, index)">
                        <view
                            class="tabbar-icon"
                            :class="{
                                'tabbar-icon--animated': tabbarStore.tabbarEnableAnimation,
                                'tabbar-icon--active': tabbarStore.currentActiveValue === index
                            }"
                        >
                            <view class="icon-wrapper">
                                <image v-show="tabbarStore.currentActiveValue === index" class="tabbar-icon-img" :src="imageFormat(item.activeImage)" />
                                <image v-show="tabbarStore.currentActiveValue !== index" class="tabbar-icon-img" :src="imageFormat(item.image)" />
                                <view v-if="isCartTab(index) && cartStore.cartCount > 0" class="cart-badge">
                                    {{ cartStore.cartCount > 99 ? "99+" : cartStore.cartCount }}
                                </view>
                            </view>
                        </view>
                        <view class="tabbar-text" :class="{ active: tabbarStore.currentActiveValue === index }">{{ $t(item.text) }}</view>
                    </view>
                </template>
            </view>
        </view>
        <view class="placeholder" />
    </view>
</template>

<script setup lang="ts">
import { watch, computed } from "vue";
import { useTabbarStore } from "@/store/tabbar";
import { useCartStore } from "@/store/cart";
import { redirect } from "@/utils";
import { urlFormat } from "@/utils/format";
import { imageFormat } from "@/utils/format";

const tabbarStore = useTabbarStore();
const cartStore = useCartStore();

const props = defineProps({
    backgroundColor: {
        type: String,
        default: "#fff"
    },
    color: {
        type: String,
        default: "#000"
    },
    activeColor: {
        type: String,
        default: "#ea3c2d"
    }
});

// 自定义背景色优先，否则用系统默认
const effectiveBackgroundColor = computed(() => tabbarStore.tabbarBackground || props.backgroundColor);
// 顶部圆角
const effectiveRadius = computed(() => tabbarStore.tabbarRadius);
// 自定义配色时用后台文字色，否则用系统默认
const isCustomColor = computed(() => tabbarStore.tabbarIconColorType === 2);
const effectiveColor = computed(() => (isCustomColor.value && tabbarStore.tabbarUnselectedTextColor ? tabbarStore.tabbarUnselectedTextColor : props.color));
const effectiveActiveColor = computed(() =>
    isCustomColor.value && tabbarStore.tabbarSelectedTextColor ? tabbarStore.tabbarSelectedTextColor : props.activeColor
);

const isCartTab = (index: number) => {
    const path = tabbarStore.formatTabarList[index];
    return path && path.includes("/pages/cart/");
};

watch(
    () => tabbarStore.currRoute,
    () => {
        cartStore.fetchCartCount();
    },
    { immediate: true }
);

const handleTabbar = (item: any, index: number) => {
    if (tabbarStore.currentActiveValue === index) return;

    if (item.link) {
        redirect({ url: urlFormat(item.link) });
    }
};
</script>

<style lang="scss" scoped>
.placeholder {
    width: 100%;
    height: 0;
    padding-bottom: var(--tabbar-height);
}
.tabbar-container {
    background-color: v-bind("effectiveBackgroundColor");
    border-radius: v-bind("effectiveRadius");
    z-index: 98;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    box-sizing: border-box;
    height: var(--tabbar-height);
    padding-bottom: var(--safe-bottom);
    .tabbar {
        display: flex;
        align-items: center;
        box-sizing: border-box;
        height: 100%;
        .tabbar-item {
            color: v-bind("effectiveColor");
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            height: inherit;
            padding: 20rpx 0;
            .tabbar-icon {
                display: flex;
                align-items: center;
                justify-content: center;
                .icon-wrapper {
                    position: relative;
                    display: inline-flex;
                }
                .tabbar-icon-img {
                    height: 42rpx;
                    width: 42rpx;
                }
                &.tabbar-icon--animated.tabbar-icon--active {
                    .tabbar-icon-img {
                        animation: tabbar-icon-scale 260ms ease-out;
                        transform-origin: center center;
                    }
                }
                .cart-badge {
                    position: absolute;
                    top: -10rpx;
                    right: -20rpx;
                    min-width: 28rpx;
                    height: 28rpx;
                    line-height: 28rpx;
                    padding: 0 6rpx;
                    background-color: #ea3c2d;
                    color: #fff;
                    font-size: 18rpx;
                    border-radius: 28rpx;
                    text-align: center;
                    white-space: nowrap;
                    box-sizing: border-box;
                }
            }
            .tabbar-text {
                padding-top: 5rpx;
                font-size: 24rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                &.active {
                    color: v-bind("effectiveActiveColor");
                }
            }
        }
    }
}

@keyframes tabbar-icon-scale {
    0% {
        transform: scale(0.8);
    }
    60% {
        transform: scale(1.15);
    }
    100% {
        transform: scale(1);
    }
}
</style>

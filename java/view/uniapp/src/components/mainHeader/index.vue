<template>
    <view class="common-header-box" :style="{ position, background: props.background }">
        <view class="common-header" :class="{ 'no-safe-top': noSafeTop }" :style="{ background: props.background }">
            <view class="main-header" :style="mainHeaderStyle">
                <view class="main-header-box flex">
                    <view class="box-left flex align-center">
                        <view class="menu-icon" @click="showMenu = !showMenu">
                            <view class="iconfont-h5 icon-caidan" />
                        </view>
                        <view v-show="configStore.baseInfo.shopLogo" class="header-logo" @click="handleLink('/pages/index/index')">
                            <image mode="widthFix" :src="configStore.baseInfo.shopLogo" />
                        </view>
                    </view>
                    <view class="box-right flex align-center">
                        <view class="user-icon" @click="handleLink('/pages/member/index')">
                            <view class="iconfont-h5 icon-xinjianwenbenwendang" />
                        </view>
                        <view class="cart-icon" @click="handleLink('/pages/cart/index')">
                            <view class="iconfont-h5 icon-gouwuche4" />
                            <template v-if="cartStore.cartCount && cartStore.cartCount > 0">
                                <view class="corner-mark">{{ cartStore.cartCount }}</view>
                            </template>
                        </view>
                    </view>
                </view>
            </view>

            <slot name="navbar">
                <template v-if="title">
                    <view class="navbar-content">
                        <view class="left">
                            <template v-if="showLeft">
                                <slot name="left">
                                    <view class="iconfont icon-houtui" @click="handleIcon" />
                                </slot>
                            </template>
                        </view>

                        <view class="middle">
                            <slot name="middle">
                                <view class="title line1">{{ $t(title) }}</view>
                            </slot>
                        </view>

                        <view class="right">
                            <slot name="right" />
                        </view>
                    </view>
                </template>
            </slot>
        </view>
    </view>

    <template v-if="showMenu">
        <view class="common-menu-box" @click="showMenu = false">
            <view class="left-menu-box flex flex-column" @click.stop="() => {}">
                <!-- <view class="placeholder" :style="topPlaceholder"></view> -->
                <!-- <view class="close-icon" @click="showMenu = false">
                    <image src="@/static/images/common/close.png" />
                </view> -->
                <view class="left-menu-header common-box">
                    <view class="close-icon" @click="showMenu = false">
                        <image src="@/static/images/common/close.png" />
                    </view>
                    <view class="menu-header-content flex align-center" :style="topPlaceholder">
                        <view class="profile-photo">
                            <template v-if="userStore.userInfo.avatar">
                                <image :src="imageFormat(userStore.userInfo.avatar)" />
                            </template>
                            <template v-else>
                                <image :src="staticResource('common/default-user.png')" />
                            </template>
                        </view>
                        <template v-if="userStore.token">
                            <view class="profile-info line1">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</view>
                            <tig-button2 class="header-btn" :style="placeholder" @click="userStore.logout">
                                <text class="header-btn-text"> {{ $t("登出") }}</text>
                            </tig-button2>
                        </template>
                        <template v-else>
                            <view class="profile-info line1" @click="handleLink('/pages/login/index')">{{ $t("登入") }}</view>
                        </template>
                    </view>
                </view>

                <view class="category-box common-box flex align-center justify-between" @click="handleLink('/pages/productCate/index')">
                    <view class="category-text">
                        {{ $t("分类") }}
                    </view>
                    <view class="category-icon">
                        <image src="@/static/images/common/right_arrows.png" />
                    </view>
                </view>

                <view class="page-menu common-box">
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/user/order/list')">
                        <view class="item-left">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("订单") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <!-- <view class="right-text">0</view> -->
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/user/messageLog/index')">
                        <view class="item-left">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("消息") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <!-- <view class="right-text">0</view> -->
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/user/collectProduct/index')">
                        <view class="item-left flex">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("心愿单") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <!-- <view class="right-text">0</view> -->
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/coupon/index?type=2')">
                        <view class="item-left">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("优惠券") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <template v-if="userStore.token">
                                <view class="right-text">{{ couponCount }}</view>
                            </template>
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/address/list')">
                        <view class="item-left">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("地址") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <!-- <view class="right-text">0</view> -->
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                    <view class="page-item flex align-center justify-between" @click="handleLink('/pages/user/profile/index')">
                        <view class="item-left">
                            <view class="left-icon" />
                            <view class="left-text">{{ $t("设置") }}</view>
                        </view>
                        <view class="item-right flex align-center">
                            <!-- <view class="right-text">0</view> -->
                            <view class="category-icon">
                                <image src="@/static/images/common/right_arrows.png" />
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </template>
</template>

<script setup lang="ts">
import { computed, ref, getCurrentInstance, onMounted, nextTick } from "vue";
import { useConfigStore } from "@/store/config";
import { useUserStore } from "@/store/user";
import { onHide, onShow } from "@dcloudio/uni-app";
import { redirect, getElementRect, staticResource } from "@/utils";
import { imageFormat } from "@/utils/format";
import { useCartStore } from "@/store/cart";

const props = defineProps({
    position: {
        type: String as () => "sticky" | "static" | "fixed",
        default: "static"
    },
    title: {
        type: String,
        default: ""
    },
    showLeft: {
        type: Boolean,
        default: true
    },
    noSafeTop: {
        type: Boolean,
        default: false
    },
    background: {
        type: String,
        default: "#fff"
    }
});

const configStore = useConfigStore();

const userStore = useUserStore();

const cartStore = useCartStore();

cartStore.fetchCartCount();

const mainHeaderStyle = computed(() => {
    // #ifdef MP-WEIXIN
    if (props.noSafeTop) return "";
    return `padding-top: ${configStore.menuButtonInfo.top}px;padding-right: ${configStore.menuButtonInfo.width}px;`;
    // #endif

    return "";
});

const placeholder = computed(() => {
    // #ifdef MP-WEIXIN
    return `margin-right: ${configStore.menuButtonInfo.width / 2 + 5}px;`;
    // #endif

    return "";
});

const topPlaceholder = computed(() => {
    // #ifdef MP-WEIXIN
    return `padding-top: ${(configStore.menuButtonInfo.height + configStore.menuButtonInfo.top) / 2 + 5}px;`;
    // #endif

    return "";
});

const showMenu = ref(false);

const couponCount = computed(() => {
    const coupon = Number(userStore.userInfo?.coupon);
    if (Number.isFinite(coupon) && coupon >= 0) return coupon;
    return 0;
});

const handleLink = (url: string) => {
    redirect({ url });
};

const handleIcon = () => {
    // #ifdef H5
    history.back();
    // #endif

    // #ifndef H5
    uni.navigateBack();
    // #endif
};

const instance = getCurrentInstance();

const sleep = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

/**
 * 测量顶部栏高度写入 store，供商品详情二级 sticky 导航计算偏移。
 * 首次进入时 DOM/安全区可能尚未稳定，按跨境项目做法延后重试。
 */
const getContentRect = async () => {
    await nextTick();
    for (let i = 0; i < 3; i++) {
        try {
            const result = await getElementRect(".common-header", instance);
            if (result && result.height > 0) {
                configStore.setCommonHeaderHeight(result.height);
                return;
            }
        } catch (e) {
            console.error("getElementRect error:", e);
        }
        await sleep(50 * (i + 1));
    }
};

onMounted(() => {
    getContentRect();
});

onShow(() => {
    getContentRect();
});

onHide(() => {
    showMenu.value = false;
});

defineExpose({
    getContentRect
});
</script>

<style lang="scss" scoped>
.common-header-box {
    top: 0;
    left: 0;
    width: 100%;
    box-sizing: border-box;
    z-index: 500;
    background-color: #fff;
}

.common-header {
    // #ifndef MP-WEIXIN
    padding-top: var(--safe-top);
    // #endif

    &.no-safe-top {
        padding-top: 0;
    }
}

.common-box {
    background-color: #fff;
    padding: 0 29rpx;
}

.category-icon {
    image {
        width: 18rpx;
        height: 18rpx;
    }
}

.main-header {
    width: 100%;
    .main-header-box {
        width: 100%;
        padding: 11rpx 21rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 78rpx;

        .iconfont-h5 {
            color: #1a1a1a;
        }

        .box-left {
            flex: 1;
            column-gap: 8px;

            .menu-icon {
                image {
                    width: 43rpx;
                    height: 43rpx;
                }
                .icon-caidan {
                    font-size: 42rpx;
                    display: block;
                    // color: #000;
                }
            }

            .header-logo {
                image {
                    width: 153.9rpx;
                    height: fit-content;
                    display: block;
                    will-change: transform;
                }
            }
        }

        .box-right {
            .user-icon {
                padding-right: 25rpx;
                image {
                    width: 36rpx;
                    height: 39rpx;
                }

                .icon-xinjianwenbenwendang {
                    font-size: 39rpx;
                }
            }

            .cart-icon {
                padding-left: 25rpx;
                padding-right: 14rpx;
                position: relative;
                image {
                    width: 43rpx;
                    height: 43rpx;
                }

                .icon-gouwuche4 {
                    font-size: 42rpx;
                }

                .corner-mark {
                    position: absolute;
                    left: 43rpx;
                    top: -9rpx;
                    max-width: 65rpx;
                    height: 25rpx;
                    color: var(--main-text);
                    background: var(--main-bg);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    border-radius: 181rpx;
                    font-size: 18rpx;
                    padding: 0 5rpx;
                }
            }
        }
    }
}

.common-menu-box {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.698);
    display: flex;
    justify-content: flex-start;
    align-items: stretch;
    z-index: 2000;

    .left-menu-box {
        width: 90vw;
        height: 100%;
        position: relative;
        // row-gap: 8px;
        background-color: #f5f6fa;

        .placeholder {
            // width: 100%;
            // // #ifndef MP-WEIXIN
            // height: var(--safe-top);
            // // #endif
            background: #fff;
        }

        .close-icon {
            position: absolute;
            right: 21.7rpx;
            top: 21.7rpx;

            image {
                width: 36.2rpx;
                height: 36.2rpx;
            }
        }

        .left-menu-header {
            position: relative;
            .close-icon {
                position: absolute;
                right: 21.7rpx;
                top: 21.7rpx;
                image {
                    width: 36.2rpx;
                    height: 36.2rpx;
                }
            }
            .menu-header-content {
                padding-top: 72.4rpx;
                column-gap: 21.7rpx;
                padding-bottom: 29rpx;

                .profile-photo {
                    image {
                        width: 86.9rpx;
                        height: 86.9rpx;
                        border-radius: 50%;
                        overflow: hidden;
                        border-radius: 50%;
                        display: block;
                    }
                }

                .profile-info {
                    flex: 1;
                    color: #000;
                    font-weight: 700;
                    font-size: 29rpx;
                }

                .header-btn {
                    width: 144.8rpx;
                    height: 54.3rpx;
                    font-weight: 400;
                    padding: 0;

                    .header-btn-text {
                        font-size: 22rpx;
                    }
                }
            }
        }

        .category-box {
            margin-top: 16rpx;
            padding: 29rpx;

            .category-text {
                font-size: 29rpx;
                font-weight: 700;
            }
        }

        .page-menu {
            margin-top: 16rpx;
            .page-item {
                border-bottom: 1px solid #f0f1f2;
                padding: 29rpx 0;

                &:last-child {
                    border-bottom: none;
                }

                .item-left {
                    .left-text {
                        // color: #000;
                        font-size: 29rpx;
                    }
                }

                .item-right {
                    column-gap: 8px;

                    .right-text {
                        color: #888b94;
                        font-size: 25.3rpx;
                    }
                }
            }
        }
    }
}

.navbar-content {
    display: flex;
    align-items: center;
    width: 100%;
    padding: 8px 12px;
    position: relative;

    .left {
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1;
        .icon-houtui {
            font-size: 40rpx;
        }
    }
    .middle {
        display: flex;
        justify-content: center;
        align-items: center;
        color: #000;
        overflow: hidden;
        position: absolute;
        left: 0;
        right: 0;

        .title {
            font-size: 18px;
        }
    }

    .right {
        padding-right: 16rpx;
        z-index: 1;
    }
}
</style>

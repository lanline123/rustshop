<template>
    <view class="dec-my-wallet" :class="`dec-my-wallet--style${currentLayoutStyle}`" :style="cssVars">
        <view class="dec-my-wallet__container">
            <!-- Style 1: 原始样式（标题 + 进入钱包 + 三列数据） -->
            <template v-if="currentLayoutStyle === 1">
                <view class="dec-my-wallet__header">
                    <view class="dec-my-wallet__title">{{ $t("我的钱包") }}</view>
                    <view class="dec-my-wallet__link" @click="handleWalletClick">
                        <text>{{ $t("进入钱包") }}</text>
                        <view class="dec-my-wallet__arrow iconfont icon-xiangyou" />
                    </view>
                </view>
                <view class="dec-my-wallet__stats dec-my-wallet__stats--block">
                    <view class="dec-my-wallet__stat-item" @click="handleStatClick('balance')">
                        <view class="dec-my-wallet__stat-value">{{ formatMoney(walletStats.balance) }}</view>
                        <view class="dec-my-wallet__stat-label">{{ $t("余额") }}</view>
                    </view>
                    <view class="dec-my-wallet__stat-item" @click="handleStatClick('coupon')">
                        <view class="dec-my-wallet__stat-value">{{ walletStats.couponCount || 0 }}</view>
                        <view class="dec-my-wallet__stat-label">{{ $t("优惠券") }}</view>
                    </view>
                    <view class="dec-my-wallet__stat-item" @click="handleStatClick('point')">
                        <view class="dec-my-wallet__stat-value">{{ walletStats.points || 0 }}</view>
                        <view class="dec-my-wallet__stat-label">{{ $t(configStore.integralName || "积分") }}</view>
                    </view>
                </view>
            </template>
            <!-- Style 2: 当前样式（图标 + 竖分割线 + 三列数据） -->
            <template v-else>
                <view class="dec-my-wallet__icon-section" @click="handleWalletClick">
                    <image class="dec-my-wallet__icon" :src="walletIconUrl" mode="aspectFit" />
                    <text class="dec-my-wallet__icon-label">{{ $t("我的钱包") }}</text>
                </view>
                <view class="dec-my-wallet__divider" />
                <view class="dec-my-wallet__stats">
                    <view class="dec-my-wallet__stat" @click="handleStatClick('balance')">
                        <text class="dec-my-wallet__value">{{ formatMoney(walletStats.balance) }}</text>
                        <text class="dec-my-wallet__label">{{ $t("余额") }}</text>
                    </view>
                    <view class="dec-my-wallet__stat" @click="handleStatClick('coupon')">
                        <text class="dec-my-wallet__value">{{ walletStats.couponCount || 0 }}</text>
                        <text class="dec-my-wallet__label">{{ $t("优惠券") }}</text>
                    </view>
                    <view class="dec-my-wallet__stat" @click="handleStatClick('point')">
                        <text class="dec-my-wallet__value">{{ walletStats.points || 0 }}</text>
                        <text class="dec-my-wallet__label">{{ $t(configStore.integralName || "积分") }}</text>
                    </view>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";

import { redirect } from "@/utils";
import { imageFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";

const DEFAULT_WALLET_ICON = "https://oss.tigshop.com/official/img/pc/202605/1779873859zO64yVrNvp2y0lUsOz.png";

const STAT_URL_MAP: Record<string, string> = {
    balance: "/pages/user/account/index",
    coupon: "/pages/coupon/index?type=2",
    point: "/pages/user/point/detail"
};

interface MyWalletData extends ModuleDataWithFrame {
    layoutStyle?: number;
    walletIcon?: { picUrl?: string };
}

const userStore = useUserStore();
const configStore = useConfigStore();

const props = defineProps<{
    data: MyWalletData;
}>();

const userInfo = computed<Record<string, any>>(() => userStore.userInfo || {});
const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));

const currentLayoutStyle = computed(() => props.data?.layoutStyle ?? 1);

const walletIconUrl = computed(() => {
    const url = props.data?.walletIcon?.picUrl;
    return url ? imageFormat(url) : DEFAULT_WALLET_ICON;
});

const walletStats = computed(() => ({
    balance: userInfo.value.totalBalance || userInfo.value.balance || 0,
    couponCount: userInfo.value.coupon || 0,
    points: userInfo.value.points || 0
}));

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: currentLayoutStyle.value === 1 ? 16 : 12,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);

const handleWalletClick = () => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    redirect({ url: "/pages/user/account/index" });
};

const handleStatClick = (type: string) => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    if (STAT_URL_MAP[type]) {
        redirect({ url: STAT_URL_MAP[type] });
    }
};

const formatMoney = (value: number | string): string => {
    const num = typeof value === "string" ? parseFloat(value) : value;
    if (isNaN(num)) return "0.00";
    return num.toFixed(2);
};
</script>

<style lang="scss" scoped>
.dec-my-wallet {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 10rpx);
    padding-right: var(--dec-box-padding, 10rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, #ffffff);
        border-radius: var(--dec-border-radius, 24rpx);
        padding: var(--dec-inner-padding, 24rpx);
        box-sizing: border-box;
    }

    &--style2 &__container {
        display: flex;
        align-items: center;
    }

    /* ============ Style 1: 原始钱包样式 ============ */
    &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 40rpx;
    }

    &__title {
        font-size: 28rpx;
        font-weight: 600;
        color: #1a1a1a;
    }

    &__link {
        display: flex;
        align-items: center;
        font-size: 22rpx;
        color: #999;
    }

    &__arrow {
        font-size: 24rpx;
        margin-left: 4rpx;
        color: #888b94;
    }

    &__stats--block {
        display: flex;
        align-items: center;
        justify-content: space-around;
    }

    &__stat-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12rpx;
    }

    &__stat-value {
        font-size: 32rpx;
        font-weight: 700;
        color: #1a1a1a;
    }

    &__stat-label {
        font-size: 24rpx;
        color: #333;
    }

    /* ============ Style 2: 当前钱包样式 ============ */
    &__icon-section {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex-shrink: 0;
    }

    &__icon {
        width: 48rpx;
        height: 44rpx;
    }

    &__icon-label {
        margin-top: 8rpx;
        font-size: 24rpx;
        line-height: 40rpx;
        color: #666;
        white-space: nowrap;
    }

    &__divider {
        width: 20rpx;
        height: 108rpx;
        background-image: url("https://oss.tigshop.com/official/img/pc/202605/1779929210zBMvyOonKlS2ANuWkq.png");
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        margin: 0 16rpx;
        flex-shrink: 0;
    }

    &--style2 &__stats {
        display: flex;
        align-items: center;
        flex: 1;
        min-width: 0;
    }

    &__stat {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;
    }

    &__value {
        font-size: 32rpx;
        line-height: 48rpx;
        font-weight: 600;
        color: #333;
    }

    &__label {
        font-size: 24rpx;
        color: #666;
        line-height: 40rpx;
    }
}
</style>

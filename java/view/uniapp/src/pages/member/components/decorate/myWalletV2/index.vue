<template>
    <view class="dec-wallet-v2" :style="cssVars">
        <view class="dec-wallet-v2__container">
            <view class="dec-wallet-v2__grid">
                <view
                    v-for="(item, index) in displayItems"
                    :key="item.key"
                    class="dec-wallet-v2__cell"
                    :class="{ 'dec-wallet-v2__cell--border-bottom': index < 2, 'dec-wallet-v2__cell--border-right': index % 2 === 0 }"
                    @click="handleCellClick(item.key)"
                >
                    <view v-if="item.icon?.picUrl" class="dec-wallet-v2__icon">
                        <tig-image :src="imageFormat(item.icon.picUrl)" mode="aspectFit" />
                    </view>
                    <text class="dec-wallet-v2__label">{{ safeT(item.label) }}</text>
                    <text class="dec-wallet-v2__value">{{ getItemValue(item.key) }}</text>
                    <view class="dec-wallet-v2__arrow iconfont-h5 icon-a-gengduo20" />
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

import { redirect } from "@/utils";
import { imageFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";

interface WalletV2Item {
    key: string;
    label: string;
    icon?: { picUrl?: string };
}

interface MyWalletV2Data extends ModuleDataWithFrame {
    items?: WalletV2Item[];
}

const { t } = useI18n();
const safeT = (val: unknown, fallback = "") => {
    if (typeof val === "string") {
        const key = val.trim();
        if (!key) return fallback;
        return t(key);
    }
    return fallback;
};

const userStore = useUserStore();

const props = defineProps<{
    data: MyWalletV2Data;
}>();

const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));
const userInfo = computed<Record<string, any>>(() => userStore.userInfo || {});
const orderNum = computed<Record<string, any>>(() => userStore.orderNum || {});

const DEFAULT_ITEMS: WalletV2Item[] = [
    { key: "balance", label: "余额", icon: { picUrl: "" } },
    { key: "coupon", label: "优惠券", icon: { picUrl: "" } },
    { key: "collect", label: "商品收藏", icon: { picUrl: "" } },
    { key: "history", label: "浏览记录", icon: { picUrl: "" } }
];

const displayItems = computed(() => {
    const items = props.data?.items;
    if (!items || items.length === 0) return DEFAULT_ITEMS;
    return items;
});

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 12,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);

const VALUE_MAP: Record<string, () => string | number> = {
    balance: () => formatMoney(userInfo.value.totalBalance || userInfo.value.balance || 0),
    coupon: () => userInfo.value.coupon || 0,
    collect: () => orderNum.value.productCollect || 0,
    history: () => ""
};

const getItemValue = (key: string): string | number => {
    if (!isLoggedIn.value) return "";
    const fn = VALUE_MAP[key];
    return fn ? fn() : "";
};

const URL_MAP: Record<string, string> = {
    balance: "/pages/user/account/index",
    coupon: "/pages/coupon/index?type=2",
    collect: "/pages/user/collectProduct/index",
    history: "/pages/user/historyProduct/index"
};

const handleCellClick = (key: string) => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    if (URL_MAP[key]) {
        redirect({ url: URL_MAP[key] });
    }
};

const formatMoney = (value: number | string): string => {
    const num = typeof value === "string" ? parseFloat(value) : value;
    if (isNaN(num)) return "0.00";
    return num.toFixed(2);
};
</script>

<style lang="scss" scoped>
.dec-wallet-v2 {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 10rpx);
    padding-right: var(--dec-box-padding, 10rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, #ffffff);
        border-radius: var(--dec-border-radius, 24rpx);
        overflow: hidden;
    }

    &__grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        padding: var(--dec-inner-padding, 24rpx);
        box-sizing: border-box;
    }

    &__cell {
        display: flex;
        align-items: center;
        padding: 16rpx;
        position: relative;

        &--border-bottom {
            border-bottom: 2rpx solid #f2f3f5;
        }

        &--border-right {
            border-right: 2rpx solid #eee;
        }
    }

    &__icon {
        width: 40rpx;
        height: 40rpx;
        flex-shrink: 0;
        margin-right: 16rpx;
    }

    &__label {
        font-size: 24rpx;
        color: #666;
        line-height: 40rpx;
        flex-shrink: 0;
    }

    &__value {
        font-size: 24rpx;
        font-weight: 600;
        color: #333;
        line-height: 40rpx;
        flex: 1;
        text-align: right;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-left: 8rpx;
    }

    &__arrow {
        color: #999;
        flex-shrink: 0;
        margin-left: 6rpx;
        font-size: 20rpx;
        line-height: 20rpx;
    }
}
</style>

<template>
    <view class="dec-order-entry" :style="cssVars">
        <view class="dec-order-entry__container">
            <view v-if="showHeader" class="dec-order-entry__header">
                <view class="dec-order-entry__title">{{ safeT(data.title, $t("我的订单")) }}</view>
                <view v-if="data.showAllOrders !== false" class="dec-order-entry__more" @click="handleAllOrdersClick">
                    <text>{{ $t("全部订单") }}</text>
                    <view class="dec-order-entry__arrow iconfont-h5 icon-a-gengduo20" />
                </view>
            </view>

            <view class="dec-order-entry__list" :class="{ 'dec-order-entry__list--style1': currentLayoutStyle === 1 }">
                <template v-for="(item, index) in entryList" :key="index">
                    <!-- D1146：评价关闭时跳过评价入口，但不重排下标，图标 / 角标的下标映射保持不变 -->
                    <template v-if="isEntryVisible(item, index)">
                    <view v-if="currentLayoutStyle === 2 && index === entryList.length - 1" class="dec-order-entry__divider" />
                    <view class="dec-order-entry__item" @click="handleEntryClick(item, index)">
                        <view class="dec-order-entry__icon-wrapper">
                            <tig-image v-if="pickPic(item)" class="dec-order-entry__icon-img" :src="productImage(item)" mode="aspectFit" />
                            <view v-else class="dec-order-entry__icon-default iconfont-h5" :class="getDefaultIconClass(index)" />
                            <view v-if="getBadgeCount(index) > 0" class="dec-order-entry__badge" :style="badgeStyle">
                                {{ formatBadge(getBadgeCount(index)) }}
                            </view>
                        </view>
                        <view class="dec-order-entry__label">{{ safeT(item.picTitle) }}</view>
                    </view>
                    </template>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useI18n } from "vue-i18n";

import { redirect } from "@/utils";
import { pickPic, productImage, urlFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";

interface EntryItem {
    picUrl?: string;
    picTitle?: string;
    picLink?: { link?: string; [key: string]: unknown };
}

interface OrderEntryData extends ModuleDataWithFrame {
    title?: string;
    showTitle?: boolean;
    showAllOrders?: boolean;
    layoutStyle?: number;
    entryList?: EntryItem[];
    badgeBgColor?: string;
    badgeTextColor?: string;
}

const STYLE1_ORDER_LINKS = [
    "/pages/user/order/list?type=awaitPay",
    "/pages/user/order/list?type=awaitShipping",
    "/pages/user/order/list?type=awaitReceived",
    "/pages/user/order/list?type=awaitComment",
    "/pages/user/afterSale/list"
];

const STYLE2_ORDER_LINKS = [
    "/pages/user/order/list?type=awaitPay",
    "/pages/user/order/list?type=awaitShipping",
    "/pages/user/order/list?type=awaitReceived",
    "/pages/user/afterSale/list",
    "/pages/user/order/list"
];

const ICON_CLASSES = ["icon-daifukuan", "icon-daifahuo", "icon-daishouhuo", "icon-daipingjia", "icon-tuikuan"];

const STYLE1_BADGE_KEYS = ["awaitPay", "awaitShipping", "awaitReceived", "awaitComment", "awaitAftersalesCollect"] as const;
const STYLE2_BADGE_KEYS = ["awaitPay", "awaitShipping", "awaitReceived", "awaitAftersalesCollect", ""] as const;

const userStore = useUserStore();
const configStore = useConfigStore();

const { t } = useI18n();
const safeT = (val: unknown, fallback = "") => {
    if (typeof val === "string") {
        const key = val.trim();
        if (!key) return fallback;
        return t(key);
    }
    return fallback;
};

const props = defineProps<{
    data: OrderEntryData;
}>();

const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));
const orderNum = computed<Record<string, any>>(() => userStore.orderNum || {});

const currentLayoutStyle = computed(() => props.data?.layoutStyle ?? 1);

const showHeader = computed(() => {
    if (currentLayoutStyle.value === 2) return false;
    return props.data?.showTitle !== false;
});

const getDefaultBadgeBgColor = (layoutStyle?: number) => (layoutStyle === 1 ? "#4d9be4" : "#ff4d4f");
const badgeStyle = computed(() => ({
    backgroundColor: props.data?.badgeBgColor || getDefaultBadgeBgColor(currentLayoutStyle.value),
    color: props.data?.badgeTextColor || "#fff"
}));

const entryList = computed(() => {
    return props.data?.entryList || [];
});

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "#ffffff",
        boxRadius: 12,
        innerPadding: 16,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);

watch(
    isLoggedIn,
    (newVal) => {
        if (newVal) {
            userStore.getOrderNum();
        }
    },
    { immediate: true }
);

onMounted(() => {
    uni.$on("commentFinished", handleCommentFinished);
});

onBeforeUnmount(() => {
    uni.$off("commentFinished", handleCommentFinished);
});

function handleCommentFinished() {
    userStore.getOrderNum(true);
}

const handleAllOrdersClick = () => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    redirect({ url: "/pages/user/order/list" });
};

const handleEntryClick = (item: EntryItem, index: number) => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    const customLink = (item.picLink?.link as string | undefined)?.trim() || "";
    if (customLink) {
        redirect({ url: urlFormat(item.picLink as any) });
    } else {
        const links = currentLayoutStyle.value === 1 ? STYLE1_ORDER_LINKS : STYLE2_ORDER_LINKS;
        redirect({ url: links[index] || "/pages/user/order/list" });
    }
};

/**
 * D1146：评价关闭时隐藏指向评价的入口。
 * 按解析后的目标链接判断而非写死下标，装修自定义链接也能命中；
 * 装修链接经 urlFormat 解析出的是 await_comment 下划线形式，故两种写法都要匹配。
 */
const isEntryVisible = (item: EntryItem, index: number): boolean => {
    if (configStore.productCommentEnabled === 1) return true;
    const customLink = (item.picLink?.link as string | undefined)?.trim() || "";
    const links = currentLayoutStyle.value === 1 ? STYLE1_ORDER_LINKS : STYLE2_ORDER_LINKS;
    const target = customLink ? urlFormat(item.picLink as any) : links[index] || "";
    return !/type=await_?comment/i.test(target) && !target.includes("/pages/user/comment/");
};

const getBadgeCount = (index: number): number => {
    if (!isLoggedIn.value) return 0;
    const keys = currentLayoutStyle.value === 1 ? STYLE1_BADGE_KEYS : STYLE2_BADGE_KEYS;
    const key = keys[index];
    if (!key) return 0;
    return Number(orderNum.value[key] ?? 0);
};

const formatBadge = (count: number): string => {
    return count > 99 ? "99+" : String(count);
};

const getDefaultIconClass = (index: number): string => {
    return ICON_CLASSES[index] || ICON_CLASSES[0];
};
</script>

<style lang="scss" scoped>
.dec-order-entry {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 10rpx);
    padding-right: var(--dec-box-padding, 10rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, #ffffff);
        border-radius: var(--dec-border-radius, 24rpx);
        padding: var(--dec-inner-padding, 32rpx);
    }

    &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 40rpx;
    }

    &__title {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
    }

    &__more {
        display: flex;
        align-items: center;
        gap: 4rpx;
        font-size: 22rpx;
        color: #999;
    }

    &__arrow {
        font-size: 20rpx;
        color: #999;
    }

    &__list {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    &__divider {
        width: 20rpx;
        height: 108rpx;
        background-image: url("https://oss.tigshop.com/official/img/pc/202605/1779929210zBMvyOonKlS2ANuWkq.png");
        background-size: contain;
        background-repeat: no-repeat;
        background-position: center;
        transform: scaleX(-1);
        flex-shrink: 0;
        align-self: center;
        margin: 0 14rpx 0 24rpx;
    }

    &__item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    &__icon-wrapper {
        position: relative;
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    &__icon-img {
        width: 56rpx;
        height: 56rpx;
        margin-bottom: 4rpx;
    }

    &__list--style1 &__icon-img {
        width: 48rpx;
        height: 48rpx;
        margin-bottom: 8rpx;
    }

    &__icon-default {
        font-family: "iconfont";
        font-size: 48rpx;
        color: #333;
    }

    &__badge {
        position: absolute;
        top: 0;
        right: 0;
        transform: translate(90%, -50%);
        min-width: 32rpx;
        height: 32rpx;
        padding: 0 6rpx;
        font-size: 20rpx;
        font-weight: 500;
        color: #fff;
        border-radius: 18rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;

        &--red {
            background: #ff4d4f;
        }

        &--blue {
            background: #4d9be4;
        }
    }

    &__label {
        font-size: 24rpx;
        line-height: 40rpx;
        color: #666;
        text-align: center;
        white-space: nowrap;
    }
}
</style>

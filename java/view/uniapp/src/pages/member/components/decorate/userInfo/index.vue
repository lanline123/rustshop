<template>
    <view class="dec-user-info" :class="{ 'dec-user-info--has-bg': hasCustomBg }" :style="cssVars">
        <view class="dec-user-info__container">
            <view class="dec-user-info__header">
                <view class="dec-user-info__profile">
                    <template v-if="!isLoggedIn">
                        <view class="dec-user-info__avatar" @click="handleLogin">
                            <image class="dec-user-info__avatar-img" mode="aspectFill" :src="staticResource('common/empty-img-bg3.png')" />
                        </view>
                        <view class="dec-user-info__info" @click="handleLogin">
                            <view class="dec-user-info__name">{{ $t("登录/注册") }}</view>
                        </view>
                    </template>
                    <template v-else>
                        <view class="dec-user-info__avatar">
                            <tig-upload request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                                <tig-image class="dec-user-info__avatar-img" :src="imageFormat(userInfo.avatar || '')" mode="aspectFill" />
                            </tig-upload>
                        </view>
                        <view class="dec-user-info__info">
                            <view class="dec-user-info__name-row">
                                <template v-if="configStore.closeAuth === 1">
                                    <view v-if="userInfo.isCompanyAuth" class="dec-user-info__auth" @click="goPages('/pages/user/userCertification/index')">
                                        <image :src="staticResource('product/authenticated.svg')" mode="heightFix" />
                                    </view>
                                    <view
                                        v-else
                                        class="dec-user-info__auth dec-user-info__auth--unverified"
                                        @click="goPages('/pages/user/userCertification/index')"
                                    >
                                        <image :src="staticResource('product/unverified.png')" mode="heightFix" />
                                    </view>
                                </template>
                                <text class="dec-user-info__name">{{ userInfo.nickname || userInfo.username }}</text>
                            </view>
                            <view class="dec-user-info__level" @click="isPro() && goPages('/pages/user/levelCenter/index')">
                                <image v-if="isPro() && userInfo.rankLogo" class="dec-user-info__level-icon" :src="userInfo.rankLogo" mode="scaleToFill" />
                                <text class="dec-user-info__level-text">{{ safeT(userInfo.rankName) }}</text>
                            </view>
                        </view>
                    </template>
                </view>

                <view v-if="isLoggedIn" class="dec-user-info__shortcuts">
                    <view v-if="showSignIn" class="dec-user-info__shortcut-item" @click="handleShortcutClick('signIn')">
                        <tig-image v-if="data.signIn?.icon?.picUrl" :src="imageFormat(data.signIn.icon.picUrl)" class="dec-user-info__shortcut-icon" />
                        <view v-else class="dec-user-info__shortcut-iconfont iconfont icon-qiandao" />
                        <text class="dec-user-info__shortcut-text">{{ safeT(data.signIn?.title, $t("签到有礼")) }}</text>
                    </view>

                    <view
                        v-if="data.settings?.show !== false"
                        class="dec-user-info__shortcut-item dec-user-info__settings"
                        @click="handleShortcutClick('settings')"
                    >
                        <tig-image v-if="data.settings?.icon?.picUrl" :src="imageFormat(data.settings.icon.picUrl)" class="dec-user-info__shortcut-icon" />
                        <view v-else class="dec-user-info__shortcut-iconfont iconfont-h5 icon-shezhi" />
                    </view>
                </view>
            </view>

            <view v-if="isLoggedIn" class="dec-user-info__stats">
                <view class="dec-user-info__stat-item" @click="handleStatClick('collectProduct')">
                    <text class="dec-user-info__stat-label">{{ $t("商品收藏") }}</text>
                    <text class="dec-user-info__stat-value">{{ userStats.collectProduct || 0 }}</text>
                </view>
                <template v-if="showCollectShop">
                    <view class="dec-user-info__stat-divider" />
                    <view class="dec-user-info__stat-item" @click="handleStatClick('collectShop')">
                        <text class="dec-user-info__stat-label">{{ $t(isO2o() ? "门店关注" : "店铺收藏") }}</text>
                        <text class="dec-user-info__stat-value">{{ userStats.collectShop || 0 }}</text>
                    </view>
                </template>
                <template v-if="configStore.productCommentEnabled === 1">
                    <view class="dec-user-info__stat-divider" />
                    <view class="dec-user-info__stat-item" @click="handleStatClick('awaitComment')">
                        <text class="dec-user-info__stat-label">{{ $t("待评价") }}</text>
                        <text class="dec-user-info__stat-value">{{ userStats.awaitComment || 0 }}</text>
                    </view>
                </template>
                <view class="dec-user-info__stat-divider" />
                <view class="dec-user-info__stat-item" @click="handleStatClick('history')">
                    <text class="dec-user-info__stat-label">{{ $t("浏览记录") }}</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useI18n } from "vue-i18n";

import { redirect, staticResource, isPro, isO2o, isMerchant } from "@/utils";
import { imageFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, useFrame, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const safeT = (val: unknown, fallback = "") => {
    if (typeof val === "string") {
        const key = val.trim();
        if (!key) return fallback;
        return t(key);
    }
    return fallback;
};

interface ShortcutItem {
    show?: boolean;
    icon?: { picUrl?: string };
    title?: string;
}

interface GradientConfig {
    colorA?: string;
    colorB?: string;
}

interface UserInfoData extends ModuleDataWithFrame {
    signIn?: ShortcutItem;
    settings?: ShortcutItem;
    gradient?: GradientConfig;
}

const userStore = useUserStore();
const configStore = useConfigStore();

const props = defineProps<{
    data: UserInfoData;
}>();

const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));
const userInfo = computed<Record<string, any>>(() => userStore.userInfo || {});

const userStats = computed(() => {
    const num = (userStore.orderNum || {}) as Record<string, any>;
    return {
        collectProduct: num.productCollect || 0,
        collectShop: num.shopCollect || 0,
        awaitComment: num.awaitComment || 0
    };
});

const showCollectShop = computed(() => isMerchant() || isO2o());
const showSignIn = computed(() => {
    if (props.data?.signIn?.show === false) return false;
    return Boolean(userInfo.value?.showSign);
});

const FRAME_DEFAULTS = {
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 16,
    boxPadding: 0,
    boxPaddingTop: 0,
    boxPaddingBottom: 0
};

const DEFAULT_GRADIENT = { colorA: "#fcf6fc", colorB: "#f3f3fe" };

const cssVars = computed(() => {
    const base = useFrameVars(props.data, FRAME_DEFAULTS);
    const gradient = props.data?.gradient || {};
    return {
        ...base,
        "--dec-gradient-a": gradient.colorA || DEFAULT_GRADIENT.colorA,
        "--dec-gradient-b": gradient.colorB || DEFAULT_GRADIENT.colorB
    };
});

const frame = computed(() => useFrame(props.data, FRAME_DEFAULTS));
const hasCustomBg = computed(() => Boolean(frame.value.backgroundColor));

watch(
    isLoggedIn,
    (newVal) => {
        if (newVal) {
            userStore.getOrderNum();
        }
    },
    { immediate: true }
);

const handleShortcutClick = (kind: "signIn" | "settings") => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    if (kind === "signIn") {
        redirect({ url: "/pages/user/sign/index" });
        return;
    }
    if (kind === "settings") {
        redirect({ url: "/pages/user/profile/index" });
    }
};

const handleStatClick = (type: string) => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    const urlMap: Record<string, string> = {
        collectProduct: "/pages/user/collectProduct/index",
        collectShop: "/pages/user/collectionShop/index",
        awaitComment: "/pages/user/comment/list",
        history: "/pages/user/historyProduct/index"
    };
    if (urlMap[type]) {
        redirect({ url: urlMap[type] });
    }
};

const goPages = (url: string) => {
    uni.navigateTo({ url });
};
</script>

<style lang="scss" scoped>
.dec-user-info {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 0);
    padding-right: var(--dec-box-padding, 0);
    padding-top: var(--dec-box-padding-top, 0);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background: linear-gradient(90deg, var(--dec-gradient-a), var(--dec-gradient-b));
        border-radius: var(--dec-border-radius, 0);
        overflow: hidden;
        padding: var(--dec-inner-padding, 32rpx);
    }

    &--has-bg .dec-user-info__container {
        background: var(--dec-bg-color);
    }

    &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 30rpx;
    }

    &__profile {
        display: flex;
        align-items: center;
    }

    &__avatar {
        display: block;
        width: 110rpx;
        height: 110rpx;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 24rpx;
        flex-shrink: 0;
        box-sizing: border-box;
        border: 1rpx solid #f5f5f5;
        background-color: #fff;
    }

    &__avatar-img {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 50%;

        :deep(.base-image-box),
        :deep(.base-image) {
            width: 100%;
            height: 100%;
            border-radius: 50%;
        }
    }

    &__info {
        display: flex;
        flex-direction: column;
        justify-content: center;
        height: 110rpx;
    }

    &__name-row {
        display: flex;
        align-items: center;
        column-gap: 10rpx;
    }

    &__name {
        font-size: 28rpx;
        color: #1a1a1a;
        font-weight: 500;
    }

    &__auth {
        margin-right: 10rpx;
        display: flex;
        align-items: center;
        height: 36rpx;
        flex-shrink: 0;
        position: relative;

        image {
            height: 36rpx;
            display: block;
        }
    }

    &__level {
        display: flex;
        align-items: center;
        height: 32rpx;
        margin-top: 20rpx;
        color: #333;
        font-size: 20rpx;
        border-radius: 30rpx;
        line-height: 32rpx;
    }

    &__level-icon {
        margin-right: 5rpx;
        width: 30rpx;
        height: 22rpx;
    }

    &__level-text {
        font-size: 20rpx;
        color: #333;
    }

    &__shortcuts {
        display: flex;
        align-items: center;
        gap: 30rpx;
        align-self: flex-start;
        margin-top: 8rpx;
    }

    &__shortcut-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        color: #333;
        font-size: 24rpx;
    }

    &__shortcut-icon {
        width: 32rpx;
        height: 32rpx;
    }

    &__shortcut-iconfont {
        font-size: 28rpx;
        color: #333;
    }

    &__settings {
        padding: 8rpx;
        margin-right: -8rpx;
        color: #666;
    }

    &__stats {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    &__stat-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8rpx;
        font-size: 24rpx;
        color: #333;
    }

    &__stat-label {
        color: #333;
    }

    &__stat-value {
        font-weight: 500;
        color: #333;
    }

    &__stat-divider {
        width: 2rpx;
        height: 26rpx;
        background: #dbd1dc;
        margin: 0 8rpx;
    }
}
.dec-user-info__shortcut-text {
    white-space: nowrap;
}
</style>

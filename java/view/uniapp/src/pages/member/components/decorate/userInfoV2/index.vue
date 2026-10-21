<template>
    <view class="dec-user-info-v2" :style="cssVars">
        <view class="dec-user-info-v2__container">
            <view class="dec-user-info-v2__profile">
                <template v-if="!isLoggedIn">
                    <view class="dec-user-info-v2__avatar" @click="handleLogin">
                        <image class="dec-user-info-v2__avatar-img" mode="aspectFill" :src="staticResource('common/default-user.png')" />
                    </view>
                    <view class="dec-user-info-v2__info" @click="handleLogin">
                        <view class="dec-user-info-v2__name">{{ $t("登录") }}</view>
                        <view class="dec-user-info-v2__desc">{{ $t("为了获得最佳体验") }}</view>
                    </view>
                </template>
                <template v-else>
                    <view class="dec-user-info-v2__avatar">
                        <tig-upload request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                            <tig-image class="dec-user-info-v2__avatar-img" :src="imageFormat(userInfo.avatar)" mode="aspectFill" />
                        </tig-upload>
                    </view>
                    <view class="dec-user-info-v2__info">
                        <view class="dec-user-info-v2__name">{{ userInfo.nickname || userInfo.username }}</view>
                        <view v-if="userInfo.rankName" class="dec-user-info-v2__level" @click="handleNavigate('/pages/user/levelCenter/index')">
                            <image v-if="userInfo.rankLogo" class="dec-user-info-v2__level-icon" :src="userInfo.rankLogo" mode="scaleToFill" />
                            <text class="dec-user-info-v2__level-text">{{ safeT(userInfo.rankName) }}</text>
                        </view>
                    </view>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

import { staticResource } from "@/utils";
import { imageFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";

const props = defineProps<{
    data: ModuleDataWithFrame;
}>();

const userStore = useUserStore();

const { t } = useI18n();
const safeT = (val: unknown, fallback = "") => {
    if (typeof val === "string") {
        const key = val.trim();
        if (!key) return fallback;
        return t(key);
    }
    return fallback;
};

const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));
const userInfo = computed<Record<string, any>>(() => userStore.userInfo || {});

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: 16,
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0
    })
);

const handleNavigate = (url: string) => {
    uni.navigateTo({ url });
};
</script>

<style lang="scss" scoped>
.dec-user-info-v2 {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 0);
    padding-right: var(--dec-box-padding, 0);
    padding-top: var(--dec-box-padding-top, 0);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, #ffffff);
        border-radius: var(--dec-border-radius, 0);
        overflow: hidden;
        padding: var(--dec-inner-padding, 32rpx);
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
        background-color: #fff;
        box-sizing: border-box;
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

    &__name {
        font-size: 32rpx;
        color: #1a1a1a;
        font-weight: 500;
    }

    &__desc {
        margin-top: 10rpx;
        font-size: 23rpx;
        color: #1a1a1a;
    }

    &__level {
        display: flex;
        align-items: center;
        height: 32rpx;
        margin-top: 10rpx;
    }

    &__level-icon {
        width: 30rpx;
        height: 22rpx;
        margin-right: 5rpx;
    }

    &__level-text {
        font-size: 20rpx;
        color: #333;
    }
}
</style>

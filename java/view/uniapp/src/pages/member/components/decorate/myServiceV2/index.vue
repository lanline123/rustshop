<template>
    <view v-if="serviceList.length > 0" class="dec-my-service" :style="cssVars">
        <view class="dec-my-service__container">
            <view class="dec-my-service__title">{{ moduleTitle }}</view>
            <view class="dec-my-service__list" :class="{ 'is-grid': layoutStyle == 2, 'is-column': layoutStyle == 1 }">
                <view v-for="(item, index) in serviceList" :key="index" class="dec-my-service__item" @click="handleClick(item)">
                    <view class="dec-my-service__icon" :style="iconStyle">
                        <tig-image v-if="pickPic(item)" lazy-load :src="productImage(item)" />
                    </view>
                    <view class="dec-my-service__label">
                        {{ safeT(item.picTitle) }}
                        <view v-if="layoutStyle == 1" class="dec-my-service__arrow iconfont icon-xiangyou" />
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

import { redirect, isB2B, isOverseas, isPro } from "@/utils";
import { pickPic, productImage, urlFormat } from "@/utils/format";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";

interface ServiceItem {
    picTitle?: string;
    picUrl?: string;
    picThumb?: string;
    picLink?: { link?: string; [key: string]: unknown } | string;
}

interface MyServiceData extends ModuleDataWithFrame {
    moduleTitle?: string;
    layoutStyle?: number;
    iconRadius?: number;
    serviceList?: ServiceItem[];
}

const props = defineProps<{
    data: MyServiceData;
}>();

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
const configStore = useConfigStore();

const layoutStyle = computed(() => props.data?.layoutStyle || 1);

const moduleTitle = computed(() => {
    const title = props.data?.moduleTitle;
    return title ? safeT(title) : t("我的服务");
});

const iconStyle = computed(() => ({
    borderRadius: `${props.data?.iconRadius ?? 0}%`,
    overflow: "hidden"
}));

const serviceList = computed<ServiceItem[]>(() => {
    let list = props.data?.serviceList || [];
    if (isOverseas()) {
        list = list.filter((item) => safeT(item.picTitle) !== t("发票管理"));
    } else if (configStore.canInvoice !== 1) {
        list = list.filter((item) => safeT(item.picTitle) !== t("发票管理"));
    } else if (!list.some((item) => safeT(item.picTitle) === t("发票管理"))) {
        list = [
            ...list,
            {
                picTitle: "发票管理",
                picUrl: "",
                picLink: "/pages/user/invoiceManagement/index"
            }
        ];
    }
    if (!isPro() || Object.keys(userStore.userInfo).length === 0 || !userStore.userInfo.salesman) {
        list = list.filter((item) => safeT(item.picTitle) !== t("分销员中心"));
    }
    if (!isB2B() || configStore.closeAuth === 0) {
        list = list.filter((item) => safeT(item.picTitle) !== t("实名认证"));
    }
    return list;
});

const cssVars = computed(() =>
    useFrameVars(props.data, {
        backgroundColor: "#ffffff",
        boxRadius: 10,
        innerPadding: 15,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    })
);

const handleClick = (item: ServiceItem) => {
    if (item.picLink) {
        redirect({ url: urlFormat(item.picLink as any) });
    }
};
</script>

<style lang="scss" scoped>
.dec-my-service {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 20rpx);
    padding-right: var(--dec-box-padding, 20rpx);
    padding-top: var(--dec-box-padding-top, 20rpx);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__container {
        background-color: var(--dec-bg-color, #ffffff);
        border-radius: var(--dec-border-radius, 20rpx);
        padding: var(--dec-inner-padding, 30rpx);
    }

    &__title {
        display: flex;
        align-items: center;
        box-sizing: border-box;
        height: 60rpx;
        font-size: 26rpx;
        color: #1a1a1a;
        font-weight: bold;
    }

    &__list {
        padding-top: 20rpx;
    }

    &__list.is-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        grid-gap: 20rpx;
        font-size: 24rpx;

        .dec-my-service__item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            width: 100%;
        }

        .dec-my-service__icon {
            width: 60rpx;
            height: 60rpx;
            margin-bottom: 12rpx;
        }

        .dec-my-service__label {
            width: 100%;
            text-align: center;
            text-transform: capitalize;
            height: fit-content !important;
        }
    }

    &__list.is-column {
        display: block;
        font-size: 26rpx;
        width: 100%;

        .dec-my-service__item {
            width: 100%;
            display: flex;
            align-items: center;
            height: 96rpx;
            color: #1a1a1a;
            border-bottom: 1px solid #e5e7eb;
            position: relative;

            &:last-child {
                border-bottom: none;
            }
        }

        .dec-my-service__icon {
            width: 36rpx;
            height: 36rpx;
            margin-right: 16rpx;
        }

        .dec-my-service__label {
            display: flex;
            align-items: center;
            flex: 1;
            position: relative;
            font-weight: 500;
            font-size: 26rpx;
            color: #1a1a1a;
            text-transform: capitalize;
        }

        .dec-my-service__arrow {
            margin-left: auto;
            color: #888b94;
            font-size: 24rpx;
        }
    }
}
</style>

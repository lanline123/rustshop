<template>
    <view class="layout" :class="{ 'not-zh': !isZh && isOverseas() }" :style="{ ...cssVariable }">
        <template v-if="needSafeTop">
            <view class="safe-top-box" />
        </template>

        <template v-if="title">
            <tig-navbar :title="title" :border-bottom="borderBottom" :background="background" :color="color" :custom-back="customBack" @back="emit('back')">
                <template #right>
                    <slot name="navbar-right" />
                </template>
            </tig-navbar>
        </template>

        <slot name="top" />

        <view class="layout-content" :class="{ overflow }">
            <slot />
        </view>

        <template v-if="tabbarStore.currentActiveValue === -1 && needSafeBottom">
            <view class="layout-safe-bottom" />
        </template>

        <view v-show="tabbarStore.currentActiveValue > -1">
            <tig-tabbar />
        </view>

        <template v-if="(configStore.XClientType === 'miniProgram' || configStore.XClientType === 'wechat') && configStore.openWechatRegister === 1">
            <tig-wechart-login @login-success="$emit('loginSuccess')" />
        </template>

        <!-- #ifdef APP-PLUS || APP-HARMONY -->
        <appUpdatePop />
        <!-- #endif -->

        <tig-statistic-log ref="statisticLogRef" :immediate-log="immediateLog" :product-id="productId" :shop-id="shopId" :shop-category-id="shopCategoryId" />
    </view>
</template>

<script setup lang="ts">
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif

import { useTabbarStore } from "@/store/tabbar";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";
import { computed, ref, watch } from "vue";
import { useSafeAreaInsets, useSaveTopBoxHeight } from "@/hooks";
import { useI18nStore } from "@/store/i18n";
import { isOverseas } from "@/utils";
import appUpdatePop from "@/components/appUpdatePop/index.vue";

const props = defineProps({
    needSafeTop: {
        type: Boolean,
        default: false
    },
    needSafeBottom: {
        type: Boolean,
        default: true
    },
    safeTopBgColor: {
        type: String,
        default: "#fff"
    },
    productId: {
        type: [Number, String],
        default: ""
    },
    shopId: {
        type: [Number, String],
        default: ""
    },
    shopCategoryId: {
        type: [Number, String],
        default: ""
    },
    immediateLog: {
        type: Boolean,
        default: true
    },
    title: {
        type: String,
        default: undefined
    },
    customBack: {
        type: Boolean,
        default: false
    },
    borderBottom: {
        type: Boolean,
        default: false
    },
    background: {
        type: String,
        default: "#fff"
    },
    color: {
        type: String,
        default: "#000"
    },
    overflow: {
        type: Boolean,
        default: true
    }
});

const emit = defineEmits(["loginSuccess", "back"]);

const tabbarStore = useTabbarStore();
const configStore = useConfigStore();
const themeStore = useThemeStore();
const i18nStore = useI18nStore();
const isZh = computed(() => {
    if (isOverseas()) {
        return i18nStore.langCode ? i18nStore.langCode.includes("zh") : uni.getLocale().includes("zh");
    } else {
        return true;
    }
});

// #ifdef H5
watch(
    () => configStore.baseInfo.shopTitle,
    (shopTitle) => {
        if (props.title === undefined && shopTitle) {
            configStore.setH5Title();
        }
    },
    {
        immediate: true
    }
);
// #endif

const { safeBottom } = useSafeAreaInsets();
const { height } = useSaveTopBoxHeight();

const cssVariable = computed(() => {
    return {
        ...themeStore.themeStyle,
        "--tabbar-height": tabbarStore.tabbarHeight,
        "--safe-bottom": `${safeBottom.value}px`,
        "--safe-top": `${height.value}px`,
        "--nav-height": `${configStore.navHeight}rpx`
    };
});

const statisticLogRef = ref();
const handleLog = () => {
    statisticLogRef.value?.__commonLog();
};

defineExpose({
    handleLog,
    isZh
});
</script>

<style lang="scss" scoped>
.layout {
    max-width: 100%;

    .layout-content {
        max-width: 100%;

        &.overflow {
            overflow: hidden;
        }
    }

    .layout-safe-bottom {
        width: 100%;
        height: 0;
        padding-top: var(--safe-bottom);
    }
}
.safe-top-box {
    height: var(--safe-top);
    background: v-bind("props.safeTopBgColor");
}
</style>

<template>
    <page-meta page-style="background-color: #fff" />
    <tig-layout :shop-id="shopId">
        <view v-if="!loading" class="shop-info-page">
            <view class="identity" :style="identityStyle">
                <view class="identity-back" :style="backStyle" @click="handleBack">
                    <text class="iconfont icon-houtui" />
                </view>
                <!-- image-guideline: allow-low-res reason="店铺详情接口仅返回 shopLogo，无 picLarge 变体" -->
                <image class="identity-logo" :src="imageFormat(shopInfo.shopLogo || '')" mode="aspectFit" />
                <view class="identity-meta">
                    <view class="identity-name line1">{{ shopInfo.shopTitle || $t("店铺名称") }}</view>
                    <view class="identity-rate">
                        <uni-rate :value="displayStar" :size="12" :readonly="true" :allow-half="true" color="#e5e5e5" :active-color="generalColor" />
                        <text class="identity-score">{{ formatPadZero(totalScore) }}</text>
                        <template v-if="fanCount">
                            <view class="identity-divider" />
                            <text class="identity-fans">{{ fanCount }}{{ $t("粉丝") }}</text>
                        </template>
                    </view>
                </view>
            </view>

            <view class="page-body">
                <view class="section-title">{{ $t("店铺真实体验分") }}</view>
                <view class="score-list">
                    <view class="score-item">
                        <text class="score-label score-label--total">{{ $t("店铺总分") }}</text>
                        <view class="score-row score-row--total">
                            <text class="score-num is-lg">{{ formatPadZero(totalScore) }}</text>
                        </view>
                    </view>
                    <view v-for="item in experienceMetrics" :key="item.key" class="score-item">
                        <text class="score-label">{{ $t(item.label) }}</text>
                        <view class="score-row">
                            <view class="score-bar">
                                <view class="score-fill" :style="{ width: scorePercent(item.score), background: generalColor }" />
                            </view>
                            <text class="score-num">{{ formatPadZero(item.score) }}</text>
                        </view>
                    </view>
                </view>

                <!-- 基础信息只展示资质证照：有执照快照才出现，入口用示例缩略图，点击进公示页看原图 -->
                <view v-if="licensePic" class="section">
                    <view class="section-title">{{ $t("基础信息") }}</view>
                    <view class="sub-title">{{ $t("资质证照") }}</view>
                    <view class="license-card" @click="openLicensePage">
                        <view class="license-text">
                            <view class="license-title">{{ $t("资质证照") }}</view>
                            <view class="license-link">
                                {{ $t("查看商家资质信息") }}
                                <text class="iconfont icon-xiangyou" />
                            </view>
                        </view>
                        <!-- image-guideline: allow-low-res reason="资质入口使用本地示例图，真实执照在公示页展示" -->
                        <image class="license-img" src="@/static/images/shop/license-thumb.png" mode="widthFix" />
                    </view>
                </view>
            </view>
        </view>
        <tig-loadingpage v-model="loading" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * @description 店铺信息页
 * - 身份区直接替换标题栏：处理顶部安全区，微信小程序避开胶囊
 * - 白底；体验分每条独立、带间隔
 * - 基础信息只展示资质证照；有 businessLicensePic 才出现，入口用示例缩略图
 */
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { getShopDetail } from "@/api/shop/shop";
import { imageFormat, formatPadZero } from "@/utils/format";
import { redirect, getPx } from "@/utils";
import { useConfigStore } from "@/store/config";
import { useThemeStore } from "@/store/theme";
import type { ShopDetailItem } from "@/types/shop/shop";

const configStore = useConfigStore();
const themeStore = useThemeStore();
const shopId = ref(0);
const shopInfo = ref<Partial<ShopDetailItem>>({});
const loading = ref(true);
const generalColor = computed(() => themeStore.themeStyle["--general"] || "#ff4444");

/** 顶栏贴齐胶囊：上内边距用胶囊 top，右侧留出胶囊宽度 */
const identityStyle = computed(() => {
    const menu = configStore.menuButtonInfo;
    const padTop = menu.top || configStore.safeAreaInsets.top || 8;
    const padRight = menu.width ? menu.width + 8 : 12;
    return {
        paddingTop: `${padTop}px`,
        paddingRight: `${padRight}px`
    };
});

const backStyle = computed(() => {
    const size = configStore.menuButtonInfo.height ? configStore.menuButtonInfo.height + 4 : (getPx("64rpx") as number);
    return {
        width: `${size}px`,
        height: `${size}px`
    };
});

/** 前台只认非空执照快照 */
const licensePic = computed(() => {
    const pic = shopInfo.value.businessLicensePic;
    return typeof pic === "string" && pic.trim() ? pic : "";
});

const fanCount = computed(() => {
    const count = Number(shopInfo.value.collectCount);
    return count > 0 ? count : 0;
});

/** 接口无分时按门店列表惯例展示 5 分 */
const normalizeScore = (value: unknown) => {
    const score = Number(value);
    return score > 0 ? Math.min(score, 5) : 5;
};

const totalScore = computed(() => normalizeScore(shopInfo.value.shopStar || shopInfo.value.productScore));
const displayStar = computed(() => Number(formatPadZero(totalScore.value)));

const experienceMetrics = computed(() => [
    { key: "quality", label: "宝贝质量", score: normalizeScore(shopInfo.value.productScore || shopInfo.value.shopStar) },
    { key: "logistics", label: "物流速度", score: normalizeScore(shopInfo.value.shopStar) },
    { key: "service", label: "服务保障", score: normalizeScore(shopInfo.value.shopStar) }
]);

const scorePercent = (score: number) => `${Math.round((normalizeScore(score) / 5) * 100)}%`;

/**
 * @description 拉取店铺详情
 */
const loadShopInfo = async () => {
    if (!shopId.value) {
        loading.value = false;
        return;
    }
    loading.value = true;
    try {
        shopInfo.value = await getShopDetail(shopId.value);
    } catch (error) {
        console.error(error);
    } finally {
        loading.value = false;
    }
};

/**
 * @description 进入营业执照公示页
 */
const openLicensePage = () => {
    if (!shopId.value) return;
    redirect({ url: `/pages/shop/license?shopId=${shopId.value}` });
};

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

onLoad((options) => {
    if (options?.shopId) {
        shopId.value = Number(options.shopId);
        loadShopInfo();
    } else {
        loading.value = false;
    }
});

onShow(() => {
    configStore.getMenuButtonInfo();
    configStore.getSystemInfo();
});
</script>

<style lang="scss" scoped>
.shop-info-page {
    min-height: 100vh;
    background: #fff;
    overflow-x: hidden;
    box-sizing: border-box;
}

.identity {
    display: flex;
    align-items: flex-start;
    column-gap: 16rpx;
    padding-left: 8rpx;
    padding-bottom: 24rpx;
    background: #fff;
}

.identity-back {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .icon-houtui {
        font-size: 40rpx;
        color: #1a1a1a;
    }
}

.identity-logo {
    width: 64rpx;
    height: 64rpx;
    margin-top: 4rpx;
    border-radius: 8rpx;
    background: #f7f7f7;
    flex-shrink: 0;
}

.identity-meta {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    padding-top: 2rpx;
}

.identity-name {
    font-size: 30rpx;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 42rpx;
}

.identity-rate {
    display: flex;
    align-items: center;
    column-gap: 8rpx;
    margin-top: 6rpx;
}

.identity-score {
    font-size: 22rpx;
    font-weight: 600;
    color: var(--general);
    line-height: 1;
}

.identity-divider {
    width: 2rpx;
    height: 18rpx;
    background: #d7d7d7;
    flex-shrink: 0;
    margin: 0 1rpx 0 4rpx;
}

.identity-fans {
    font-size: 22rpx;
    color: #999;
    line-height: 1;
}

.identity-desc {
    margin-top: 8rpx;
    font-size: 22rpx;
    color: #999;
    line-height: 32rpx;
}

.page-body {
    padding: 8rpx 24rpx 48rpx;
}

.section {
    margin-top: 40rpx;
}

.section-title {
    margin-bottom: 20rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #1a1a1a;
    line-height: 42rpx;
}

.sub-title {
    margin-bottom: 16rpx;
    font-size: 26rpx;
    font-weight: 500;
    color: #666;
    line-height: 36rpx;
}

.score-list {
    display: flex;
    flex-direction: column;
    row-gap: 10rpx;
    .score-item {
        row-gap: 0rpx;
    }
}

.score-item {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    padding: 20rpx 24rpx;
    background: #f8f9fb;
    border-radius: 8rpx;
    box-sizing: border-box;
}

.score-label {
    font-size: 26rpx;
    color: #333;
    line-height: 36rpx;
    font-weight: 600;
    &--total {
        margin-bottom: 12rpx;
    }
}

.score-row {
    display: flex;
    align-items: baseline;
    column-gap: 16rpx;

    &--total {
        column-gap: 4rpx;
    }
}

.score-bar {
    flex: 1;
    min-width: 0;
    height: 8rpx;
    overflow: hidden;
    background: #ffe8d6;
    border-radius: 8rpx;
}

.score-fill {
    height: 100%;
    border-radius: 8rpx;
}

.score-num {
    flex-shrink: 0;
    font-size: 30rpx;
    line-height: 1;
    font-weight: 400;
    color: #1a1a1a;
}

.score-num.is-lg {
    font-size: 44rpx;
    line-height: 1;
    font-weight: 700;
}

.license-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    column-gap: 24rpx;
    padding: 24rpx;
    background: #f8f9fb;
    border-radius: 8rpx;
}

.license-text {
    flex: 1;
    min-width: 0;
}

.license-title {
    font-size: 26rpx;
    font-weight: 500;
    color: #1a1a1a;
    line-height: 40rpx;
}

.license-link {
    display: inline-flex;
    align-items: center;
    margin-top: 8rpx;
    font-size: 24rpx;
    color: #666;
    line-height: 34rpx;

    .icon-xiangyou {
        margin-left: 4rpx;
        font-size: 22rpx;
        color: #666;
    }
}

.license-img {
    flex-shrink: 0;
    width: 160rpx;
    height: auto;
    max-height: 100rpx;
}
</style>

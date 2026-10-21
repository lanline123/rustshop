<template>
    <view class="dec-user-info" :class="layoutClass" :style="cssVars">
        <!-- ===================== Style 1: Arc Bottom ===================== -->
        <template v-if="layoutStyle === 1">
            <view class="dec-user-info__wrap dec-user-info__wrap--style1" :style="style1WrapStyle">
                <view class="dec-user-info__container" :style="containerPaddingStyle">
                    <view class="dec-user-info__header">
                        <view class="dec-user-info__profile">
                            <template v-if="!isLoggedIn">
                                <view class="dec-user-info__avatar dec-user-info__avatar--bordered" @click="handleLogin">
                                    <image class="dec-user-info__avatar-img" mode="aspectFill" :src="staticResource('common/empty-img-bg3.png')" />
                                </view>
                                <view class="dec-user-info__info" @click="handleLogin">
                                    <view class="dec-user-info__name" :style="{ color: resolvedTextColors.nameColor }">{{ $t("登录/注册") }}</view>
                                </view>
                            </template>
                            <template v-else>
                                <view class="dec-user-info__avatar dec-user-info__avatar--bordered">
                                    <tig-upload request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                                        <tig-image class="dec-user-info__avatar-img" :src="imageFormat(userInfo.avatar || '')" mode="aspectFill" />
                                    </tig-upload>
                                </view>
                                <view class="dec-user-info__info">
                                    <view class="dec-user-info__name-row">
                                        <view class="dec-user-info__name-group">
                                            <template v-if="configStore.closeAuth === 1">
                                                <view
                                                    v-if="userInfo.isCompanyAuth"
                                                    class="dec-user-info__auth"
                                                    @click="goPages('/pages/user/userCertification/index')"
                                                >
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
                                            <text class="dec-user-info__name" :style="{ color: resolvedTextColors.nameColor }">{{
                                                userInfo.nickname || userInfo.username
                                            }}</text>
                                        </view>
                                    </view>
                                    <view
                                        class="dec-user-info__sub"
                                        :style="{ color: resolvedTextColors.subColor }"
                                        @click="goPages('/pages/user/profile/index')"
                                    >
                                        <text>{{ $t("个人资料") }}</text>
                                        <view class="iconfont-h5 icon-a-gengduo20" style="font-size: 24rpx" />
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>

                    <view v-if="showStats" class="dec-user-info__stats">
                        <view v-for="item in visibleStats" :key="item.key" class="dec-user-info__stat-item" @click="handleStatClick(item.key)">
                            <text class="dec-user-info__stat-value" :style="{ color: resolvedTextColors.statValueColor }">{{ getStatValue(item.key) }}</text>
                            <text class="dec-user-info__stat-label" :style="{ color: resolvedTextColors.statLabelColor }">{{ safeT(item.label) }}</text>
                        </view>
                    </view>

                    <view v-if="isLoggedIn && showMemberBar" class="dec-user-info__member-bar dec-user-info__member-bar--level" :style="memberBarStyle">
                        <view class="dec-user-info__member-bar-info">
                            <text class="dec-user-info__member-bar-name">{{ memberBarLevelName }}</text>
                            <view class="dec-user-info__member-bar-divider" />
                            <text class="dec-user-info__member-bar-upgrade">{{ memberBarDescription }}</text>
                        </view>
                        <view class="dec-user-info__member-bar-btn" @click="handleMemberBarClick">
                            <text>{{ memberBarButtonText }}</text>
                            <view class="iconfont-h5 icon-a-gengduo20" style="font-size: 12rpx" />
                        </view>
                    </view>
                    <view v-else class="dec-user-info__member-bar-placeholder" />
                </view>
            </view>
            <view v-if="showBottomArc" class="dec-user-info__arc" :style="arcStyle">
                <image class="dec-user-info__arc-img" :src="arcDataUrl" mode="scaleToFill" />
            </view>
        </template>

        <!-- ===================== Style 2: Light Icons ===================== -->
        <template v-else-if="layoutStyle === 2">
            <view class="dec-user-info__wrap dec-user-info__wrap--style2" :style="gradientWrapStyle">
                <view class="dec-user-info__container" :style="containerPaddingStyle">
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
                                <view class="dec-user-info__info dec-user-info__info--flex1">
                                    <view class="dec-user-info__name-row dec-user-info__name-row--between">
                                        <view class="dec-user-info__name-group">
                                            <template v-if="configStore.closeAuth === 1">
                                                <view
                                                    v-if="userInfo.isCompanyAuth"
                                                    class="dec-user-info__auth"
                                                    @click="goPages('/pages/user/userCertification/index')"
                                                >
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
                                            <text class="dec-user-info__name" :style="{ color: resolvedTextColors.nameColor }">{{
                                                userInfo.nickname || userInfo.username
                                            }}</text>
                                        </view>
                                        <view class="dec-user-info__action-icons">
                                            <tig-image
                                                v-if="data.settings?.show !== false"
                                                :src="imageFormat(data.settings?.icon?.picUrl || '')"
                                                class="dec-user-info__action-icon"
                                                @click="handleShortcutClick('settings')"
                                            />
                                            <view
                                                v-if="data.notification?.show !== false"
                                                class="dec-user-info__action-icon-wrap"
                                                @click="handleNotificationClick"
                                            >
                                                <tig-image :src="imageFormat(data.notification?.icon?.picUrl || '')" class="dec-user-info__action-icon" />
                                                <view v-if="hasUnread" class="dec-user-info__notification-dot" />
                                            </view>
                                        </view>
                                    </view>
                                    <view class="dec-user-info__sub" :style="{ color: resolvedTextColors.subColor }">
                                        <text>{{ maskedPhone }}</text>
                                    </view>
                                </view>
                            </template>
                        </view>
                    </view>

                    <view v-if="showStats" class="dec-user-info__stats">
                        <view v-for="item in visibleStats" :key="item.key" class="dec-user-info__stat-item" @click="handleStatClick(item.key)">
                            <text class="dec-user-info__stat-value" :style="{ color: resolvedTextColors.statValueColor }">{{ getStatValue(item.key) }}</text>
                            <text class="dec-user-info__stat-label" :style="{ color: resolvedTextColors.statLabelColor }">{{ safeT(item.label) }}</text>
                        </view>
                    </view>
                    <view class="dec-user-info__member-bar-placeholder dec-user-info__member-bar-placeholder--sm" />
                </view>
            </view>
        </template>

        <!-- ===================== Style 3: Minimal Growth ===================== -->
        <template v-else-if="layoutStyle === 3">
            <view class="dec-user-info__wrap dec-user-info__wrap--style3" :style="gradientWrapStyle">
                <view class="dec-user-info__container" :style="containerPaddingStyle">
                    <view class="dec-user-info__header dec-user-info__header--arrow" @click="isLoggedIn && goPages('/pages/user/profile/index')">
                        <view class="dec-user-info__profile">
                            <template v-if="!isLoggedIn">
                                <view class="dec-user-info__avatar" @click.stop="handleLogin">
                                    <image class="dec-user-info__avatar-img" mode="aspectFill" :src="staticResource('common/empty-img-bg3.png')" />
                                </view>
                                <view class="dec-user-info__info" @click.stop="handleLogin">
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
                                    <view class="dec-user-info__name" :style="{ color: resolvedTextColors.nameColor }">{{
                                        userInfo.nickname || userInfo.username
                                    }}</view>
                                    <view class="dec-user-info__sub" :style="{ color: resolvedTextColors.subColor }">
                                        <text>{{ maskedPhone }}</text>
                                    </view>
                                </view>
                            </template>
                        </view>
                        <view v-if="isLoggedIn" class="dec-user-info__nav-arrow iconfont-h5 icon-a-gengduo20" />
                    </view>

                    <view v-if="isLoggedIn && showMemberBar" class="dec-user-info__member-bar dec-user-info__member-bar--growth" :style="memberBarStyle">
                        <text class="dec-user-info__growth-label">{{ $t("成长值") }}</text>
                        <view class="dec-user-info__growth-right">
                            <text class="dec-user-info__growth-value">{{ userInfo.growthPoints || 0 }}</text>
                            <view class="dec-user-info__growth-arrow iconfont-h5 icon-a-gengduo20" />
                        </view>
                    </view>
                </view>
            </view>
        </template>

        <!-- ===================== Style 4: Centered Avatar + Growth Card ===================== -->
        <template v-else>
            <view class="dec-user-info__wrap dec-user-info__wrap--style4">
                <view class="dec-user-info__style4-grad" :style="style4GradStyle" />
                <view class="dec-user-info__container dec-user-info__container--style4" :style="containerPaddingStyle">
                    <view class="dec-user-info__center-header">
                        <template v-if="!isLoggedIn">
                            <view class="dec-user-info__avatar dec-user-info__avatar--lg" @click="handleLogin">
                                <image
                                    class="dec-user-info__avatar-img dec-user-info__avatar-img--lg"
                                    mode="aspectFill"
                                    :src="staticResource('common/empty-img-bg3.png')"
                                />
                            </view>
                            <view
                                class="dec-user-info__center-name dec-user-info__center-name--login"
                                :style="{ color: resolvedTextColors.nameColor }"
                                @click="handleLogin"
                            >
                                {{ $t("登录/注册") }}
                            </view>
                        </template>
                        <template v-else>
                            <view class="dec-user-info__avatar-wrap">
                                <view class="dec-user-info__avatar dec-user-info__avatar--lg">
                                    <tig-upload request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                                        <tig-image
                                            class="dec-user-info__avatar-img dec-user-info__avatar-img--lg"
                                            :src="imageFormat(userInfo.avatar || '')"
                                            mode="aspectFill"
                                        />
                                    </tig-upload>
                                </view>
                                <image
                                    v-if="showEditIcon"
                                    class="dec-user-info__edit-icon"
                                    :src="EDIT_ICON"
                                    @click.stop="goPages('/pages/user/profile/index')"
                                />
                            </view>
                            <view class="dec-user-info__center-name-row">
                                <text class="dec-user-info__center-name" :style="{ color: resolvedTextColors.nameColor }">
                                    {{ userInfo.nickname || userInfo.username }}
                                </text>
                                <template v-if="configStore.closeAuth === 1">
                                    <image
                                        class="dec-user-info__auth-badge"
                                        mode="aspectFit"
                                        :src="userInfo.isCompanyAuth ? AUTH_VERIFIED_ICON : AUTH_UNVERIFIED_ICON"
                                        @click="goPages('/pages/user/userCertification/index')"
                                    />
                                </template>
                            </view>
                        </template>
                    </view>

                    <view v-if="isLoggedIn && showMemberBar" class="dec-user-info__growth-card" :style="growthCardStyle">
                        <image v-if="growthWaveUrl" class="dec-user-info__growth-card-wave" :src="growthWaveUrl" mode="scaleToFill" />
                        <view class="dec-user-info__growth-card-left" @click="goPages('/pages/user/levelCenter/index')">
                            <view class="dec-user-info__growth-card-label-row">
                                <text class="dec-user-info__growth-card-label">{{ $t("成长值") }}</text>
                                <view class="dec-user-info__growth-card-label-arrow iconfont-h5 icon-a-gengduo20" />
                            </view>
                            <text class="dec-user-info__growth-card-value">{{ userInfo.growthPoints || 0 }}</text>
                        </view>
                        <view class="dec-user-info__growth-card-btn" @click="goPages('/pages/user/levelCenter/index')">{{ growthBtnText }}</view>
                    </view>
                </view>
            </view>
        </template>
    </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from "vue";
import { useI18n } from "vue-i18n";

import { redirect, staticResource, isPro, isO2o, isMerchant } from "@/utils";
import { imageFormat, urlFormat } from "@/utils/format";
import { handleLogin } from "@/utils/handleLogin";
import { useFrameVars, type ModuleDataWithFrame } from "@/utils/decorateFrame";

import { useUserStore } from "@/store/user";
import { useConfigStore } from "@/store/config";
import { getUserLevelList } from "@/api/user/levelCenter";

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
    gradientType?: string;
}

interface StatsItem {
    key: string;
    label: string;
    show?: boolean;
}

interface MemberBarConfig {
    show?: boolean;
    backgroundColor?: string;
    backgroundColorB?: string;
    borderRadius?: number;
    levelName?: string;
    description?: string;
    buttonText?: string;
    buttonLink?: { link?: string; [key: string]: any };
    waveImage?: { picUrl?: string };
}

interface BottomArcConfig {
    show?: boolean;
    height?: number;
}

interface TextColorsConfig {
    nameColor?: string;
    subColor?: string;
    statValueColor?: string;
    statLabelColor?: string;
}

interface UserInfoData extends ModuleDataWithFrame {
    layoutStyle?: number;
    showEditIcon?: boolean;
    signIn?: ShortcutItem;
    settings?: ShortcutItem;
    notification?: ShortcutItem;
    gradient?: GradientConfig;
    bgColor?: string;
    backgroundImage?: { picUrl?: string };
    backgroundImageShow?: boolean;
    bottomArc?: BottomArcConfig;
    memberBar?: MemberBarConfig;
    stats?: { show?: boolean; items?: StatsItem[] };
    textColors?: TextColorsConfig;
}

const AUTH_VERIFIED_ICON = "https://oss.tigshop.com/official/img/pc/202606/1780880708HREkXQF9XjeKH1ddgB.png";
const AUTH_UNVERIFIED_ICON = "https://oss.tigshop.com/official/img/pc/202606/1780880708c8LMqisZYuPNQfoj3j.png";
const EDIT_ICON = "https://oss.tigshop.com/official/img/pc/202606/1780880709E3AknH2icO6fSZeMly.png";
const DEFAULT_GROWTH_CARD_BG = "https://oss.tigshop.com/official/img/pc/202606/17808830195Hk80LmOeUpDCCCEKW.png";

const userStore = useUserStore();
const configStore = useConfigStore();

/** 沉浸式渐变向上延伸的高度：跨境为外贸头实测高度，国内为导航栏 */
const headerOffsetValue = computed(() => {
    return "var(--nav-height, 0px)";
});

const props = withDefaults(
    defineProps<{
        data: UserInfoData;
        // 沉浸式：渐变背景延伸到页面顶部（含导航栏区域），从上到下渐隐到页面底色
        immersive?: boolean;
    }>(),
    {
        immersive: false
    }
);

const token = computed(() => userStore.token || uni.getStorageSync("token"));
const isLoggedIn = computed(() => Boolean(token.value));
const userInfo = computed<Record<string, any>>(() => userStore.userInfo || {});

const layoutStyle = computed(() => props.data?.layoutStyle ?? 1);
const layoutClass = computed(() => [`dec-user-info--style${layoutStyle.value}`, { "dec-user-info--immersive": props.immersive }]);

const STYLE_TEXT_COLOR_DEFAULTS: Record<number, Required<TextColorsConfig>> = {
    1: { nameColor: "#ffffff", subColor: "#ffffff", statValueColor: "#ffffff", statLabelColor: "#ffffff" },
    2: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    3: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    4: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" }
};

const resolvedTextColors = computed(() => {
    const defaults = STYLE_TEXT_COLOR_DEFAULTS[layoutStyle.value] || STYLE_TEXT_COLOR_DEFAULTS[1];
    const tc = props.data?.textColors;
    return {
        nameColor: tc?.nameColor || defaults.nameColor,
        subColor: tc?.subColor || defaults.subColor,
        statValueColor: tc?.statValueColor || defaults.statValueColor,
        statLabelColor: tc?.statLabelColor || defaults.statLabelColor
    };
});

const DEFAULT_STATS: StatsItem[] = [
    { key: "collect", label: "商品收藏", show: true },
    { key: "follow", label: isO2o() ? t("门店关注") : t("店铺收藏"), show: true },
    { key: "history", label: "浏览记录", show: true }
];

const FRAME_DEFAULTS = {
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 16,
    boxPadding: 0,
    boxPaddingTop: 0,
    boxPaddingBottom: 0
};

const cssVars = computed(() => useFrameVars(props.data, FRAME_DEFAULTS));

const containerPaddingStyle = computed(() => {
    const ip = props.data?.frame?.innerPadding ?? 16;
    return { paddingLeft: `${ip * 2}rpx`, paddingRight: `${ip * 2}rpx` };
});

const STYLE_GRADIENT_DEFAULTS: Record<number, { colorA: string; colorB: string }> = {
    1: { colorA: "#57D45C", colorB: "#34C759" },
    2: { colorA: "#E0FCED", colorB: "#FDF1F3" },
    3: { colorA: "#F1EFE9", colorB: "#E7F0E6" },
    4: { colorA: "#FBD9E1", colorB: "#FDEDEF" }
};
// 顶部背景渐变方向映射
const GRADIENT_DIR_MAP: Record<string, string> = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "135deg"
};

const style1WrapStyle = computed(() => {
    const styles: Record<string, string> = {};
    const g = props.data?.gradient || {};
    const legacy = props.data?.bgColor;
    const defaults = STYLE_GRADIENT_DEFAULTS[1];
    const colorA = legacy || g.colorA || defaults.colorA;
    const colorB = legacy || g.colorB || defaults.colorB;
    const dir = GRADIENT_DIR_MAP[g.gradientType || "leftRight"] || "to right";
    const grad = `linear-gradient(${dir}, ${colorA}, ${colorB})`;
    if (props.data?.backgroundImageShow !== false && props.data?.backgroundImage?.picUrl) {
        styles.backgroundImage = `url(${props.data.backgroundImage.picUrl}), ${grad}`;
        styles.backgroundSize = "cover";
        styles.backgroundPosition = "center";
    } else {
        styles.background = grad;
    }
    return styles;
});
const gradientWrapStyle = computed(() => {
    if (layoutStyle.value !== 2 && layoutStyle.value !== 3) return {};
    const defaults = STYLE_GRADIENT_DEFAULTS[layoutStyle.value] || STYLE_GRADIENT_DEFAULTS[2];
    // 沉浸式：渐变由页面容器统一绘制并延伸到下方模块，组件自身保持透明，避免双层叠加
    if (props.immersive) return {};
    const g = props.data?.gradient || {};
    const colorA = g.colorA || defaults.colorA;
    const colorB = g.colorB || defaults.colorB;
    const dir = GRADIENT_DIR_MAP[g.gradientType || "leftRight"] || "to right";
    return {
        background: `linear-gradient(${dir}, ${colorA}, ${colorB})`
    };
});

const showMemberBar = computed(() => props.data?.memberBar?.show !== false);

const memberBarStyle = computed(() => {
    const bar = props.data?.memberBar;
    const r = bar?.borderRadius ?? 8;
    const styles: Record<string, string> = {
        borderRadius: `${r * 2}rpx ${r * 2}rpx 0 0`
    };
    if (layoutStyle.value === 3) {
        const colorA = bar?.backgroundColor || "#292929";
        const colorB = bar?.backgroundColorB || "#525252";
        styles.background = `linear-gradient(to right, ${colorA}, ${colorB})`;
    } else {
        styles.backgroundColor = bar?.backgroundColor || "#333";
    }
    return styles;
});

// --- Style 4: 居中头像 + 成长值卡 ---
const showEditIcon = computed(() => props.data?.showEditIcon !== false);

// 样式四背景渐变：独立图层，仅覆盖到成长值卡片的一半高度；无卡片时覆盖头部
// bottom 值 = 成长值卡 margin-bottom(20rpx) + 卡片高度(156rpx)的一半(78rpx) = 98rpx
const style4GradStyle = computed(() => {
    const defaults = STYLE_GRADIENT_DEFAULTS[4];
    const g = props.data?.gradient || {};
    const colorA = g.colorA || defaults.colorA;
    const colorB = g.colorB || defaults.colorB;
    const dir = GRADIENT_DIR_MAP[g.gradientType || "leftRight"] || "to right";
    const hasCard = isLoggedIn.value && showMemberBar.value;
    return {
        position: "absolute" as const,
        left: "0",
        right: "0",
        top: props.immersive ? `calc(-1 * ${headerOffsetValue.value})` : "0",
        bottom: hasCard ? "98rpx" : "0",
        background: `linear-gradient(${dir}, ${colorA}, ${colorB})`,
        zIndex: "0"
    };
});

const growthBtnText = computed(() => props.data?.memberBar?.buttonText || t("查看"));

const growthWaveUrl = computed(() => props.data?.memberBar?.waveImage?.picUrl || DEFAULT_GROWTH_CARD_BG);

const growthCardStyle = computed(() => {
    const bar = props.data?.memberBar;
    const r = bar?.borderRadius ?? 12;
    const colorA = bar?.backgroundColor || "#FF9EB5";
    const colorB = bar?.backgroundColorB || "#FF6E97";
    return {
        borderRadius: `${r * 2}rpx`,
        background: `linear-gradient(135deg, ${colorA}, ${colorB})`
    };
});

const showBottomArc = computed(() => layoutStyle.value === 1 && props.data?.bottomArc?.show !== false);

const arcStyle = computed(() => {
    const h = props.data?.bottomArc?.height ?? 24;
    return { height: `${h * 2}rpx` };
});

const arcHeight = computed(() => props.data?.bottomArc?.height ?? 24);

const arcDataUrl = computed(() => {
    const h = arcHeight.value;
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 375 ${h}" preserveAspectRatio="none"><path d="M 0 0 Q 187.5 ${h * 1.4} 375 0 L 375 ${h} L 0 ${h} Z" fill="#f5f5f5"/></svg>`;
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
});

const showStats = computed(() => props.data?.stats?.show !== false);

const visibleStats = computed(() => {
    const items = props.data?.stats?.items || DEFAULT_STATS;
    const followLabel = isO2o() ? "门店关注" : "店铺收藏";
    // 单商户版无「门店关注/店铺收藏」入口，与 userInfo 模块口径一致
    return items
        .filter((item) => item.show !== false && (item.key !== "follow" || isMerchant() || isO2o()))
        .map((item) => {
            // 融合版：默认「门店关注/店铺收藏」文案按 VITE_IS_STORE 覆盖，后台自定义文案保留
            if (item.key === "follow" && (!item.label || item.label === "门店关注" || item.label === "店铺关注" || item.label === "店铺收藏")) {
                return { ...item, label: followLabel };
            }
            return item;
        });
});

const orderNum = computed<Record<string, any>>(() => userStore.orderNum || {});

const STAT_VALUE_MAP: Record<string, (num: Record<string, any>, info: Record<string, any>) => string | number> = {
    collect: (num) => num.productCollect || 0,
    follow: (num) => num.shopCollect || 0,
    history: (num) => num.historyCount || 0
};

const getStatValue = (key: string): string | number => {
    if (!isLoggedIn.value) return "—";
    const fn = STAT_VALUE_MAP[key];
    if (fn) return fn(orderNum.value, userInfo.value);
    return "";
};

const STAT_URL_MAP: Record<string, string> = {
    collect: "/pages/user/collectProduct/index",
    follow: "/pages/user/collectionShop/index",
    history: "/pages/user/historyProduct/index"
};

const handleStatClick = (key: string) => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    if (STAT_URL_MAP[key]) {
        redirect({ url: STAT_URL_MAP[key] });
    }
};

const upgradeText = ref("");

const memberBarLevelName = computed(() => safeT(props.data?.memberBar?.levelName, "付费会员") || "付费会员");
const memberBarDescription = computed(() => {
    const value = props.data?.memberBar?.description;
    return value === undefined ? "购买会员，畅享更多专属权益" : safeT(value);
});
const memberBarButtonText = computed(() => safeT(props.data?.memberBar?.buttonText, "查看") || "查看");

const handleMemberBarClick = () => {
    const link = props.data?.memberBar?.buttonLink;
    if (link?.link) {
        redirect({ url: urlFormat(link as any) });
        return;
    }
};

const fetchUpgradeInfo = async () => {
    if (!isLoggedIn.value || !isPro()) {
        upgradeText.value = "";
        return;
    }
    try {
        const result = await getUserLevelList();
        const list = result?.item || result?.data?.item || [];
        const currentLevel = parseInt(userInfo.value.rankLevel || "0");
        const nextRank = list.find((r: any) => parseInt(r.rankLevel) > currentLevel);
        if (!nextRank) {
            upgradeText.value = t("已达最高等级");
            return;
        }
        const needed = Math.max(0, nextRank.minGrowthPoints - (userInfo.value.growthPoints || 0));
        if (userInfo.value.rankType === 2) {
            upgradeText.value = t("再消费") + needed + t("元升级到") + nextRank.rankName;
        } else {
            upgradeText.value = t("再获取") + needed + t("成长值升级到") + nextRank.rankName;
        }
    } catch {
        upgradeText.value = "";
    }
};

const hasUnread = computed(() => {
    const num = userStore.orderNum as Record<string, any>;
    return num && (num.userMessageCount || num.orderMessageCount || num.imMessageCount);
});

const handleNotificationClick = () => {
    if (!isLoggedIn.value) {
        handleLogin();
        return;
    }
    redirect({ url: "/pages/user/message/index" });
};

const maskedPhone = computed(() => {
    const mobile = userInfo.value.mobile || "";
    if (mobile.length >= 7) {
        return mobile.slice(0, 3) + "****" + mobile.slice(-4);
    }
    return mobile || "";
});

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

const goPages = (url: string) => {
    uni.navigateTo({ url });
};

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
    if (layoutStyle.value === 1 && showMemberBar.value) {
        fetchUpgradeInfo();
    }
});

watch(
    () => [isLoggedIn.value, layoutStyle.value],
    () => {
        if (isLoggedIn.value && layoutStyle.value === 1 && showMemberBar.value) {
            fetchUpgradeInfo();
        }
    }
);
</script>

<style lang="scss" scoped>
.dec-user-info {
    box-sizing: border-box;
    padding-left: var(--dec-box-padding, 0);
    padding-right: var(--dec-box-padding, 0);
    padding-top: var(--dec-box-padding-top, 0);
    padding-bottom: var(--dec-box-padding-bottom, 0);

    &__wrap {
        box-sizing: border-box;
        overflow: hidden;
    }

    &__wrap--style1 {
        background-size: cover;
        background-position: center;
    }

    &__wrap--style2,
    &__wrap--style3,
    &__wrap--style4 {
        border-radius: var(--dec-border-radius, 0);
    }

    &__wrap--style4 {
        position: relative;
    }

    &__style4-grad {
        position: absolute;
        pointer-events: none;
        z-index: 0;
    }

    &__container--style4 {
        position: relative;
        z-index: 1;
    }

    // 沉浸式：取消左右外边距与圆角，让页面容器的渐变背景全屏铺到顶部
    &--immersive {
        padding-left: 0 !important;
        padding-right: 0 !important;
        padding-top: 0 !important;

        .dec-user-info__wrap--style2,
        .dec-user-info__wrap--style3,
        .dec-user-info__wrap--style4 {
            border-radius: 0;
            background: transparent !important;
        }

        // 样式四：放开裁剪，让渐变层向上延伸到导航栏区域，形成一整段连续渐变
        .dec-user-info__wrap--style4 {
            overflow: visible;
        }
    }

    &__container {
        box-sizing: border-box;
    }

    &__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 64rpx;
        padding-bottom: 32rpx;
    }

    &__header--arrow {
        padding-bottom: 32rpx;
    }

    &__profile {
        display: flex;
        align-items: center;
        flex: 1;
        min-width: 0;
    }

    &__avatar {
        display: block;
        width: 108rpx;
        height: 108rpx;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 16rpx;
        flex-shrink: 0;
        background-color: #eee;
        box-sizing: border-box;
    }

    &__avatar--bordered {
        border: 2rpx solid #ffffff;
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
        min-width: 0;
    }

    &__info--flex1 {
        flex: 1;
    }

    &__name-row {
        display: flex;
        align-items: center;
        column-gap: 10rpx;
    }

    &__name-row--between {
        justify-content: space-between;
    }

    &__name-group {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;
    }

    &__name {
        font-size: 40rpx;
        line-height: 56rpx;
        font-weight: 500;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    &__sub {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 28rpx;
        line-height: 44rpx;
    }

    &__auth {
        margin-right: 10rpx;
        display: flex;
        align-items: center;
        height: 36rpx;
        flex-shrink: 0;
        image {
            height: 36rpx;
            display: block;
        }
    }

    &__action-icons {
        display: flex;
        align-items: center;
        gap: 40rpx;
        flex-shrink: 0;
        margin-left: 24rpx;
    }

    &__action-icon {
        width: 48rpx;
        height: 48rpx;
    }

    &__action-icon-wrap {
        position: relative;
        display: flex;
    }

    &__notification-dot {
        position: absolute;
        top: -4rpx;
        right: -4rpx;
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        background: #ff4d4f;
    }

    &__nav-arrow {
        font-size: 32rpx;
        color: #333;
        flex-shrink: 0;
    }

    &__stats {
        display: flex;
        align-items: center;
    }

    &__stat-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4rpx;
    }

    &__stat-value {
        font-size: 32rpx;
        font-weight: 600;
        line-height: 48rpx;
    }

    &__stat-label {
        font-size: 24rpx;
        line-height: 40rpx;
    }

    &__member-bar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        color: #fff;
        box-sizing: border-box;
    }

    &__member-bar--level {
        margin-top: 28rpx;
        padding: 16rpx 24rpx 36rpx;
    }

    &__member-bar--growth {
        margin-top: 28rpx;
        padding: 24rpx 24rpx 16rpx;
    }

    &__member-bar-placeholder {
        height: 44rpx;

        &--sm {
            height: 24rpx;
        }
    }

    &__member-bar-info {
        display: flex;
        align-items: center;
        flex: 1;
        min-width: 0;
        gap: 20rpx;
    }

    &__member-bar-name {
        font-size: 32rpx;
        font-weight: 700;
        line-height: 48rpx;
        color: #fcdabe;
        flex-shrink: 0;
    }

    &__member-bar-divider {
        width: 2rpx;
        height: 24rpx;
        background: #999;
        flex-shrink: 0;
    }

    &__member-bar-upgrade {
        font-size: 24rpx;
        line-height: 40rpx;
        color: #fcdabe;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    &__member-bar-btn {
        flex-shrink: 0;
        margin-left: 20rpx;
        padding: 12rpx 18rpx;
        border-radius: 24rpx;
        background: #fcdabe;
        color: #333;
        font-size: 24rpx;
        line-height: 24rpx;
        display: flex;
        align-items: center;
        gap: 8rpx;
    }

    &__growth-label {
        font-size: 28rpx;
        line-height: 44rpx;
        color: #fff;
    }

    &__growth-right {
        display: flex;
        align-items: center;
        gap: 12rpx;
    }

    &__growth-value {
        font-size: 36rpx;
        font-weight: 600;
        color: #ffdea0;
    }

    &__growth-arrow {
        font-size: 24rpx;
        color: #ffe3b1;
    }

    &__arc {
        width: 100%;
        margin-top: -28rpx;
        position: relative;
        z-index: 1;
        overflow: hidden;
        line-height: 0;
    }

    &__arc-img {
        width: 100%;
        height: 100%;
        display: block;
    }

    // --- Style 4: Centered Avatar + Growth Card ---
    &__center-header {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-top: 40rpx;
        padding-bottom: 28rpx;
    }

    &__avatar-wrap {
        position: relative;
        width: 160rpx;
        height: 160rpx;
    }

    &__avatar--lg {
        width: 160rpx;
        height: 160rpx;
        margin-right: 0;
    }

    &__avatar-img--lg {
        width: 100%;
        height: 100%;
    }

    &__edit-icon {
        position: absolute;
        right: -4rpx;
        bottom: -4rpx;
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        z-index: 2;
    }

    &__center-name-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-top: 12rpx;
    }

    &__center-name {
        font-size: 32rpx;
        font-weight: 600;
        line-height: 48rpx;
        max-width: 360rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    // 未登录：登录文字直接位于头像下方，需补回与头像的间距（登录态由 center-name-row 提供）
    &__center-name--login {
        margin-top: 12rpx;
    }

    &__auth-badge {
        height: 36rpx;
        width: 150rpx;
        flex-shrink: 0;
    }

    &__growth-card {
        position: relative;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 0 0 20rpx;
        padding: 32rpx 32rpx;
        overflow: hidden;
        box-sizing: border-box;
    }

    &__growth-card-wave {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;
    }

    &__growth-card-left {
        position: relative;
        z-index: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;
    }

    &__growth-card-label-row {
        display: flex;
        align-items: center;
        gap: 6rpx;
    }

    &__growth-card-label {
        font-size: 24rpx;
        line-height: 36rpx;
        color: rgba(255, 255, 255, 0.95);
    }

    &__growth-card-label-arrow {
        font-size: 20rpx;
        color: rgba(255, 255, 255, 0.9);
    }

    &__growth-card-value {
        font-size: 48rpx;
        font-weight: 700;
        line-height: 60rpx;
        color: #fff;
    }

    &__growth-card-btn {
        position: relative;
        z-index: 1;
        flex-shrink: 0;
        padding: 12rpx 46rpx;
        border-radius: 100rpx;
        background: #fff;
        color: #ff5f8c;
        font-size: 28rpx;
        font-weight: 600;
        line-height: 36rpx;
    }
}
</style>

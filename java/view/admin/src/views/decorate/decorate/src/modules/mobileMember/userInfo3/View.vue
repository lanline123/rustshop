<template>
    <div class="module-ad-con module-member-user-info-v3" :style="outerStyle">
        <div class="module-ad-content">
            <!-- Style 1: Arc Bottom -->
            <template v-if="layoutStyle === 1">
                <div class="user-info-wrap user-info-wrap--style1" :style="style1WrapStyle">
                    <div class="user-info-container" :style="containerStyle">
                        <div class="user-info-header">
                            <div class="user-info-profile">
                                <div class="user-info-avatar user-info-avatar--bordered">
                                    <img :src="demoAvatar" alt="头像" />
                                </div>
                                <div class="user-info-text">
                                    <div class="user-info-name" :style="{ color: resolvedTextColors.nameColor }">{{ $t('我饿了了了') }}</div>
                                    <div class="user-info-sub" :style="{ color: resolvedTextColors.subColor }">{{ $t('个人资料') }}<i class="iconfont-h5 icon-a-gengduo20"></i></div>
                                </div>
                            </div>
                        </div>
                        <div class="user-info-stats" v-if="module.stats?.show !== false">
                            <div class="stat-item" v-for="item in visibleStats" :key="item.key">
                                <span class="stat-value" :style="{ color: resolvedTextColors.statValueColor }">{{ statsDemoValues[item.key] ?? 0 }}</span>
                                <span class="stat-label" :style="{ color: resolvedTextColors.statLabelColor }">{{ item.label }}</span>
                            </div>
                        </div>
                        <div class="member-bar member-bar--level" v-if="module.memberBar?.show !== false" :style="memberBarStyle">
                            <div class="member-bar-info">
                                <span class="member-bar-level-name">{{ $t('永久团长') }}</span>
                                <span class="member-bar-divider-line"></span>
                                <span class="member-bar-upgrade-text">{{ $t('再获取500成长值升级到SVIP团长') }}</span>
                            </div>
                            <span class="member-bar-btn">{{ $t('查看') }}<i class="iconfont-h5 icon-a-gengduo20"></i></span>
                        </div>
                        <div class="member-bar-placeholder" v-else style="height: 22px"></div>
                    </div>
                </div>
                <div class="user-info-arc" v-if="module.bottomArc?.show !== false" :style="arcOverlayStyle">
                    <svg
                        class="user-info-arc-svg"
                        :viewBox="`0 0 375 ${module.bottomArc?.height ?? 24}`"
                        preserveAspectRatio="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            :d="`M 0 0 Q 187.5 ${(module.bottomArc?.height ?? 14) * 1.4} 375 0 L 375 ${module.bottomArc?.height ?? 14} L 0 ${module.bottomArc?.height ?? 14} Z`"
                            fill="#f5f5f5"
                        />
                    </svg>
                </div>
            </template>

            <!-- Style 2: Light Icons -->
            <template v-else-if="layoutStyle === 2">
                <div class="user-info-wrap user-info-wrap--style2" :style="gradientWrapStyle">
                    <div class="user-info-container" :style="containerStyle">
                        <div class="user-info-header">
                            <div class="user-info-avatar">
                                <img :src="demoAvatar" alt="头像" />
                            </div>
                            <div class="user-info-text">
                                <div class="user-info-name-row">
                                    <div class="user-info-name" :style="{ color: resolvedTextColors.nameColor }">{{ $t('我饿了了了') }}</div>
                                    <div class="user-info-action-icons">
                                        <img
                                            v-if="module.settings?.show !== false"
                                            :src="module.settings?.icon?.picUrl || defaultSettingsIcon"
                                            alt="设置"
                                            class="action-icon"
                                        />
                                        <div class="action-icon-wrap" v-if="module.notification?.show !== false">
                                            <img :src="module.notification?.icon?.picUrl || defaultNotificationIcon" alt="消息" class="action-icon" />
                                            <span class="notification-dot"></span>
                                        </div>
                                    </div>
                                </div>
                                <div class="user-info-sub" :style="{ color: resolvedTextColors.subColor }">156****8888</div>
                            </div>
                        </div>
                        <div class="user-info-stats" v-if="module.stats?.show !== false">
                            <div class="stat-item" v-for="item in visibleStats" :key="item.key">
                                <span class="stat-value" :style="{ color: resolvedTextColors.statValueColor }">{{ statsDemoValues[item.key] ?? 0 }}</span>
                                <span class="stat-label" :style="{ color: resolvedTextColors.statLabelColor }">{{ item.label }}</span>
                            </div>
                        </div>
                        <div class="member-bar-placeholder" style="height: 12px"></div>
                    </div>
                </div>
            </template>

            <!-- Style 3: Minimal Growth -->
            <template v-else-if="layoutStyle === 3">
                <div class="user-info-wrap user-info-wrap--style3" :style="gradientWrapStyle">
                    <div class="user-info-container" :style="containerStyle">
                        <div class="user-info-header user-info-header--arrow">
                            <div class="user-info-profile">
                                <div class="user-info-avatar">
                                    <img :src="demoAvatar" alt="头像" />
                                </div>
                                <div class="user-info-text">
                                    <div class="user-info-name" :style="{ color: resolvedTextColors.nameColor }">{{ $t('我饿了了了') }}</div>
                                    <div class="user-info-sub" :style="{ color: resolvedTextColors.subColor }">156****8888</div>
                                </div>
                            </div>
                            <span class="nav-arrow iconfont-h5 icon-a-gengduo20"></span>
                        </div>
                        <div class="member-bar member-bar--growth" v-if="module.memberBar?.show !== false" :style="memberBarStyle">
                            <span class="member-bar-growth-label">{{ $t('成长值') }}</span>
                            <span class="member-bar-growth-right">
                                <span class="member-bar-growth-value">56515</span>
                                <span class="member-bar-growth-arrow iconfont-h5 icon-a-gengduo20"></span>
                            </span>
                        </div>
                    </div>
                </div>
            </template>

            <!-- Style 4: Centered Avatar + Growth Card -->
            <template v-else>
                <div class="user-info-wrap user-info-wrap--style4">
                    <div class="user-info-style4-grad" :style="style4GradStyle"></div>
                    <div class="user-info-container user-info-container--style4" :style="containerStyle">
                        <div class="user-info-center-header">
                            <div class="user-info-avatar-wrap">
                                <div class="user-info-avatar user-info-avatar--lg">
                                    <img :src="demoAvatar" alt="头像" />
                                </div>
                                <img v-if="module.showEditIcon !== false" class="user-info-edit-icon" :src="editIcon" alt="编辑" />
                            </div>
                            <div class="user-info-center-name-row">
                                <span class="user-info-center-name" :style="{ color: resolvedTextColors.nameColor }">{{ $t('我饿了了了') }}</span>
                                <img class="user-info-auth-badge" :src="authVerifiedIcon" alt="已认证" />
                            </div>
                        </div>
                        <div class="growth-card" v-if="module.memberBar?.show !== false" :style="growthCardStyle">
                            <img v-if="growthWaveUrl" class="growth-card-wave" :src="growthWaveUrl" alt="" />
                            <div class="growth-card-left">
                                <div class="growth-card-label-row">
                                    <span class="growth-card-label">{{ $t('成长值') }}</span>
                                    <i class="iconfont-h5 icon-a-gengduo20"></i>
                                </div>
                                <span class="growth-card-value">56515</span>
                            </div>
                            <span class="growth-card-btn">{{ module.memberBar?.buttonText || $t('查看') }}</span>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, inject, ref, Ref } from "vue";
import { ModuleType, ModuleIcoPicType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface UserInfoShortcut {
    show?: boolean;
    icon?: ModuleIcoPicType;
    title?: string;
}
interface UserInfoGradient {
    colorA?: string;
    colorB?: string;
    gradientType?: string;
}
interface UserInfoStatsItem {
    key: string;
    label: string;
    show: boolean;
}
interface UserInfoMemberBar {
    show?: boolean;
    backgroundColor?: string;
    backgroundColorB?: string;
    borderRadius?: number;
    buttonText?: string;
    waveImage?: ModuleIcoPicType;
}
interface UserInfoBottomArc {
    show?: boolean;
    height?: number;
}
interface UserInfoTextColors {
    nameColor?: string;
    subColor?: string;
    statValueColor?: string;
    statLabelColor?: string;
}
type ModuleUserInfoType = ModuleType & {
    layoutStyle?: number;
    signIn?: UserInfoShortcut;
    settings?: UserInfoShortcut;
    gradient?: UserInfoGradient;
    bgColor?: string;
    backgroundImage?: ModuleIcoPicType;
    backgroundImageShow?: boolean;
    bottomArc?: UserInfoBottomArc;
    memberBar?: UserInfoMemberBar;
    notification?: UserInfoShortcut;
    stats?: { show?: boolean; items?: UserInfoStatsItem[] };
    textColors?: UserInfoTextColors;
    showEditIcon?: boolean;
    showSignIn?: boolean;
    showSettings?: boolean;
};

const module = defineModel<ModuleUserInfoType>("module") as Ref<ModuleUserInfoType>;

// 由 decorate/Index.vue 注入：页面是否正在绘制沉浸式连续渐变带（首模块为 userInfo3 样式 2/3）
const memberImmersiveActive = inject<Ref<boolean>>("memberImmersiveActive", ref(false));

const demoAvatar = "https://oss.tigshop.com/img/gallery/202605/1779691335wtQOROIkUPPNOzuply.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultSignInIcon = "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultSettingsIcon = "https://oss.tigshop.com/official/img/pc/202605/1779872068yYRoxuCDxWgGcOti6b.png";
const defaultNotificationIcon = "https://oss.tigshop.com/official/img/pc/202605/1779958812O0gl221tWQVIEt7OXD.png";
const authVerifiedIcon = "https://oss.tigshop.com/official/img/pc/202606/1780880708HREkXQF9XjeKH1ddgB.png";
const editIcon = "https://oss.tigshop.com/official/img/pc/202606/1780880709E3AknH2icO6fSZeMly.png";
const DEFAULT_GROWTH_CARD_BG = "https://oss.tigshop.com/official/img/pc/202606/17808830195Hk80LmOeUpDCCCEKW.png";
const styleGradientDefaults: Record<number, { colorA: string; colorB: string }> = {
    1: { colorA: "#57D45C", colorB: "#34C759" },
    2: { colorA: "#E0FCED", colorB: "#FDF1F3" },
    3: { colorA: "#F1EFE9", colorB: "#E7F0E6" },
    4: { colorA: "#FBD9E1", colorB: "#FDEDEF" }
};

const defaultStatsItems: UserInfoStatsItem[] = [
    { key: "collect", label: t('商品收藏'), show: true },
    { key: "follow", label: t('门店关注'), show: true },
    { key: "history", label: t('浏览记录'), show: true }
];
const statsDemoValues: Record<string, number> = { collect: 12, follow: 824, history: 5688 };

// Old data migration
const mod = module.value as any;
if (mod.signIn === undefined && mod.showSignIn !== undefined) {
    mod.signIn = { show: !!mod.showSignIn };
}
if (mod.settings === undefined && mod.showSettings !== undefined) {
    mod.settings = { show: !!mod.showSettings };
}
if (mod.bgColor !== undefined) {
    if (!mod.gradient || typeof mod.gradient !== "object") mod.gradient = {};
    mod.gradient.colorA = mod.bgColor;
    mod.gradient.colorB = mod.bgColorB || mod.bgColor;
    mod.gradient.gradientType = mod.bgGradientType || mod.gradient.gradientType || "leftRight";
    delete mod.bgColor;
    delete mod.bgColorB;
    delete mod.bgGradientType;
}

const defaultStyle1BgImage = "https://oss.tigshop.com/official/img/pc/202605/1779860190FsB8OM4MrGwgOCvKsA.png";

mergeDefaultModule(module.value, {
    layoutStyle: 1,
    showEditIcon: true,
    signIn: { show: true, icon: { picUrl: defaultSignInIcon, picThumb: defaultSignInIcon }, title: t('签到有礼') },
    settings: { show: true, icon: { picUrl: defaultSettingsIcon, picThumb: defaultSettingsIcon } },
    gradient: { ...(styleGradientDefaults[mod.layoutStyle ?? 1] || styleGradientDefaults[1]), gradientType: "leftRight" },
    backgroundImage: { picUrl: defaultStyle1BgImage, picThumb: defaultStyle1BgImage },
    bottomArc: { show: true, height: 24 },
    memberBar: { show: true, backgroundColor: "", borderRadius: 10, buttonText: t('查看'), waveImage: { picUrl: DEFAULT_GROWTH_CARD_BG, picThumb: DEFAULT_GROWTH_CARD_BG } },
    notification: { show: true, icon: { picUrl: defaultNotificationIcon, picThumb: defaultNotificationIcon } },
    stats: { show: true, items: defaultStatsItems.map((i) => ({ ...i })) },
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
});

const layoutStyle = computed(() => module.value.layoutStyle ?? 1);
const frame = computed(() => module.value.frame ?? {});
const gradient = computed(() => {
    const defaults = styleGradientDefaults[layoutStyle.value] || styleGradientDefaults[1];
    const g = module.value.gradient;
    return {
        colorA: g?.colorA || defaults.colorA,
        colorB: g?.colorB || defaults.colorB,
        gradientType: g?.gradientType || "leftRight"
    };
});
// 顶部背景渐变方向映射
const GRADIENT_DIR_MAP: Record<string, string> = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "135deg"
};
const frameFormat = computed(() => formatFrame(frame.value));

const visibleStats = computed(() => {
    const items = module.value.stats?.items || defaultStatsItems;
    return items.filter((item) => item.show !== false);
});

const styleTextColorDefaults: Record<number, Required<UserInfoTextColors>> = {
    1: { nameColor: "#ffffff", subColor: "#ffffff", statValueColor: "#ffffff", statLabelColor: "#ffffff" },
    2: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    3: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" },
    4: { nameColor: "#333333", subColor: "#666666", statValueColor: "#333333", statLabelColor: "#666666" }
};
const resolvedTextColors = computed(() => {
    const style = layoutStyle.value;
    const defaults = styleTextColorDefaults[style] || styleTextColorDefaults[1];
    const tc = module.value.textColors;
    return {
        nameColor: tc?.nameColor || defaults.nameColor,
        subColor: tc?.subColor || defaults.subColor,
        statValueColor: tc?.statValueColor || defaults.statValueColor,
        statLabelColor: tc?.statLabelColor || defaults.statLabelColor
    };
});

const outerStyle = computed(() => frameFormat.value.boxPadding + frameFormat.value.boxPaddingTop + frameFormat.value.boxPaddingBottom);

const style1WrapStyle = computed(() => {
    const styles: Record<string, string> = {};

    const dir = GRADIENT_DIR_MAP[gradient.value.gradientType || "leftRight"] || "to right";
    const grad = `linear-gradient(${dir}, ${gradient.value.colorA}, ${gradient.value.colorB})`;

    if (module.value.backgroundImageShow !== false && module.value.backgroundImage?.picUrl) {
        styles.backgroundImage = `url(${module.value.backgroundImage.picUrl}), ${grad}`;
        styles.backgroundSize = "cover";
        styles.backgroundPosition = "center";
    } else {
        styles.background = grad;
    }

    return styles;
});

const arcOverlayStyle = computed(() => {
    const h = module.value.bottomArc?.height ?? 14;
    return {
        height: `${h}px`
    };
});

const gradientWrapStyle = computed(() => {
    // 沉浸式：渐变由页面容器统一绘制并向上延伸到标题栏，组件自身保持透明，避免双层叠加产生接缝
    if (memberImmersiveActive.value) {
        return {
            background: "transparent",
            borderRadius: "0"
        };
    }
    const dir = GRADIENT_DIR_MAP[gradient.value.gradientType || "leftRight"] || "to right";
    return {
        background: `linear-gradient(${dir}, ${gradient.value.colorA}, ${gradient.value.colorB})`,
        borderRadius: `${frame.value.boxRadius ?? 0}px`
    };
});

const containerStyle = computed(() => {
    const ip = frame.value.innerPadding ?? 16;
    const styles: Record<string, string> = {
        paddingLeft: `${ip}px`,
        paddingRight: `${ip}px`
    };
    return styles;
});

const memberBarStyle = computed(() => {
    const bar = module.value.memberBar;
    const r = bar?.borderRadius ?? 25;
    const styles: Record<string, string> = {
        borderRadius: `${r}px ${r}px 0 0`
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

// Style 4: 顶部头像区渐变（浅粉）—— 仅覆盖到成长值卡片的一半高度；无卡片时覆盖头部
// bottom 值 = 成长值卡 margin-bottom(10px) + 卡片高度(82px)的一半(41px) = 51px
// 样式四：渐变独立图层，仅覆盖到成长值卡片的一半高度（非沉浸式时保留与移动端一致的「半卡」效果）。
// 沉浸式下，渐变改由页面容器统一绘制的「一层连续渐变带」透出（见 Index.vue，参考样式 2/3 机制），
// 组件自身置透明，避免与标题栏区域出现颜色分离/接缝。
const style4GradStyle = computed(() => {
    if (memberImmersiveActive.value) {
        return { display: "none" };
    }
    const showCard = module.value.memberBar?.show !== false;
    const dir = GRADIENT_DIR_MAP[gradient.value.gradientType || "leftRight"] || "to right";
    return {
        position: "absolute" as const,
        left: "0",
        right: "0",
        top: "0",
        bottom: showCard ? "51px" : "0",
        background: `linear-gradient(${dir}, ${gradient.value.colorA}, ${gradient.value.colorB})`,
        zIndex: "0"
    };
});

// Style 4: 成长值卡（四角圆角 + 饱和粉渐变）
const growthCardStyle = computed(() => {
    const bar = module.value.memberBar;
    const r = bar?.borderRadius ?? 10;
    const colorA = bar?.backgroundColor || "#FF9EB5";
    const colorB = bar?.backgroundColorB || "#FF6E97";
    return {
        borderRadius: `${r}px`,
        background: `linear-gradient(135deg, ${colorA}, ${colorB})`
    };
});

const growthWaveUrl = computed(() => module.value.memberBar?.waveImage?.picUrl || DEFAULT_GROWTH_CARD_BG);
</script>

<style lang="less" scoped>
/* ======================== Base ======================== */
.user-info-wrap {
    box-sizing: border-box;
    overflow: hidden;
    position: relative;
    z-index: 2;
}
.user-info-container {
    box-sizing: border-box;
}
.user-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 32px 0 16px;
}
.user-info-profile {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
}
.user-info-avatar {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 8px;
    flex-shrink: 0;
    background-color: #eee;
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
}
.user-info-avatar--bordered {
    border: 1px solid #ffffff;
}
.user-info-text {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
    flex: 1;
}
.user-info-name {
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.user-info-sub {
    font-size: 14px;
    line-height: 22px;
    display: flex;
    align-items: center;
    gap: 4px;
    .iconfont-h5 {
        font-size: 12px;
    }
}

/* ======================== Stats ======================== */
.user-info-stats {
    display: flex;
    align-items: center;
}
.stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
}
.stat-value {
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
}
.stat-label {
    font-size: 12px;
    line-height: 20px;
}

/* ======================== Style 1: Shortcuts ======================== */
.user-info-shortcuts {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;
    margin-left: 12px;
}
.shortcut-item {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    font-size: 12px;
    font-weight: 500;
    color: #333;
}
.shortcut-item--light {
    color: #fff;
}
.shortcut-icon {
    width: 16px;
    height: 16px;
    object-fit: contain;
}

/* ======================== Style 2: Action Icons ======================== */
.user-info-name-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.user-info-action-icons {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-shrink: 0;
    margin-left: 12px;
}
.action-icon {
    width: 24px;
    height: 24px;
    object-fit: contain;
    cursor: pointer;
}
.action-icon-wrap {
    position: relative;
    display: flex;
}
.notification-dot {
    position: absolute;
    top: -2px;
    right: -2px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ff4d4f;
}

/* ======================== Style 3: Arrow ======================== */
.user-info-header--arrow {
    cursor: pointer;
}
.nav-arrow {
    font-size: 16px;
    color: #333;
    flex-shrink: 0;
}

/* ======================== Arc Overlay ======================== */
.user-info-arc {
    position: relative;
    z-index: 3;
    width: 100%;
    pointer-events: none;
    line-height: 0;
    display: block;
    overflow: hidden;
    margin-top: -14px;
}
.user-info-arc-svg {
    width: 100%;
    height: 100%;
    display: block;
}

/* ======================== Member Bar ======================== */
.member-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #fff;
    box-sizing: border-box;
    position: relative;
    z-index: 1;
}
.member-bar--level {
    margin-top: 14px;
    padding: 8px 12px 18px;
}
.member-bar-info {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    gap: 10px;
}
.member-bar-level-name {
    font-size: 16px;
    font-weight: 700;
    line-height: 24px;
    color: #fcdabe;
    flex-shrink: 0;
}
.member-bar-divider-line {
    width: 1px;
    height: 12px;
    background: #999999;
    flex-shrink: 0;
}
.member-bar-upgrade-text {
    font-size: 12px;
    line-height: 20px;
    color: #fcdabe;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.member-bar-btn {
    flex-shrink: 0;
    margin-left: 10px;
    padding: 6px 9px;
    border-radius: 12px;
    background: #fcdabe;
    color: #333;
    font-size: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    .iconfont-h5 {
        font-size: 6px;
    }
}

.member-bar--growth {
    margin: 14px 0 0;
    padding: 12px 12px 8px;
}
.member-bar-growth-label {
    font-size: 14px;
    line-height: 22px;
    color: #fff;
}
.member-bar-growth-right {
    display: flex;
    align-items: center;
    gap: 6px;
}
.member-bar-growth-value {
    font-size: 18px;
    font-weight: 600;
    color: #ffdea0;
}
.member-bar-growth-arrow {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    color: #ffe3b1;
}

/* ======================== Style 4: Centered Avatar + Growth Card ======================== */
.user-info-wrap--style4 {
    position: relative;
}
.user-info-style4-grad {
    position: absolute;
    pointer-events: none;
}
.user-info-container--style4 {
    position: relative;
    z-index: 1;
}
.user-info-center-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0 16px;
}
.user-info-avatar-wrap {
    position: relative;
    width: 80px;
    height: 80px;
}
.user-info-avatar--lg {
    width: 80px;
    height: 80px;
    margin-right: 0;
}
.user-info-edit-icon {
    position: absolute;
    right: -2px;
    bottom: -2px;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    z-index: 2;
}
.user-info-center-name-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 6px;
}
.user-info-center-name {
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
    max-width: 180px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.user-info-auth-badge {
    height: 18px;
    width: auto;
    flex-shrink: 0;
}
.growth-card {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 0 10px;
    padding: 15px 16px;
    overflow: hidden;
    color: #fff;
    box-sizing: border-box;
}
.growth-card-wave {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    pointer-events: none;
}
.growth-card-left {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
}
.growth-card-label-row {
    display: flex;
    align-items: center;
    gap: 4px;
    .iconfont-h5 {
        font-size: 10px;
        color: rgba(255, 255, 255, 0.9);
    }
}
.growth-card-label {
    font-size: 12px;
    line-height: 18px;
    color: rgba(255, 255, 255, 0.95);
}
.growth-card-value {
    font-size: 24px;
    font-weight: 700;
    line-height: 32px;
    color: #fff;
}
.growth-card-btn {
    position: relative;
    z-index: 1;
    flex-shrink: 0;
    padding: 6px 23px;
    border-radius: 100px;
    background: #fff;
    color: #ff5f8c;
    font-size: 14px;
    font-weight: 600;
    line-height: 18px;
}
</style>

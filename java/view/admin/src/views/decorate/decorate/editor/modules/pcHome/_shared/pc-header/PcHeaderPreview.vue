<template>
    <div class="dec-pc-header-preview">
        <div class="benefit-bar-wrap" v-if="showTopStrip" :style="benefitBarWrapStyle" ref="benefitBarWrapRef">
            <div class="w">
                <div class="benefit-bar" :class="{ 'benefit-bar--simple': styleType === 1, 'benefit-bar--only-right': isBenefitBarOnlyRight }">
                    <div class="benefit-bar__left" v-if="showStripLeft" :style="topMenuTextStyle">
                        <span class="benefit-bar__location" v-if="showStripRegion" :style="topMenuTextStyle">
                            <span class="benefit-bar__region">{{ topRegionText }}</span>
                        </span>
                        <a class="top-link top-member-entry" v-if="showTopMemberName" :style="topMenuTextStyle">会员信息</a>
                    </div>

                    <div class="benefit-bar__right" v-if="showStripRight" :style="topMenuTextStyle">
                        <a class="top-link top-link--cart" v-if="styleType === 1 && showTopMenuCart" :style="topMenuTextStyle">
                            <span class="pc-icon pc-icon--cart"></span>
                            <span>购物车</span>
                        </a>

                        <div class="top-links" v-if="styleType === 1 && showTopMenuBar && showTopUserMenu">
                            <a class="top-link top-link--member-center" v-if="showTopUserMenu && showTopMemberCenter" :style="topMenuTextStyle">
                                <span>会员中心</span>
                                <span class="pc-icon pc-icon--arrow top-bar-nav__arrow" style="margin-top: 7px; margin-left: 0"></span>
                            </a>
                            <a class="top-link" v-if="showTopUserMenu && showTopMessage" :style="topMenuTextStyle">站内消息</a>
                            <a class="top-link" v-if="showTopUserMenu && showTopOrders" :style="topMenuTextStyle">我的订单</a>
                        </div>

                        <a class="top-link top-link--nav" v-if="showTopSiteNav" :style="topMenuTextStyle">
                            <span>站点导航</span>
                            <span class="pc-icon pc-icon--arrow top-bar-nav__arrow" style="margin-top: 7px; margin-left: 0"></span>
                        </a>
                    </div>
                </div>
            </div>
        </div>

            <PcHeaderSimplePreviewBody
                :is-white-header="isWhiteHeader"
                :logo-src="logoSrc"
                :logo-img-style="logoImgStyle"
                :logo-align-class="logoAlignClass"
                :show-category="showCategory"
                :category-margin-top="categoryMarginTop"
                :simple-search-style="simpleSearchStyle"
                :show-main-nav="showMainNav"
                :main-nav-padding-top="mainNavPaddingTop"
                :main-nav-padding-bottom="mainNavPaddingBottom"
                :show-right-cart="showRightCart"
                :cart-style="cartStyle"
                :cart-pic-thumb="cartPicThumb"
                :bottom-divider-type="bottomDividerType"
                :nav-items="navItems"
            />
    </div>
</template>

<script lang="ts" setup>
/**
 * PC 首页头部预览（编辑器内，照搬 Vort-Admin pc-header）
 * 仅用于编辑器画布快速识别风格与关键开关；不可配置、不进保存数据。
 * config 缺省时用 pcHeaderDefaults（跨境版默认跨境风），导航项拉真实 pcNavigation 数据。
 */
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import type { PcHeaderConfig, PcHeaderNoticeItem } from "./types";
import { pcHeaderDefaults } from "./types";
import { imageFormat, productImage } from "@/utils/format";
import { isOverseas } from "@/utils/version";
import { getPcNavigationList } from "@/api/decorate/pcNavigation";
import { getConfigBasic } from "@/api/setting/config";
import defaultLogoUrl from "@/assets/logo/main_logo.png";
import PcHeaderOverseasPreviewBody from "./components/PcHeaderOverseasPreviewBody.vue";
import PcHeaderSimplePreviewBody from "./components/PcHeaderSimplePreviewBody.vue";

defineOptions({ name: "PcHeaderPreview" });

const props = defineProps<{
    config?: PcHeaderConfig;
    pageType?: number;
}>();

// 跨境版默认跨境风（styleType=2），标准版默认简约风（styleType=1）
const defaultConfig = computed<PcHeaderConfig>(() => ({ ...pcHeaderDefaults, styleType: isOverseas() ? 2 : 1 }));
const cfg = computed<PcHeaderConfig>(() => props.config ?? defaultConfig.value);

const styleType = computed(() => (cfg.value.styleType ?? 1) as 1 | 2);
const bottomDividerType = computed(() => {
    const raw = cfg.value.bottomDividerType;
    if (raw === 0 || raw === 1 || raw === 2) return raw;
    // 保持历史默认表现：简约风=边框，跨境风=阴影
    return styleType.value === 2 ? 1 : 2;
});
const showBenefitBar = computed(() => (cfg.value.showBenefitBar ?? 1) !== 0);
const showTopMenuBar = computed(() => (cfg.value.showTopMenuBar ?? 1) !== 0);
const showTopRegion = computed(() => (cfg.value.showTopRegion ?? 1) !== 0);
const topRegionText = computed(() => String(cfg.value.topRegionText || "北京市"));
const showTopUserMenu = computed(() => (cfg.value.showTopUserMenu ?? 1) !== 0);
const showTopMemberCenter = computed(() => (cfg.value.showTopMemberCenter ?? 1) !== 0);
const showTopMemberName = computed(() => (cfg.value.showTopMemberName ?? 1) !== 0);
const showTopMessage = computed(() => (cfg.value.showTopMessage ?? 1) !== 0);
const showTopOrders = computed(() => (cfg.value.showTopOrders ?? 1) !== 0);
const showTopSiteNav = computed(() => (cfg.value.showTopSiteNav ?? 1) !== 0);
const showRightCart = computed(() => (cfg.value.rightCartShow ?? 1) !== 0);
const cartStyle = computed(() => (cfg.value.cartStyle ?? 1) as 1 | 2);
const isHomePage = computed(() => (props.pageType ?? 1) === 1);
const showCategory = computed(() => {
    if ((cfg.value.showCategory ?? 1) === 0) return false;
    if (!isHomePage.value) return true;
    return (cfg.value.showHomeCategory ?? 1) !== 0;
});
const categoryMarginTop = computed(() => cfg.value.categoryMarginTop ?? 0);
const showMainNav = computed(() => (cfg.value.showMainNav ?? 1) !== 0);
const mainNavPaddingTop = computed(() => cfg.value.mainNavPaddingTop ?? 0);
const mainNavPaddingBottom = computed(() => cfg.value.mainNavPaddingBottom ?? 0);
const showTopMenuCart = computed(() => (cfg.value.topMenuCartShow ?? 1) !== 0);
const isWhiteHeader = computed(() => (cfg.value.isWhite ?? 1) !== 0);
const searchColorType = computed(() => (cfg.value.searchColorType ?? 1) as 1 | 2);
const topSearchStyle = computed(() => {
    if (searchColorType.value !== 2) return {};
    const color = String(cfg.value.searchColor || "#1a1a1a");
    return { "--pc-header-search-accent": color } as Record<string, string>;
});
const simpleSearchStyle = computed(() => {
    // 简约风与跨境风保持一致：默认跟随系统，选择“自定义”时复用 searchColor
    if (searchColorType.value !== 2) return {};
    const color = String(cfg.value.searchColor || "#1a1a1a");
    return { "--pc-header-search-accent": color } as Record<string, string>;
});

// 主导航项：拉真实 pcNavigation 数据，失败回退演示导航
const navItems = ref<string[]>([]);

const cartPicThumb = computed(() => productImage(cfg.value.cartPic));

const showBenefitContent = computed(() => styleType.value === 2 && showBenefitBar.value && benefitNoticeGroups.value.length > 0);

const isBenefitBarOnlyRight = computed(() => {
    // 左侧(地区/会员信息)隐藏 / 跨境风无内容时，只剩右侧菜单，保证仍然靠右展示
    return showStripRight.value && !showStripLeft.value && !showBenefitContent.value;
});

// 基础设置的商城 LOGO（标准版=商城LOGO，跨境=商城LOGO-黑底，字段均为 shopLogo）
const shopLogoUrl = ref("");

// 回退优先级对齐前台 pcHeaderRuntime：头部配置图 -> 平台商城 LOGO -> 内置图
const logoSrc = computed(() => productImage(cfg.value.logoPic) || (shopLogoUrl.value ? imageFormat(shopLogoUrl.value) : "") || defaultLogoUrl);

const logoImgStyle = computed(() => {
    const w = Math.min(cfg.value.logoWidth ?? 100, 160);
    const h = Math.min(cfg.value.logoHeight ?? 25, 60);
    return {
        width: `${w}px`,
        height: `${h}px`,
        objectFit: "contain"
    } as const;
});

const logoAlignClass = computed(() => {
    const align = cfg.value.logoAlign ?? 1;
    if (align === 2) return "logo--center";
    if (align === 3) return "logo--right";
    return "logo--left";
});

const benefitBarStyle = computed(() => {
    const styles: Record<string, string> = {};
    if (cfg.value.benefitBarBackgroundColor) {
        styles.backgroundColor = String(cfg.value.benefitBarBackgroundColor);
    }
    return styles;
});

const benefitBarWrapStyle = benefitBarStyle;

const topMenuTextStyle = computed(() => {
    // 悬浮色走 CSS 变量，避免 inline color 压过 :hover
    const styles: Record<string, string> = {};
    const color = String(cfg.value.topMenuTextColor || "");
    const hoverColor = String(cfg.value.topMenuTextHoverColor || "");
    if (color) styles.color = color;
    if (hoverColor) styles["--pc-header-top-menu-hover-color"] = hoverColor;
    return styles;
});

// benefit 主/副标题取色优先级：各自的设置项 > 顶部菜单文字颜色 > 内置默认
const benefitTitleStyle = computed(() => {
    return { color: cfg.value.benefitTitleColor || cfg.value.topMenuTextColor || "#ffffff" };
});

const benefitDescStyle = computed(() => {
    return { color: cfg.value.benefitDescColor || cfg.value.topMenuTextColor || "#ffffffcc" };
});

function hasNoticeContent(notice?: PcHeaderNoticeItem | null): boolean {
    return Boolean(getNoticePic(notice) || getNoticeTitle(notice) || getNoticeDesc(notice));
}

const benefitNoticeGroups = computed(() => {
    const rawGroups = cfg.value.benefitNoticeGroups ?? [];
    return rawGroups
        .map((group) => {
            const rawNotices = Array.isArray((group as { notices?: PcHeaderNoticeItem[] }).notices)
                ? (group as { notices?: PcHeaderNoticeItem[] }).notices!
                : [];
            const notices = rawNotices.filter((notice) => hasNoticeContent(notice));
            return { ...group, notices };
        })
        .filter((group) => (group?.notices?.length ?? 0) > 0);
});

const showTopStrip = computed(() => {
    // 顶部提示条总开关：两种风格都由 showBenefitBar 控制
    return showBenefitBar.value;
});

const showStripRegion = computed(() => styleType.value === 1 && showTopRegion.value);
const showStripLeft = computed(
    () => styleType.value === 1 && (showStripRegion.value || showTopMemberName.value)
);

const showStripRight = computed(() => {
    if (styleType.value === 2) return showTopSiteNav.value || showTopMenuCart.value;
    // 简约风：站点导航独立；顶部菜单仅指右侧菜单（不含站点导航）
    const hasUserMenu = showTopMenuBar.value && showTopUserMenu.value;
    return showTopSiteNav.value || showTopMenuCart.value || hasUserMenu;
});

const transparentPixel = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";

function getSafeImgSrc(src: string): string {
    return src || transparentPixel;
}

function getNoticePic(notice?: PcHeaderNoticeItem | null): string {
    return productImage(notice);
}

function getNoticeTitle(notice?: PcHeaderNoticeItem | null): string {
    return String(notice?.picTitle || "");
}

function getNoticeDesc(notice?: PcHeaderNoticeItem | null): string {
    return String(notice?.picDesc || "");
}

function getCarouselNotices(notices: PcHeaderNoticeItem[]) {
    // 轮播无缝衔接：追加第一条作为尾部占位，避免循环瞬间跳变
    if ((notices?.length ?? 0) <= 1) return notices;
    return [...notices, notices[0]];
}

const benefitBarWrapRef = ref<HTMLElement | null>(null);
const carouselWidth = ref<number>(150);
const carouselIndexMap = ref<Record<number, number>>({});
const carouselAnimatingMap = ref<Record<number, boolean>>({});
const carouselTimerMap = ref<Record<number, number>>({});

const CAROUSEL_ITEM_HEIGHT = 40;
const CAROUSEL_PAUSE_MS = 3000;
const CAROUSEL_MOVE_MS = 400;
const CAROUSEL_INTERVAL_MS = CAROUSEL_PAUSE_MS + CAROUSEL_MOVE_MS;

const carouselWidthStyle = computed(() => {
    return { width: `${carouselWidth.value}px` };
});

async function recalcCarouselWidth() {
    await nextTick();
    const root = benefitBarWrapRef.value;
    if (!root) return;

    const nodes = Array.from(root.querySelectorAll<HTMLElement>(".secound-txt"));
    let maxWidth = 150;
    nodes.forEach((el) => {
        const w = Math.ceil(el.getBoundingClientRect().width);
        if (w > maxWidth) {
            maxWidth = w;
        }
    });
    carouselWidth.value = maxWidth + 20;
}

function resetCarouselTimers() {
    Object.values(carouselTimerMap.value).forEach((timerId) => {
        window.clearInterval(timerId);
    });
    carouselTimerMap.value = {};
}

function startCarouselTimers() {
    resetCarouselTimers();
    carouselIndexMap.value = {};
    carouselAnimatingMap.value = {};

    benefitNoticeGroups.value.forEach((group, groupIndex) => {
        const count = group?.notices?.length ?? 0;
        if (count <= 1) return;

        carouselIndexMap.value[groupIndex] = 0;
        carouselAnimatingMap.value[groupIndex] = true;

        const timerId = window.setInterval(() => {
            // 先执行 0.4s 的滚动，再停留 3s（interval = 3.4s）
            carouselAnimatingMap.value[groupIndex] = true;
            const nextIndex = (carouselIndexMap.value[groupIndex] ?? 0) + 1;
            carouselIndexMap.value[groupIndex] = nextIndex;

            // 由于末尾追加了第一条作为占位，滚到占位后立刻无动画重置到 0，形成无缝循环
            if (nextIndex >= count) {
                window.setTimeout(() => {
                    carouselAnimatingMap.value[groupIndex] = false;
                    carouselIndexMap.value[groupIndex] = 0;
                    // 使用双 RAF，确保“关闭过渡 -> 复位”先落帧，再恢复过渡，避免出现闪动
                    window.requestAnimationFrame(() => {
                        window.requestAnimationFrame(() => {
                            carouselAnimatingMap.value[groupIndex] = true;
                        });
                    });
                }, CAROUSEL_MOVE_MS);
            }
        }, CAROUSEL_INTERVAL_MS);

        carouselTimerMap.value[groupIndex] = timerId;
    });
}

function getCarouselContainerStyle(groupIndex: number, count: number) {
    if (count <= 1) return {};
    const index = carouselIndexMap.value[groupIndex] ?? 0;
    const shouldAnimate = carouselAnimatingMap.value[groupIndex] !== false;
    const y = `-${index * CAROUSEL_ITEM_HEIGHT}px`;
    return {
        transform: `translateY(${y})`,
        transition: shouldAnimate ? `transform ${CAROUSEL_MOVE_MS}ms linear` : "none"
    } as Record<string, string>;
}

async function loadNavItems() {
    try {
        const result = await getPcNavigationList({ page: 1, size: 10 });
        const titles = (result.records || [])
            .filter((item: any) => item.isShow !== false && item.title)
            .map((item: any) => String(item.title));
        if (titles.length) {
            navItems.value = titles.slice(0, 8);
        }
    } catch {
        // 接口异常时保留演示导航（由 NavPreviewBar 回退）
    }
}

async function loadShopLogo() {
    try {
        const cfg = await getConfigBasic();
        shopLogoUrl.value = String(cfg?.shopLogo || "");
    } catch {
        shopLogoUrl.value = "";
    }
}

onMounted(() => {
    recalcCarouselWidth();
    startCarouselTimers();
    loadShopLogo();
});

watch(
    () => benefitNoticeGroups.value,
    () => {
        recalcCarouselWidth();
        startCarouselTimers();
    },
    { deep: true }
);

onBeforeUnmount(() => {
    resetCarouselTimers();
});
</script>

<style lang="less" scoped>
@import "./components/_pcHeaderPreviewIcons.less";

.dec-pc-header-preview {
    position: relative;
    z-index: 20;
    border: 0;
}

.benefit-bar-wrap {
    height: 40px;
    line-height: 40px;
    min-width: 0;
    width: 100%;
    background: #1a1a1a;
    box-sizing: border-box;
    padding: 0 16px;
    overflow: hidden;
    /* 高于下方 header 主体，遮住底部分割阴影向上渗出的 blur */
    position: relative;
    z-index: 2;

    .w {
        margin: 0 auto;
        min-width: 0;
        width: 100%;

        .benefit-bar {
            display: flex;
            align-items: center;
            height: 40px;
            gap: clamp(12px, 3vw, 40px);

            &.benefit-bar--simple {
                justify-content: space-between;
                gap: 0;
            }

            &.benefit-bar--only-right {
                justify-content: flex-end;
                gap: 0;
            }

            .benefit-bar__left {
                flex-shrink: 0;
                display: flex;
                align-items: center;
                gap: 18px;
                min-width: 0;

                .top-link {
                    display: flex;
                    align-items: center;
                    color: #ffffffd6;
                    cursor: pointer;
                    line-height: 40px;
                    user-select: none;
                    white-space: nowrap;
                    font-size: 12px;
                    font-weight: 400;

                    &:hover {
                        color: var(--pc-header-top-menu-hover-color, #ffffff) !important;
                    }
                }
            }

            .benefit-bar__location {
                display: inline-flex;
                align-items: center;
                gap: 10px;
                color: #ffffffd6;
                font-size: 12px;
                font-weight: 600;
                min-width: 0;
                cursor: pointer;

                &::before {
                    content: "";
                    width: 12px;
                    height: 12px;
                    background: currentColor;
                    flex-shrink: 0;
                    mask-image: url("@/assets/icons/decorate/header-position.svg");
                    mask-size: contain;
                    mask-repeat: no-repeat;
                    mask-position: center;
                    -webkit-mask-image: url("@/assets/icons/decorate/header-position.svg");
                    -webkit-mask-size: contain;
                    -webkit-mask-repeat: no-repeat;
                    -webkit-mask-position: center;
                }
            }

            .benefit-bar__region {
                max-width: 240px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .benefit-bar__content {
                flex: 1;
                min-width: 0;
                display: flex;
                align-items: center;
                gap: clamp(12px, 3vw, 40px);
                color: #fff;
            }

            .tag-wrap {
                display: flex;
                align-items: center;
                color: inherit;

                .tag-wrap-inner {
                    display: flex;
                    align-items: center;
                }

                .img {
                    width: 14px;
                    height: 14px;
                }

                .txt {
                    display: flex;
                    flex-direction: row;
                    align-items: baseline;
                    line-height: 1;
                    margin-left: 4px;

                    .title {
                        font-size: 12px;
                        font-weight: 700;
                        margin-right: 6px;
                        white-space: nowrap;
                    }

                    .sub-title {
                        font-size: 12px;
                        font-weight: 500;
                        white-space: nowrap;
                        color: #ffffffcc;
                    }
                }
            }

            .benefit-bar__right {
                flex-shrink: 0;
                display: flex;
                align-items: center;
                justify-content: flex-end;
                gap: 18px;
                white-space: nowrap;

                .top-links {
                    display: inline-flex;
                    align-items: center;
                    gap: 18px;
                }

                .top-link {
                    display: flex;
                    align-items: center;
                    color: #ffffffd6;
                    cursor: pointer;
                    line-height: 40px;
                    user-select: none;
                    gap: 8px;

                    &:hover {
                        color: var(--pc-header-top-menu-hover-color, #ffffff) !important;
                    }
                }

                .top-link--nav {
                    font-weight: normal;
                }

                .top-link--cart {
                    font-weight: normal;
                }

                .top-bar-nav__arrow {
                    margin-left: 4px;
                    width: 12px;
                    height: 12px;
                    opacity: 1;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    align-self: center;
                    line-height: 1;
                }

                .top-bar-nav__name {
                    font-size: 12px;
                    font-weight: 600;
                    line-height: 40px;
                    white-space: nowrap;
                }
            }
        }
    }
}

.pc-carousel {
    height: 40px;
    overflow: hidden;
    min-width: 150px;
}

.pc-carousel__container {
    height: 40px;
    will-change: transform;
}

.pc-carousel__item {
    height: 40px;
    display: flex;
    align-items: center;
}
</style>

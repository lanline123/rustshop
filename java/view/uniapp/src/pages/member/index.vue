<template>
    <!-- 后台页面背景色 -->
    <page-meta :page-style="pageMetaStyle" />
    <tig-layout ref="layoutRef" @login-success="loginSuccess">
        <!-- 沉浸式：铺一层固定页面底色，与 userInfo3 渐变的渐隐目标色一致，保证顶部渐变到页面无缝过渡 -->
        <view v-if="immersive" class="immersive-page-bg" />
        <tig-navbar
            v-if="!isOverseasSkin"
            :show-left="false"
            :title="$t('会员中心')"
            :need-placeholder="!immersive"
            :background="navbarBackground"
            :color="navbarColor"
        />

        <template v-if="decorateLoaded">
            <!-- 装修模式：modules 非空时按装修渲染 -->
            <template v-if="hasDecorate">
                <view class="user" :class="{ 'user--immersive': immersive }" :style="immersive ? pageImmersiveStyle : pageGradientStyle">
                    <decorate :modules="modules" :immersive="immersive" />
                </view>
            </template>

            <!-- 兜底模式：装修无数据且为首页配置时显示原硬编码布局 -->
            <template v-else-if="showFallbackLayout">
                <view class="user" :class="{ 'overseas-legacy': isOverseasSkin }">
                    <view v-if="!isOverseasSkin" class="user_header user_bg_color">
                        <view class="picTxt">
                            <template v-if="!hasToken">
                                <view class="pic-btn-box" @click="handleLogin">
                                    <image class="pic-btn" mode="aspectFill" :src="staticResource('common/empty-img-bg3.png')" />
                                </view>
                            </template>
                            <template v-else>
                                <tig-upload class="pictrue" request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                                    <view class="pic-btn-box">
                                        <image class="pic-btn" :src="imageFormat(member!.avatar)" mode="aspectFill" />
                                    </view>
                                </tig-upload>
                            </template>
                            <view class="text">
                                <template v-if="!hasToken">
                                    <view class="name" @click="handleLogin">{{ $t("登录/注册") }}</view>
                                </template>
                                <template v-else>
                                    <view class="acea-row row-middle info">
                                        <template v-if="configStore.closeAuth === 1">
                                            <view v-if="member.isCompanyAuth" class="auth" @click="goPages('/pages/user/userCertification/index')">
                                                <image :src="staticResource('product/authenticated.svg')" mode="heightFix" />
                                            </view>
                                            <view v-else class="auth" @click="goPages('/pages/user/userCertification/index')">
                                                <image :src="staticResource('product/unverified.png')" mode="heightFix" />
                                            </view>
                                        </template>
                                        <view class="name line1">{{ member.nickname || member.username }}</view>
                                        <template v-if="member?.showSign">
                                            <view class="acea-row qiandao" @click="goPages('/pages/user/sign/index')">
                                                <view class="iconfont icon-qiandao" />
                                                <view class>{{ $t("签到有礼") }}</view>
                                            </view>
                                        </template>
                                        <view class="iconfont-h5 icon-shezhi" @click="goPages('/pages/user/profile/index')" />
                                    </view>
                                    <view class="logo acea-row row-middle" @click="isPro() && goRankPages('/pages/user/levelCenter/index')">
                                        <template v-if="isPro()">
                                            <image class="vip_logo" :src="member?.rankLogo" mode="widthFix" />
                                        </template>
                                        {{ member.rankName ? $t(member.rankName) : "" }}
                                    </view>
                                </template>
                            </view>
                        </view>
                        <view class="user_top_group">
                            <view class="item" @click="goPages('/pages/user/collectProduct/index')">
                                <view class="tit">
                                    {{ $t("商品收藏") }}
                                    <text class="txt">{{ orderNum.productCollect >= 0 ? orderNum.productCollect : "--" }}</text>
                                </view>
                            </view>
                            <view v-if="isMerchant() || isO2o()" class="item" @click="goPages('/pages/user/collectionShop/index')">
                                <view class="tit">
                                    {{ $t(isO2o() ? "门店关注" : "店铺收藏") }}
                                    <text class="txt">{{ orderNum.shopCollect >= 0 ? orderNum.shopCollect : "--" }}</text>
                                </view>
                            </view>
                            <view
                                v-if="layoutRef && layoutRef.isZh && configStore.productCommentEnabled === 1"
                                class="item"
                                @click="goPages('/pages/user/comment/list')"
                            >
                                <view class="tit">
                                    {{ $t("待评价") }}
                                    <text class="txt">{{ orderNum.awaitComment >= 0 ? orderNum.awaitComment : "--" }}</text>
                                </view>
                            </view>
                            <view class="item" @click="goPages('/pages/user/historyProduct/index')">
                                <view class="tit">{{ $t("浏览记录") }} </view>
                            </view>
                        </view>
                    </view>
                    <view v-else class="user_header user_bg_color overseas-user-header">
                        <view class="picTxt">
                            <template v-if="!hasToken">
                                <view class="pictrue" @click="handleLogin"><image class="pic-btn" :src="staticResource('common/default-user.png')" /></view>
                            </template>
                            <template v-else>
                                <tig-upload class="pictrue" request-url="user/user/modifyAvatar" @change="userStore.getUserInfo()">
                                    <image class="pic-btn" :src="imageFormat(member!.avatar)" mode="aspectFill" />
                                </tig-upload>
                            </template>
                            <view class="text">
                                <template v-if="!hasToken">
                                    <view class="name" @click="handleLogin">{{ $t("登录") }}</view>
                                    <view class="desc">{{ $t("为了获得最佳体验") }}</view>
                                </template>
                                <view v-else class="name line1">{{ member.nickname || member.username }}</view>
                                <view v-if="hasToken && member.rankName" class="logo">{{ $t(member.rankName) }}</view>
                            </view>
                        </view>
                    </view>
                    <view class="user_wrapper">
                        <!-- 跨境旧版兜底布局 -->
                        <template v-if="isOverseasSkin">
                            <template v-if="hasToken">
                                <view class="list-group my-order overseas-order">
                                    <view class="title acea-row row-between-wrapper">
                                        <view class="order-title">{{ $t("订单") }}</view>
                                        <view class="more" @click="goPages('/pages/user/order/list')">
                                            <view>{{ $t("查看更多") }}</view>
                                            <view class="iconfont icon-xiangyou" />
                                        </view>
                                    </view>
                                    <view class="wrap">
                                        <view class="li" @click="goPages('/pages/user/order/list?type=awaitPay')">
                                            <view class="content">
                                                <view v-if="orderNum.awaitPay > 0" class="num">{{ orderNum.awaitPay }}</view>
                                                <view class="icon iconfont-h5 icon-a-pending-payment" />
                                                <view class="txt">{{ $t("待付款") }}</view>
                                            </view>
                                        </view>
                                        <view class="li" @click="goPages('/pages/user/order/list?type=awaitReceived')">
                                            <view class="content">
                                                <view v-if="orderNum.awaitReceived > 0" class="num">{{ orderNum.awaitReceived }}</view>
                                                <view class="icon iconfont-h5 icon-dadao" />
                                                <view class="txt">{{ $t("进行中") }}</view>
                                            </view>
                                        </view>
                                        <view class="li" @click="goPages('/pages/user/order/list?type=awaitComment')">
                                            <view class="content">
                                                <view class="icon iconfont-h5 icon-complete" />
                                                <view class="txt">{{ $t("已完成") }}</view>
                                            </view>
                                        </view>
                                    </view>
                                </view>
                                <view class="overseas-term-pay"><termPayCard /></view>
                                <view class="overseas-other-menu">
                                    <view class="cell-wrap" @click="goPages('/pages/user/order/list?type=awaitComment')">
                                        <view class="icon iconfont-h5 icon-evaluate" />
                                        <view class="cell-name">{{ $t("评价") }}</view>
                                        <view class="right-icon"><text class="iconfont icon-xiangyou" /></view>
                                    </view>
                                    <view class="cell-wrap" @click="goPages('/pages/user/afterSale/list')">
                                        <view class="icon iconfont-h5 icon-after-sale" />
                                        <view class="cell-name">{{ $t("售后") }}</view>
                                        <view class="right-icon"><text class="iconfont icon-xiangyou" /></view>
                                    </view>
                                </view>
                            </template>
                            <view v-else class="login-btn-wrap overseas-login-btn">
                                <tig-button2 theme style="height: 75rpx" @click="handleLogin">{{ $t("登入/注册") }}</tig-button2>
                            </view>
                            <up-gap height="8" bg-color="#f5f6fa" />
                            <advertising v-if="legacyBanner && legacyBanner.picUrl" :data="legacyBanner" />
                            <myService v-if="legacyMenuList.length > 0" :data="legacyMenuList" :menu-type="legacyMenuType" />
                        </template>
                        <!-- 普通版旧版兜底布局 -->
                        <template v-else>
                            <view class="list-group my-order">
                                <view class="title acea-row row-between-wrapper">
                                    <view class="">{{ $t("我的订单") }}</view>
                                    <view class="more" @click="goPages('/pages/user/order/list')">
                                        <view>{{ $t("全部订单") }}</view>
                                        <view class="iconfont icon-xiangyou" />
                                    </view>
                                </view>
                                <view class="wrap">
                                    <view class="li" @click="goPages('/pages/user/order/list?type=awaitPay')">
                                        <template v-if="orderNum.awaitPay > 0">
                                            <uni-badge class="badgecolor" :text="orderNum.awaitPay" absolute="rightTop" size="small">
                                                <text class="iconfont-h5 icon-daifukuan" />
                                            </uni-badge>
                                            <view class="txt">{{ $t("待付款") }}</view>
                                        </template>

                                        <template v-else>
                                            <text class="iconfont-h5 icon-daifukuan" />
                                            <view class="txt">{{ $t("待付款") }}</view>
                                        </template>
                                    </view>
                                    <view class="li" @click="goPages('/pages/user/order/list?type=awaitShipping')">
                                        <template v-if="orderNum.awaitShipping > 0">
                                            <uni-badge class="badgecolor" :text="orderNum.awaitShipping" absolute="rightTop" size="small">
                                                <text class="iconfont-h5 icon-daifahuo" />
                                            </uni-badge>
                                            <view class="txt">{{ $t("待发货") }}</view>
                                        </template>
                                        <template v-else>
                                            <text class="iconfont-h5 icon-daifahuo" />
                                            <view class="txt">{{ $t("待发货") }}</view>
                                        </template>
                                    </view>
                                    <view class="li" @click="goPages('/pages/user/order/list?type=awaitReceived')">
                                        <template v-if="orderNum.awaitReceived > 0">
                                            <uni-badge class="badgecolor" :text="orderNum.awaitReceived" absolute="rightTop" size="small">
                                                <text class="iconfont-h5 icon-daishouhuo" />
                                            </uni-badge>
                                            <view class="txt">{{ $t("待收货") }}</view>
                                        </template>
                                        <template v-else>
                                            <text class="iconfont-h5 icon-daishouhuo" />
                                            <view class="txt">{{ $t("待收货") }}</view>
                                        </template>
                                    </view>
                                    <view
                                        v-if="configStore.productCommentEnabled === 1"
                                        class="li"
                                        @click="goPages('/pages/user/order/list?type=awaitComment')"
                                    >
                                        <template v-if="orderNum.awaitComment > 0">
                                            <uni-badge class="badgecolor" :text="orderNum.awaitComment" absolute="rightTop" size="small">
                                                <text class="iconfont-h5 icon-daipingjia" />
                                            </uni-badge>
                                            <view class="txt">{{ $t("待评价") }}</view>
                                        </template>
                                        <template v-else>
                                            <text class="iconfont-h5 icon-daipingjia" />
                                            <view class="txt">{{ $t("待评价") }}</view>
                                        </template>
                                    </view>
                                    <view class="li" @click="goPages('/pages/user/afterSale/list')">
                                        <template v-if="orderNum.awaitAftersalesCollect > 0">
                                            <uni-badge class="badgecolor" :text="orderNum.awaitAftersalesCollect" absolute="rightTop" size="small">
                                                <text class="iconfont-h5 icon-tuikuan" />
                                            </uni-badge>
                                            <view class="txt">{{ $t("退换/售后") }}</view>
                                        </template>
                                        <template v-else>
                                            <text class="iconfont-h5 icon-tuikuan" />
                                            <view class="txt line1">{{ $t("退换/售后") }}</view>
                                        </template>
                                    </view>
                                </view>
                            </view>
                            <view class="list-group my-wallet">
                                <view class="title acea-row row-between-wrapper">
                                    <view class="">{{ $t("我的钱包") }}</view>
                                    <view class="more" @click="goPages('/pages/user/account/index')">
                                        <view>{{ $t("进入钱包") }}</view>
                                        <view class="iconfont icon-xiangyou" />
                                    </view>
                                </view>
                                <view class="wrap">
                                    <view class="li" @click="goPages('/pages/user/account/index')">
                                        <text class="num">{{ Number(member.totalBalance) >= 0 ? member.totalBalance : 0 }}</text>
                                        <view class="txt">{{ $t("余额") }}</view>
                                    </view>
                                    <view class="li" @click="goPages('/pages/coupon/index?type=2')">
                                        <text class="num">{{ couponCount }}</text>
                                        <view class="txt">{{ $t("优惠券") }}</view>
                                    </view>
                                    <view class="li" @click="goPages('/pages/user/point/detail')">
                                        <text class="num">{{ member.points >= 0 ? member.points : 0 }}</text>
                                        <view class="txt">{{ $t(configStore.integralName) }}</view>
                                    </view>
                                </view>
                            </view>
                            <termPayCard />
                            <!-- 广告图（老装修兜底） -->
                            <advertising v-if="legacyBanner && legacyBanner.picUrl" :data="legacyBanner" />
                            <!-- 我的服务（老装修兜底） -->
                            <myService v-if="legacyMenuList.length > 0" :data="legacyMenuList" :menu-type="legacyMenuType" />
                        </template>
                    </view>
                </view>
            </template>
        </template>

        <recommend />
        <tig-copyright />
    </tig-layout>
    <tig-back-top :scroll-top="scrollTop" />
</template>

<script lang="ts" setup>
/**
 * 用户中心首页
 * - 装修非空时按装修模块渲染
 * - 装修为空时回退原硬编码布局
 */
import recommend from "@/components/recommend/index.vue";
import decorate from "./components/decorate/index.vue";
import advertising from "./components/decorate/advertising/index.vue";
import myService from "./components/decorate/myService/index.vue";
import { useUserStore } from "@/store/user";
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import type { UserItem } from "@/types/user/user";
import type { OrderNumItem } from "@/types/user/order";
import { getMemberDecorate, getMemberDecorateLegacy } from "@/api/home/home";
import { ensureMemberFixedModules } from "@/utils/ensureFixedModules";
import { staticResource, isPro, isO2o, isMerchant } from "@/utils";
import { handleLogin } from "@/utils/handleLogin";
import { useScrollTop } from "@/hooks";
import { imageFormat } from "@/utils/format";
import { handleDecorateLoadError } from "@/utils/handleDecorateLoadError";
import { useConfigStore } from "@/store/config";
import type { MemberDecorateModule } from "@/types/decorate/member-decorate";
import termPayCard from "./components/termPay/termPayCard.vue";

const { scrollTop } = useScrollTop();

const configStore = useConfigStore();

let isOverseasSkin = false;

const userStore = useUserStore();

const layoutRef = ref();

const member = computed({
    get() {
        return userStore.userInfo;
    },
    set(val) {
        return userStore.setUserInfo(val);
    }
});

/** 优惠券数量：与钱包组件、侧边菜单统一读取 userInfo.coupon */
const couponCount = computed(() => {
    const coupon = Number(member.value?.coupon);
    if (Number.isFinite(coupon) && coupon >= 0) return coupon;
    return 0;
});

const orderNum = computed<OrderNumItem>(() => userStore.orderNum || ({} as OrderNumItem));

const goPages = (url: string) => {
    uni.navigateTo({
        url
    });
};

const goRankPages = (url: string) => {
    if (isPro()) {
        uni.navigateTo({
            url
        });
    }
};

/** 重置用户信息，用于退出登录后清空页面数据 */
const resetUser = () => {
    member.value = {} as UserItem;
};

const loginSuccess = () => {
    fetchMemberDecorate();
    userStore.getOrderNum(true);
};

const hasToken = computed(() => {
    return userStore.token || uni.getStorageSync("token");
});

const previewId = ref<string | number>("");

const modules = ref<MemberDecorateModule[]>([]);
const pageModule = ref<Record<string, any>>({});
const decorateLoaded = ref(false);
// 是否展示硬编码兜底布局（订单/钱包等 + 老接口广告图、我的服务）
const showFallbackLayout = ref(false);
// 固定功能模块类型：装修为空时会被兜底注入，不能单独算作「有装修」
const FIXED_MODULE_TYPES = ["fixedFunctionalArea", "fixed-functional-area"];
// 仅当存在至少一个非固定功能模块时，才视为装修生效
const hasDecorate = computed(() => modules.value.some((m) => !FIXED_MODULE_TYPES.includes(m.type || "")));

// 页面标题是否沉浸式（来自后台页面设置 pageModule.immersive）
const immersive = computed(() => Boolean(pageModule.value?.immersive));

/** 顶部头部高度：跨境为 mainHeader 实测高度（fixed 后脱离文档流），国内为导航栏 */
const headerOffsetValue = computed(() => {
    return "var(--nav-height)";
});

/** 外贸皮肤 mainHeader 占位 */
const mainHeaderSpacerStyle = computed(() => {
    return undefined;
});

const pageGradientStyle = computed(() => {
    if (!hasDecorate.value) return {} as Record<string, string>;
    const first = modules.value[0];
    if (!first) return {} as Record<string, string>;
    const t = first.type;
    if (t !== "userInfo" && t !== "user-info") return {} as Record<string, string>;
    const mod = first.module as any;
    const ls = mod?.layoutStyle;
    if (ls !== 2 && ls !== 3) return {} as Record<string, string>;
    const g = mod?.gradient;
    const colorA = g?.colorA || "#fcf6fc";
    const colorB = g?.colorB || "#f3f3fe";
    return {
        background: `linear-gradient(180deg, ${colorA} 0%, ${colorB} 20%, #f5f5f5 45%)`,
        marginTop: `calc(-1 * ${headerOffsetValue.value})`,
        paddingTop: headerOffsetValue.value
    };
});

// 跨境外贸头背景：沉浸式随滚动渐显，否则取后台标题栏背景色，缺省白色
const headerBackground = computed(() => {
    if (immersive.value) {
        const progress = Math.min(1, scrollTop.value / 70);
        return `rgba(255, 255, 255, ${progress})`;
    }
    return pageModule.value?.titleBackgroundColor || "#fff";
});

const navbarBackground = computed(() => {
    const configBg = pageModule.value?.titleBackgroundColor;
    // 沉浸式：随滚动从透明渐显到白色
    if (immersive.value) {
        const progress = Math.min(1, scrollTop.value / 70);
        return `rgba(255, 255, 255, ${progress})`;
    }
    // 非沉浸式：优先取后台页面设置的标题栏背景颜色
    if (configBg) return configBg;
    if (Object.keys(pageGradientStyle.value).length === 0) {
        return "linear-gradient(90deg, #fcf6fc, #f3f3fe)";
    }
    const progress = Math.min(1, scrollTop.value / 70);
    return `rgba(255, 255, 255, ${progress})`;
});

// 导航栏文字颜色：取后台页面设置的标题栏文字颜色，缺省为黑色
const navbarColor = computed(() => pageModule.value?.titleColor || "#000");

// 样式 2/3/4 的默认渐变色
const STYLE_GRADIENT_DEFAULTS: Record<number, { colorA: string; colorB: string }> = {
    2: { colorA: "#E0FCED", colorB: "#FDF1F3" },
    3: { colorA: "#F1EFE9", colorB: "#E7F0E6" },
    4: { colorA: "#FBD9E1", colorB: "#FDEDEF" }
};

// 取首个 userInfo3 样式 2/3/4 的渐变色（仅沉浸式生效）
const gradientColors = computed(() => {
    if (!immersive.value) return null;
    const first = modules.value[0];
    if (!first) return null;
    const t = first.type;
    if (t !== "userInfo3" && t !== "user-info3") return null;
    const mod = first.module as any;
    const ls = Number(mod?.layoutStyle);
    // 样式 4 的渐变改由组件自身按「成长值卡一半高度」绘制，页面不再统一铺底
    if (ls !== 2 && ls !== 3) return null;
    const def = STYLE_GRADIENT_DEFAULTS[ls] ?? { colorA: "#fcf6fc", colorB: "#f3f3fe" };
    const g = mod?.gradient;
    return { colorA: g?.colorA || def.colorA, colorB: g?.colorB || def.colorB, gradientType: g?.gradientType || "leftRight" };
});
const GRADIENT_DIR_MAP: Record<string, string> = { upDown: "to bottom", leftRight: "to right", diagonal: "135deg" };

// 沉浸式：页面容器统一绘制「左右 + 上下渐隐」渐变，并延伸到导航栏下方（与 Admin 预览一致）
const IMMERSIVE_BAND = 880; // rpx
const pageImmersiveStyle = computed(() => {
    if (!immersive.value) return {} as Record<string, string>;
    const base: Record<string, string> = { paddingTop: headerOffsetValue.value };
    // 样式四：导航栏区域的渐变改由组件渐变层向上延伸绘制（整段连续，避免接缝），页面只保留占位 paddingTop
    const c = gradientColors.value;
    if (!c) return base;
    const dir = GRADIENT_DIR_MAP[c.gradientType || "leftRight"] || "to right";
    return {
        ...base,
        backgroundImage: `linear-gradient(to bottom, transparent 0, #f5f5f5 ${IMMERSIVE_BAND}rpx), linear-gradient(${dir}, ${c.colorA}, ${c.colorB})`,
        backgroundSize: `100% ${IMMERSIVE_BAND}rpx`,
        backgroundRepeat: "no-repeat",
        backgroundColor: "#f5f5f5"
    };
});

// 后台页面背景色
const pageMetaStyle = computed(() => {
    // 跨境旧版兜底沿用独立跨境 Uniapp 的白色页面底，不使用普通版渐变或后台残留背景色
    if (isOverseasSkin && showFallbackLayout.value) return "--member-page-bg:#fff";
    const bg = pageModule.value?.backgroundColor;
    if (!bg || immersive.value) return "";
    return `--member-page-bg:${bg}`;
});

/**
 * 把字符串化的 boolean / 数字回填为真实类型，避免下游判断失效
 */
const normalizeModuleValues = (raw: any): any => {
    if (raw === null || raw === undefined) return raw;
    if (Array.isArray(raw)) {
        return raw.map((v) => normalizeModuleValues(v));
    }
    if (typeof raw === "object") {
        const result: Record<string, any> = {};
        Object.keys(raw).forEach((key) => {
            result[key] = normalizeModuleValues(raw[key]);
        });
        return result;
    }
    if (typeof raw === "string") {
        if (raw === "true") return true;
        if (raw === "false") return false;
        if (raw !== "" && /^-?\d+(\.\d+)?$/.test(raw)) return Number(raw);
        return raw;
    }
    return raw;
};

/**
 * 从装修接口结果中取出页面设置 pageModule（兼容新/老结构），并规整布尔/数字
 */
const parsePageModule = (result: any): Record<string, any> => {
    let pm: any = result?.pageModule ?? result?.data?.pageModule ?? {};
    if (typeof pm === "string") {
        try {
            pm = JSON.parse(pm);
        } catch {
            pm = {};
        }
    }
    return normalizeModuleValues(pm) || {};
};

/**
 * 把模块数组规整为 MemberDecorateModule[]（兼容 module 为对象或 JSON 字符串）
 */
const mapModuleList = (list: any): MemberDecorateModule[] => {
    return (Array.isArray(list) ? list : []).map((m: any) => {
        let mod: any = m?.module;
        if (typeof mod === "string") {
            try {
                mod = JSON.parse(mod);
            } catch {
                /* ignore */
            }
        }
        return {
            type: m?.type,
            isShow: m?.isShow,
            fixed: m?.fixed,
            module: normalizeModuleValues(mod) || {}
        } as MemberDecorateModule;
    });
};

/**
 * 解析新接口装修模块（模块列表在 moduleList 字段）
 */
const parseModuleList = (result: any): MemberDecorateModule[] => mapModuleList(result?.moduleList);

/**
 * 老接口兜底数据：广告位 / 菜单（老协议「我的服务」）
 * 仅在新装修无数据时请求，渲染进下方硬编码兜底布局
 */
const legacyBanner = ref<Record<string, any> | null>(null);
const legacyMenuType = ref<number | string>("");
const legacyMenuList = ref<any[]>([]);

/**
 * 请求老接口 memberDecorate，取出广告位与菜单数据（兼容老用户）
 * - 老接口模块在 data 字段（老协议：广告位 bannerList / 菜单 menus）
 */
const fetchLegacyMemberDecorate = async () => {
    try {
        const result: any = await getMemberDecorateLegacy();
        const list = Array.isArray(result?.data) ? result.data : [];
        const banner = list.find((m: any) => m?.type === "bannerList");
        const menu = list.find((m: any) => m?.type === "menus");
        legacyBanner.value = banner?.module?.bannerList || null;
        legacyMenuType.value = menu?.module?.menuType ?? "";
        legacyMenuList.value = Array.isArray(menu?.module?.menuList) ? menu.module.menuList : [];
    } catch (error) {
        console.error("获取老版本装修配置失败:", error);
        legacyBanner.value = null;
        legacyMenuType.value = "";
        legacyMenuList.value = [];
    }
};

const fetchMemberDecorate = async () => {
    // 等待全局配置就绪，确保已拿到后端下发的 useNewMemberCenter 新旧版开关（避免冷启动/H5 刷新时读到默认值）
    await configStore.whenConfigReady();
    // 个人中心新旧版开关：useNewMemberCenter=0（旧版）时强制走兜底布局 + 老接口（广告位/我的服务），不使用新版装修
    if (Number(configStore.useNewMemberCenter) === 0) {
        modules.value = [];
        pageModule.value = {};
        showFallbackLayout.value = true;
        await fetchLegacyMemberDecorate();
        decorateLoaded.value = true;
        return;
    }
    try {
        const result: any = await getMemberDecorate(previewId.value);
        const parsed = parseModuleList(result);
        // 仅有固定功能模块（装修为空时被兜底注入）时，不算有效新装修，走下方硬编码兜底
        const hasRealModule = parsed.some((m) => !FIXED_MODULE_TYPES.includes(m.type || ""));
        if (hasRealModule) {
            modules.value = parsed;
            pageModule.value = parsePageModule(result);
            ensureMemberFixedModules(modules.value);
            showFallbackLayout.value = false;
        } else {
            modules.value = [];
            pageModule.value = {};
            // 仅当该会员页为首页配置时，才展示硬编码兜底布局并请求老接口补齐广告图与我的服务
            showFallbackLayout.value = result?.isHome === 1;
            if (showFallbackLayout.value) {
                await fetchLegacyMemberDecorate();
            }
        }
    } catch (error) {
        if (await handleDecorateLoadError(error)) return;
        console.error("获取装修配置失败:", error);
        modules.value = [];
        pageModule.value = {};
        showFallbackLayout.value = true;
        await fetchLegacyMemberDecorate();
    } finally {
        decorateLoaded.value = true;
    }
};

onLoad((options: any) => {
    previewId.value = options?.previewId ?? "";
    fetchMemberDecorate();
});

/**
 * onShow 刷新用户信息和订单数量
 * - 未登录时清空数据展示登录入口
 * - 已登录时刷新最新用户信息和订单统计
 */
onShow(() => {
    uni.hideTabBar({
        fail: () => {}
    });
    if (!hasToken.value) {
        resetUser();
        return;
    }
    Promise.all([userStore.getUserInfo(), userStore.getOrderNum()]);
});
</script>
<style>
page {
    /* #ifdef MP-WEIXIN */
    /* --member-page-bg 由 page-meta 下发的后台「页面背景色」，缺省回退默认渐变 */
    background: var(--member-page-bg, linear-gradient(90deg, #fcf6fc, #f3f3fe));
    /* #endif */
}
</style>
<style scoped lang="scss">
page {
    /* --member-page-bg 由 page-meta 下发的后台「页面背景色」，缺省回退默认渐变 */
    background: var(--member-page-bg, linear-gradient(90deg, #fcf6fc, #f3f3fe));
}

/* 外贸皮肤 mainHeader 为 fixed，高度由 inline style 写入 */
.main-header-spacer {
    width: 100%;
    flex-shrink: 0;
}

/* 沉浸式页面底色：固定铺满视口，作为 userInfo3 顶部渐变的渐隐目标 */
.immersive-page-bg {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: #f5f5f5;
    z-index: -1;
}

.user--immersive {
    position: relative;
    z-index: 1;
}

.user_bg_color {
    background: linear-gradient(90deg, #fcf6fc, #f3f3fe);
}
.user.overseas-legacy {
    background: #fff;
}
.overseas-user-header {
    background: #fff;
    .picTxt {
        padding: 32rpx;
    }
    .pictrue,
    .pic-btn {
        width: 110rpx;
        height: 110rpx;
        border-radius: 50%;
        overflow: hidden;
    }
    .text {
        margin-left: 24rpx;
    }
    .name {
        font-size: 32rpx;
        color: #1a1a1a;
    }
    .desc {
        margin-top: 10rpx;
        font-size: 23rpx;
        color: #1a1a1a;
    }
    .logo {
        margin-top: 12rpx;
        font-size: 22rpx;
        color: #888;
    }
}
.user .user_header.overseas-user-header .picTxt {
    padding-top: 16rpx !important;
    padding-bottom: 16rpx !important;
    padding-left: 32rpx !important;
    padding-right: 32rpx !important;
}
.user .user_header.overseas-user-header .picTxt .text .name {
    font-size: 32rpx;
    width: auto;
}
.user.overseas-legacy .user_wrapper {
    padding-left: 32rpx;
    padding-right: 32rpx;
}
.user.overseas-legacy :deep(.advertising-box) {
    padding: 0 0 30rpx;
    background: #fff;
}
.user.overseas-legacy :deep(.advertising-content) {
    border-radius: 18rpx;
    overflow: hidden;
}
.user.overseas-legacy :deep(.service-box) {
    margin: 0;
    border-radius: 0;
}
.user.overseas-legacy :deep(.service-title) {
    box-sizing: border-box;
    height: 75rpx;
    padding: 0;
}
.user.overseas-legacy :deep(.service-list.column) {
    padding: 0 0 20rpx;
}
.user.overseas-legacy :deep(.dec-account) {
    padding-left: 0;
    padding-right: 0;
}
.user.overseas-legacy :deep(.dec-account__container) {
    padding-left: 0;
    padding-right: 0;
}
.user.overseas-legacy .overseas-order .title,
.user.overseas-legacy .overseas-order .wrap,
.user.overseas-legacy .overseas-other-menu {
    padding-left: 0;
    padding-right: 0;
}
.overseas-order {
    background: #fff;
}
.user.overseas-legacy .overseas-order {
    margin: 0;
}
.user.overseas-legacy .overseas-order .title {
    font-size: 30rpx;
}
.user.overseas-legacy .overseas-order .more {
    font-size: 24rpx;
}
.user.overseas-legacy .overseas-order .more .iconfont {
    font-size: 24rpx;
}
.overseas-order .wrap {
    display: flex;
    position: relative;
    flex-flow: row nowrap;
    column-gap: 16rpx;
    padding-bottom: 0;
}
.user.overseas-legacy .overseas-order .wrap {
    padding-bottom: 0;
}
.overseas-order .wrap .li {
    display: flex;
    flex: 1;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200rpx;
    text-align: center;
    background: #f5f6fa;
    border-radius: 16rpx;
}
.overseas-order .wrap .li .content {
    position: relative;
    color: #1a1a1a;
}
.overseas-order .wrap .li .content .icon {
    font-size: 40rpx;
}
.overseas-order .wrap .li .content .txt {
    display: block;
    margin-top: 24rpx;
    font-size: 30rpx;
    font-weight: 500;
}
.user.overseas-legacy .overseas-order .wrap .li .content .txt {
    padding: 0;
    font-size: 30rpx;
}
.overseas-order .wrap .li .content .num {
    position: absolute;
    top: 25rpx;
    left: 108rpx;
    min-width: 25rpx;
    padding: 2rpx 6rpx;
    border-radius: 20rpx;
    color: #fff;
    background-color: var(--main-bg);
    font-size: 20rpx;
    text-align: center;
}
.overseas-other-menu {
    padding: 0 32rpx;
    background: #fff;
}
.overseas-other-menu .cell-wrap {
    display: flex;
    align-items: center;
    height: 108rpx;
    color: #1a1a1a;
    border-top: 1px solid #e5e7eb;
}
.overseas-other-menu .cell-wrap:first-child {
    border-top: 0;
}
.overseas-other-menu .icon {
    margin-right: 16rpx;
    font-size: 34rpx;
}
.overseas-other-menu .cell-name {
    font-size: 26rpx;
    font-weight: 500;
}
.overseas-other-menu .right-icon {
    margin-left: auto;
    color: #888b94;
}
.overseas-login-btn {
    padding: 23rpx 0;
    background: #fff;
}
.overseas-term-pay {
    padding: 30rpx 0;
    background: #fff;
}
.overseas-term-pay :deep(.delivery-card) {
    margin: 0;
}
.user.overseas-legacy .overseas-other-menu {
    margin-top: 0;
}
.user .user_header {
    _height: 280rpx;
    position: relative;
}
.user .user_header .picTxt {
    display: flex;
    padding: 0 20rpx;
    align-items: center;
}
.user .user_header .picTxt {
    .pic-btn-box {
        width: 110rpx;
        height: 110rpx;
        overflow: hidden;
        border-radius: 50%;
        flex-shrink: 0;

        .pic-btn {
            width: 100%;
            height: 100%;
            display: block;
            border-radius: 50%;
        }
    }
}
.user .user_header .picTxt .pictrue {
    overflow: hidden;
    width: 110rpx;
    height: 110rpx;
    box-sizing: border-box;
    border: 1rpx solid #f5f5f5;
}
.user .user_header .picTxt .text {
    flex: 1;
    min-width: 0;
    margin-left: 24rpx;
}
.user .user_header .picTxt .text .name {
    font-size: 28rpx;
    width: 240rpx;
}
.user .user_header .picTxt .text .member {
    padding: 0 20rpx;
    height: 32rpx;
    color: #333;
    font-size: 20rpx;
    border-radius: 30rpx;
    line-height: 32rpx;
    display: inline-block;
    margin-top: 10rpx;
}
.user .user_header .picTxt .text .logo {
    display: flex;
    align-items: center;
    height: 32rpx;
    color: #333;
    font-size: 20rpx;
    border-radius: 30rpx;
    line-height: 32rpx;
    margin-top: 20rpx;
}
.user .user_header .picTxt .text .vip_logo {
    margin-right: 5rpx;
    width: 30rpx;
    height: 22rpx;
}

.info {
    display: flex;
    align-items: center;
    margin-bottom: 10rpx;
    width: 100%;
}
/* 名字占据左侧，签到 / 设置图标推到右侧成组 */
.user .user_header .picTxt .text .info .name {
    width: auto;
    max-width: 240rpx;
    margin-right: auto;
}
.user .user_header .picTxt .text .auth {
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
.user .user_header .picTxt .text .member image {
    width: 28rpx;
    height: 28rpx;
    font-size: 20rpx;
    margin-right: 8rpx;
}
.user .user_header .picTxt .text .id {
    color: rgba(255, 255, 255, 0.6);
    font-size: 26rpx;
    margin-top: 15rpx;
}
.user .user_header .picTxt .text .id .iconfont {
    font-size: 30rpx;
    margin-left: 12rpx;
}
.user .user_header .picTxt .text .shanghu {
    color: rgba(255, 255, 255, 0.9);
    display: inline-block;
    padding: 0 20rpx;
    height: 36rpx;
    background-color: rgba(0, 0, 0, 0.2);
    font-size: 20rpx;
    border-radius: 30rpx;
    margin-left: 17rpx;
    line-height: 36rpx;
    position: absolute;
    top: 19rpx;
    right: 100rpx;
}
.user .user_header .picTxt .text .shanghu .iconfont {
    font-size: 24rpx;
    margin-right: 10rpx;
}
.user .user_header .icon-shezhi {
    display: inline-flex;
    align-items: center;
    margin-left: 10rpx;
    font-size: 28rpx;
    color: #333;
}

.user .user_header .qiandao {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    margin-right: 2rpx;
    transform: translateY(-1rpx);
}
.user .user_header .icon-qiandao {
    font-size: 24rpx;
    color: #333;
    padding-top: 3rpx;
    margin-right: 6rpx;
}

.user_top_group {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    margin-top: 30rpx;
}
.user_top_group .item {
    text-align: center;
    flex: 1;
    position: relative;
}
.user_top_group .item::after {
    content: "";
    position: absolute;
    top: 4rpx;
    right: 0;
    width: 2rpx;
    height: 26rpx;
    background: #dbd1dc;
}
.user_top_group .item:last-child::after {
    width: 0;
}
.user_top_group .item .tit {
    font-size: 24rpx;
}
.user_top_group .item .tit .txt {
    margin-left: 6rpx;
}

.user .user_wrapper {
    padding: 0 10px;
}
.user .user_wrapper .legacy-account {
    margin-top: -20rpx;
}
.user .user_wrapper .title {
    height: 88rpx;
    padding: 0 30rpx;
    font-size: 26rpx;
    font-weight: bold;
}
.user .user_wrapper .title .more {
    display: flex;
    align-items: center;
    font-size: 22rpx;
    color: #999999;
    font-weight: normal;
}
.user .user_wrapper .title .more .iconfont {
    font-size: 24rpx;
    padding-top: 4rpx;
}
.user .list-group {
    background: #fff;
    margin: 20rpx 0;
    border-radius: 18rpx;
    overflow: hidden;
}
.user .list-group .wrap .iconfont {
    font-size: 60rpx;
}
.user .list-group .wrap {
    display: flex;
    position: relative;
    flex-flow: row nowrap;
    padding-bottom: 30rpx;
}
.user .list-group .wrap .li {
    position: relative;
    flex: 1;
    text-align: center;

    .iconfont-h5 {
        color: #333;
        font-size: 60rpx;
    }
}
.user .list-group .wrap .li .txt {
    color: #333;
    font-size: 24rpx;
    position: relative;
}
.user.overseas-legacy .overseas-order .wrap .li .content .icon.iconfont-h5 {
    font-size: 40rpx;
}
.user.overseas-legacy .overseas-order .wrap .li .content {
    position: static;
}
.user.overseas-legacy .overseas-order .wrap .li .content .num {
    top: 25rpx;
    left: 108rpx;
    min-width: 25rpx;
    padding: 2rpx 6rpx;
    font-size: 20rpx;
    line-height: normal;
    background-color: #2f2f34;
}
.user.overseas-legacy .overseas-order .title .more {
    font-size: 24rpx;
}
.user .list-group.my-wallet .wrap {
    height: 120rpx;
}
.user .list-group.my-wallet .wrap .li .txt {
    top: 12rpx;
}
.user .list-group.my-wallet .wrap .li .num {
    font-weight: 700;
}
.premium-member-entry {
    display: flex;
    align-items: center;
    margin-top: 20rpx;
    padding: 24rpx 28rpx;
    border-radius: 18rpx;
    background: #fff;
}
.premium-member-entry__icon {
    margin-right: 20rpx;
    color: var(--general);
    font-size: 46rpx;
}
.premium-member-entry__content {
    min-width: 0;
    flex: 1;
}
.premium-member-entry__title {
    color: #222;
    font-size: 30rpx;
    font-weight: 600;
}
.premium-member-entry__desc {
    margin-top: 6rpx;
    color: #999;
    font-size: 23rpx;
}
.premium-member-entry__arrow {
    color: #999;
    font-size: 26rpx;
}
.user .list-group.my-order {
    position: relative;
}
.user .list-group.my-order .counts {
    border-radius: 20rpx;
    font-size: 20rpx;
    line-height: 27rpx;
    min-width: 28rpx;
    height: 28rpx;
    position: absolute;
    text-align: center;
    top: 30rpx;
    color: #f15353;
    border: 1rpx solid;
    padding: 0 3rpx;
    right: 55rpx;
    background: #fff;
}

.badgecolor {
    :deep(.uni-badge--error) {
        background-color: var(--general);
    }
}

.wrap {
    :deep(.uni-badge) {
        z-index: 50;
    }
}
</style>

<template>
    <div class="dec-pc-login">
        <!-- 登录页专属头部：对齐前台 CommonLoginHeader（logo + 欢迎语/注册互链），高度与阴影随配置变化 -->
        <div class="dec-pc-login__header" :style="headerStyle">
            <div class="dec-pc-login__header-inner">
                <img v-if="logoSrc" class="dec-pc-login__header-logo" :src="logoSrc" :style="logoStyle" alt="logo" />
                <div class="dec-pc-login__header-info" v-if="showWelcome">
                    <span>{{ $t("您好，欢迎光临商城！") }}</span>
                    <span class="dec-pc-login__header-link">{{ $t("没有账号？注册新用户！") }}</span>
                </div>
            </div>
        </div>

        <!-- 登录区：应用模块背景图/背景色配置（对齐前台 .login-warp） -->
        <div class="dec-pc-login__warp" :style="warpStyle">
            <!-- 经典版：右侧白卡（对齐前台 MemberLoginBaseLogin） -->
            <div v-if="loginMode === 1" class="dec-login-classic">
                <div class="dec-login-classic__card">
                    <div class="dec-login-classic__title-box">
                        <div class="dec-login-classic__title">{{ $t("欢迎登录") }}</div>
                        <div class="dec-login-classic__reg">{{ $t("没有账号？") }}<span>{{ $t("去注册") }}</span></div>
                    </div>
                    <div class="dec-login-classic__tabs">
                        <div class="dec-login-classic__tab is-active">{{ $t("普通登录") }}</div>
                        <div class="dec-login-classic__tab">{{ $t("手机登录") }}</div>
                    </div>
                    <div class="dec-login-classic__body">
                        <div class="dec-login-classic__input">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                                <circle cx="12" cy="7" r="4" />
                            </svg>
                            <span>{{ $t("用户名/手机") }}</span>
                        </div>
                        <div class="dec-login-classic__input">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" />
                                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                            </svg>
                            <span>{{ $t("密码") }}</span>
                        </div>
                        <div class="dec-login-classic__agreement">
                            <span class="dec-login-classic__checkbox"></span>
                            <span>{{ $t("我已阅读并同意《服务协议》和《隐私协议》") }}</span>
                        </div>
                        <div class="dec-login-classic__btn">{{ $t("登录") }}</div>
                        <div class="dec-login-classic__forget">{{ $t("忘记密码？") }}</div>
                        <div class="dec-login-classic__oauth">
                            <div class="dec-login-classic__oauth-tit">{{ $t("合作网站账号登录") }}</div>
                            <span class="dec-login-wechat dec-login-wechat--lg" v-html="WECHAT_SVG"></span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 现代版：居中表单卡（对齐前台 MemberLoginOverseasLogin） -->
            <div v-else class="dec-login-modern">
                <div class="dec-login-modern__content">
                    <div class="dec-login-modern__menu">
                        <div class="dec-login-modern__menu-item is-active">
                            {{ $t("账号登录") }}
                            <span class="dec-login-modern__menu-line"></span>
                        </div>
                        <div class="dec-login-modern__menu-item">{{ $t("手机号登录") }}</div>
                    </div>
                    <div class="dec-login-modern__form">
                        <div class="dec-login-modern__form-item">
                            <div class="dec-login-modern__label">{{ $t("用户名/手机") }}</div>
                            <div class="dec-login-modern__input">{{ $t("用户名/手机") }}</div>
                        </div>
                        <div class="dec-login-modern__form-item">
                            <div class="dec-login-modern__label">
                                <span>{{ $t("密码") }}</span>
                                <span class="dec-login-modern__label-special">{{ $t("忘记密码？") }}</span>
                            </div>
                            <div class="dec-login-modern__input">{{ $t("密码") }}</div>
                        </div>
                    </div>
                    <div class="dec-login-modern__btn-box">
                        <div class="dec-login-modern__btn">{{ $t("登录") }}</div>
                        <div class="dec-login-modern__divider">
                            <span class="dec-login-modern__divider-line"></span>
                            <span>{{ $t("或通过以下方式加入") }}</span>
                            <span class="dec-login-modern__divider-line"></span>
                        </div>
                        <div class="dec-login-modern__oauth">
                            <div class="dec-login-modern__oauth-item">
                                <span class="dec-login-wechat" v-html="WECHAT_SVG"></span>
                                <p>{{ $t("微信") }}</p>
                            </div>
                        </div>
                    </div>
                    <div class="dec-login-modern__register">
                        <span>{{ $t("还没有账号？") }}</span>
                        <span class="dec-login-modern__register-btn">{{ $t("去注册") }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, type Ref } from "vue";
import type { PcLoginModule } from "./types";
import { pcLoginDefaults } from "./types";
import { getConfigBasic } from "@/api/setting/config";
import { imageFormat } from "@/utils/format";

const module = defineModel<PcLoginModule>("module", { default: () => ({}) }) as Ref<PcLoginModule>;

const moduleConfig = computed<PcLoginModule>(() => ({
    ...pcLoginDefaults,
    ...module.value
}));

const loginMode = computed(() => moduleConfig.value.loginMode ?? 2);
// 头部欢迎语与登录/注册互链是否展示
const showWelcome = computed(() => moduleConfig.value.showWelcome ?? true);

// 商城 LOGO：取基础设置 shopLogo（跨境为黑底）；未配置则不展示，不回退默认图
const shopLogoUrl = ref("");
const logoSrc = computed(() => (shopLogoUrl.value ? imageFormat(shopLogoUrl.value) : ""));

// Logo 尺寸：固定宽高 + object-fit: contain，按原比例缩放进盒子，防止变形
const logoStyle = computed(() => ({
    maxWidth: `${moduleConfig.value.logoWidth ?? 140}px`,
    maxHeight: `${moduleConfig.value.logoHeight ?? 40}px`,
    objectFit: "contain" as const
}));

// 头部阴影（对齐前台 CommonLoginHeader）
const HEADER_SHADOW = "0 2px 12px 0 rgba(0, 0, 0, 0.04), 0 4px 6px 0 rgba(0, 0, 0, 0.06)";

// 头部高度/阴影按配置动态绑定
const headerStyle = computed(() => {
    const height = moduleConfig.value.headerHeight ?? 70;
    const shadow = moduleConfig.value.headerShadow ?? true;
    return {
        height: `${height}px`,
        boxShadow: shadow ? HEADER_SHADOW : "none"
    };
});

// 对齐前台 .login-warp：背景图居中、不平铺、原始尺寸；无配置时白底
const WARP_HEIGHT_MIN = 620;
const warpStyle = computed(() => {
    const bgPic = moduleConfig.value.backgroundPic?.picUrl;
    const warpHeight = Math.max(WARP_HEIGHT_MIN, Number(moduleConfig.value.warpHeight) || WARP_HEIGHT_MIN);
    return {
        backgroundColor: moduleConfig.value.backgroundColor || "#fff",
        backgroundImage: bgPic ? `url(${bgPic})` : "none",
        // 作为最小高度，下限 620，避免低于内容高度时调节无效果
        minHeight: `${warpHeight}px`
    };
});

const WECHAT_SVG = `
<svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg" fill="#09bb07" aria-hidden="true">
    <path d="M11.176 14.429c-2.665 0-4.826-1.8-4.826-4.018 0-2.22 2.159-4.02 4.824-4.02S16 8.191 16 10.411c0 1.21-.65 2.301-1.666 3.036a.324.324 0 0 0-.12.366l.218.81a.616.616 0 0 1 .029.117.166.166 0 0 1-.162.162.177.177 0 0 1-.092-.03l-1.057-.61a.519.519 0 0 0-.256-.074.509.509 0 0 0-.142.021 5.668 5.668 0 0 1-1.576.22ZM9.064 9.542a.647.647 0 1 0 .557-1 .645.645 0 0 0-.646.647.615.615 0 0 0 .09.353Zm3.232.001a.646.646 0 1 0 .546-1 .645.645 0 0 0-.644.644.627.627 0 0 0 .098.356Z"/>
    <path d="M0 6.826c0 1.455.781 2.765 2.001 3.656a.385.385 0 0 1 .143.439l-.161.6-.1.373a.499.499 0 0 0-.032.14.192.192 0 0 0 .193.193.213.213 0 0 0 .111-.036l1.268-.733a.622.622 0 0 1 .308-.088.62.62 0 0 1 .17.025 6.83 6.83 0 0 0 1.625.26 4.45 4.45 0 0 1-.177-1.251c0-2.936 2.785-5.02 5.824-5.02l.15.002C10.587 3.429 8.392 2 5.796 2 2.596 2 0 4.16 0 6.826Zm4.632-1.555a.77.77 0 1 1-1.54 0 .77.77 0 0 1 1.54 0Zm3.875 0a.77.77 0 1 1-1.54 0 .77.77 0 0 1 1.54 0Z"/>
</svg>
`;

const getShopLogo = async () => {
    try {
        const cfg = await getConfigBasic();
        // 标准版=商城LOGO；跨境=商城LOGO-黑底，字段均为 shopLogo
        shopLogoUrl.value = String(cfg?.shopLogo || "");
    } catch {
        shopLogoUrl.value = "";
    }
};

onMounted(async () => {
    getShopLogo();
});
</script>

<style lang="scss" scoped>
.dec-pc-login {
    width: 100%;
    background: #fff;
    // 主题色对齐前台 --general；编辑器无该变量时退回前台默认红
    --dec-login-general: var(--general, #e1251b);
}

/* ===== 专属头部（对齐前台 CommonLoginHeader）===== */
.dec-pc-login__header {
    background: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    z-index: 2;
}

.dec-pc-login__header-inner {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 15px 40px;
}

.dec-pc-login__header-logo {
    // 宽高由内联 style 控制（装修 logoWidth/logoHeight），此处仅保留防变形
    object-fit: contain;
}

.dec-pc-login__header-info {
    color: #666;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.dec-pc-login__header-link {
    color: var(--dec-login-general);
}

/* ===== 登录区容器（对齐前台 .login-warp）===== */
.dec-pc-login__warp {
    position: relative;
    z-index: 1;
    padding: 40px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-position: center;
    background-repeat: no-repeat;
}

/* ===== 经典版（对齐前台 BaseLogin：右侧白卡）===== */
.dec-login-classic {
    width: 100%;
    max-width: 1230px;
    padding: 0 20px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    &__card {
        background: #fff;
        min-width: 380px;
        min-height: 280px;
        padding: 20px 0 40px;
        border-radius: 8px;
        overflow: hidden;
        box-sizing: border-box;
    }

    &__title-box {
        display: flex;
        align-items: center;
        padding: 0 25px;
    }

    &__title {
        flex: 1;
        font-size: 20px;
        font-weight: bold;
        color: #333;
    }

    &__reg {
        font-size: 12px;
        color: #333;

        span {
            color: var(--dec-login-general);
        }
    }

    &__tabs {
        display: flex;
        margin: 10px 25px 0;
        border-bottom: 1px solid #e7e7e7;
    }

    &__tab {
        flex: 1;
        height: 46px;
        line-height: 46px;
        text-align: center;
        font-size: 14px;
        font-weight: bold;
        color: #333;
        position: relative;

        &.is-active {
            color: var(--dec-login-general);

            &::after {
                content: "";
                position: absolute;
                left: 0;
                right: 0;
                bottom: 0;
                height: 2px;
                background: var(--dec-login-general);
            }
        }
    }

    &__body {
        padding: 30px 25px 0;
    }

    &__input {
        display: flex;
        align-items: center;
        height: 36px;
        border: 1px solid #dcdfe6;
        border-radius: 3px;
        padding: 0 11px;
        gap: 8px;
        color: #a8abb2;
        font-size: 14px;
        background: #fff;

        & + & {
            margin-top: 20px;
        }

        svg {
            width: 20px;
            height: 20px;
            color: #9ca3af;
            flex-shrink: 0;
        }
    }

    &__agreement {
        padding-top: 12px;
        font-size: 12px;
        color: #606266;
        display: flex;
        align-items: center;
        gap: 6px;

        em {
            font-style: normal;
            color: var(--dec-login-general);
        }
    }

    &__checkbox {
        width: 14px;
        height: 14px;
        border: 1px solid #dcdfe6;
        border-radius: 2px;
        background: #fff;
        flex-shrink: 0;
        box-sizing: border-box;
    }

    &__btn {
        margin-top: 16px;
        height: 40px;
        border-radius: 3px;
        background: var(--dec-login-general);
        color: #fff;
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    &__forget {
        padding-top: 15px;
        text-align: center;
        color: #666;
        font-size: 12px;
    }

    &__oauth {
        margin-top: 14px;
        display: flex;
        flex-direction: column;
        align-items: center;
        color: #666;
        font-size: 12px;

        .dec-login-wechat {
            margin-top: 15px;
        }
    }

    &__oauth-tit {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        column-gap: 12px;

        &::before,
        &::after {
            content: "";
            height: 1px;
            flex: 1;
            background: #e5e5e5;
        }
    }
}

/* ===== 现代版（对齐前台 OverseasLogin：居中表单卡）===== */
.dec-login-modern {
    width: 100%;
    max-width: 1230px;
    padding: 0 20px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;

    &__content {
        background: #fff;
        min-width: 420px;
        padding: 20px 0 10px;
        box-sizing: border-box;
    }

    &__menu {
        display: flex;
        align-items: flex-end;
        justify-content: center;
        gap: 25px;
        margin-bottom: 40px;
    }

    &__menu-item {
        position: relative;
        height: 40px;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        font-size: 16px;
        color: #666;

        &.is-active {
            color: #1a1a1a;
        }
    }

    &__menu-line {
        position: absolute;
        left: calc(50% - 15px);
        bottom: -10px;
        width: 30px;
        height: 3px;
        border-radius: 10px;
        background: #1a1a1a;
    }

    &__form {
        padding: 0 20px;
    }

    &__form-item {
        display: flex;
        flex-direction: column;
        row-gap: 10px;
        margin-top: 20px;
    }

    &__label {
        font-weight: 600;
        color: #1a1a1a;
        padding-left: 2px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 14px;
    }

    &__label-special {
        font-weight: 500;
        color: var(--dec-login-general);
    }

    &__input {
        padding: 14px 15px;
        border: 1px solid #c2c4cc;
        border-radius: 4px;
        font-size: 15px;
        color: #505259;
        line-height: 1;
        background: #fff;
    }

    &__btn-box {
        padding: 20px;
        display: flex;
        flex-direction: column;
        align-items: center;
        row-gap: 15px;
    }

    &__btn {
        width: 100%;
        margin-top: 10px;
        height: 50px;
        border-radius: 3px;
        background: var(--dec-login-general);
        color: #fff;
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    &__divider {
        width: 100%;
        margin-top: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        font-size: 14px;
        color: #888b94;
    }

    &__divider-line {
        width: 22px;
        height: 1px;
        background: #c2c4cc;
    }

    &__oauth {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 60px;
        margin-top: 10px;
    }

    &__oauth-item {
        text-align: center;

        p {
            font-size: 14px;
            line-height: 18px;
            margin: 12px 0;
            color: #1a1a1a;
        }
    }

    &__register {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 5px 0 15px;
        font-size: 12px;
        color: #333;
    }

    &__register-btn {
        margin-left: 5px;
        color: var(--dec-login-general);
    }
}

/* 微信图标（v-html 注入的 svg 需穿透） */
.dec-login-wechat {
    display: inline-flex;

    :deep(svg) {
        width: 25px;
        height: 25px;
    }

    &--lg :deep(svg) {
        width: 32px;
        height: 32px;
    }
}
</style>

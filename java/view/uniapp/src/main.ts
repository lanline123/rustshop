import { createSSRApp } from "vue";
import * as Pinia from "pinia";
import App from "./App.vue";
import pinia from "@/store/index";
import { useConfigStore } from "@/store/config";
import { useI18nStore } from "@/store/i18n";
import "@/utils/permission";
import uviewPlus from "uview-plus";
import mixin from "@/utils/mixin";
import i18n from "@/i18n";
import { formatI18nParams, setTranslationFunction } from "@/utils/i18n";
import { isOverseas } from "@/utils";
import init from "@/utils/init";
import { handleLogin } from "@/utils/handleLogin";
import { configureRequestRuntime } from "@/utils/requestRuntime";

export function createApp() {
    const app = createSSRApp(App);
    app.use(pinia).use(i18n).use(uviewPlus).use(mixin);
    configureRequestRuntime({
        getClientType: () => useConfigStore(pinia).XClientType,
        getLocaleCode: () => (isOverseas() ? useI18nStore(pinia).langCode || uni.getLocale() : ""),
        handleLogin,
        initialize: init
    });
    const { t } = i18n.global;
    setTranslationFunction(t);

    // #ifdef MP-WEIXIN
    // 微信小程序端：部分场景 `$t(key, params)` 不会对 `{0}` 等占位符做替换，导致原样输出。
    // 全局包一层：先走 vue-i18n 翻译，再兜底做 `{0}` / `{name}` 替换。
    const rawT = i18n.global.t as any;
    const tCompat = (key: string, params?: unknown) => {
        const translated = rawT(key, params);
        return typeof translated === "string" ? formatI18nParams(translated, params) : translated;
    };
    (app.config.globalProperties as any).$t = tCompat;
    // #endif

    return {
        app,
        pinia,
        // nvue 依赖 createApp 返回的 Pinia 命名空间才能正确解析 createPinia / defineStore
        Pinia
    };
}

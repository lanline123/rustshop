import { createI18n } from "vue-i18n";
import messages from "@/locale/index";

const i18n = createI18n({
    legacy: false,
    // 非跨境版本默认使用中文，避免 vue-i18n 默认 locale(en) 覆盖本地中文词条
    locale: "zh",
    fallbackLocale: "zh",
    globalInjection: true,
    missing: (locale, key) => {
        return key;
    },
    // 使用新版配置项完全禁用警告
    missingWarn: false,
    fallbackWarn: false,
    // 保留旧版配置，以确保兼容性
    silentTranslationWarn: true,
    silentFallbackWarn: true,
    messages
});

export default i18n;

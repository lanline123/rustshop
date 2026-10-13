import { createI18n } from "vue-i18n";
import zh from "./locales/zh";

const i18n = createI18n({
    legacy: false,
    locale: localStorage.getItem("app-locale") || "zh",
    fallbackLocale: "zh",
    messages: { zh },
    missingWarn: import.meta.env.DEV,
    fallbackWarn: false,
    missing: (_locale, key) => key,
    postTranslation: (translated, key) => {
        if (translated === "" || translated == null) return String(key);
        return translated;
    }
});

// 跨标签页语言同步
const localeChannel = new BroadcastChannel("app-locale-sync");
localeChannel.onmessage = (event) => {
    if (event.data !== getLocale()) {
        setLocale(event.data, false).then(() => {
            window.location.reload();
        });
    }
};

export async function setLocale(locale: string, broadcast = true) {
    if (!(i18n.global.availableLocales as string[]).includes(locale)) {
        const messages = await import(`./locales/${locale}.ts`);
        i18n.global.setLocaleMessage(locale, messages.default);
    }
    (i18n.global.locale as unknown as { value: string }).value = locale;
    document.documentElement.setAttribute("lang", locale);
    localStorage.setItem("app-locale", locale);
    if (broadcast) {
        localeChannel.postMessage(locale);
    }
}

/** 应用启动时恢复持久化的语言（非中文时需异步加载语言包） */
export async function initLocale() {
    const saved = localStorage.getItem("app-locale");
    if (saved && saved !== "zh") {
        await setLocale(saved, false);
    } else {
        document.documentElement.setAttribute("lang", "zh");
    }
}

export function getLocale(): string {
    return (i18n.global.locale as unknown as { value: string }).value;
}

export default i18n;

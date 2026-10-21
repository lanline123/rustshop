/**
 * @description 多语言状态管理
 * - langList: 可用语言列表
 * - langCode: 当前语言代码
 * - 支持从本地缓存恢复语言设置
 * - 支持动态获取和切换语言数据包
 */
import { defineStore } from "pinia";
import { ref } from "vue";
import { getLang, getLangList, getDefaultLang } from "@/api/common";
import i18n from "@/i18n";
import { preprocessMessages, localMessages } from "@/locale/index";
import type { LangListItem } from "@/types/common";

/**
 * 将业务语言代码映射到 UniApp 框架支持的 locale。
 * 框架仅内置 zh-Hans / zh-Hant / en / fr / es，向 uni.setLocale 传入
 * 未识别的 code 会导致系统级文案（如 uni.chooseImage.sourceType.camera）
 * 渲染为原始 key；在 App 端某些机型上还可能触发框架内部 indexOf 报错。
 */
function toUniLocale(code: string): string {
    if (!code) return "zh-Hans";
    const c = code.toLowerCase().replace(/_/g, "-");
    if (/^zh[_-]?(hant|tw|hk|mo|cht)$/i.test(code)) return "zh-Hant";
    if (c.startsWith("zh")) return "zh-Hans";
    if (c.startsWith("fr")) return "fr";
    if (c.startsWith("es")) return "es";
    if (c.startsWith("en")) return "en";
    return "en";
}

const LOCALE_SYNCED_KEY = "__locale_synced";

/**
 * 同步 uni-app 框架 locale。Android 上 uni.setLocale 切换会触发整个 app 重启，
 * 因此用本地 storage 做幂等守卫，避免重启循环。
 */
function syncFrameworkLocale(localeCode: string) {
    const uniLocale = toUniLocale(localeCode);
    if (uni.getLocale() === uniLocale) return false;

    // #ifdef APP-PLUS
    const lastSynced = uni.getStorageSync(LOCALE_SYNCED_KEY);
    if (lastSynced === uniLocale) return false;
    uni.setStorageSync(LOCALE_SYNCED_KEY, uniLocale);
    // #endif

    uni.setLocale(uniLocale);
    return true;
}

/** 切换后是否会触发 App 端框架 locale 同步（Android 上会杀进程重启） */
export function willFrameworkLocaleChange(localeCode: string): boolean {
    return uni.getLocale() !== toUniLocale(localeCode);
}

export function clearLocaleSyncGuard() {
    // #ifdef APP-PLUS
    uni.removeStorageSync(LOCALE_SYNCED_KEY);
    // #endif
}

export { syncFrameworkLocale };

export const useI18nStore = defineStore("i18n", () => {
    const langList = ref<LangListItem[]>([]);

    const data = ref(uni.getStorageSync("localeLang") || null);

    const langCode = ref<string>(uni.getStorageSync("langCode") || "");

    /**
     * @description 检查本地是否有语言缓存
     * 如果有缓存则直接使用，避免重复请求
     * @returns 是否存在有效缓存
     */
    const isCache = () => {
        if (data.value && data.value.localeCode && data.value.langData) {
            const storedLangCode = langCode.value || uni.getStorageSync("langCode") || "";
            // langCode 已更新但 localeLang 未落盘（Android setLocale 中断切换）时，丢弃旧缓存
            if (storedLangCode && storedLangCode !== data.value.localeCode) {
                return false;
            }
            setLangData(data.value.localeCode, data.value.langData);
            setLangCode(data.value.localeCode);
            return true;
        }
        return false;
    };

    /**
     * @description 初始化语言设置
     * 如果本地有语言代码则获取对应语言包，否则获取默认语言配置
     */
    const init = async () => {
        if (!langCode.value) {
            await getDefaultLangConfig();
        } else {
            await getLangData(langCode.value, { clearLocaleGuard: true });
        }
    };

    /**
     * @description 获取可用语言列表
     */
    const getLangListData = async () => {
        try {
            const result = await getLangList();
            langList.value = result;
        } catch (error) {
            console.error(error);
        }
    };

    /**
     * @description 获取指定语言的数据包
     * @param localeCode 语言代码（如 zh-CN, en-US）
     * @param options.clearLocaleGuard 用户主动切换时清除 App 端 locale 同步守卫
     * @returns 是否需要在切换完成后 reLaunch（App 端框架 locale 变更会自行重启，返回 false）
     */
    const getLangData = async (localeCode: string, options?: { clearLocaleGuard?: boolean }): Promise<boolean> => {
        try {
            if (options?.clearLocaleGuard) {
                clearLocaleSyncGuard();
            }
            const needsFrameworkSync = willFrameworkLocaleChange(localeCode);
            const result = await getLang(localeCode);
            setLangData(localeCode, result);
            setLangCode(localeCode, { skipFrameworkSync: needsFrameworkSync });
            if (needsFrameworkSync) {
                syncFrameworkLocale(localeCode);
            }
            // #ifdef APP-PLUS
            return !needsFrameworkSync;
            // #endif
            // #ifndef APP-PLUS
            return true;
            // #endif
        } catch (error) {
            console.error(error);
            return false;
        }
    };

    /**
     * @description 获取默认语言配置（根据系统语言）
     */
    const getDefaultLangConfig = async () => {
        try {
            const result = await getDefaultLang(uni.getLocale());
            if (!result || !result.localeCode) {
                console.warn("Default language config is empty, skipping i18n init");
                return;
            }
            await getLangData(result.localeCode);
        } catch (error) {
            console.error(error);
        }
    };

    /**
     * @description 设置当前语言代码（同步到 storage 和 uni-app 框架）
     */
    const setLangCode = (localeCode: string, options?: { skipFrameworkSync?: boolean }) => {
        langCode.value = localeCode;
        uni.setStorageSync("langCode", localeCode);
        if (!options?.skipFrameworkSync) {
            syncFrameworkLocale(localeCode);
        }
    };

    /**
     * @description 设置语言数据包（更新 vue-i18n 与本地缓存）
     *
     * 关键：这里直接操作 `i18n.global`，而不是在 store setup 中调用 `useI18n()`。
     * `useI18n()` 依赖 active vue instance + 已 install 的 i18n plugin，
     * 任何在 `app.use(i18n)` 之前对 useI18nStore() 的访问都会让它失败，
     * 在 App(weex) 端可能直接表现为 createInstanceContext failed / 白屏。
     */
    const setLangData = (localeCode: string, langData: Record<string, string>) => {
        (i18n.global.locale as unknown as { value: string }).value = localeCode;
        const baseLocale = localeCode.startsWith("zh") ? "zh" : localeCode.startsWith("en") ? "en" : "zh";
        const merged = { ...(localMessages[baseLocale] || {}), ...langData };
        i18n.global.setLocaleMessage(localeCode, preprocessMessages(merged));
        const payload = { langData, localeCode };
        data.value = payload;
        uni.setStorageSync("localeLang", payload);
    };

    /**
     * @description 清除语言缓存
     */
    const clearCache = () => {
        langCode.value = "";
        data.value = null;
        langList.value = [];
        uni.removeStorageSync("langCode");
        uni.removeStorageSync("localeLang");
        clearLocaleSyncGuard();
    };

    return {
        langList,
        langCode,
        getLangData,
        getLangListData,
        getDefaultLangConfig,
        init,
        isCache,
        clearCache
    };
});

let tFunction: ((key: string) => string) | null = null;

export const setTranslationFunction = (t: (key: string) => string) => {
    tFunction = t;
};

export const t = (key: string): string => {
    if (tFunction) {
        return tFunction(key);
    }
    return key;
};

const escapeRegExp = (str: string) => str.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

/**
 * 兼容小程序端 `{0}` / `{name}` 这类占位符替换。
 * - `params` 为数组时替换 `{0}` `{1}`...
 * - `params` 为对象时替换 `{key}`...
 */
export const formatI18nParams = (text: string, params?: unknown): string => {
    if (!text || params == null) return text;

    let result = String(text);

    if (Array.isArray(params)) {
        params.forEach((val, idx) => {
            const reg = new RegExp(`\\{${idx}\\}`, "g");
            result = result.replace(reg, String(val));
        });
        return result;
    }

    if (typeof params === "object") {
        Object.entries(params as Record<string, unknown>).forEach(([k, v]) => {
            const reg = new RegExp(`\\{${escapeRegExp(k)}\\}`, "g");
            result = result.replace(reg, String(v));
        });
    }

    return result;
};

export const tFormat = (key: string, params?: unknown) => formatI18nParams(t(key), params);

/**
 * 登录 / SSO 回跳：只允许站内相对路径，并正确拆分 path 与 query。
 * 避免 next({ path: "/a?b=1" }) 把 ? 当 path 字面量导致 404。
 */

export function sanitizeInternalRedirect(redirect: unknown): string {
    let raw = Array.isArray(redirect) ? redirect[0] : redirect;
    if (typeof raw !== "string" || !raw) return "";
    try {
        if (raw.includes("%")) {
            raw = decodeURIComponent(raw);
        }
    } catch {
        // 非法编码则继续用原串校验
    }
    if (!raw.startsWith("/") || raw.startsWith("//")) return "";
    return raw;
}

/** 转为 vue-router 可用的 path + query（不含 hash） */
export function internalRedirectLocation(redirect: unknown): { path: string; query?: Record<string, string> } | null {
    const target = sanitizeInternalRedirect(redirect);
    if (!target) return null;
    const qi = target.indexOf("?");
    if (qi < 0) return { path: target };
    const path = target.slice(0, qi) || "/";
    const query = Object.fromEntries(new URLSearchParams(target.slice(qi + 1)));
    return { path, query };
}

/**
 * 仅 web-view 嵌入（embed）深链允许回跳目标页；其余登录一律回首页。
 * 返回 null 时调用方应跳转到首页。
 */
export function embedRedirectLocation(redirect: unknown): { path: string; query?: Record<string, string> } | null {
    const loc = internalRedirectLocation(redirect);
    if (loc && loc.query?.embed === "1") {
        return loc;
    }
    return null;
}

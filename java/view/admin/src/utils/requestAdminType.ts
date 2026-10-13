/** Convert UI identity names to the admin-type values understood by the API. */
export const normalizeRequestAdminType = (adminType: string | null | undefined): string => {
    if (adminType === "store") return "shop";
    return adminType || "admin";
};

/**
 * 收银台整个标签页以 pos 身份访问后端。
 * 按 pathname 判定而非路由状态：首屏取菜单、取用户信息等请求早于路由就绪，用路由状态会漏判。
 */
export const isPosWorkbenchPath = (pathname: string, baseDir?: string | null): boolean => {
    const base = `/${(baseDir ?? "").replace(/^\/+|\/+$/g, "")}`;
    let relative = pathname;
    if (base !== "/") {
        if (pathname === base) relative = "/";
        else if (pathname.startsWith(`${base}/`)) relative = pathname.slice(base.length);
    }
    return /^\/pos(\/|$)/.test(relative);
};

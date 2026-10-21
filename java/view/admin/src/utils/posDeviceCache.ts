const STORAGE_KEY_PREFIX = "tigshop:pos:boundDeviceCode";

export const getPosDeviceCacheKey = (shopId?: string | number | null): string => {
    const resolved = shopId ?? (typeof localStorage === "undefined" ? "" : localStorage.getItem("shopId"));
    return `${STORAGE_KEY_PREFIX}:${resolved || "0"}`;
};

export const getCachedPosDeviceCode = (shopId?: string | number | null): string => {
    try {
        return (localStorage.getItem(getPosDeviceCacheKey(shopId)) || "").trim();
    } catch {
        return "";
    }
};

export const setCachedPosDeviceCode = (deviceCode: string, shopId?: string | number | null): void => {
    const key = getPosDeviceCacheKey(shopId);
    const value = (deviceCode || "").trim();
    try {
        if (value) {
            localStorage.setItem(key, value);
        } else {
            localStorage.removeItem(key);
        }
    } catch {
        // 隐私模式等无法写入时忽略，不影响收银台其余功能
    }
};

export const withCachedPosDeviceCode = <T extends Record<string, unknown>>(payload: T): T & { deviceCode?: string } => {
    const deviceCode = getCachedPosDeviceCode();
    return deviceCode ? { ...payload, deviceCode } : payload;
};

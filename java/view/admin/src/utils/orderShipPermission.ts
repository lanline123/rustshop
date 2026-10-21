const getNumericValue = (value: unknown): number | null => {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
};

export const hasVendorOrderIdentity = (order: any): boolean => {
    const vendorId = getNumericValue(order?.vendorId);
    if (vendorId !== null && vendorId > 0) return true;
    return !!order?.vendorName;
};

export const getOrderShopId = (order: any): number => {
    const shopId = getNumericValue(order?.shopId);
    if (shopId !== null) return shopId;

    const nestedShopId = getNumericValue(order?.shop?.shopId);
    if (nestedShopId !== null) return nestedShopId;

    return 0;
};

export const isPlatformSelfOrder = (order: any): boolean => {
    return !hasVendorOrderIdentity(order) && getOrderShopId(order) === 0;
};

export const canCurrentShopWriteOffOrder = (order: any, adminType: string | null, currentShopId: unknown): boolean => {
    if (adminType === "admin") return isPlatformSelfOrder(order);
    if (adminType !== "shop" && adminType !== "pos") return false;

    const shopId = getNumericValue(currentShopId);
    if (shopId === null || shopId <= 0) return false;

    return getOrderShopId(order) === shopId;
};

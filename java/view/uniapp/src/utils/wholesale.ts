export interface WholesaleCardItem {
    firstWholesalePrice?: string | number | null;
    wholesaleCardMoq?: number | null;
}

export interface WholesaleSkuQuantity {
    skuId: number;
    num: number;
}

export interface WholesaleCartScope {
    shopId: number;
    productId: number;
}

export const WHOLESALE_MOQ_NOT_MET_HINT = "未达到起批量";

export const WholesaleErrorCode = {
    DISABLED: 5300,
    MOQ_NOT_MET: 5320,
    MULTIPLE_NOT_MET: 5321,
    PRICE_EXPIRED: 5322,
    MIXED_TIER_NOT_MET: 5323
} as const;

export interface WholesaleErrorAction {
    refreshIdentity: boolean;
    refreshPricing: boolean;
}

export const isWholesaleBusinessError = (code: number | undefined): boolean => {
    return typeof code === "number" && code >= 5300 && code < 5400;
};

export const getWholesaleErrorAction = (code: number | undefined): WholesaleErrorAction => ({
    refreshIdentity: code === 403 || code === WholesaleErrorCode.DISABLED,
    refreshPricing: code === WholesaleErrorCode.PRICE_EXPIRED
});

const positiveId = (value: unknown): number => {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : 0;
};

const nonNegativeShopId = (value: unknown): number | null => {
    const id = Number(value);
    return Number.isInteger(id) && id >= 0 ? id : null;
};

const requiredShopId = (shopId: unknown): number => {
    const id = nonNegativeShopId(shopId);
    if (id === null) throw new Error("wholesale shopId must be a non-negative integer");
    return id;
};

export const isWholesaleCard = (item: WholesaleCardItem | null | undefined, showWholesale: boolean): boolean => {
    return showWholesale && item?.firstWholesalePrice !== null && item?.firstWholesalePrice !== undefined;
};

export const canWholesaleQuickAdd = (item: WholesaleCardItem | null | undefined, showWholesale: boolean): boolean => {
    return isWholesaleCard(item, showWholesale) && item?.wholesaleCardMoq !== null && item?.wholesaleCardMoq !== undefined;
};

export const getWholesaleTierMatchQuantity = (
    priceMode: number | null | undefined,
    pickedQty: Record<number, number>,
    skuId: number | null | undefined
): number => {
    if (Number(priceMode) === 1) {
        return Object.values(pickedQty).reduce((total, qty) => total + Math.max(0, Number(qty) || 0), 0);
    }
    return skuId == null ? 0 : Math.max(0, Number(pickedQty[skuId]) || 0);
};

export const resolveWholesaleShopId = (detailShopId: unknown, requestedShopId: unknown): number => {
    const requested = nonNegativeShopId(requestedShopId);
    const detail = nonNegativeShopId(detailShopId);
    const hasRequestedShopId = requestedShopId !== null && requestedShopId !== undefined && requestedShopId !== "";
    const hasDetailShopId = detailShopId !== null && detailShopId !== undefined && detailShopId !== "";
    if (hasRequestedShopId && requested === null) {
        throw new Error(`invalid wholesale shopId: ${requestedShopId}`);
    }
    if (hasDetailShopId && detail === null) {
        throw new Error(`invalid wholesale shopId: ${detailShopId}`);
    }
    if (requested !== null && detail !== null && requested !== detail) {
        throw new Error(`wholesale shopId mismatch: requested ${requested}, received ${detail}`);
    }
    return requested ?? detail ?? 0;
};

export const buildWholesaleBuyRuleParams = (productId: number, shopId: number) => ({
    productId: positiveId(productId),
    shopId: requiredShopId(shopId)
});

export const buildWholesaleAmountPayload = (productId: number, shopId: number, skuItem: WholesaleSkuQuantity[]) => ({
    id: positiveId(productId),
    shopId: requiredShopId(shopId),
    skuItem
});

export const buildWholesaleAddPayload = (productId: number, shopId: number, skuItem: WholesaleSkuQuantity[]) => ({
    id: positiveId(productId),
    number: skuItem.reduce((total, item) => total + Math.max(0, Number(item.num) || 0), 0),
    shopId: requiredShopId(shopId),
    skuItem
});

export const submitWholesaleSelection = async <T>(options: {
    addToCart: (payload: ReturnType<typeof buildWholesaleAddPayload> & { isQuick: number }) => Promise<T>;
    productId: number;
    shopId: number;
    skuItem: WholesaleSkuQuantity[];
    isQuick: boolean;
}): Promise<T> => {
    return options.addToCart({
        ...buildWholesaleAddPayload(options.productId, options.shopId, options.skuItem),
        isQuick: options.isQuick ? 1 : 0
    });
};

export const wholesaleCartScopeKey = ({ shopId, productId }: WholesaleCartScope): string => {
    return `${requiredShopId(shopId)}:${positiveId(productId)}`;
};

export const createWholesaleIdentityGuard = () => {
    let version = 0;
    return {
        issue: () => ++version,
        invalidate: () => {
            version++;
        },
        isCurrent: (requestVersion: number) => requestVersion === version
    };
};

export interface WholesaleRequestSnapshot {
    generation: number;
    productId: number;
    shopId: number;
}

export const createWholesaleRequestSnapshotGuard = () => {
    let generation = 0;
    return {
        issue: (productId: number, shopId: number): WholesaleRequestSnapshot => ({
            generation: ++generation,
            productId: positiveId(productId),
            shopId: Number(shopId) || 0
        }),
        invalidate: () => {
            generation++;
        },
        isCurrent: (snapshot: WholesaleRequestSnapshot, productId: number, shopId: number): boolean =>
            snapshot.generation === generation && snapshot.productId === positiveId(productId) && snapshot.shopId === (Number(shopId) || 0)
    };
};

export const createWholesaleIdentityState = <T>(fetchIdentity: () => Promise<T>, defaultIdentity: () => T) => {
    let identity = defaultIdentity();
    let loaded = false;
    let pending: Promise<T> | null = null;
    const guard = createWholesaleIdentityGuard();

    const load = async (force = false): Promise<T> => {
        if (!force && loaded) return identity;
        if (pending) return pending;
        const requestVersion = guard.issue();
        const currentRequest = fetchIdentity()
            .then((result) => {
                if (guard.isCurrent(requestVersion)) {
                    identity = { ...defaultIdentity(), ...result };
                    loaded = true;
                }
                return identity;
            })
            .catch(() => {
                if (guard.isCurrent(requestVersion)) {
                    identity = defaultIdentity();
                    loaded = true;
                }
                return identity;
            })
            .finally(() => {
                if (pending === currentRequest) pending = null;
            });
        pending = currentRequest;
        return currentRequest;
    };

    const reset = () => {
        guard.invalidate();
        identity = defaultIdentity();
        loaded = false;
        pending = null;
    };

    return {
        get identity() {
            return identity;
        },
        get loaded() {
            return loaded;
        },
        load,
        reset
    };
};

export const createWholesaleAmountPreview = <Payload, Result>(options: {
    request: (payload: Payload) => Promise<Result>;
    onClear?: () => void;
    onSuccess: (result: Result) => void | Promise<void>;
    onError: (error: unknown) => void | Promise<void>;
}) => {
    let generation = 0;
    const load = async (payload: Payload) => {
        const requestGeneration = ++generation;
        try {
            const result = await options.request(payload);
            if (requestGeneration !== generation) return;
            await options.onSuccess(result);
        } catch (error) {
            if (requestGeneration !== generation) return;
            await options.onError(error);
        }
    };
    const invalidate = () => {
        generation++;
    };
    return { load, invalidate };
};

export const getRepresentativeMoq = (rule: { configured?: boolean; moq?: number } | null | undefined): number => {
    return rule?.configured && Number(rule.moq) > 0 ? Number(rule.moq) : 0;
};

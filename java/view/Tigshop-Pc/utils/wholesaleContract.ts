export interface WholesaleSkuQuantity {
    skuId: number;
    quantity: number;
}

const normalizeShopId = (shopId: number | null | undefined): number | null => {
    if (shopId === null || shopId === undefined) return null;
    const value = Number(shopId);
    return Number.isInteger(value) && value >= 0 ? value : null;
};

/** 显式销售门店优先；后端不得用另一个非零门店静默替换。 */
export const resolveEffectiveShopId = (responseShopId?: number | null, requestedShopId?: number | null): number => {
    const response = normalizeShopId(responseShopId);
    const requested = normalizeShopId(requestedShopId);
    if ((requested ?? 0) > 0 && (response ?? 0) > 0 && requested !== response) {
        throw new Error(`销售门店不一致：请求 ${requested}，响应 ${response}`);
    }
    if ((requested ?? 0) > 0) return requested!;
    return response ?? requested ?? 0;
};

export const buildWholesaleBuyRuleParams = (productId: number, shopId: number) => ({
    productId,
    shopId: normalizeShopId(shopId) ?? 0
});

/** 批发商品合计金额统一显示两位小数，不影响公共价格组件的其他使用场景。 */
export const normalizeWholesaleAmountText = (amount: string | number): string => {
    const value = Number(amount);
    return Number.isFinite(value) ? value.toFixed(2) : String(amount);
};

const buildSkuItem = (items: WholesaleSkuQuantity[]) => items.map(({ skuId, quantity }) => ({ skuId, num: quantity }));

export const buildWholesaleAmountPayload = (id: number, shopId: number, items: WholesaleSkuQuantity[]) => ({
    id,
    shopId: normalizeShopId(shopId) ?? 0,
    skuItem: buildSkuItem(items)
});

export const buildWholesaleAddToCartPayload = (id: number, shopId: number, items: WholesaleSkuQuantity[], isQuick: number) => ({
    id,
    number: items.reduce((sum, item) => sum + item.quantity, 0),
    shopId: normalizeShopId(shopId) ?? 0,
    skuItem: buildSkuItem(items),
    isQuick
});

export const wholesaleSpuScopeKey = (line: { shopId?: number; productId?: number }): string => `${normalizeShopId(line.shopId) ?? 0}:${Number(line.productId ?? 0)}`;

export const sumWholesaleSpuQuantities = (
    lines: Array<{
        shopId?: number;
        productId?: number;
        quantity?: number;
        wholesaleScope?: boolean;
        wholesaleMoqDimension?: 1 | 2;
    }>
): Record<string, number> => {
    const sums: Record<string, number> = {};
    for (const line of lines) {
        if (!line.wholesaleScope || line.wholesaleMoqDimension !== 1 || line.productId == null) continue;
        const key = wholesaleSpuScopeKey(line);
        sums[key] = (sums[key] ?? 0) + (line.quantity || 0);
    }
    return sums;
};

export const isCurrentIdentityResponse = (
    requestGeneration: number,
    currentGeneration: number,
    requestToken: string | null,
    currentToken: string | null
): boolean => requestGeneration === currentGeneration && requestToken === currentToken;

export const isWholesaleIdentityError = (error: any): boolean =>
    [403, 5300].includes(Number(error?.code ?? error?.data?.code ?? error?.response?.data?.code ?? error?.statusCode ?? error?.status));

export const isWholesaleIdentityEnabled = (identity: { globalEnabled?: boolean; isWholesaler?: boolean }): boolean =>
    identity.globalEnabled === true && identity.isWholesaler === true;

export type WholesaleIdentityRevisionAction = "ignore" | "clear" | "reload";

/**
 * 首次身份就绪只参与首屏批发态判定，不应触发第二次商品详情请求。
 * 页面已展示后，reset 先清数据；新身份加载完成后再按新账号重拉详情。
 */
export const resolveWholesaleIdentityRevisionAction = (
    hasRenderedProduct: boolean,
    identityLoaded: boolean
): WholesaleIdentityRevisionAction => {
    if (!hasRenderedProduct) return "ignore";
    return identityLoaded ? "reload" : "clear";
};

type WholesaleProductFields = {
    firstWholesalePrice?: string | number | null;
    wholesaleCardMoq?: number | null;
    wholesaleMoqDimension?: 1 | 2 | null;
    wholesaleMultiple?: number | null;
    wholesalePriceMode?: number | null;
    priceMode?: number | null;
};

/** 身份失效时立即移除账号相关的批发商品增量，避免旧等级数据再次显现。 */
export const clearWholesaleProductFields = <T extends WholesaleProductFields>(item: T): T => {
    delete item.firstWholesalePrice;
    delete item.wholesaleCardMoq;
    delete item.wholesaleMoqDimension;
    delete item.wholesaleMultiple;
    delete item.wholesalePriceMode;
    delete item.priceMode;
    return item;
};

type MutableSelectionRef<T> = { value: T };

interface WholesaleSelectionState {
    skuSelector: MutableSelectionRef<{ resetPicked?: () => void } | null>;
    frontSelection: MutableSelectionRef<Record<number, number>>;
    activeSkuId: MutableSelectionRef<number | null>;
    pickedQty: MutableSelectionRef<Record<number, number>>;
    pickedTotal: MutableSelectionRef<number>;
    pickedKinds: MutableSelectionRef<number>;
}

/** 原子清空选购器 UI 与父级选择态，避免作用域重挂后残留隐藏数量。 */
export const resetWholesaleSelectionState = (state: WholesaleSelectionState): void => {
    state.skuSelector.value?.resetPicked?.();
    state.frontSelection.value = {};
    state.activeSkuId.value = null;
    state.pickedQty.value = {};
    state.pickedTotal.value = 0;
    state.pickedKinds.value = 0;
};

/** 仅允许最近一次请求写入，用于身份、商品或门店作用域会变化的资源。 */
export const createLatestRequestRunner = <TPayload, TResult>(
    request: (payload: TPayload) => Promise<TResult>,
    apply: (result: TResult) => void,
    onError?: (error: unknown) => void | Promise<void>
) => {
    let generation = 0;
    const invalidate = () => {
        generation++;
    };
    const run = async (payload: TPayload): Promise<TResult | undefined> => {
        const current = ++generation;
        try {
            const result = await request(payload);
            if (current === generation) apply(result);
            return result;
        } catch (error) {
            if (current === generation && onError) await onError(error);
            return undefined;
        }
    };
    return { run, invalidate };
};

export const refreshWholesaleIdentityAfterError = async (
    error: unknown,
    identity: { reset: () => void; load: (force: boolean) => Promise<unknown> }
): Promise<boolean> => {
    if (!isWholesaleIdentityError(error)) return false;
    identity.reset();
    await identity.load(false);
    return true;
};

export type WholesaleCardAction = "wholesale-quick-add" | "retail-quick-add" | "detail";

export const resolveWholesaleCardAction = (
    item: { firstWholesalePrice?: string | number | null; wholesaleCardMoq?: number | null; productSku?: unknown[]; productType?: number },
    identityEnabled: boolean
): WholesaleCardAction => {
    const isWholesaleCard = identityEnabled && item.firstWholesalePrice != null;
    if (isWholesaleCard) return item.wholesaleCardMoq != null ? "wholesale-quick-add" : "detail";
    return (item.productSku?.length ?? 0) > 1 || [7, 8].includes(Number(item.productType)) ? "detail" : "retail-quick-add";
};

export type PreviewState<TResult> = { status: "idle" | "loading" | "success" | "error"; value: TResult | null; error?: unknown };

export const createLatestPreviewRunner = <TPayload, TResult>(
    request: (payload: TPayload) => Promise<TResult>,
    apply: (state: PreviewState<TResult>) => void
) => {
    let generation = 0;
    const invalidate = () => {
        generation++;
        apply({ status: "idle", value: null });
    };
    const run = async (payload: TPayload) => {
        const current = ++generation;
        const snapshot = typeof structuredClone === "function" ? structuredClone(payload) : payload;
        apply({ status: "loading", value: null });
        try {
            const result = await request(snapshot);
            if (current === generation) apply({ status: "success", value: result });
        } catch (error) {
            if (current === generation) apply({ status: "error", value: null, error });
        }
    };
    return { run, invalidate };
};

export const createWholesaleIdentityLoader = <TIdentity>(deps: {
    getToken: () => string | null;
    request: () => Promise<TIdentity>;
    apply: (identity: TIdentity) => void;
    reset: () => void;
}) => {
    let generation = 0;
    let pending: Promise<TIdentity | undefined> | null = null;
    let pendingToken: string | null = null;
    const reset = () => {
        generation++;
        pending = null;
        pendingToken = null;
        deps.reset();
    };
    const load = (force = false): Promise<TIdentity | undefined> => {
        if (force) reset();
        const token = deps.getToken();
        if (pending && pendingToken === token) return pending;
        const current = ++generation;
        pendingToken = token;
        const request = deps.request()
            .then((identity) => {
                if (isCurrentIdentityResponse(current, generation, token, deps.getToken())) deps.apply(identity);
                return identity;
            })
            .catch((error) => {
                if (isCurrentIdentityResponse(current, generation, token, deps.getToken())) deps.reset();
                throw error;
            })
            .finally(() => {
                if (pending === request) {
                    pending = null;
                    pendingToken = null;
                }
            });
        pending = request;
        return request;
    };
    return { load, reset };
};

import { ref } from "vue";
import { getPromotion } from "~/api/product/product";
import type { PromotionList } from "~/types/product/product.d";

export type PromotionActivityInfo = Record<string, unknown>;

type PromotionSourceItem = {
    id?: string | number;
    productId?: string | number;
    shopId?: string | number;
};

type FetchPromotionOptions = {
    from?: string;
    shopId?: string | number;
    merge?: boolean;
    // 跨店铺商品集合需显式声明，避免 fallback 误用首条商品的 shopId 污染请求，
    // 否则后端会按单店铺过滤掉其它店铺的拼团等活动
    ignoreItemShopId?: boolean;
};

export function usePromotionActivity() {
    const promotionList = ref<Record<string, PromotionList>>({});

    const getProductId = (item: PromotionSourceItem): string => {
        return String(item?.productId ?? item?.id ?? "");
    };

    const getActivityInfo = (productId: string | number): PromotionActivityInfo[] => {
        if (productId === undefined || productId === null || String(productId).trim() === "") return [];
        const activityInfo = promotionList.value[String(productId)]?.activityInfo;
        return Array.isArray(activityInfo) ? (activityInfo as PromotionActivityInfo[]) : [];
    };

    const clearPromotionList = (): void => {
        promotionList.value = {};
    };

    const fetchPromotionList = async (items: PromotionSourceItem[], options: FetchPromotionOptions = {}): Promise<void> => {
        const products = items
            .map((item) => getProductId(item))
            .filter((productId) => productId.trim() !== "")
            .map((productId) => ({ productId }));

        if (products.length === 0) {
            clearPromotionList();
            return;
        }

        const rawShopId = options.shopId ?? (options.ignoreItemShopId ? undefined : items.find((item) => item.shopId)?.shopId);

        try {
            const result = await getPromotion({
                products,
                shopId: rawShopId ? String(rawShopId) : undefined,
                from: options.from ?? "list"
            });

            promotionList.value = options.merge ? { ...promotionList.value, ...result } : (result as Record<string, PromotionList>);
        } catch {
            clearPromotionList();
        }
    };

    return {
        promotionList,
        getProductId,
        getActivityInfo,
        fetchPromotionList,
        clearPromotionList
    };
}

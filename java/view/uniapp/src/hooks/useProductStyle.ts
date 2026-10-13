import { ref, watch, type Ref } from "vue";
import { batchActiveStyle } from "@/api/product/productStyle";
import type { ProductActiveStyleVO } from "@/types/product/productStyle";

/**
 * 商品样式（边框/活动背景）composable
 * 监听商品列表变化，自动批量获取商品生效样式并合并到 styleMap
 */
export function useProductStyle(productList: Ref<any[]>) {
    const styleMap = ref<Record<number, ProductActiveStyleVO>>({});
    let lastFetchedKey = "";

    const getProductIds = (products: any[]) => products.map((p: any) => p.productId || p.id).filter(Boolean);

    const fetchStyles = async (products: any[]) => {
        if (!products || products.length === 0) return;
        const ids = getProductIds(products).join(",");
        if (!ids) return;
        try {
            const result = await batchActiveStyle(ids);
            if (result && typeof result === "object") {
                styleMap.value = { ...styleMap.value, ...result };
            }
        } catch {
            // 样式获取失败不影响主流程
        }
    };

    watch(
        () => productList.value,
        (newList) => {
            if (!newList || newList.length === 0) {
                styleMap.value = {};
                lastFetchedKey = "";
                return;
            }
            const ids = getProductIds(newList);
            const nextFetchedKey = ids.join(",");
            if (!nextFetchedKey) {
                styleMap.value = {};
                lastFetchedKey = "";
                return;
            }
            if (nextFetchedKey !== lastFetchedKey) {
                styleMap.value = {};
                lastFetchedKey = nextFetchedKey;
                fetchStyles(newList);
            }
        },
        { immediate: true }
    );

    const resetStyles = () => {
        styleMap.value = {};
        lastFetchedKey = "";
    };

    return { styleMap, fetchStyles, resetStyles };
}

export default useProductStyle;

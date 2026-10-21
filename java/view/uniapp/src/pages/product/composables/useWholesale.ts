import { ref, computed, type Ref } from "vue";
import { useWholesaleStore } from "@/store/wholesale";
import { getWholesaleBuyRule } from "@/api/wholesale/product";
import { getRepresentativeMoq } from "@/utils/wholesale";
import type { WholesalePriceTier } from "@/types/wholesale/price";
import type { BuyRule } from "@/types/wholesale/buyRule";
import type { ProductItem, SkuList } from "@/types/product/product";

/**
 * 批发阶梯展示模型（由 WholesaleSkuPopup 广播，供详情页外部阶梯表同源渲染）
 */
export interface WholesaleDisplayModel {
    tiers: { tierMinQty: number; qtyText: string; displayPrice: string }[];
    singleRange: { minPrice: string; maxPrice: string; isRange: boolean } | null;
}

export interface UseWholesaleOptions {
    /** 商品详情（来自 useProductDetail） */
    product: Ref<ProductItem>;
    /** SKU 列表（来自 useProductDetail） */
    skuList: Ref<SkuList[]>;
    /** SPU 兜底零售价（来自 useProductDetail） */
    productPrice: Ref<string>;
}

/**
 * 商品详情页批发能力 Composable（B2B 批发态）
 *
 * @description 承载批发身份判定、阶梯价、起批规则与展示模型；与零售 specification 互斥。
 * 整文件受 common.wholesale 门控，纯零售构建下被裁切，详情页对应引用与模板块同步裁切。
 */
export function useWholesale(options: UseWholesaleOptions) {
    const { product, skuList, productPrice } = options;

    /** 批发 store（身份 + 总开关） */
    const wholesaleStore = useWholesaleStore();
    /** 批发阶梯价表（取自详情接口扩展字段；仅批发商身份返回） */
    const wholesalePriceList = ref<WholesalePriceTier[]>([]);
    /** 批发多规格定价模式（详情接口 wholesalePriceMode）：1=混批 / 2=SKU级 / null=未配置 */
    const wholesaleProductPriceMode = ref<number | null>(null);
    /** 批发阶梯展示模型（由 WholesaleSkuPopup 广播，供外部阶梯表同源渲染） */
    const wholesaleDisplayModel = ref<WholesaleDisplayModel | null>(null);
    /** 起批规则（wholesale/product/buyRule），页面级拉取一次并下传弹层复用 */
    const wholesaleBuyRule = ref<BuyRule | null>(null);

    /**
     * 批发态判定：批发商身份 + 总开关 + 商品命中批发（wholesalePriceList 非空）
     * - 满足时详情页整体切换为批发形态，唤起 WholesaleSkuPopup、隐藏零售价与规格弹层
     * - 不满足时按 o2o 现有零售逻辑渲染（含 batchBuy tab），行为零变化
     */
    const isWholesaleMode = computed(() => {
        return wholesaleStore.showWholesale && wholesalePriceList.value.length > 0;
    });

    /** 零售最高价：各 SKU 原始零售价最高值，无 SKU 时退到 SPU 零售价 productPrice */
    const wholesaleRetailMax = computed<string>(() => {
        let maxStr = "";
        let maxN = -Infinity;
        for (const sku of skuList.value) {
            const raw = sku.skuPrice ?? sku.price;
            const n = Number(raw);
            if (raw == null || Number.isNaN(n)) continue;
            if (n > maxN) {
                maxN = n;
                maxStr = String(raw);
            }
        }
        if (maxStr === "" && productPrice.value && !Number.isNaN(Number(productPrice.value))) {
            maxStr = productPrice.value;
        }
        return maxStr;
    });

    const loadWholesaleBuyRule = async () => {
        const pid = product.value?.productId;
        if (!pid) return;
        try {
            const res = await getWholesaleBuyRule(pid);
            wholesaleBuyRule.value = res as unknown as BuyRule;
        } catch (err) {
            console.error("[wholesale] loadWholesaleBuyRule failed:", err);
        }
    };

    /** 代表性起批量（阶梯表首档起批量展示用；为 0 时阶梯表回退到档位门槛） */
    const wholesaleMoq = computed(() => getRepresentativeMoq(wholesaleBuyRule.value));

    /**
     * 从详情原始响应落地批发字段（普通会员响应缺该字段，前端自动降级为零售态）
     * @param result 详情接口原始响应
     */
    const populate = (result: any) => {
        const wr = (result ?? {}) as {
            wholesalePriceList?: WholesalePriceTier[];
            wholesalePriceMode?: number | null;
            item?: { wholesalePriceMode?: number | null };
        };
        wholesalePriceList.value = Array.isArray(wr.wholesalePriceList) ? wr.wholesalePriceList : [];
        wholesaleProductPriceMode.value = wr.wholesalePriceMode ?? wr.item?.wholesalePriceMode ?? null;
        if (isWholesaleMode.value && !wholesaleBuyRule.value) {
            loadWholesaleBuyRule();
        }
    };

    /** 批发加车成功回调：规格已在弹层内刷新，这里不强刷详情，避免视图闪烁 */
    const onWholesaleAddSuccess = () => {};

    return {
        wholesaleStore,
        wholesalePriceList,
        wholesaleProductPriceMode,
        wholesaleDisplayModel,
        wholesaleBuyRule,
        isWholesaleMode,
        wholesaleRetailMax,
        wholesaleMoq,
        loadWholesaleBuyRule,
        populate,
        onWholesaleAddSuccess
    };
}

export default useWholesale;

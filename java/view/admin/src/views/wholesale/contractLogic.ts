import type { PriceRow, SkuPriceForm, Ladder } from "@/types/wholesale/price";
import type { WholesaleProductBatchAddVO } from "@/types/wholesale/product";
import type { WholesalerImportResultVO } from "@/types/wholesale/wholesaler";

export const cloneSkuPriceDraft = (_skus: SkuPriceForm[]): SkuPriceForm[] => {
    return _skus.map((sku) => ({
        ...sku,
        generalLadders: sku.generalLadders.map((ladder) => ({ ...ladder })),
        gradeLadders: sku.gradeLadders.map((grade) => ({
            ...grade,
            ladders: grade.ladders.map((ladder) => ({ ...ladder }))
        }))
    }));
};

export const createBlankSkuPriceDraft = (_skus: SkuPriceForm[]): SkuPriceForm[] => {
    return cloneSkuPriceDraft(_skus).map((sku) => ({
        ...sku,
        generalLadders: sku.generalLadders.map((ladder) => ({ qty: ladder.qty, value: undefined as unknown as number })),
        gradeLadders: sku.gradeLadders.map((grade) => ({
            ...grade,
            ladders: grade.ladders.map((ladder) => ({ qty: ladder.qty, value: undefined as unknown as number }))
        }))
    }));
};

export type SkuPriceMode = "discount" | "fixed";
export type SkuPriceSnapshots = Record<SkuPriceMode, SkuPriceForm[]>;

export const transitionSkuPriceMode = ({
    previousMode,
    nextMode,
    currentSkus,
    snapshots
}: {
    previousMode: SkuPriceMode;
    nextMode: SkuPriceMode;
    currentSkus: SkuPriceForm[];
    snapshots: SkuPriceSnapshots;
}): { nextSkus: SkuPriceForm[]; nextSnapshots: SkuPriceSnapshots; nextPreviousMode: SkuPriceMode } => {
    const nextSnapshots: SkuPriceSnapshots = {
        discount: cloneSkuPriceDraft(snapshots.discount),
        fixed: cloneSkuPriceDraft(snapshots.fixed)
    };
    if (nextMode === previousMode) {
        return {
            nextSkus: cloneSkuPriceDraft(currentSkus),
            nextSnapshots,
            nextPreviousMode: previousMode
        };
    }
    nextSnapshots[previousMode] = cloneSkuPriceDraft(currentSkus);
    return {
        nextSkus: nextSnapshots[nextMode].length > 0 ? cloneSkuPriceDraft(nextSnapshots[nextMode]) : createBlankSkuPriceDraft(currentSkus),
        nextSnapshots,
        nextPreviousMode: nextMode
    };
};

export const buildUniformPriceRows = (_skus: SkuPriceForm[], _priceMode: 1 | 2): PriceRow[] => {
    const rows: PriceRow[] = [];
    const append = (skuId: number, gradeId: number, ladder: Ladder) => {
        rows.push({
            skuId,
            gradeId,
            tierMinQty: ladder.qty,
            priceMode: _priceMode,
            discount: _priceMode === 1 ? Number(ladder.value).toFixed(2) : null,
            wholesalePrice: _priceMode === 2 ? Number(ladder.value).toFixed(2) : null
        });
    };
    _skus.forEach((sku) => {
        sku.generalLadders.forEach((ladder) => append(sku.skuId, 0, ladder));
        sku.gradeLadders.forEach((grade) => grade.ladders.forEach((ladder) => append(sku.skuId, grade.gradeId, ladder)));
    });
    return rows;
};

export const resolveBatchAddSelection = (_failedItems: { productId: number; errorCode: number }[], _refreshedProductIds: number[]) => {
    const refreshed = new Set(_refreshedProductIds);
    return _failedItems.filter((item) => item.errorCode !== 5315 && refreshed.has(item.productId)).map((item) => item.productId);
};

export const summarizeBatchAddResult = (_result: WholesaleProductBatchAddVO) => {
    const failedItems = _result.failedItems || [];
    const skippedIds = _result.skippedIds || [];
    return {
        severity: failedItems.length > 0 || skippedIds.length > 0 ? "warning" as const : "success" as const,
        successCount: _result.successCount || 0,
        skippedCount: skippedIds.length,
        failedCount: failedItems.length,
        failedDetails: failedItems.map((item) => `${item.productId}（${item.errorCode}）：${item.errorMessage}`).join("；")
    };
};

export const summarizeImportResult = (_result: WholesalerImportResultVO) => {
    return {
        severity: _result.failedCount > 0 || _result.skippedCount > 0 ? "warning" as const : "success" as const,
        failedDetails: (_result.failedDetails || []).map((item) => `${item.userId ?? "--"}${item.errorCode != null ? `（${item.errorCode}）` : ""}：${item.errorMessage || item.reason || "--"}`).join("；")
    };
};

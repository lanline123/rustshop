/**
 * @file 商品 SKU 明细重算 Web Worker
 * @description WORKER 性能档位使用：在子线程执行 buildProductForm，避免大基数 SKU 重算冻结主线程。
 * @module utils/skuFormBuilder.worker
 */
import { buildProductForm } from "./skuFormBuilder";
import type { SpecGroup, ProductFormRow, BuildVariant } from "./skuFormBuilder";

self.onmessage = (ev: MessageEvent) => {
    const { id, speList, existingList, variant } = ev.data || {};
    try {
        const result: ProductFormRow[] = buildProductForm(speList as SpecGroup[], existingList as ProductFormRow[], {
            useMap: true,
            variant: (variant as BuildVariant) || "product"
        });
        (self as any).postMessage({ id, result });
    } catch (e: any) {
        (self as any).postMessage({ id, error: e?.message || String(e) });
    }
};

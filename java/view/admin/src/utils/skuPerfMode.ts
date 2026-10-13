/**
 * @file 商品 SKU 规格明细性能档位路由
 * @description
 *   多规格商品 SKU 明细在「笛卡尔积后条数」较大时，重算/校验/渲染都会变慢。
 *   这里按 SKU 条数把重算策略分为三档：
 *     - LEGACY(1)      : 老的 O(N²) 全量重算（find 匹配），用于对照基线
 *     - INCREMENTAL(2) : 用 Map 匹配的 O(N) 全量重算 + 分页校验
 *     - WORKER(3)      : 在 Web Worker 里跑重算，避免冻结主线程
 *   实际档位由 `resolveSkuPerfMode(skuCount)` 按条数自动分档。
 * @module utils/skuPerfMode
 */

export enum SkuPerfMode {
    /** 自动：按 SKU 条数分档（占位；resolveSkuPerfMode 不会返回该值） */
    AUTO = 0,
    /** 老分支：O(N²) 全量重算 */
    LEGACY = 1,
    /** 增量分支：O(N) 全量重算 + 分页校验 */
    INCREMENTAL = 2,
    /** Worker 分支：重算放子线程 */
    WORKER = 3
}

/** 启用前端分页 UI 的阈值：SKU 条数超过该值才分页 */
export const SKU_PAGINATION_THRESHOLD = 50;

/** 自动分档：超过该条数启用增量算法 */
export const AUTO_INCREMENTAL_THRESHOLD = 500;
/** 自动分档：超过该条数启用 Web Worker */
export const AUTO_WORKER_THRESHOLD = 2000;

/**
 * 根据 SKU 条数解析出实际使用的性能档位。
 * @param skuCount 当前 productList 的条数
 * @returns 实际生效的性能档位（不会返回 AUTO）
 */
export function resolveSkuPerfMode(skuCount: number): SkuPerfMode {
    if (skuCount > AUTO_WORKER_THRESHOLD) return SkuPerfMode.WORKER;
    if (skuCount > AUTO_INCREMENTAL_THRESHOLD) return SkuPerfMode.INCREMENTAL;
    return SkuPerfMode.LEGACY;
}

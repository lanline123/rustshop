/**
 * @file 商品多规格 SKU 明细（productList）重算逻辑
 * @description
 *   从 Attr.vue 抽出的纯函数版「规格笛卡尔积 → SKU 明细」重算逻辑，供三种性能档位复用：
 *     - LEGACY: buildProductForm(spe, existing, { useMap: false }) —— O(N²)，find 匹配，行为与老代码一致
 *     - INCREMENTAL: buildProductForm(spe, existing, { useMap: true }) —— O(N)，Map 匹配
 *     - WORKER: buildProductFormInWorker(...) —— 在 Web Worker 内跑同一套逻辑
 *   三档产出结果一致，仅重算路径/耗时不同，便于对照测试。
 * @module utils/skuFormBuilder
 */

/** 规格值项 */
export interface SpecAttrValue {
    attrName?: string;
    attrValue: string;
    [key: string]: any;
}

/** 规格组（如 颜色 / 尺寸）。子集合字段兼容两种：product 侧用 attrList；supplier 侧用 attrs */
export interface SpecGroup {
    attrName: string;
    attrList?: SpecAttrValue[];
    attrs?: SpecAttrValue[];
    [key: string]: any;
}

/**
 * SKU builder 变体：
 * - product  : 商家侧商品编辑（child key = attrList；携带 skuPrice/marketPrice/costPrice/...）
 * - supplier : 供应商侧商品编辑（child key = attrs；携带 supplyPrice/supplyPriceLimitVal/supplyPriceLimitType/skuWeight/...）
 * - vendor   : 平台代客（供应商）商品编辑（child key = attrList；规格不可改，仅保留 skuStock/skuSn/skuTsn/skuPrice/supplyPrice/vendorProductSkuId）
 */
export type BuildVariant = "product" | "supplier" | "vendor";

/** SKU 明细行 */
export interface ProductFormRow {
    attrNames: string[];
    attrValues: string[];
    attrs: { attrName: string; attrValue: string }[];
    skuValue: string;
    skuId?: number;
    skuUnit?: any;
    skuStock?: any;
    skuSn?: string;
    skuTsn?: string;
    skuPrice?: any;
    marketPrice?: any;
    costPrice?: any;
    isEnabled?: 0 | 1;
    __dirty?: boolean;
    [key: string]: any;
}

/**
 * 生成规格值的笛卡尔积组合（与 Attr.vue 原 getCombinations 行为一致）。
 * 会在返回值里为每个规格值补全其所属规格组的 attrName（不改动入参）。
 * @param speList 规格组列表
 * @param childKey 子集合字段名，默认 attrList；供应商侧传 attrs
 */
export function getCombinations(speList: SpecGroup[], childKey: "attrList" | "attrs" = "attrList"): SpecAttrValue[][] {
    if (!Array.isArray(speList) || speList.length === 0) {
        return [[]];
    }
    // 归一化 + 补全 attrName（操作副本，避免污染入参）
    const groups = speList.map((g) => {
        const raw = (g as any)[childKey];
        return {
            attrName: g.attrName,
            attrList: Array.isArray(raw) ? raw.map((a: SpecAttrValue) => ({ ...a, attrName: g.attrName })) : []
        };
    });
    const first = groups[0];
    const rest = groups.slice(1);
    const restCombinations = getCombinations(rest as SpecGroup[], "attrList");
    const combinations: SpecAttrValue[][] = [];
    for (let i = 0; i < first.attrList.length; i++) {
        for (let j = 0; j < restCombinations.length; j++) {
            combinations.push([first.attrList[i], ...restCombinations[j]]);
        }
    }
    return combinations;
}

/**
 * 依据规格结构 speList 重算 SKU 明细，并尽量保留 existingList 中已填写的价格/库存/skuId。
 * 匹配规则与老代码一致：先按位置(idx)带出旧值，再用 skuValue 精确匹配覆盖（含 skuId）。
 *
 * @param speList 规格结构（attrList.spe，需为可序列化的普通数据）
 * @param existingList 现有 productList（普通数据）
 * @param options.useMap 是否用 Map 做 skuValue 匹配（O(N)）；false 则用 find（O(N²)，老行为）
 */
export function buildProductForm(
    speList: SpecGroup[],
    existingList: ProductFormRow[],
    options: { useMap?: boolean; variant?: BuildVariant } = {}
): ProductFormRow[] {
    const useMap = options.useMap !== false;
    const variant: BuildVariant = options.variant || "product";
    const childKey: "attrList" | "attrs" = variant === "supplier" ? "attrs" : "attrList";
    if (!Array.isArray(speList) || speList.length === 0) {
        return [];
    }
    const existing = Array.isArray(existingList) ? existingList : [];
    const list = getCombinations(speList, childKey);

    // 按 skuValue 分桶（supplier 额外把 skuAttrVal 作为别名键），桶内保持 existing 原顺序。
    // 当多个规格值文字完全相同、skuValue 重复时，需要按出现顺序一一对应，而不是都命中第一行。
    let matchBuckets: Map<string, ProductFormRow[]> | null = null;
    if (useMap) {
        matchBuckets = new Map();
        for (const item of existing) {
            if (!item) continue;
            const keys: (string | undefined)[] =
                variant === "supplier" ? [item.skuValue as string | undefined, (item as any).skuAttrVal as string | undefined] : [item.skuValue as string | undefined];
            // 同一 item 的多个别名键只入一次对应桶，避免同一行被放大成多份
            const seen = new Set<string>();
            for (const k of keys) {
                if (k == null || seen.has(k)) continue;
                seen.add(k);
                const bucket = matchBuckets.get(k);
                if (bucket) bucket.push(item);
                else matchBuckets.set(k, [item]);
            }
        }
    }

    // 游标：记录每个 skuValue 已消费到桶内第几个；消费完后固定返回最后一个作为兜底
    const matchCursor = new Map<string, number>();
    const consumeFromBucket = (skuValue: string): ProductFormRow | undefined => {
        const bucket = matchBuckets!.get(skuValue);
        if (!bucket || bucket.length === 0) return undefined;
        const pos = matchCursor.get(skuValue) ?? 0;
        matchCursor.set(skuValue, pos + 1);
        return bucket[Math.min(pos, bucket.length - 1)];
    };

    // find 档（LEGACY）同样按顺序消费：优先返回尚未被占用的匹配行，全部占用后返回最后一个匹配
    const usedIndex = new Set<number>();
    const findMatchSequential = (skuValue: string): ProductFormRow | undefined => {
        let lastMatch: ProductFormRow | undefined;
        for (let i = 0; i < existing.length; i++) {
            const it = existing[i];
            if (!it) continue;
            const hit = variant === "supplier" ? it.skuValue === skuValue || (it as any).skuAttrVal === skuValue : it.skuValue === skuValue;
            if (!hit) continue;
            lastMatch = it;
            if (!usedIndex.has(i)) {
                usedIndex.add(i);
                return it;
            }
        }
        return lastMatch;
    };

    const findMatch = (skuValue: string): ProductFormRow | undefined => {
        if (matchBuckets) return consumeFromBucket(skuValue);
        return findMatchSequential(skuValue);
    };

    const res: ProductFormRow[] = [];
    for (let idx = 0; idx < list.length; idx++) {
        const combo = list[idx];
        const row: ProductFormRow = { attrNames: [], attrValues: [], attrs: [], skuValue: "" };
        const parts: string[] = [];
        for (let i = 0; i < combo.length; i++) {
            row.attrs[i] = { attrName: combo[i].attrName as string, attrValue: combo[i].attrValue };
            parts.push(combo[i].attrName + ":" + combo[i].attrValue);
            row.attrNames.push(combo[i].attrName as string);
            row.attrValues.push(combo[i].attrValue);
        }
        row.skuValue = parts.join("|");

        if (variant === "supplier") {
            // 默认值：与供应商 Attr 老 getProductForm 一致
            row.skuStock = "";
            row.skuWeight = "";
            row.supplyPriceLimitVal = "";
            row.supplyPrice = "";
            row.supplyPriceLimitType = 1;
            const match = findMatch(row.skuValue);
            if (match) {
                row.skuStock = match.skuStock;
                row.skuWeight = (match as any).skuWeight;
                row.supplyPriceLimitType = (match as any).supplyPriceLimitType;
                row.skuSn = match.skuSn;
                row.skuTsn = match.skuTsn;
                row.supplyPrice = (match as any).supplyPrice;
                row.supplyPriceLimitVal = (match as any).supplyPriceLimitVal;
            }
        } else if (variant === "vendor") {
            const match = findMatch(row.skuValue);
            if (match) {
                row.skuStock = match.skuStock;
                row.skuSn = match.skuSn;
                row.skuTsn = match.skuTsn;
                row.skuPrice = match.skuPrice;
                row.supplyPrice = (match as any).supplyPrice;
                (row as any).vendorProductSkuId = (match as any).vendorProductSkuId;
            }
        } else {
            // 商品 SKU 默认启用；状态只允许由 skuValue 精确匹配继承，不能按位置错配。
            row.isEnabled = 1;
            // 位置带出（与老 product Attr 代码一致：按 idx 从旧列表取值）
            const posRef = existing[idx];
            if (posRef) {
                row.skuPrice = posRef.skuPrice;
                row.skuStock = posRef.skuStock;
                row.marketPrice = posRef.marketPrice;
                row.costPrice = posRef.costPrice;
                row.skuSn = posRef.skuSn;
                row.skuTsn = posRef.skuTsn;
            }
            const match = findMatch(row.skuValue);
            if (match) {
                row.skuUnit = match.skuUnit;
                row.skuStock = match.skuStock ?? 0;
                row.skuSn = match.skuSn || "";
                row.skuTsn = match.skuTsn || "";
                row.skuPrice = match.skuPrice || "";
                row.marketPrice = match.marketPrice || "";
                row.costPrice = match.costPrice || "";
                row.skuId = match.skuId;
                row.isEnabled = match.isEnabled === 0 ? 0 : 1;
                if (match.__dirty) row.__dirty = true;
            }
        }
        res.push(row);
    }
    // 规格值被删除后，对应 SKU 同步移出 productList；仅当前笛卡尔积仍存在的行才保留历史字段和状态。
    return res;
}

let _worker: Worker | null = null;
let _seq = 0;

/** 懒创建 Worker（Vite 原生 Worker 语法） */
function getWorker(): Worker {
    if (!_worker) {
        _worker = new Worker(new URL("./skuFormBuilder.worker.ts", import.meta.url), { type: "module" });
    }
    return _worker;
}

/**
 * 在 Web Worker 中重算 SKU 明细（WORKER 档位）。
 * 入参必须为可结构化克隆的普通数据（调用方需先去除 Vue 响应式代理）。
 * Worker 不可用时抛错，调用方应回退到主线程 buildProductForm。
 */
export function buildProductFormInWorker(
    speList: SpecGroup[],
    existingList: ProductFormRow[],
    options: { variant?: BuildVariant } = {}
): Promise<ProductFormRow[]> {
    return new Promise((resolve, reject) => {
        let worker: Worker;
        try {
            worker = getWorker();
        } catch (e) {
            reject(e);
            return;
        }
        const id = ++_seq;
        const onMessage = (ev: MessageEvent) => {
            const data = ev.data || {};
            if (data.id !== id) return;
            worker.removeEventListener("message", onMessage);
            worker.removeEventListener("error", onError);
            if (data.error) {
                reject(new Error(data.error));
            } else {
                resolve(data.result as ProductFormRow[]);
            }
        };
        const onError = (err: ErrorEvent) => {
            worker.removeEventListener("message", onMessage);
            worker.removeEventListener("error", onError);
            reject(err.error || new Error(err.message));
        };
        worker.addEventListener("message", onMessage);
        worker.addEventListener("error", onError);
        worker.postMessage({ id, speList, existingList, variant: options.variant || "product" });
    });
}

export type SkuEnabledState = 0 | 1;
export type SkuStatusFilter = "enabled" | "disabled" | "all";
export type SkuStatusTarget = "enable" | "disable";

export interface SkuStatusRow {
    skuId?: number | string | null;
    skuValue?: string | null;
    isEnabled?: number | null;
    [key: string]: any;
}

/** 兼容旧数据：只有严格数值 0 表示禁用，其余值统一按启用处理。 */
export const normalizeSkuEnabled = (value: unknown): SkuEnabledState => (value === 0 ? 0 : 1);

export const isSkuEnabled = (row: SkuStatusRow | null | undefined): boolean => normalizeSkuEnabled(row?.isEnabled) === 1;

export const getEnabledSkus = <T extends SkuStatusRow>(list: T[] | null | undefined): T[] => (list || []).filter(isSkuEnabled);

export const filterSkusByStatus = <T extends SkuStatusRow>(list: T[] | null | undefined, filter: SkuStatusFilter): T[] => {
    const source = list || [];
    if (filter === "all") return source;
    return source.filter((row) => (filter === "enabled" ? isSkuEnabled(row) : !isSkuEnabled(row)));
};

export const getSkuSelectionKey = (row: SkuStatusRow): string => {
    if (row.skuId !== undefined && row.skuId !== null && row.skuId !== "") return `sku:${String(row.skuId)}`;
    return `value:${String(row.skuValue || "").trim()}`;
};

export const updateSkuSelection = <T extends SkuStatusRow>(selectedKeys: ReadonlySet<string>, rows: T[], checked: boolean): Set<string> => {
    const next = new Set(selectedKeys);
    rows.forEach((row) => {
        const key = getSkuSelectionKey(row);
        if (checked) next.add(key);
        else next.delete(key);
    });
    return next;
};

export const getSelectedSkuTargets = <T extends SkuStatusRow>(list: T[] | null | undefined, selectedKeys: ReadonlySet<string>, target: SkuStatusTarget): T[] =>
    (list || []).filter((row) => selectedKeys.has(getSkuSelectionKey(row)) && (target === "enable" ? !isSkuEnabled(row) : isSkuEnabled(row)));

export const canDisableSkuTargets = <T extends SkuStatusRow>(list: T[] | null | undefined, targets: T[]): boolean => {
    const targetKeys = new Set(targets.filter(isSkuEnabled).map(getSkuSelectionKey));
    const remainingEnabled = getEnabledSkus(list).filter((row) => !targetKeys.has(getSkuSelectionKey(row))).length;
    return remainingEnabled >= 1;
};

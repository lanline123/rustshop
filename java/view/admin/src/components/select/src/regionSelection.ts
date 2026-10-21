export type RegionSelectionState = "select" | "text" | "loading" | "error";

export const normalizeRegionId = (value: unknown): number | null => {
    if (typeof value === "number" && Number.isInteger(value)) return value;
    if (typeof value !== "string" || !value.trim()) return null;
    const parsed = Number(value.trim());
    return Number.isInteger(parsed) ? parsed : null;
};

export const truncateRegionIds = (ids: unknown[], index: number, value: unknown): number[] => {
    const normalized = normalizeRegionId(value);
    if (normalized === null || index < 0) return [];
    return ids.slice(0, index).map(normalizeRegionId).filter((id): id is number => id !== null).concat(normalized);
};

type RegionNode = { regionId?: number | string; id?: number | string; regionName?: string; name?: string };

const flatten = (nodes: RegionNode[], result: RegionNode[] = []): RegionNode[] => {
    for (const node of nodes) {
        result.push(node);
        if (Array.isArray((node as any).children)) flatten((node as any).children, result);
    }
    return result;
};

export const resolveRegionEcho = (ids: unknown[] = [], names: string[] = [], options: RegionNode[] = []) => {
    const available = flatten(options);
    const byId = new Map<number, RegionNode>();
    const byName = new Map<string, RegionNode>();
    available.forEach((node) => {
        const id = normalizeRegionId(node.regionId ?? node.id);
        const name = String(node.regionName ?? node.name ?? "");
        if (id !== null) byId.set(id, node);
        if (name) byName.set(name, node);
    });
    const resultIds: number[] = [];
    const resultNames: string[] = [];
    const invalidNames: string[] = [];
    ids.forEach((rawId, index) => {
        const id = normalizeRegionId(rawId);
        const expectedName = names[index] || "";
        const node = id === null ? undefined : byId.get(id);
        const fallback = !node && expectedName ? byName.get(expectedName) : undefined;
        const resolved = node || fallback;
        if (resolved) {
            const resolvedId = normalizeRegionId(resolved.regionId ?? resolved.id);
            if (resolvedId !== null) resultIds.push(resolvedId);
            resultNames.push(String(resolved.regionName ?? resolved.name ?? expectedName));
        } else if (expectedName) {
            invalidNames.push(expectedName);
        }
    });
    return { ids: resultIds, names: resultNames, invalidNames };
};

export const getRegionSelectionState = (state: { loading: boolean; error: unknown; hasChildren: boolean }): RegionSelectionState => {
    if (state.loading) return "loading";
    if (state.error) return "error";
    return state.hasChildren ? "select" : "text";
};

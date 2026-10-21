import { useRegionStore } from "@/store/region";

export interface RegionMatchResult {
    ids: number[];
    names: string[];
}

/**
 * 根据高德逆地理编码返回的省市区信息，在系统地区树中匹配对应的 regionId。
 * 优先 adcode 精确匹配，失败后回退到名称模糊匹配（含直辖市兼容）。
 */
export function matchRegionByNames(
    province: string,
    city: string,
    district: string,
    adcode?: string
): RegionMatchResult {
    const tree = useRegionStore().allRegion;
    if (!tree?.length) return { ids: [], names: [] };

    // 策略1：adcode 直接匹配 regionId，取完整路径
    if (adcode) {
        const path = findPath(tree, (n) => String(n.regionId) === adcode);
        if (path) return toResult(path);
    }

    // 策略2：名称逐级匹配
    const provNode = findByName(tree, province);
    if (!provNode?.children?.length) return toResult(provNode ? [provNode] : []);

    let cityNode = findByName(provNode.children, city);
    // 直辖市：province === city，但树中城市层级可能是 "市辖区"
    if (!cityNode && isSameName(province, city)) {
        cityNode = provNode.children.find((n: any) => n.regionName === "市辖区") || provNode.children[0];
    }
    // 市级仍无法匹配，尝试在所有二级节点的 children 中搜索区
    if (!cityNode) {
        if (district) {
            for (const child of provNode.children) {
                const dNode = findByName(child.children, district);
                if (dNode) return toResult([provNode, child, dNode]);
            }
        }
        return toResult([provNode]);
    }

    const distNode = district ? findByName(cityNode.children, district) : null;
    return toResult(distNode ? [provNode, cityNode, distNode] : [provNode, cityNode]);
}

function toResult(path: any[]): RegionMatchResult {
    return { ids: path.map((n) => n.regionId), names: path.map((n) => n.regionName) };
}

/** 递归搜索满足条件的节点，返回从根到该节点的完整路径 */
function findPath(nodes: any[], predicate: (n: any) => boolean, path: any[] = []): any[] | null {
    if (!nodes?.length) return null;
    for (const node of nodes) {
        const cur = [...path, node];
        if (predicate(node)) return cur;
        const found = findPath(node.children, predicate, cur);
        if (found) return found;
    }
    return null;
}

/** 去除行政区划后缀用于模糊比较 */
function normalize(name: string): string {
    return name.replace(/特别行政区|自治区|自治州|自治县|地区|林区|省|市|区|县|盟|旗/g, "").trim();
}

/** 在同级节点中按名称查找：先精确匹配，再去后缀模糊匹配 */
function findByName(nodes: any[], name: string): any {
    if (!name || !nodes?.length) return null;
    const exact = nodes.find((n: any) => n.regionName === name);
    if (exact) return exact;
    const norm = normalize(name);
    if (!norm) return null;
    return nodes.find((n: any) => {
        const nn = normalize(n.regionName);
        return nn === norm || nn.includes(norm) || norm.includes(nn);
    }) || null;
}

function isSameName(a: string, b: string): boolean {
    return a === b || normalize(a) === normalize(b);
}

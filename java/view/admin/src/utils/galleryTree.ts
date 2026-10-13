import type { GalleryNodeId, GalleryPicItem, GalleryTreeNode, VideoItem } from "@/types/setting/gallery.d";
import { timestampToDate } from "@/utils/time";

const formatExportDate = (value?: string | number | null): string => {
    if (value === undefined || value === null || value === "") return "";
    if (/^\d+$/.test(String(value))) return timestampToDate(Number(value));
    return String(value);
};

const formatExportSize = (size?: number | string | null): string => {
    if (size === undefined || size === null || size === "" || size === 0 || size === "0") return "";
    const bytes = typeof size === "string" ? parseFloat(size) : size;
    if (isNaN(bytes) || bytes <= 0) return typeof size === "string" ? size : "";
    if (bytes < 1024) return `${bytes}B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
};

export const getNodeId = (node: Pick<GalleryTreeNode, "id" | "galleryId">) => node.galleryId ?? node.id;

/** 写入侧：0 表示不归入任何分组 */
export const toApiGalleryId = (id: GalleryNodeId | undefined | null): number | string => {
    if (id === undefined || id === null || id === "") return 0;
    return id;
};

/** 列表筛选：全部不传 galleryId/id；具体分组传对应 id */
export const toListApiGalleryId = (id: GalleryNodeId | undefined | null): number | string | undefined => {
    if (id === undefined || id === null || id === "" || id === 0) return undefined;
    return id;
};

export const buildTreeFromFlat = <T extends { id: GalleryNodeId; parentId?: GalleryNodeId; children?: T[] }>(
    nodes: T[],
    idSelector: (node: T) => GalleryNodeId = (node) => node.id
): T[] => {
    const flat: T[] = [];
    const flatten = (items: T[]) => {
        for (const item of items) {
            const copy = { ...item, children: [] as T[] };
            flat.push(copy);
            if (item.children?.length) flatten(item.children);
        }
    };

    flatten(nodes);
    if (flat.length === 0) return [];
    if (!flat.some((node) => node.parentId != null && String(node.parentId) !== "0")) return nodes;

    const map = new Map<string, T>();
    flat.forEach((node) => map.set(String(idSelector(node)), node));

    const roots: T[] = [];
    for (const node of flat) {
        const parentId = node.parentId != null ? String(node.parentId) : "0";
        const parent = parentId !== "0" ? map.get(parentId) : undefined;
        if (parent) {
            if (!parent.children) parent.children = [];
            parent.children.push(node);
        } else {
            roots.push(node);
        }
    }
    return roots;
};

export const filterTreeByName = <T extends { galleryName?: string; name?: string; children?: T[] }>(nodes: T[], keyword: string): T[] => {
    const key = keyword.trim().toLowerCase();
    if (!key) return nodes;
    return nodes
        .map((node) => {
            const children = node.children ? filterTreeByName(node.children, key) : [];
            const label = (node.galleryName || node.name || "").toLowerCase();
            if (label.includes(key) || children.length > 0) return { ...node, children };
            return null;
        })
        .filter(Boolean) as T[];
};

export const getMaterialUrl = (item: GalleryPicItem | VideoItem, type: "image" | "video") => {
    const rawUrl = type === "image" ? (item as GalleryPicItem).picUrl || (item as GalleryPicItem).picThumb : (item as VideoItem).videoUrl;
    return rawUrl ? String(rawUrl) : "";
};

export const buildExportRows = (items: Array<GalleryPicItem | VideoItem>, type: "image" | "video") => {
    if (type === "image") {
        return (items as GalleryPicItem[]).map((item) => ({
            图片名称: item.picName || "",
            格式: (item.fileType || item.picName?.split(".").pop() || "").toUpperCase(),
            尺寸: item.picWidth && item.picHeight ? `${item.picWidth} x ${item.picHeight}` : "",
            大小: formatExportSize(item.picSize),
            上传时间: formatExportDate(item.createTime ?? item.addTime),
            图片链接地址: getMaterialUrl(item, "image")
        }));
    }

    return (items as VideoItem[]).map((item) => ({
        视频名称: item.videoName || "",
        上传时间: formatExportDate(item.createTime),
        格式: (item.format || "").toUpperCase(),
        大小: formatExportSize(item.size),
        时长: item.duration || "",
        视频链接地址: getMaterialUrl(item, "video")
    }));
};

export const copyText = async (text: string) => {
    if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return;
    }
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.style.position = "fixed";
    textarea.style.opacity = "0";
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
};

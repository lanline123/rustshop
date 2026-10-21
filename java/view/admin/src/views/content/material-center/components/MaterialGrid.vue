<template>
    <div class="material-grid">
        <ul v-if="items.length" class="grid-list">
            <MaterialGridCard
                v-for="item in items"
                :key="String(getId(item))"
                :item="item"
                :active-tab="activeTab"
                :selected="selectedIds.includes(getId(item))"
                @select="emit('select', item)"
                @preview="emit('preview', item)"
                @edit="emit('edit', item)"
                @download="emit('download', item)"
                @copy="emit('copy', item)"
                @delete="emit('delete', item)"
            />
        </ul>

        <div v-else-if="!loading" class="grid-empty">
            <div class="empty-bg">{{ $t('暂无素材') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { defineComponent, h, resolveComponent } from "vue";
import { ElCheckbox, ElImage, ElPopconfirm } from "element-plus";
import { imageFormat } from "@/utils/format";
import { timestampToDate } from "@/utils/time";
import type { GalleryNodeId, GalleryPicItem, VideoItem } from "@/types/setting/gallery.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
defineProps<{
    activeTab: "image" | "video";
    items: Array<GalleryPicItem | VideoItem>;
    selectedIds: GalleryNodeId[];
    loading?: boolean;
}>();

const emit = defineEmits(["select", "preview", "edit", "delete", "download", "copy"]);

const getId = (item: GalleryPicItem | VideoItem): GalleryNodeId => (item as GalleryPicItem).picId || (item as VideoItem).id;

const getName = (item: GalleryPicItem | VideoItem, type: "image" | "video") => (type === "image" ? (item as GalleryPicItem).picName : (item as VideoItem).videoName) || "";
const getMeta = (item: GalleryPicItem | VideoItem, type: "image" | "video") => {
    if (type === "image") {
        const pic = item as GalleryPicItem;
        const ext = pic.fileType?.toUpperCase() || pic.picName?.split(".").pop()?.toUpperCase() || pic.format?.toUpperCase() || "";
        const time = pic.addTime ? timestampToDate(Number(pic.addTime), "YYYY-MM-DD") : "";
        return [ext, formatFileSize(pic.picSize), time].filter(Boolean).join(" · ");
    }
    const video = item as VideoItem;
    return [video.format?.toUpperCase(), video.size, video.duration].filter(Boolean).join(" · ");
};
const formatFileSize = (size?: number | string) => {
    if (!size) return "";
    const bytes = typeof size === "string" ? Number(size) : size;
    if (!Number.isFinite(bytes)) return String(size);
    if (bytes < 1024) return `${bytes}B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
};

const MaterialGridCard = defineComponent({
    props: {
        item: { type: Object, required: true },
        activeTab: { type: String, required: true },
        selected: { type: Boolean, default: false }
    },
    emits: ["select", "preview", "edit", "download", "copy", "delete"],
    setup(cardProps, { emit: cardEmit }) {
        const SvgIcon = resolveComponent("SvgIcon");
        const actionIcon = (name: string) => h(SvgIcon, { name, width: "18", height: "18", "aria-hidden": "true" });
        return () => {
            const item = cardProps.item as GalleryPicItem | VideoItem;
            const isImage = cardProps.activeTab === "image";
            const thumb = isImage ? (item as GalleryPicItem).picThumb || (item as GalleryPicItem).picUrl : (item as VideoItem).videoCover;
            const videoUrl = (item as VideoItem).videoUrl;
            return h("li", { class: ["grid-item", { selected: cardProps.selected, "grid-item-video": !isImage }] }, [
                h("div", { class: ["item-thumb", { "video-thumb": !isImage }], onClick: () => cardEmit("preview") }, [
                    isImage || thumb
                        ? h(ElImage, { src: imageFormat(thumb), fit: "cover", class: "thumb-img" })
                        : h("video", { src: imageFormat(videoUrl), class: "thumb-img" }),
                    !isImage ? h("div", { class: "video-play-icon" }, [h("span", { class: "thumb-play-icon" })]) : null
                ]),
                h("div", { class: "item-info" }, [
                    h("div", { class: "item-info-top" }, [
                        h("span", { class: "item-name", title: getName(item, cardProps.activeTab as any) }, getName(item, cardProps.activeTab as any)),
                        h(ElCheckbox, {
                            modelValue: cardProps.selected,
                            "onUpdate:modelValue": () => cardEmit("select"),
                            onClick: (event: Event) => event.stopPropagation()
                        })
                    ]),
                    h("span", { class: "item-meta" }, getMeta(item, cardProps.activeTab as any))
                ]),
                h("div", { class: "item-actions" }, [
                    h("button", { class: "action-btn", title: t('编辑'), onClick: () => cardEmit("edit") }, [actionIcon("gallery-grid-edit")]),
                    h("button", { class: "action-btn", title: t('下载'), onClick: () => cardEmit("download") }, [actionIcon("gallery-grid-download")]),
                    h("button", { class: "action-btn", title: t('复制链接'), onClick: () => cardEmit("copy") }, [actionIcon("gallery-grid-link")]),
                    h(
                        ElPopconfirm,
                        {
                            title: t('确认删除该{isImage}吗?', { isImage: isImage ? t('图片') : t('视频') }),
                            width: 230,
                            placement: "top",
                            confirmButtonText: t('确定'),
                            cancelButtonText: t('取消'),
                            onConfirm: () => cardEmit("delete")
                        },
                        {
                            reference: () => h("button", { class: "action-btn danger", title: t('删除') }, [actionIcon("gallery-grid-delete")])
                        }
                    )
                ])
            ]);
        };
    }
});
</script>

<style scoped lang="less">
.material-grid {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding-right: 4px;
}
.grid-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
    list-style: none;
    padding: 0;
    margin: 0;
}
:deep(.grid-item) {
    position: relative;
    border-radius: 4px;
    border: 1px solid #eee;
    background: #fff;
    overflow: hidden;
    transition: box-shadow 0.2s, border-color 0.2s;
    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
    &.selected {
        border-color: var(--tig-primary);
        box-shadow: 0 0 0 1px rgba(21, 91, 212, 0.18);
    }
    .item-thumb {
        position: relative;
        width: 100%;
        aspect-ratio: 1;
        cursor: pointer;
        background: #f5f5f5;
        overflow: hidden;
        .thumb-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
        }
    }
    .video-thumb {
        aspect-ratio: 16 / 10;
    }
    .video-play-icon {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.42);
        display: flex;
        align-items: center;
        justify-content: center;
        pointer-events: none;
        .thumb-play-icon {
            width: 0;
            height: 0;
            margin-left: 4px;
            border-top: 10px solid transparent;
            border-bottom: 10px solid transparent;
            border-left: 15px solid #fff;
        }
    }
    .item-info {
        padding: 8px 12px;
        border-bottom: 1px solid #eee;
        .item-info-top {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 8px;
        }
        .item-name {
            flex: 1;
            font-size: 13px;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .item-meta {
            display: block;
            margin-top: 4px;
            font-size: 12px;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            min-height: 18px;
        }
    }
    .item-actions {
        display: flex;
        align-items: center;
        justify-content: space-around;
        padding: 10px 0;
        .action-btn {
            width: 24px;
            height: 24px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: 0;
            background: transparent;
            color: #333;
            cursor: pointer;
            &:hover {
                color: var(--tig-primary);
            }
            &.danger:hover {
                color: #f56c6c;
            }
        }
    }
}
.grid-empty {
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 移动端响应式适配 */
@media screen and (max-width: 768px) {
    .material-grid {
        height: auto !important;
        overflow-y: visible !important;
        padding-right: 0 !important;
    }
    .grid-list {
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        gap: 8px;
    }
    :deep(.grid-item) .item-info {
        padding: 6px 8px;
    }
    :deep(.grid-item) .item-actions .action-btn {
        width: 20px;
        height: 20px;
        font-size: 14px;
    }
}
</style>

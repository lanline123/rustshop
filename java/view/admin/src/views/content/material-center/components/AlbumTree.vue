<template>
    <div class="album-tree">
        <div class="album-node root" :class="{ active: activeId == 0 }" @click="emit('select', 0)">
            <span class="album-arrow" :class="{ expanded: rootExpanded }" @click.stop="rootExpanded = !rootExpanded"></span>
            <span class="album-name">{{ activeTab === "image" ? $t('全部图片') : $t('全部视频') }}</span>
            <el-dropdown class="album-more always-visible" trigger="click" popper-class="album-dropdown-menu" @click.stop>
                <span class="more-icon">⋯</span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item @click="emit('create')">{{ $t('添加子分组') }}</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
        <template v-if="rootExpanded">
            <draggable v-model="sortableTree" item-key="id" handle=".drag-handle" :animation="200" @end="handleRootSort(sortableTree)">
                <template #item="{ element }">
                    <AlbumTreeNode
                        :node="element"
                        :active-id="activeId"
                        :expanded-ids="expandedIds"
                        :depth="1"
                        @select="emit('select', $event)"
                        @toggle="toggle"
                        @create="emit('create', $event)"
                        @rename="emit('rename', $event)"
                        @delete="emit('delete', $event)"
                        @sort="emit('sort', $event)"
                    />
                </template>
            </draggable>
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import draggable from "vuedraggable";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";
import { filterTreeByName } from "@/utils/galleryTree";
import AlbumTreeNode from "./AlbumTreeNode.vue";

const props = defineProps<{
    treeData: GalleryTreeNode[];
    activeId: GalleryNodeId;
    searchKey: string;
    activeTab: "image" | "video";
}>();

const emit = defineEmits<{
    select: [id: GalleryNodeId];
    create: [node?: GalleryTreeNode];
    rename: [node: GalleryTreeNode];
    delete: [node: GalleryTreeNode];
    sort: [ids: GalleryNodeId[]];
}>();

const rootExpanded = ref(true);
const expandedIds = reactive(new Set<string>());
const filteredTree = computed(() => filterTreeByName(props.treeData, props.searchKey));
const sortableTree = ref<GalleryTreeNode[]>([]);
/** 是否已完成首次默认展开，避免排序/刷新后再次强制全部展开 */
const hasInitializedExpand = ref(false);

const toggle = (id: string) => {
    expandedIds.has(id) ? expandedIds.delete(id) : expandedIds.add(id);
};

const collectExpandedIds = (nodes: GalleryTreeNode[]) => {
    nodes.forEach((node) => {
        const id = String(node.galleryId || node.id);
        if (node.children?.length) {
            expandedIds.add(id);
            collectExpandedIds(node.children);
        }
    });
};

const collectAllIds = (nodes: GalleryTreeNode[], ids: Set<string> = new Set()) => {
    nodes.forEach((node) => {
        ids.add(String(node.galleryId || node.id));
        if (node.children?.length) collectAllIds(node.children, ids);
    });
    return ids;
};

const handleRootSort = (nodes: GalleryTreeNode[]) => {
    emit(
        "sort",
        nodes.map((node) => node.galleryId || node.id)
    );
};

watch(
    filteredTree,
    (nodes) => {
        sortableTree.value = [...(nodes || [])];
    },
    { immediate: true }
);

// 切换图片/视频时重置展开状态并默认展开
watch(
    () => props.activeTab,
    () => {
        expandedIds.clear();
        hasInitializedExpand.value = false;
    }
);

watch(
    () => props.treeData,
    (nodes) => {
        const list = nodes || [];
        if (!hasInitializedExpand.value) {
            expandedIds.clear();
            collectExpandedIds(list);
            hasInitializedExpand.value = true;
            return;
        }
        // 排序/增删后仅清理已不存在的展开项，保留用户折叠状态
        const validIds = collectAllIds(list);
        [...expandedIds].forEach((id) => {
            if (!validIds.has(id)) expandedIds.delete(id);
        });
    },
    { immediate: true }
);
</script>

<template>
    <div class="album-tree-item">
        <div class="album-node" :class="{ active: String(activeId) === String(nodeId) }" :style="{ paddingLeft: `${12 + depth * 18}px` }" @click="emit('select', nodeId)">
            <span class="drag-handle" @click.stop>☰</span>
            <span v-if="hasChildren" class="album-arrow" :class="{ expanded: expandedIds.has(String(nodeId)) }" @click.stop="emit('toggle', String(nodeId))"></span>
            <span v-else class="album-arrow-placeholder"></span>
            <span class="album-name">{{ node.galleryName || node.name }}</span>
            <el-dropdown class="album-more" trigger="click" popper-class="album-dropdown-menu" @click.stop>
                <span class="more-icon">⋯</span>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item @click="emit('create', node)">{{ $t('添加子分组') }}</el-dropdown-item>
                        <el-dropdown-item @click="emit('rename', node)">{{ $t('重命名') }}</el-dropdown-item>
                        <el-dropdown-item @click="emit('delete', node)">{{ $t('删除') }}</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
        <template v-if="hasChildren && expandedIds.has(String(nodeId))">
            <draggable v-model="localChildren" item-key="id" handle=".drag-handle" :animation="200" @end="emitSort(localChildren)">
                <template #item="{ element }">
                    <AlbumTreeNode
                        :node="element"
                        :active-id="activeId"
                        :expanded-ids="expandedIds"
                        :depth="depth + 1"
                        @select="emit('select', $event)"
                        @toggle="emit('toggle', $event)"
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
import { computed, ref, watch } from "vue";
import draggable from "vuedraggable";
import type { GalleryNodeId, GalleryTreeNode } from "@/types/setting/gallery.d";

const props = defineProps<{
    node: GalleryTreeNode;
    activeId: GalleryNodeId;
    expandedIds: Set<string>;
    depth: number;
}>();

const emit = defineEmits<{
    select: [id: GalleryNodeId];
    toggle: [id: string];
    create: [node: GalleryTreeNode];
    rename: [node: GalleryTreeNode];
    delete: [node: GalleryTreeNode];
    sort: [ids: GalleryNodeId[]];
}>();

const nodeId = computed(() => props.node.galleryId || props.node.id);
const hasChildren = computed(() => !!props.node.children?.length);
const localChildren = ref<GalleryTreeNode[]>([]);

const emitSort = (nodes: GalleryTreeNode[]) => {
    emit(
        "sort",
        nodes.map((node) => node.galleryId || node.id)
    );
};

watch(
    () => props.node.children,
    (children) => {
        localChildren.value = [...(children || [])];
    },
    { immediate: true, deep: true }
);
</script>

<style lang="less">
.album-dropdown-menu {
    .el-dropdown-menu__item {
        color: var(--tig-primary) !important;
    }
}
</style>

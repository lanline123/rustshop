<template>
    <div>
        <div class="gallery-picker-tree-node" :class="{ active: String(activeId) === String(nodeId) }" :style="{ paddingLeft: `${12 + depth * 18}px` }" @click="emit('select', nodeId)">
            <span v-if="hasChildren" class="tree-arrow" :class="{ expanded: expandedIds.has(String(nodeId)) }" @click.stop="emit('toggle', String(nodeId))"></span>
            <span v-else class="tree-arrow-placeholder"></span>
            <span class="tree-node-label">{{ node.galleryName || node.name }}</span>
        </div>
        <template v-if="hasChildren && expandedIds.has(String(nodeId))">
            <PickerTreeNode
                v-for="child in node.children"
                :key="String(child.galleryId || child.id)"
                :node="child"
                :active-id="activeId"
                :expanded-ids="expandedIds"
                :depth="depth + 1"
                @select="emit('select', $event)"
                @toggle="emit('toggle', $event)"
            />
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { GalleryTreeNode, GalleryNodeId } from "@/types/setting/gallery.d";

const props = defineProps<{
    node: GalleryTreeNode;
    activeId: GalleryNodeId;
    expandedIds: Set<string>;
    depth: number;
}>();

const emit = defineEmits<{
    select: [id: GalleryNodeId];
    toggle: [id: string];
}>();

const nodeId = computed(() => props.node.galleryId || props.node.id);
const hasChildren = computed(() => !!props.node.children?.length);
</script>

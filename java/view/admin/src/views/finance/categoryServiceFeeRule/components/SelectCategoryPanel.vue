<template>
    <div class="select-category-panel">
        <a-spin :spinning="loading">
            <div class="category-cascader-con">
                <div v-for="(column, columnIndex) in visibleColumns" :key="columnIndex" class="category-menu">
                    <div
                        v-for="node in column"
                        :key="node.categoryId"
                        class="category-menu-item"
                        :class="{ active: isActiveNode(node, columnIndex) }"
                        @click="activateNode(node, columnIndex)"
                    >
                        <el-checkbox
                            :model-value="isNodeChecked(node)"
                            :indeterminate="isNodeIndeterminate(node)"
                            @click.stop
                            @change="() => toggleNode(node, columnIndex)"
                        />
                        <span class="category-name">{{ node.categoryName }}</span>
                        <span v-if="node.children?.length" class="category-arrow">›</span>
                    </div>
                </div>
            </div>
        </a-spin>
        <div class="category-panel-actions">
            <el-button type="primary" size="small" @click="checkAll">{{ $t('全选') }}</el-button>
            <el-button size="small" @click="clearAll">{{ $t('清空') }}</el-button>
            <span class="selected-count">{{ $t('已选择{selectedCount}个类目', { selectedCount }) }}</span>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { getAllCategoryList } from "@/api/product/category";
import type { CategoryFilterState } from "@/types/product/category";

type CategoryNode = CategoryFilterState & {
    children?: CategoryNode[];
};

const selectedCategoryIds = defineModel<number[]>({ default: () => [] });

const loading = ref(false);
const categoryOptions = ref<CategoryNode[]>([]);
const activePath = ref<number[]>([]);

const selectedCount = computed(() => selectedCategoryIds.value.length);
const selectedIdSet = computed(() => new Set((selectedCategoryIds.value || []).map((id) => Number(id))));

const findPathById = (nodes: CategoryNode[], targetId: number, parentPath: number[] = []): number[] | null => {
    for (const node of nodes) {
        const currentId = Number(node.categoryId);
        const currentPath = [...parentPath, currentId];
        if (currentId === targetId) {
            return currentPath;
        }
        if (node.children?.length) {
            const childPath = findPathById(node.children, targetId, currentPath);
            if (childPath) {
                return childPath;
            }
        }
    }
    return null;
};

const findNodeInColumn = (nodes: CategoryNode[], id: number) => {
    return nodes.find((node) => Number(node.categoryId) === Number(id));
};

const visibleColumns = computed(() => {
    const columns: CategoryNode[][] = [];
    let currentColumn = categoryOptions.value;
    columns.push(currentColumn);

    for (const id of activePath.value) {
        const activeNode = findNodeInColumn(currentColumn, Number(id));
        if (!activeNode?.children?.length) break;
        currentColumn = activeNode.children;
        columns.push(currentColumn);
    }

    return columns;
});

const collectCategoryIdsDeep = (nodes: CategoryNode[]): number[] => {
    const ids: number[] = [];
    nodes.forEach((node) => {
        const currentId = Number(node.categoryId);
        if (Number.isFinite(currentId)) {
            ids.push(currentId);
        }
        if (node.children?.length) {
            ids.push(...collectCategoryIdsDeep(node.children));
        }
    });
    return ids;
};

const normalizeSelectedIds = (ids: number[]) => {
    selectedCategoryIds.value = Array.from(new Set(ids.filter((id) => Number.isFinite(id))));
};

const normalizeCategoryTree = (nodes: CategoryNode[]): CategoryNode[] => {
    return nodes.map((node) => {
        const children = node.children?.length ? normalizeCategoryTree(node.children) : undefined;
        return {
            ...node,
            children
        };
    });
};

const expandFirstChildPath = (node: CategoryNode) => {
    const path: number[] = [];
    let currentNode: CategoryNode | undefined = node;
    while (currentNode?.children?.length) {
        const firstChild = currentNode.children[0];
        path.push(Number(firstChild.categoryId));
        currentNode = firstChild;
    }
    return path;
};

const activateNode = (node: CategoryNode, columnIndex: number) => {
    const nodeId = Number(node.categoryId);
    activePath.value = [...activePath.value.slice(0, columnIndex), nodeId, ...expandFirstChildPath(node)];
};

const isActiveNode = (node: CategoryNode, columnIndex: number) => activePath.value[columnIndex] === Number(node.categoryId);

const isNodeChecked = (node: CategoryNode) => {
    const ids = collectCategoryIdsDeep([node]);
    return ids.length > 0 && ids.every((id) => selectedIdSet.value.has(id));
};

const isNodeIndeterminate = (node: CategoryNode) => {
    const ids = collectCategoryIdsDeep([node]);
    const checkedCount = ids.filter((id) => selectedIdSet.value.has(id)).length;
    return checkedCount > 0 && checkedCount < ids.length;
};

const toggleNode = (node: CategoryNode, columnIndex: number) => {
    activateNode(node, columnIndex);
    const nodeIds = collectCategoryIdsDeep([node]);
    const nextIds = new Set(selectedCategoryIds.value || []);
    if (isNodeChecked(node)) {
        nodeIds.forEach((id) => nextIds.delete(id));
    } else {
        nodeIds.forEach((id) => nextIds.add(id));
    }
    normalizeSelectedIds(Array.from(nextIds));
};

const checkAll = () => {
    normalizeSelectedIds(collectCategoryIdsDeep(categoryOptions.value));
};

const clearAll = () => {
    selectedCategoryIds.value = [];
};

const loadCategory = async () => {
    loading.value = true;
    try {
        const result = await getAllCategoryList();
        categoryOptions.value = normalizeCategoryTree(result as CategoryNode[]);
        syncActivePath();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const syncActivePath = () => {
    if (activePath.value.length > 0) return;
    const firstSelectedId = selectedCategoryIds.value?.[0];
    if (firstSelectedId) {
        const selectedPath = findPathById(categoryOptions.value, Number(firstSelectedId));
        if (selectedPath) {
            activePath.value = selectedPath;
            return;
        }
    }
    if (categoryOptions.value[0]) {
        activateNode(categoryOptions.value[0], 0);
    }
};

watch(() => selectedCategoryIds.value, syncActivePath, { deep: true });

onMounted(() => {
    loadCategory();
});
</script>

<style lang="less" scoped>
.select-category-panel {
    width: 100%;
    background-color: #fafafa;
    padding: 20px;
}

.category-cascader-con {
    width: 100%;
    height: 330px;
    display: flex;
    overflow-x: auto;
    overflow-y: hidden;
    box-sizing: border-box;
}

.category-panel-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-top: 10px;
}

.category-menu {
    width: 220px;
    height: 326px;
    flex: none;
    overflow-y: auto;
    border: 1px solid #e5e7eb;
    border-right: 0;
    background-color: #fff;

    &:last-child {
        border-right: 1px solid #e5e7eb;
    }
}

.category-menu-item {
    display: flex;
    align-items: center;
    min-height: 34px;
    padding: 0 10px;
    cursor: pointer;

    &:hover,
    &.active {
        background-color: #F6F7FA;
    }
}

.category-name {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-left: 6px;
}

.category-arrow {
    color: #999;
    font-size: 18px;
    line-height: 1;
}

.selected-count {
    color: #666;
    margin-left: 4px;
}
</style>

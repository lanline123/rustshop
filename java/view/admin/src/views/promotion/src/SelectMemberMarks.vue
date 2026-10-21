<template>
    <div class="member-marks-drawer">
        <div class="three-col">
            <!-- 左：分类（暂定只有会员标签） -->
            <div class="col col-left">
                <div class="left-menu">
                    <div class="left-menu-item" :class="{ active: activeCategory === 'memberMark' }" @click="onSelectCategory('memberMark')">{{ $t('会员标签') }}</div>
                </div>
            </div>

            <!-- 中：数据表 -->
            <div class="col col-middle">
                <el-table ref="tableRef" :data="allRows" height="520" row-key="value" @selection-change="onSelectionChange">
                    <el-table-column type="selection" width="48" />
                    <el-table-column :label="$t('标记')">
                        <template #default="{ row }">
                            <div class="mark-cell">
                                <SignTag :value="row.value" />
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- 右：已选 -->
            <div class="col col-right">
                <div class="col-title">{{ $t('已选：{length}个标签', { length: selectedValues.length }) }}</div>
                <div class="selected-list">
                    <div v-if="selectedValues.length > 0" class="selected-items">
                        <div v-for="v in selectedValues" :key="v" class="selected-item">
                            <div class="selected-item-left">
                                <SignTag :value="v" />
                            </div>
                            <button class="remove-btn" type="button" @click="removeSelected(v)" aria-label="删除">
                                <el-icon class="remove-icon">
                                    <RemoveFilled />
                                </el-icon>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import SignTag from "@/views/user/user/src/SignTag.vue";
import { RemoveFilled } from "@element-plus/icons-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    selectedIds: {
        type: Array as () => number[],
        default: () => []
    }
});

type Row = { value: number };
const allRows = ref<Row[]>([{ value: 1 }, { value: 2 }, { value: 3 }, { value: 4 }, { value: 5 }]);

const activeCategory = ref("memberMark");

const tableRef = ref();
const selectedValues = ref<number[]>([]);
const isSyncingSelection = ref(false);

const syncTableSelection = async () => {
    await nextTick();
    const table = tableRef.value;
    if (!table) return;
    isSyncingSelection.value = true;
    try {
        table.clearSelection();
        const selectedSet = new Set(selectedValues.value);
        allRows.value.forEach((row) => {
            if (selectedSet.has(row.value)) {
                table.toggleRowSelection(row, true);
            }
        });
    } finally {
        // 让 clearSelection / toggleRowSelection 触发的 selection-change 不影响 selectedValues
        isSyncingSelection.value = false;
    }
};

const onSelectionChange = (rows: Row[]) => {
    if (isSyncingSelection.value) return;
    // selection-change 只返回当前表格数据的勾选项，这里需要与全量已选合并/移除
    const inTable = new Set(allRows.value.map((r) => r.value));
    const selectedInTable = new Set(rows.map((r) => r.value));

    // 先移除当前表格范围内但未勾选的
    selectedValues.value = selectedValues.value.filter((v) => !(inTable.has(v) && !selectedInTable.has(v)));
    // 再合并勾选的
    rows.forEach((r) => {
        if (!selectedValues.value.includes(r.value)) selectedValues.value.push(r.value);
    });
    selectedValues.value.sort((a, b) => a - b);
};

const removeSelected = async (v: number) => {
    selectedValues.value = selectedValues.value.filter((x) => x !== v);
    await syncTableSelection();
};

const onSelectCategory = async (key: string) => {
    activeCategory.value = key;
    await syncTableSelection();
};

const onFormSubmit = async () => {
    if (selectedValues.value.length === 0) {
        message.error(t('请至少选择一个会员标记'));
        return;
    }
    emit("submitCallback", selectedValues.value);
};

onMounted(async () => {
    selectedValues.value = Array.isArray(props.selectedIds) ? [...props.selectedIds] : [];
    selectedValues.value = selectedValues.value.filter((v) => typeof v === "number").sort((a, b) => a - b);
    await syncTableSelection();
});

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.three-col {
    display: flex;
}
.col {
    border: 1px solid #eef0f3;
    background: #fff;
    overflow: hidden;
}
.col-title {
    padding: 10px 12px;
    font-weight: 600;
    border-bottom: 1px solid #eef0f3;
}
.col-left {
    width: 180px;
    padding: 8px;
    box-sizing: border-box;
    .left-menu {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    .left-menu-item {
        padding: 10px 12px;
        border-radius: 4px;
        cursor: pointer;
        color: #333;
        user-select: none;
        background: transparent;
    }
    .left-menu-item:hover {
        background: #f5f6fa;
    }
    .left-menu-item.active {
        background: #f5f6fa;
        color: var(--tig-primary);
    }
}
.col-middle {
    flex: 1;
    min-width: 400px;
    padding: 10px;
    :deep(.el-table) {
        border: none;
    }
}
.col-right {
    width: 280px;
    .selected-list {
        padding: 12px;
        height: 560px;
        overflow: auto;
    }
    .ml6 {
        margin-left: 6px;
    }
    .selected-items {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    .selected-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding: 10px 12px;
        background: #f5f6fa;
        border-radius: 8px;
        box-sizing: border-box;
    }
    .selected-item-left {
        display: inline-flex;
        align-items: center;
        min-width: 0;
    }
    .remove-btn {
        border: none;
        background: transparent;
        padding: 0;
        margin-left: 10px;
        cursor: pointer;
        line-height: 1;
        flex: 0 0 auto;
    }
    .remove-icon {
        color: #df4544;
        font-size: 18px;
    }
}
.mark-cell {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    .text {
        color: #333;
    }
}
</style>

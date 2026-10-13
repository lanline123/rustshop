/**
 * @file 排序按钮组件
 * @description 表格列头排序按钮，支持升序/降序/无排序三种状态切换
 * @features
 * - 三态切换：无排序 → 降序 → 升序 → 无排序
 * - 高亮显示当前排序方向
 */
<template>
    <div class="sort-main" @click="toggleSort">{{ text }}
        <div class="sort-button">
            <div :style="{ 'border-top-color': currentOrder === 'desc' ? '#4d8dfa' : '#b7bec9' }" class="triangle-down"></div>
            <div :style="{ 'border-bottom-color': currentOrder === 'asc' ? '#4d8dfa' : '#b7bec9' }" class="triangle-up"></div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps({
    /** 显示文本 */
    text: String,
    /** 当前字段名 */
    sortName: String,
    /** 当前激活的排序字段 */
    sortField: String,
    /** 当前排序方向 */
    sortOrder: String,
})

const emit = defineEmits(["update:sortOrder", "update:sortField", 'loadData']);

// 受控派生：当且仅当本列是当前激活列时，才显示父级方向；否则视为未排序
const currentOrder = computed(() =>
    props.sortField === props.sortName ? (props.sortOrder ?? '') : ''
);

/**
 * 切换排序状态
 * @description 循环切换：无排序 → 降序 → 升序 → 无排序
 * 切换到其它列时，旧列的 sortField 不再等于自身 sortName，自然回到未排序态
 */
const toggleSort = () => {
    let nextOrder = '';
    let nextField: string | undefined = '';
    if (currentOrder.value === '') {
        nextOrder = 'desc';
        nextField = props.sortName;
    } else if (currentOrder.value === 'desc') {
        nextOrder = 'asc';
        nextField = props.sortName;
    } else {
        nextOrder = '';
        nextField = '';
    }
    emit("update:sortField", nextField);
    emit("update:sortOrder", nextOrder);
    emit("loadData", props.sortName);
};

</script>
<style lang="less" scoped>
.sort-main {
    vertical-align: middle;
    display: inline-block;
    cursor: pointer;
}

.sort-button {
    display: inline-block;
    position: relative;
    cursor: pointer;
    padding: 6px;
    margin-left: 4px;
    vertical-align: middle;
}

.triangle-up,
.triangle-down {
    width: 0;
    height: 0;
    display: block;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
}

.triangle-up {
    border-bottom: 4px solid gray;
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
}

.triangle-down {
    border-top: 4px solid gray;
    position: absolute;
    bottom: 1px;
    left: 50%;
    transform: translateX(-50%);
}
</style>

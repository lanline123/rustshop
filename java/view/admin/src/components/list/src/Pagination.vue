/**
 * @file 分页组件
 * @description 封装 Element Plus 分页器，统一分页样式和行为
 * @features
 * - 自定义每页数量选项
 * - 显示当前条目范围和总数
 * - 响应式布局适配移动端
 */
<template>
    <div class="pagination">
        <el-pagination
            class="pagination"
            @size-change="sizeChange"
            @current-change="pageChange"
            :current-page="page"
            :page-sizes="pageSizes"
            :page-size="size"
            :background="props.background"
            :layout="props.layout"
            :total="total"
            :pager-count="pagerCount"
        >
            <template #default>
                <div class="pagination-text" v-if="paginationText">{{ $t('第{size1}-{pagesize}条 / 总共{total}条', { size1: (page - 1) * size + 1, pagesize: page * size, total }) }}</div>
                <div class="pagination-text" v-else>{{ $t('总共{total}条', { total }) }}</div>
            </template>
        </el-pagination>
    </div>
</template>
<script setup lang="ts">
import type { PropType } from "vue";

const props = defineProps({
    /** 当前页码 */
    page: { type: Number, default: 1 },
    /** 每页数量 */
    size: { type: Number, default: 10 },
    /** 总条数 */
    total: { type: Number, default: 10 },
    /** 每页条数选项 */
    pageSizes: { type: Array as PropType<number[]>, default: () => [10, 15, 20, 30, 50, 100] },
    /** 分页器布局 */
    layout: { type: String, default: "slot ,prev, pager, next, sizes" },
    /** 是否显示背景色 */
    background: { type: Boolean, default: true },
    /** 页码按钮数量 */
    pagerCount: { type: Number, default: 5 },
    /** 是否显示详细分页文本 */
    paginationText: { type: Boolean, default: true }
});

const emit = defineEmits(["update:page", "update:size", "callback"]);

/**
 * 每页数量变更
 * @param {number} value - 新的每页数量
 */
const sizeChange = (value: number) => {
    emit("update:size", value);
    emit("callback", value);
};

/**
 * 页码变更
 * @param {number} value - 新的页码
 */
const pageChange = (value: number) => {
    emit("update:page", value);
    emit("callback", value);
};
</script>
<style lang="less" scoped>
.pagination {
    :deep(.el-select) {
        min-width: 100px !important;
        width: 100px !important;
    }
}

@media only screen and (max-width: 767px) {
    .el-pagination {
        gap: 10px 0;
        flex-wrap: wrap;
        justify-content: right;
    }
}
</style>

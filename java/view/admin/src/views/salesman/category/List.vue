<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="list-table-tool-col">
                            <el-space>
                                <TigInput
                                    name="keyword"
                                    v-model="filterParams.categoryName"
                                    :placeholder="$t('输入分类名称')"
                                    @keyup.enter="onSearchSubmit"
                                    clearable
                                    @clear="onSearchSubmit"
                                >
                                    <template #append>
                                        <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                    </template>
                                </TigInput>
                            </el-space>
                            <el-space class="ml10">
                                <DialogForm
                                    isDrawer
                                    @okCallback="loadFilter"
                                    :title="$t('新建分类')"
                                    width="600px"
                                    path="salesman/category/Info"
                                    :params="{ act: 'add' }"
                                >
                                    <el-button type="primary">{{ $t('新建分类') }}</el-button>
                                </DialogForm>
                            </el-space>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="categoryId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column :label="$t('分类名称')" prop="categoryId">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <div>{{ row.categoryName }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" sortable="custom">
                                <template #default="{ row }">
                                    <div>{{ row.addTime }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <div>{{ row.sortOrder || "--" }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑分类')"
                                        width="600px"
                                        path="salesman/category/Info"
                                        :params="{ act: 'detail', id: row.categoryId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delcategory" :params="{ id: row.categoryId }">{{ $t('删除') }}</DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
                <div class="selected-action-warp selected-warp-left" v-if="selectedIds.length > 0">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span
                            >
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                <template #reference><el-button>{{ $t('批量删除') }}</el-button></template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { ref, reactive, onMounted } from "vue";
import { DeleteRecord, Switch, Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import type { categoryFilterState, categoryFilterParams } from "@/types/salesman/category.d";
import { getcategoryList, batchSubmit, delcategory } from "@/api/salesman/category";
const config:any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<categoryFilterState, categoryFilterParams>({
    apiFunction: getcategoryList,
    idKey: "categoryId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        categoryName: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>

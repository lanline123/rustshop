<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp" style="margin-bottom: 0px">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            name="keyword"
                                            v-model="filterParams.groupName"
                                            :placeholder="$t('输入分组名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadFilter"
                                            :title="$t('新建分组')"
                                            width="600px"
                                            path="salesman/subgroup/Info"
                                            :params="{ act: 'add' }"
                                        >
                                            <el-button type="primary">{{ $t('新建分组') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            row-key="groupId"
                            @selection-change="onSelectChange"
                            :total="total"
                            @sort-change="onSortChange"
                            :loading="loading"
                        >
                            <el-table-column :label="$t('分组名称')" prop="groupId">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <div>{{ row.groupName }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" sortable="custom">
                                <template #default="{ row }">
                                    <div>{{ row.addTime }}</div>
                                </template>
                            </el-table-column>
                            <!-- <el-table-column label="操作人" prop="sortOrder">
                                <template #default="{ row }">
                                    <PopForm label="排序" type="input" :requestApi="updateExampleFiled" v-model:org-value="row.sortOrder"
                                        :params="{ id: row.groupId, field: 'sortOrder' }" extra="默认值为50，数值越小，排序越靠前">
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column> -->
                            <el-table-column :label="$t('操作')" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑分组')"
                                        width="600px"
                                        path="salesman/subgroup/Info"
                                        :params="{ act: 'detail', id: row.groupId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delgroup" :params="{ id: row.groupId }">{{ $t('删除') }}</DeleteRecord>
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
import type { groupFilterState, groupFilterParams } from "@/types/salesman/subgroup.d";
import { getgroupList, batchSubmit, delgroup } from "@/api/salesman/subgroup";
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
} = useListRequest<groupFilterState, groupFilterParams>({
    apiFunction: getgroupList,
    idKey: "groupId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        groupName: "",
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

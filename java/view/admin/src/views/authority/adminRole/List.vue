/**
 * @file 角色列表页面
 * @description 管理员角色管理
 * @features
 * - 角色列表展示（名称、描述）
 * - 添加/编辑角色
 * - 批量删除角色
 * - 行内编辑角色名称
 * - 系统内置角色保护（基于 isSystem 隐藏编辑/删除入口）
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="keyword"
                                            :placeholder="$t('输入角色名称')"
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
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="authority/adminRole/Info"
                                        :title="$t('添加角色')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加角色') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-space>
                                        <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                            <template #reference>
                                                <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                            </template>
                                        </el-popconfirm>
                                        <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>
                                    </el-space>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="roleId"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column :selectable="isSelectable" type="selection" width="32" />
                            <el-table-column :label="$t('角色名称')" width="200">
                                <template #default="{ row }">
                                    <template v-if="isSystemRole(row)">
                                        <div>{{ row.roleName }}</div>
                                    </template>
                                    <PopForm
                                        v-else
                                        v-model:org-value="row.roleName"
                                        :max="10"
                                        :params="{ id: row.roleId, field: 'roleName' }"
                                        :requestApi="updateAdminRoleField"
                                        :label="$t('角色名称')"
                                        type="input"
                                    >
                                        <div>{{ row.roleName }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('描述')" prop="roleDesc"></el-table-column>
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        v-if="!isSystemRole(row)"
                                        :params="{ act: 'detail', id: row.roleId }"
                                        isDrawer
                                        path="authority/adminRole/Info"
                                        :title="$t('编辑角色')"
                                        width="800px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="!isSystemRole(row)" direction="vertical" />
                                    <DeleteRecord v-if="!isSystemRole(row)" :params="{ id: row.roleId }" :requestApi="delAdminRole" @afterDelete="loadFilter"
                                        >{{ $t('删除') }} </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { PopForm } from "@/components/pop-form";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import type { AdminRoleFilterParams, AdminRoleFilterState } from "@/types/authority/adminRole";
import { batchSubmit, delAdminRole, getAdminRoleList, updateAdminRoleField } from "@/api/authority/adminRole";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
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
} = useListRequest<AdminRoleFilterState, AdminRoleFilterParams>({
    apiFunction: getAdminRoleList,
    idKey: "roleId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize"),
    }
});

const isSystemRole = (row: AdminRoleFilterState) => Boolean(Number(row.isSystem));

const isSelectable = (row: AdminRoleFilterState) => !isSystemRole(row);

// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();

</script>

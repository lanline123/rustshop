<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm :params="{ act: 'add' }" isDrawer path="authority/adminRole/Info"
                                                :title="$t('添加管理员权限组')" width="800px"
                                                @okCallback="loadFilter">
                                        <el-button type="primary">{{ $t('添加管理员权限组') }}</el-button>
                                    </DialogForm>
                                    <TigInput v-model="filterParams.keyword" class="width240" name="keyword" :placeholder="$t('输入权限组名称')">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">

                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="roleId"
                                  @selection-change="onSelectChange" @sort-change="onSortChange">
                            <el-table-column type="selection" width="32"/>
                            <el-table-column :label="$t('权限组名称')" width="200">
                                <template #default="{ row }">
                                    <PopForm v-model:org-value="row.roleName"
                                             :max="10"
                                             :params="{ id: row.roleId, field: 'roleName' }"
                                             :requestApi="updateAdminRoleField"
                                             :label="$t('权限组名称')" type="input">
                                        <div>{{ row.roleName }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('描述')" prop="roleDesc"></el-table-column>
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm v-if="row.roleId>1" :params="{ act: 'detail', id: row.roleId }" isDrawer
                                                path="authority/adminRole/Info"
                                                :title="$t('编辑管理员权限组')" width="800px"
                                                @okCallback="loadFilter">
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider v-if="row.roleId>2" direction="vertical"/>
                                    <DeleteRecord v-if="row.roleId>2" :params="{ id: row.roleId }"
                                                  :requestApi="delAdminRole"
                                                  @afterDelete="loadFilter">{{ $t('删除') }} </DeleteRecord>
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
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                                    @callback="loadFilter"/>
                    </div>
                </div>
                <div v-if="selectedIds.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span>{{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }}</span>
                            <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')"
                                           @confirm="onBatchSubmit('del')">
                                <template #reference>
                                    <el-button>{{ $t('批量删除') }}</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import {DialogForm} from '@/components/dialog'
import {PopForm} from '@/components/pop-form'
import {onMounted, reactive, ref} from 'vue';
import {DeleteRecord, Pagination, Switch} from '@/components/list';
import {message} from 'ant-design-vue'
import {useConfigStore} from "@/store/config";
import type {AdminRoleFilterParams, AdminRoleFilterState} from '@/types/authority/adminRole';
import {batchSubmit, delAdminRole, getAdminRoleList, updateAdminRoleField} from "@/api/authority/adminRole";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config:any = useConfigStore();
// 基本参数定义
const filterState = ref(<AdminRoleFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<AdminRoleFilterParams>({   //初始化用于查询的参数
    page: 1,
    size: config.get('pageSize'),
    sortField: '',
    sortOrder: '',
    keyword: '',
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAdminRoleList({...filterParams});
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }

}
onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter()
};
// 修改排序
const onSortChange = ({prop, order}: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == 'ascending' ? 'asc' : order == 'descending' ? 'desc' : '';
    loadFilter();
};

// 批量操作
const onBatchSubmit = async (action: string) => {
    try {
        const result = await batchSubmit(action, {ids: selectedIds.value});
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    }
};
// 多选操作
const onSelectChange = (e: AdminRoleFilterState[]) => {
    selectedIds.value = e.map((item) => item.roleId);
};


</script>

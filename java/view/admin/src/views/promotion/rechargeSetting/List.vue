<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm isDrawer @okCallback="loadFilter" :title="$t('添加充值余额')" width="700px" path="promotion/rechargeSetting/Info" :params="{ act: 'add' }">
                                        <el-button type="primary">{{ $t('添加充值余额') }}</el-button>
                                    </DialogForm>
                                    <el-popconfirm :title="$t('您确认要批量删除所选数据吗？')" @confirm="onBatchSubmit('del')">
                                        <template #reference>
                                            <el-button :disabled="selectedIds.length === 0">{{ $t('批量删除') }}</el-button>
                                        </template>
                                    </el-popconfirm>
                                    <span v-if="selectedIds.length > 0"> {{ $t('已选择：') }}<b>{{ selectedIds.length }}</b> {{ $t('项') }} </span>

                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="rechargeId" @selection-change="onSelectChange" :total="total" @sort-change="onSortChange" :loading="loading">
                            <el-table-column type="selection" width="32" />
                            <el-table-column :label="$t('充值金额')" prop="money">
                                <template #default="{ row }">
                                    <div style="position: relative;">
                                        <PopForm :label="$t('充值金额')" type="decimal" :requestApi="updateRechargeSettingFiled" v-model:org-value="row.money"
                                            :params="{ id: row.rechargeId, field: 'money' }" :max="50">
                                            <div>{{ row.money }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('赠送金额')" prop="discountMoney">
                                <template #default="{ row }">
                                    <div style="position: relative;">
                                        <PopForm :label="$t('赠送金额')" type="decimal" :requestApi="updateRechargeSettingFiled" v-model:org-value="row.discountMoney"
                                            :params="{ id: row.rechargeId, field: 'discountMoney' }" :max="50">
                                            <div>{{ row.discountMoney }}</div>
                                        </PopForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否显示')" prop="isShow" sortable="custom">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isShow" :requestApi="updateRechargeSettingFiled" :params="{ id: row.rechargeId, field: 'isShow' }" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('排序')" prop="sortOrder" sortable="custom">
                                <template #default="{ row }">
                                    <PopForm :label="$t('排序')" type="integer" :requestApi="updateRechargeSettingFiled" v-model:org-value="row.sortOrder"
                                        :params="{ id: row.rechargeId, field: 'sortOrder' }" :extra="$t('默认值为50，数值越小，排序越靠前')">
                                        <div>{{ row.sortOrder }}</div>
                                    </PopForm>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="150">
                                <template #default="{ row }">
                                    <DialogForm isDrawer @okCallback="loadFilter" :title="$t('编辑充值余额')" width="660px" path="promotion/rechargeSetting/Info"
                                        :params="{ act: 'detail', id: row.rechargeId }">
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord @afterDelete="loadFilter" :requestApi="delRechargeSetting" :params="{ id: row.rechargeId }">{{ $t('删除') }}</DeleteRecord>
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
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import '@/style/css/list.less'
import { DialogForm } from '@/components/dialog'
import { PopForm } from '@/components/pop-form'
import { DeleteRecord, Switch, Pagination } from '@/components/list';
import { useConfigStore } from "@/store/config";
import { RechargeSettingFilterState, RechargeSettingFilterParams } from '@/types/promotion/rechargeSetting.d';
import { getRechargeSettingList, batchSubmit, updateRechargeSettingFiled, delRechargeSetting } from "@/api/promotion/rechargeSetting";
const config:any = useConfigStore();
import { useListRequest } from '@/hooks/useListRequest';
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
} = useListRequest<RechargeSettingFilterState, RechargeSettingFilterParams>({
  apiFunction: getRechargeSettingList,
  idKey: 'rechargeId',
  defaultParams: {
      sortField: '',
      sortOrder: '',
      page: 1,
      size: config.get("pageSize"),
  }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
  await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>

/**
 * @file 会员余额变动记录页
 * @description 展示会员账户余额变动流水
 * @features
 * - 按变动原因/会员名称筛选
 * - 显示变动金额/余额/时间/来源
 * - 区分收入和支出类型
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入账户变动原因')" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span> </el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('会员名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.username" @keyup.enter="onSearchSubmit" clearable @clear="onSearchSubmit" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                                <div class="simple-form-field">
                                    <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="logId">
                            <el-table-column :label="$t('账户变动时间')" prop="changeTime" width="160"></el-table-column>
                            <el-table-column :label="$t('会员用户名')" prop="username" width="120">
                                <template #default="{ row }">
                                    <DialogForm v-if="row.username" :params="{ act: 'edit', id: row.userId }" isDrawer
                                                path="user/user/AccountLog"
                                                :title="$t('会员')+row.username+$t('的账目明细')" width="800px"
                                                @okCallback="loadFilter">
                                        <a class="btn-link">{{ row.username }}</a>
                                    </DialogForm>
                                    <div v-else>--</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('账户变动原因')" prop="changeDesc"></el-table-column>
                            <el-table-column :label="$t('变动资金账户')" width="250">
                                <template #default="{ row }">
                                    <span v-if="row.changeType===1" style="color:#0000FF">+{{ row.balance }}</span>
                                    <span v-else-if="row.changeType===2" style="color:#FF0000">-{{ row.balance }}</span>
                                    <span v-else>{{ row.balance }}</span>
                                    <span v-if="row.afterBalance" style="color:#999;font-size:12px">{{ $t('（变更后金额:{afterBalance}）', { afterBalance: row.afterBalance }) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('冻结资金账户')" width="250">
                                <template #default="{ row }">
                                    <span v-if="row.changeType===1" style="color:#0000FF">+{{ row.frozenBalance }}</span>
                                    <span v-else-if="row.changeType===2" style="color:#FF0000">-{{ row.frozenBalance }}</span>
                                    <span v-else>{{ row.frozenBalance }}</span>
                                    <span v-if="row.afterFrozenBalance" style="color:#999;font-size:12px">{{ $t('（变更后金额:{afterFrozenBalance}）', { afterFrozenBalance: row.afterFrozenBalance }) }}</span>
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
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import {Pagination} from '@/components/list';
import {useConfigStore} from "@/store/config";
import {UserBalanceLogFilterParams, UserBalanceLogFilterState} from '@/types/finance/userBalanceLog';
import {getUserBalanceLogList} from "@/api/finance/userBalanceLog";
import {DialogForm} from "@/components/dialog";
import { useListRequest } from "@/hooks/useListRequest";
const config:any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<UserBalanceLogFilterState, UserBalanceLogFilterParams>({
    apiFunction: getUserBalanceLogList,
    idKey: "logId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        username: "",
        page: 1,
        size: config.get("pageSize")
    }
});
// 初始化加载
loadFilter();
</script>

/**
 * @file 会员提现申请列表页
 * @description 管理会员提现申请记录
 * @features
 * - 按会员名称/提现状态筛选
 * - 显示提现金额/收款方式/状态/申请时间
 * - 支持审核通过/拒绝/打款操作
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入会员名称')"
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
                                        <label class="control-label"><span>{{ $t('到款状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择状态')" v-model="filterParams.status" @change="onSearchSubmit">
                                                <el-option :value="0" :label="$t('待处理')" />
                                                <el-option :value="1" :label="$t('已完成')" />
                                                <el-option :value="2" :label="$t('拒绝申请')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                </div>
                                <div class="simple-form-field">
                                    <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <DialogForm
                                        v-if="props.walletType !== 1"
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        path="finance/userWithdrawApply/Info"
                                        :title="$t('添加申请')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加申请') }}</el-button>
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
                        <el-table
                            :data="filterState"
                            :loading="loading"
                            :total="total"
                            row-key="id"
                            @selection-change="onSelectChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="32" :selectable="(row) => row.status !== 0" />
                            <el-table-column :label="$t('会员名称')" prop="username"></el-table-column>
                            <el-table-column :label="$t('金额')" prop="amount">
                                <template #default="{ row }">
                                    {{ priceFormat(row.amount) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费(元)')" prop="handlingFee">
                                <template #default="{ row }">
                                    {{ row.handlingFee ? row.handlingFee : "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费率')" prop="handlingRate">
                                <template #default="{ row }"> {{ row.handlingRate ? row.handlingRate : "-" }} <span v-if="row.handlingRate">%</span> </template>
                            </el-table-column>
                            <el-table-column :label="$t('提现账号类型')">
                                <template #default="{ row }">
                                    {{ row.accountData.accountType === 1
                                            ? $t('银行卡')
                                            : row.accountData.accountType === 2
                                              ? $t('支付宝')
                                              : row.accountData.accountType === 3
                                                ? $t('微信')
                                                : "N/A" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('到款状态')" prop="statusType"></el-table-column>
                            <el-table-column :width="150" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.id }"
                                        isDrawer
                                        path="finance/userWithdrawApply/Info"
                                        :title="$t('查看申请')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('查看') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord
                                        :params="{ id: row.id }"
                                        :requestApi="delUserWithdrawApply"
                                        @afterDelete="loadFilter"
                                        :disabled="row.status == 0"
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
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { UserWithdrawApplyFilterParams, UserWithdrawApplyFilterState } from "@/types/finance/userWithdrawApply";
import { getUserWithdrawApplyList, batchSubmit, delUserWithdrawApply } from "@/api/finance/userWithdrawApply";
import { DialogForm } from "@/components/dialog";
import { priceFormat } from "@/utils/format";
import { useListRequest } from "@/hooks/useListRequest";
const config: any = useConfigStore();
// D1107：walletType 区分余额提现(0)/佣金提现(1)
const props = withDefaults(defineProps<{ walletType?: number }>(), { walletType: 0 });
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
} = useListRequest<UserWithdrawApplyFilterState, UserWithdrawApplyFilterParams>({
    apiFunction: getUserWithdrawApplyList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: "",
        walletType: props.walletType
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 初始化加载
loadFilter();
</script>

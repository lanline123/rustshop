/**
 * @file 店铺提现申请列表页
 * @description 管理店铺提现申请记录
 * @features
 * - 按申请时间/状态/店铺筛选
 * - 显示提现金额/状态/收款账户/操作时间
 * - 支持审核通过/拒绝/打款操作
 * - 批量处理功能
 */
<template>
    <div class="content_wrapper">
        <div class="container">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:start-date="filterParams.addTimeStart"
                                            v-model:end-date="filterParams.addTimeEnd"
                                        ></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('提现状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable @change="onSearchSubmit">
                                                <el-option v-for="(item, key) in statusList" :key="String(key)" :value="key" :label="item" />
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
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="shopWithdrawLogId" @sort-change="onSortChange">
                            <el-table-column :width="200" :label="$t('申请时间')" prop="addTime" sortable="custom"></el-table-column>
                            <el-table-column :label="`${$t('金额')}(${config.config.dollarSignCn})`" prop="amount"></el-table-column>

                            <el-table-column :label="`${$t('手续费')}(${config.config.dollarSignCn})`" prop="handlingFee" width="120">
                                <template #default="{ row }">
                                    {{ row.handlingFee ? row.handlingFee : "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费率')" prop="handlingRate">
                                <template #default="{ row }"> {{ row.handlingRate ? row.handlingRate : "-" }} <span v-if="row.handlingRate">%</span> </template>
                            </el-table-column>
                            <el-table-column :label="$t('提现类型')" :width="100">
                                <template #default="{ row }">
                                    {{ row.withdrawType === 2 ? $t('线上') : $t('线下') }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('状态')" prop="status">
                                <template #default="{ row }">
                                    <template v-if="row.status == 0 || row.status == 4">
                                        <StatusDot color="blue" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 2">
                                        <StatusDot color="red" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="row.status == 3">
                                        <StatusDot color="green" :flicker="true"></StatusDot>
                                    </template>
                                    <span>{{ row.statusText }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('收款人')">
                                <template #default="{ row }">
                                    <span>{{ row.accountData?.accountName || "--" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('收款账号')">
                                <template #default="{ row }">
                                    <span>{{ row.accountData?.accountNo || "--" }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('到账方式')" :width="200">
                                <template #default="{ row }">
                                    <span>
                                        {{ row.accountData?.accountTypeText || "--" }}
                                    </span>
                                    <span v-if="row.accountData?.bankBranch && row.accountData?.accountType == 1"> - {{ row.accountData?.bankBranch }}</span>
                                </template>
                            </el-table-column>

                            <el-table-column :label="$t('备注')" prop="remark">
                                <template #default="{ row }">
                                    {{ row.remark || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('打款凭证')" :width="160" align="center">
                                <template #default="{ row }">
                                    <Image :src="row.paymentVoucher" fit="contain" style="height: 25px; width: 80px" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('拒绝理由')" prop="auditRemark">
                                <template #default="{ row }">
                                    {{ row.auditRemark || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="120" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <DialogForm
                                        v-if="row.status === 0"
                                        :params="{ act: 'detail', id: row.shopWithdrawLogId }"
                                        isDrawer
                                        path="finance/shopFunds/shopAccountRaply/Info"
                                        :title="$t('提现详情')"
                                        width="500"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('审核') }}</a>
                                    </DialogForm>
                                    <DialogForm
                                        v-if="row.status == 4"
                                        :params="{ act: 'detail', id: row.shopWithdrawLogId }"
                                        isDrawer
                                        path="finance/shopFunds/shopAccountRaply/Info"
                                        :title="$t('上传打款凭证')"
                                        width="500"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('上传打款凭证') }}</a>
                                    </DialogForm>
                                    <DialogForm
                                        v-if="row.status == 3 || row.status == 2"
                                        :showOnOk="row.status === 0 || row.status === 4"
                                        :params="{ act: 'detail', id: row.shopWithdrawLogId }"
                                        isDrawer
                                        path="finance/shopFunds/shopAccountRaply/Info"
                                        :title="$t('提现详情')"
                                        width="500"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('详情') }}</a>
                                    </DialogForm>
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
import { onMounted, reactive, ref } from "vue";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { Pagination } from "@/components/list";
import { Image } from "@/components/image";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { WithdrawFilterParams, WithdrawFilterState } from "@/types/merchant/capitalfund/withdraw.d";
import { getWithdrawList, getWithdrawConfig } from "@/api/merchant/capitalfund/withdraw";
import { SelectTimeInterval } from "@/components/select";
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
    resetParams
} = useListRequest<WithdrawFilterState, WithdrawFilterParams>({
    apiFunction: getWithdrawList,
    idKey: "shopWithdrawLogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        status: "",
        addTimeStart: "",
        addTimeEnd: ""
    }
});

// 初始化加载
loadFilter();
const statusList = ref();
const _getWithdrawConfig = async () => {
    try {
        const config = await getWithdrawConfig();
        statusList.value = config?.data?.statusList || config?.statusList || {};
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
_getWithdrawConfig();
</script>
<style lang="less" scoped></style>

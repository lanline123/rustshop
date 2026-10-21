<template>
    <div class="content_wrapper">
        <div class="container">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('申请时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:start-date="filterParams.addTimeStart"
                                            v-model:end-date="filterParams.addTimeEnd"
                                        />
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('状态：') }}</span></label>
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
                            <el-table-column :label="$t('{shopTypeName}名称', { shopTypeName })" min-width="160">
                                <template #default="{ row }">
                                    {{ row.shopTitle || props.shopTitle || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('渠道')">
                                <template #default="{ row }">
                                    {{ row.accountData?.accountTypeText || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('提现账号')" min-width="130">
                                <template #default="{ row }">
                                    {{ row.accountData?.accountNo || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('提现金额({currency})', { currency })" prop="amount" min-width="120">
                                <template #default="{ row }">
                                    {{ row.amount }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费({currency})', { currency })" prop="handlingFee" min-width="120">
                                <template #default="{ row }">
                                    {{ row.handlingFee === undefined || row.handlingFee === null || row.handlingFee === "" ? "--" : row.handlingFee }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('手续费率')" prop="handlingRate">
                                <template #default="{ row }"> {{ row.handlingRate ? row.handlingRate + "%" : "-" }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('流水单号')" min-width="180">
                                <template #default="{ row }">
                                    {{ row.withdrawSn || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('备注')" prop="remark">
                                <template #default="{ row }">
                                    {{ row.remark || row.remarks || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :width="200" :label="$t('申请时间')" prop="addTime" sortable="custom" />
                            <el-table-column :label="$t('状态')" prop="status" width="120">
                                <template #default="{ row }">
                                    <span>{{ row.statusText || "--" }}</span>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>

                    <div class="withdraw-total-card">
                        <div class="withdraw-total-wrap">
                            <span class="withdraw-total-prefix">{{ $t('总计：') }}</span>
                            <div class="withdraw-total-values">
                                <div class="withdraw-total-item">
                                    <span class="label">{{ $t('审核中：') }}</span>
                                    <span class="value value-pending">{{ priceFormat(pendingWithdrawAmount) }}</span>
                                </div>
                                <div class="withdraw-total-item">
                                    <span class="label">{{ $t('已提现：') }}</span>
                                    <span class="value value-withdrawn">{{ priceFormat(withdrawnAmount) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>

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
import { computed, ref } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { isMerchant, isStore } from "@/utils/version";
import type { WithdrawFilterParams, WithdrawFilterState } from "@/types/merchant/capitalfund/withdraw";
import { getWithdrawList, getWithdrawConfig } from "@/api/merchant/capitalfund/withdraw";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    shopId?: number | string;
    shopTitle?: string;
    pendingWithdrawAmount?: number;
    withdrawnAmount?: number;
    isDialog?: boolean;
    visible?: boolean;
}>();

const config: any = useConfigStore();

const currency = computed(() => config.get("dollarSign") ?? "¥");
const shopTypeName = computed(() => {
    return isStore() ? t('门店') : isMerchant() ? t('店铺') : t('店铺');
});

const {
    listData: filterState,
    loading,
    total,
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
        shopId: props.shopId,
        status: "",
        addTimeStart: "",
        addTimeEnd: ""
    }
});

loadFilter();

const statusList = ref<Record<string, string>>({});
const _getWithdrawConfig = async () => {
    try {
        const cfg = await getWithdrawConfig();
        statusList.value = cfg?.data?.statusList || cfg?.statusList || {};
    } catch (error: any) {
        message.error(error.message);
    }
};
_getWithdrawConfig();

// 兼容 DialogForm 可能触发的 onOk 调用
const onFormSubmit = () => {};
defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.withdraw-total-card {
    display: flex;
    justify-content: flex-end;
    background: #f5f7fa;
    border-radius: 6px;
    padding: 14px 18px;
    color: rgba(0, 0, 0, 0.88);
}
.withdraw-total-wrap {
    display: flex;
    align-items: flex-start;
    gap: 0;
}
.withdraw-total-prefix {
    color: #1a1a1a;
    font-weight: 500;
    white-space: nowrap;
    padding-top: 1px;
}
.withdraw-total-values {
    display: flex;
    flex-direction: column;
    gap: 10px;
}
.withdraw-total-item {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 0;
    .label {
        display: inline-block;
        width: 64px;
        text-align: right;
        color: rgba(0, 0, 0, 0.65);
    }
    .value {
        font-weight: 600;
    }
    .value-pending {
        color: #efb522;
    }
    .value-withdrawn {
        color: var(--el-color-primary);
    }
}
</style>

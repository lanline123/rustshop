/** * @file 保证金交易明细列表页 * @description 列表（关键字/类型/时间快捷区间）+ 只读详情 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row advanced-search-warp">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('组织类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.ownerType" :placeholder="$t('请选择组织类型')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('店铺')" :value="OrgScope.Shop" />
                                                <el-option :label="$t('供应商')" :value="OrgScope.Supplier" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('组织名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.ownerName"
                                                :placeholder="$t('请输入组织名称')"
                                                clearable
                                                @keyup.enter="onSearchSubmit"
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
                                        <label class="control-label"><span>{{ $t('主体类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.subjectType" :placeholder="$t('请选择主体类型')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('个人')" :value="MerchantType.Personal" />
                                                <el-option :label="$t('企业')" :value="MerchantType.Enterprise" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('交易类型：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.type" :placeholder="$t('请选择交易类型')" clearable @change="onSearchSubmit">
                                                <el-option v-for="item in txTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('交易时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:startDate="filterParams.startDate"
                                            v-model:endDate="filterParams.endDate"
                                            clearable
                                        />
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <el-radio-group v-model="quickRange" @change="onQuickRange">
                                        <el-radio-button v-for="item in quickOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio-button>
                                    </el-radio-group>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="onReset">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="shopDepositLogId">
                            <el-table-column :label="$t('组织名称')" min-width="120">
                                <template #default="{ row }">{{ row.ownerName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('主体类型')" min-width="100">
                                <template #default="{ row }">{{ row.subjectTypeText || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('交易类型')" prop="typeText" min-width="100" />
                            <el-table-column :label="$t('交易金额')" min-width="110">
                                <template #default="{ row }">{{ priceFormat(row.changeAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('支付方式')" min-width="100">
                                <template #default="{ row }">{{ row.payeeAccountTypeText || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('交易备注')" min-width="120">
                                <template #default="{ row }">
                                    <el-tooltip
                                        v-if="remarkText(row)"
                                        effect="light"
                                        placement="top"
                                        :content="remarkText(row)"
                                        popper-class="deposit-remark-tooltip"
                                    >
                                        <div class="remark-cell">{{ remarkText(row) }}</div>
                                    </el-tooltip>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('关联凭证')" min-width="100">
                                <template #default="{ row }">
                                    <el-image
                                        v-if="toImgList(row.payVoucher).length"
                                        :src="toImgList(row.payVoucher)[0]"
                                        :preview-src-list="toImgList(row.payVoucher)"
                                        :preview-teleported="true"
                                        fit="cover"
                                        style="width: 40px; height: 40px"
                                    />
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column fixed="right" :label="$t('操作')" width="90">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ id: row.shopDepositLogId }"
                                        isDrawer
                                        :title="$t('交易详情')"
                                        width="700px"
                                        path="finance/deposit/transaction/Info"
                                        :showOnOk="false"
                                    >
                                        <a class="btn-link">{{ $t('详细') }}</a>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp"><div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div></div>
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
import { onMounted, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import { getDepositTransactionList } from "@/api/finance/deposit";
import { DepositTxType, MerchantType, OrgScope } from "@/types/finance/deposit.d";
import type { DepositTransactionFilterParams, DepositTransactionFilterState } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
const quickRange = ref<string>("");

const toImgList = (voucher?: string) => (voucher ? voucher.split(",").filter(Boolean) : []);

// 充值/退还展示关联单备注，其余展示普通备注
const remarkText = (row: DepositTransactionFilterState) =>
    row.type === DepositTxType.Recharge || row.type === DepositTxType.Refund ? row.relatedRemark : row.remark;

const txTypeOptions = [
    { value: DepositTxType.Recharge, label: t('缴纳') },
    { value: DepositTxType.Deduct, label: t('扣除') },
    { value: DepositTxType.Refund, label: t('退还') }
];

const quickOptions = [
    { value: "today", label: t('今天') },
    { value: "yesterday", label: t('昨天') },
    { value: "7days", label: t('近7天') },
    { value: "30days", label: t('近30天') },
    { value: "halfYear", label: t('近半年') }
];

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<DepositTransactionFilterState, DepositTransactionFilterParams>({
    apiFunction: getDepositTransactionList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        ownerType: "",
        ownerName: "",
        subjectType: "",
        type: "",
        startDate: "",
        endDate: ""
    }
});

const onQuickRange = (value: string) => {
    const fmtStart = (d: Date) => formattedDate(d, "YYYY-MM-DD 00:00:00");
    const fmtEnd = (d: Date) => formattedDate(d, "YYYY-MM-DD 23:59:59");
    const today = new Date();
    switch (value) {
        case "today":
            filterParams.startDate = fmtStart(today);
            filterParams.endDate = fmtEnd(today);
            break;
        case "yesterday":
            filterParams.startDate = fmtStart(getDays(1, "sub"));
            filterParams.endDate = fmtEnd(getDays(1, "sub"));
            break;
        case "7days":
            filterParams.startDate = fmtStart(getDays(7, "sub"));
            filterParams.endDate = fmtEnd(today);
            break;
        case "30days":
            filterParams.startDate = fmtStart(getDays(30, "sub"));
            filterParams.endDate = fmtEnd(today);
            break;
        case "halfYear":
            filterParams.startDate = fmtStart(getDays(180, "sub"));
            filterParams.endDate = fmtEnd(today);
            break;
    }
    onSearchSubmit();
};

const onReset = () => {
    quickRange.value = "";
    resetParams();
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.remark-cell {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>

<style lang="less">
.deposit-remark-tooltip.el-popper {
    max-width: 320px;
}
</style>

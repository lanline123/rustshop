/** * @file 保证金查询列表页（店铺保证金总览） * @description 列表 + 筛选 + 详情/调整/扣除/发送预警操作 */
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
                                        <label class="control-label"><span>{{ $t('经营类目：') }}</span></label>
                                        <div class="control-container">
                                            <SelectCategory v-model:categoryId="filterParams.categoryId" />
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('保证金状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.depositStatus" :placeholder="$t('请选择保证金状态')" clearable @change="onSearchSubmit">
                                                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('入驻时间：') }}</span></label>
                                        <SelectTimeInterval
                                            type="date"
                                            v-model:startDate="filterParams.startDate"
                                            v-model:endDate="filterParams.endDate"
                                            clearable
                                        />
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"><span></span></label>
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :row-key="rowKey">
                            <el-table-column :label="$t('组织名称')" min-width="140">
                                <template #default="{ row }">{{ row.ownerName || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('主体类型')" min-width="100">
                                <template #default="{ row }">{{ row.subjectTypeText || "--" }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('经营类目')" min-width="160">
                                <template #default="{ row }">
                                    <el-tooltip
                                        v-if="row.businessCategory"
                                        effect="light"
                                        placement="top"
                                        popper-class="deposit-category-tooltip"
                                    >
                                        <template #content>
                                            <div class="tooltip-content">{{ row.businessCategory }}</div>
                                        </template>
                                        <div class="ellipsis-3">{{ row.businessCategory }}</div>
                                    </el-tooltip>
                                    <template v-else>--</template>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('应缴金额')" min-width="110">
                                <template #default="{ row }">{{ priceFormat(row.required) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('已缴金额')" min-width="110">
                                <template #default="{ row }">{{ priceFormat(row.paid) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('剩余待缴')" min-width="110">
                                <template #default="{ row }">{{ priceFormat(row.pending) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('保证金状态')" min-width="110">
                                <template #default="{ row }">{{ row.depositStatusText || statusText(row.depositStatus) }}</template>
                            </el-table-column>
                            <el-table-column fixed="right" :label="$t('操作')" width="220">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            :params="{ ownerType: row.ownerType, ownerId: row.ownerId, shopId: row.shopId }"
                                            is-drawer
                                            :title="row.ownerName || row.shopTitle || $t('保证金详情')"
                                            width="980px"
                                            path="finance/deposit/shop/src/Detail"
                                            :showOnOk="false"
                                        >
                                            <a class="btn-link">{{ $t('详细') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            :params="{ ownerType: row.ownerType, ownerId: row.ownerId, shopId: row.shopId }"
                                            is-drawer
                                            :title="$t('调整保证金')"
                                            width="980px"
                                            path="finance/deposit/shop/src/AdjustForm"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t('调整保证金') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <el-dropdown>
                                            <el-icon class="btn-link" size="12">
                                                <MoreFilled />
                                            </el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item>
                                                        <DialogForm
                                                            :params="{ ownerType: row.ownerType, ownerId: row.ownerId, shopId: row.shopId }"
                                                            is-drawer
                                                            :title="$t('扣除保证金')"
                                                            width="980px"
                                                            path="finance/deposit/shop/src/DeductForm"
                                                            @okCallback="loadFilter"
                                                        >
                                                            <el-button style="width: 100%" link type="primary">{{ $t('扣除保证金') }}</el-button>
                                                        </DialogForm>
                                                    </el-dropdown-item>
                                                    <DeleteRecord
                                                        :params="{ ownerType: row.ownerType, ownerId: row.ownerId, shopId: row.shopId }"
                                                        :requestApi="sendDepositWarning"
                                                        :title="$t('确认向该{Supplier}发送保证金预警？', { Supplier: row.ownerType === OrgScope.Supplier ? '供应商' : '店铺' })"
                                                        okType="primary"
                                                    >
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link type="primary">{{ $t('发送预警') }}</el-button>
                                                        </el-dropdown-item>
                                                    </DeleteRecord>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
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
import { computed, onMounted } from "vue";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { SelectTimeInterval, SelectCategory } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { getDepositQueryList, sendDepositWarning } from "@/api/finance/deposit";
import { DepositAccountStatus, MerchantType, OrgScope } from "@/types/finance/deposit.d";
import type { DepositQueryFilterParams, DepositQueryFilterState } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();

const statusOptions = [
    { value: DepositAccountStatus.Normal, label: t('正常') },
    { value: DepositAccountStatus.Insufficient, label: t('不足') },
    { value: DepositAccountStatus.Frozen, label: t('冻结') },
    { value: DepositAccountStatus.Unpaid, label: t('未缴纳') }
];

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<DepositQueryFilterState, DepositQueryFilterParams>({
    apiFunction: getDepositQueryList,
    idKey: "shopId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        ownerType: "",
        ownerName: "",
        subjectType: "",
        categoryId: "",
        depositStatus: "",
        startDate: "",
        endDate: ""
    }
});

// 供应商行 shopId 为兼容字段（可能为 0/空），用 ownerType-ownerId 组合键避免重复
const rowKey = (row: DepositQueryFilterState) => `${row.ownerType}-${row.ownerId}`;

const statusText = (status: number) => {
    switch (status) {
        case DepositAccountStatus.Normal:
            return t('正常');
        case DepositAccountStatus.Insufficient:
            return t('不足');
        case DepositAccountStatus.Frozen:
            return t('冻结');
        case DepositAccountStatus.Unpaid:
            return t('未缴纳');
        default:
            return "--";
    }
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
.control-label {
    width: 90px !important;
}
.ellipsis-3 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-word;
    line-height: 1.5;
}
</style>
<style lang="less">
.deposit-category-tooltip {
    .tooltip-content {
        max-width: 340px;
    }
}
</style>

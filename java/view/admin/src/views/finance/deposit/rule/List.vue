/** * @file 保证金规则列表页 * @description 管理按类目收取的保证金规则 */
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
                                        <label class="control-label"><span>{{ $t('规则名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.ruleName"
                                                name="ruleName"
                                                :placeholder="$t('请输入规则名称')"
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
                                        <label class="control-label"><span>{{ $t('规则状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" :placeholder="$t('请选择规则状态')" clearable @change="onSearchSubmit">
                                                <el-option :label="$t('启用')" :value="1" />
                                                <el-option :label="$t('禁用')" :value="0" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('生效时间：') }}</span></label>
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
                                        <label class="control-label"></label>
                                        <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                        <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <DialogForm
                                    :params="{ act: 'add' }"
                                    isDrawer
                                    :title="$t('添加规则')"
                                    width="1000px"
                                    path="finance/deposit/rule/Info"
                                    @okCallback="loadFilter"
                                >
                                    <el-button type="primary">{{ $t('添加规则') }}</el-button>
                                </DialogForm>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="shopDepositRuleId">
                            <el-table-column :label="$t('规则名称')" min-width="160" prop="ruleName" />
                            <el-table-column :label="$t('适用类目')" min-width="280">
                                <template #default="{ row }">
                                    <el-tooltip
                                        v-if="row.categoryNames?.length"
                                        effect="light"
                                        placement="top"
                                        popper-class="deposit-category-tooltip"
                                    >
                                        <template #content>
                                            <div class="tooltip-content">{{ row.categoryNames.join("、") }}</div>
                                        </template>
                                        <div class="ellipsis-3">{{ row.categoryNames.join("、") }}</div>
                                    </el-tooltip>
                                    <template v-else>--</template>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('商家类型')" min-width="160">
                                <template #default="{ row }">
                                    {{ row.merchantTypeText || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('保证金金额')" min-width="120">
                                <template #default="{ row }">{{ priceFormat(row.depositAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('规则状态')" width="110" align="center">
                                <template #default="{ row }">
                                    <el-switch
                                        :model-value="row.status"
                                        :active-value="1"
                                        :inactive-value="0"
                                        @change="(value: unknown) => onStatusChange(row, value as number)"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('生效时间')" min-width="320">
                                <template #default="{ row }">{{ formatEffectiveTime(row) }}</template>
                            </el-table-column>
                            <el-table-column fixed="right" :label="$t('操作')" width="140">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'edit', id: row.shopDepositRuleId }"
                                        isDrawer
                                        :title="$t('编辑规则')"
                                        width="1000px"
                                        path="finance/deposit/rule/Info"
                                        @okCallback="loadFilter"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.shopDepositRuleId }" :requestApi="delDepositRule" @afterDelete="loadFilter()">
                                        <a class="btn-link">{{ $t('删除') }}</a>
                                    </DeleteRecord>
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
import { onMounted } from "vue";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { delDepositRule, getDepositRuleList, updateDepositRule } from "@/api/finance/deposit";
import type { DepositRuleFilterParams, DepositRuleFilterState } from "@/types/finance/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<DepositRuleFilterState, DepositRuleFilterParams>({
    apiFunction: getDepositRuleList,
    idKey: "shopDepositRuleId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        ruleName: "",
        status: "",
        startDate: "",
        endDate: ""
    }
});

const formatToMinute = (text?: string, timestamp?: number) => {
    if (text) return dayjs(text).format("YYYY-MM-DD HH:mm");
    if (timestamp) return dayjs.unix(timestamp).format("YYYY-MM-DD HH:mm");
    return "";
};

const formatEffectiveTime = (row: DepositRuleFilterState) => {
    if (!row.startTimeText && !row.startTime) return "--";
    const start = formatToMinute(row.startTimeText, row.startTime);
    const end = row.endTime === 0 ? row.endTimeText || "长期有效" : row.endTimeText || row.endTime ? formatToMinute(row.endTimeText, row.endTime) : "--";
    return `${start} ~ ${end}`;
};

const onStatusChange = async (row: DepositRuleFilterState, nextStatus: number) => {
    const previousStatus = row.status;
    row.status = nextStatus;
    try {
        await updateDepositRule("status", { shopDepositRuleId: row.shopDepositRuleId, status: nextStatus });
        message.success(t('操作成功'));
        await loadFilter();
    } catch (error: any) {
        row.status = previousStatus;
        message.error(error.message);
    }
};

onMounted(() => {
    loadFilter();
});
</script>

<style lang="less" scoped>
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

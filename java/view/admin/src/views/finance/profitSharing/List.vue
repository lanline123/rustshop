<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <!-- 目前只有店铺/门店分账结算；供应商分账结算尚未实现，实现后在此处扩展 tab 与 settlementType -->
                    <div class="profit-sharing-tabs">
                        <el-tabs model-value="shop">
                            <el-tab-pane :label="shopSettlementLabel" name="shop" />
                        </el-tabs>
                    </div>
                    <el-form :model="filterParams">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ shopTypeName }}：</span></label>
                                        <div class="control-container">
                                            <SelectStore v-if="isStore()" v-model:storeId="filterParams.shopId" :placeholder="shopNamePlaceholder" @onChange="onSearchSubmit"></SelectStore>
                                            <SelectShop v-else v-model:shopId="filterParams.shopId" :placeholder="shopNamePlaceholder" @onChange="onSearchSubmit"></SelectShop>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('分账状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择')" v-model="filterParams.status" @change="onSearchSubmit">
                                                <el-option value="PENDING" :label="$t('待处理')" />
                                                <el-option value="PROCESSING" :label="$t('处理中')" />
                                                <el-option value="SUCCESS" :label="$t('成功')" />
                                                <el-option value="FAILED" :label="$t('失败')" />
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('完结状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select clearable :placeholder="$t('请选择')" v-model="filterParams.finishStatus" @change="onSearchSubmit">
                                                <el-option value="UNFINISHED" :label="$t('未完结')" />
                                                <el-option value="FINISHED" :label="$t('已完结')" />
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
                        <el-table :data="filterState" :loading="loading" row-key="id">
                            <el-table-column :label="$t('分账单号')" prop="outOrderNo" min-width="190" />
                            <el-table-column :label="$t('订单号')" prop="orderSn" min-width="170" />
                            <el-table-column :label="shopTypeName" min-width="160">
                                <template #default="{ row }">
                                    <div>{{ row.shopName || "-" }}</div>
                                    <div class="extra">ID: {{ row.shopId }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('订单金额')" align="right" width="110">
                                <template #default="{ row }">{{ priceFormat(row.totalAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('分账金额')" align="right" width="110">
                                <template #default="{ row }">{{ priceFormat(row.splitAmount) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('服务费')" align="right" width="100">
                                <template #default="{ row }">{{ priceFormat(row.serviceFee) }}</template>
                            </el-table-column>
                            <el-table-column :label="$t('分账状态')" width="100">
                                <template #default="{ row }">
                                    <span :class="getStatusClass(row.status)">{{ row.statusDesc || row.status }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('完结状态')" width="100">
                                <template #default="{ row }">
                                    <span :class="row.finishStatus === 'FINISHED' ? 'green' : 'yellow'">
                                        {{ row.finishStatusDesc || row.finishStatus }}
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('触发方式')" width="90">
                                <template #default="{ row }">
                                    <el-tag v-if="row.triggerType === 'AUTO'" type="primary" disable-transitions>{{ $t('自动') }}</el-tag>
                                    <el-tag v-else-if="row.triggerType === 'MANUAL'" type="info" disable-transitions>{{ $t('手动') }}</el-tag>
                                    <span v-else>-</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="createTime" width="170" />
                            <el-table-column :width="170" fixed="right" :label="$t('操作')">
                                <template #default="{ row }">
                                    <a v-if="row.status === 'PENDING'" class="btn-link" @click="handleManualSubmit(row)">{{ $t('手动分账') }}</a>
                                    <template v-if="row.canFinish">
                                        <el-divider v-if="row.status === 'PENDING'" direction="vertical" />
                                        <a class="btn-link" @click="handleManualFinish(row)">{{ $t('手动完结') }}</a>
                                    </template>
                                    <span v-if="row.status !== 'PENDING' && !row.canFinish">-</span>
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
import { message, Modal } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { SelectShop, SelectStore } from "@/components/select";
import { priceFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { getProfitSharingList, submitProfitSharingBatch, manualFinishProfitSharing } from "@/api/finance/profitSharing";
import type { ProfitSharingOrderItem, ProfitSharingListParams } from "@/types/finance/profitSharing.d";
import { isStore } from "@/utils/version";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const shopTypeName = isStore() ? t("门店") : t("店铺");
const shopSettlementLabel = isStore() ? t("门店分账结算") : t("店铺分账结算");
const shopNamePlaceholder = isStore() ? t("请输入门店名称") : t("请输入店铺名称");
/** 结算主体类型：1店铺 2供应商；当前仅实现店铺分账 */
const SHOP_SETTLEMENT_TYPE = 1;

const getStatusClass = (status?: string) => {
    const map: Record<string, string> = { PENDING: "yellow", PROCESSING: "black", SUCCESS: "green", FAILED: "red" };
    return map[status ?? ""] ?? "gray";
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<ProfitSharingOrderItem, ProfitSharingListParams>({
    apiFunction: getProfitSharingList,
    idKey: "id",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        shopId: "",
        settlementType: SHOP_SETTLEMENT_TYPE,
        status: "",
        finishStatus: "",
        startDate: "",
        endDate: ""
    }
}, ["settlementType"]);

const handleManualSubmit = (row: ProfitSharingOrderItem) => {
    if (!row.settlementId) {
        message.warning(t('缺少结算单信息，无法分账'));
        return;
    }
    Modal.confirm({
        title: t('确认分账'),
        content: t('是否确认对该订单发起分账？此操作不可撤销。'),
        async onOk() {
            try {
                await submitProfitSharingBatch({ settlementIds: [Number(row.settlementId)] });
                message.success(t('分账已提交'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message || t('操作失败'));
            }
        }
    });
};

const handleManualFinish = (row: ProfitSharingOrderItem) => {
    if (!row.id) return;
    Modal.confirm({
        title: t('确认完结'),
        content: t('完结后剩余未分账资金将解冻回商户，是否确认完结？'),
        async onOk() {
            try {
                await manualFinishProfitSharing({ id: row.id });
                message.success(t('已完结'));
                loadFilter();
            } catch (error: any) {
                message.error(error.message || t('操作失败'));
            }
        }
    });
};

loadFilter();
</script>

<style lang="less" scoped>
.profit-sharing-tabs {
    margin-bottom: 12px;
}
.black {
    color: #333;
}
</style>

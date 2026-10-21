/** * @file 历史结算（抽屉） * @description 对接接口：GET /credit/settlement/history，POST /credit/settlement/cancel */
<template>
    <div class="drawer-container">
        <div class="list-table-tool lyecs-search-warp">
            <div class="advanced-search-warp list-table-tool-row">
                <div class="simple-form-warp">
                    <div class="simple-form-field">
                        <div class="form-group">
                            <label class="control-label" style="width: 90px;"><span>{{ $t('回款流水单号') }}</span></label>
                            <div class="control-container">
                                <TigInput
                                    v-model="filterParams.settlementSn"
                                    :placeholder="$t('请输入回款流水单号')"
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
                            <label class="control-label" style="width: 90px;"><span>{{ $t('结算时间') }}</span></label>
                            <SelectTimeInterval
                                v-model:end-date="filterParams.settlementTimeEnd"
                                v-model:start-date="filterParams.settlementTimeStart"
                                :clearable="true"
                                type="date"
                                value-format="YYYY-MM-DD"
                                @callback="onSearchSubmit"
                            />
                        </div>
                    </div>
                    <div class="simple-form-field">
                        <div class="control-container">
                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="table-container" style="margin-top: 16px">
            <a-spin :spinning="loading">
                <el-table :data="list" row-key="settlementId" :loading="loading">
                    <el-table-column :label="$t('回款流水单号')" prop="settlementSn" min-width="200" />
                    <el-table-column :label="$t('结算金额')" width="100">
                        <template #default="{ row }">{{ money(row.settlementAmount) }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('结算订单数')" prop="orderCount" width="120" />
                    <el-table-column :label="$t('备注')" prop="remark" min-width="200" show-overflow-tooltip />
                    <el-table-column :label="$t('结算时间')" prop="settlementTime" width="170" />
                    <el-table-column :label="$t('结算状态')" prop="settlementStatusText" width="100" />
                    <el-table-column :label="$t('操作')" width="180" fixed="right">
                        <template #default="{ row }">
                            <DeleteRecord
                                :requestApi="cancelCreditSettlement"
                                :params="{ settlementId: row.settlementId }"
                                :title="$t('撤销结算')"
                                :content="renderCancelContent(row)"
                                :okText="$t('确认撤销')"
                                okType="primary"
                                modalWidth="450px"
                                :contentStyle="{ height: '60px', padding: '10px 0' }"
                                :message="$t('撤销成功')"
                                :disabled="row.settlementStatus !== 1"
                                @afterDelete="onCancelSuccess"
                            >
                                <el-button text type="primary" :disabled="row.settlementStatus !== 1">{{ $t('撤销结算') }}</el-button>
                            </DeleteRecord>
                            <el-divider direction="vertical" style="margin: 0" />
                            <DialogForm
                                :title="$t('回款流水单号：{settlementSn}', { settlementSn: row.settlementSn || '-' })"
                                isDrawer
                                width="1020px"
                                :showFooter="false"
                                :showOnOk="false"
                                :showClose="false"
                                :params="{ settlementId: row.settlementId }"
                                path="order/termPay/termPay/src/HistoryOrders"
                            >
                                <el-button text type="primary">{{ $t('详情') }}</el-button>
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
        </div>

        <div class="pagination-con" v-if="total > 0">
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadList" />
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { h, onMounted, reactive, ref } from "vue";
import { DeleteRecord, Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { getCreditSettlementHistory, cancelCreditSettlement } from "@/api/termPay/creditSettlement";
import type { CreditSettlementHistoryQuery, CreditSettlementHistoryRecord } from "@/types/termPay/creditSettlement.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{
    userId?: number;
    username?: string;
    isDialog?: boolean;
}>();
const emit = defineEmits(["callback"]);

const loading = ref(false);
const total = ref(0);
const list = ref<CreditSettlementHistoryRecord[]>([]);

const filterParams = reactive<CreditSettlementHistoryQuery>({
    page: 1,
    size: 10,
    userId: props.userId,
    settlementSn: "",
    settlementTimeStart: "",
    settlementTimeEnd: ""
});

const money = (n: number) => {
    const v = Number.isFinite(Number(n)) ? Number(n) : 0;
    return v.toFixed(2);
};

const renderCancelContent = (row: CreditSettlementHistoryRecord) => {
    const settlementSn = row.settlementSn || "-";
    const settlementAmount = '￥' + money(row.settlementAmount);
    return h(
        "div",
        { style: { lineHeight: "22px", color: "#000", fontSize: "14px" } },
        [
            h("span", t('确定撤销结算回款单流水号：')),
            h("span", { style: { color: "#e9ac0b", fontWeight: "600" } }, settlementSn),
            h("span", t('，金额：')),
            h("span", { style: { color: "#e9ac0b", fontWeight: "600" } }, settlementAmount),
            h("span", t(' 的结算单吗？'))
        ]
    );
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = 10;
    filterParams.settlementSn = "";
    filterParams.settlementTimeStart = "";
    filterParams.settlementTimeEnd = "";
    loadList();
};

const loadList = async () => {
    loading.value = true;
    try {
        const params: CreditSettlementHistoryQuery = {
            page: filterParams.page,
            size: filterParams.size,
            userId: filterParams.userId || undefined,
            settlementSn: filterParams.settlementSn || undefined,
            settlementTimeStart: filterParams.settlementTimeStart || undefined,
            settlementTimeEnd: filterParams.settlementTimeEnd || undefined
        };
        const res = await getCreditSettlementHistory(params);
        list.value = res.records || [];
        total.value = res.total || 0;
    } finally {
        loading.value = false;
    }
};

const onCancelSuccess = () => {
    emit("callback", { type: "historyChanged" });
    loadList();
};

onMounted(() => {
    loadList();
});
</script>

<style scoped lang="less">
.drawer-container {
    padding: 0 16px 16px 16px;
}

.table-container {
    overflow-x: auto;
}
</style>

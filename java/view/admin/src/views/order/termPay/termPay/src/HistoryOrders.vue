/**
 * @file 历史结算订单明细（弹窗）
 * @description 对接接口：GET /credit/settlement/historyOrders
 */
<template>
    <div class="drawer-container">
        <div class="list-table-tool lyecs-search-warp">
            <div class="advanced-search-warp list-table-tool-row">
                <div class="simple-form-warp">
                    <div class="simple-form-field">
                        <div class="form-group">
                            <label class="control-label"><span>{{ $t('订单编号：') }}</span></label>
                            <div class="control-container">
                                <TigInput
                                    v-model="filterParams.orderSn"
                                    :placeholder="$t('订单编号')"
                                    @keyup.enter="onSearchSubmit"
                                    clearable
                                    @clear="onSearchSubmit"
                                >
                                </TigInput>
                            </div>
                        </div>
                    </div>

                    <div class="simple-form-field">
                        <div class="form-group">
                            <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                            <SelectTimeInterval
                                v-model:end-date="filterParams.orderTimeEnd"
                                v-model:start-date="filterParams.orderTimeStart"
                                :clearable="true"
                                type="date"
                                value-format="YYYY-MM-DD"
                                @callback="onSearchSubmit"
                            />
                        </div>
                    </div>

                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <label class="control-label"></label>
                            <div class="control-container">
                                <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table :data="pageList" row-key="orderId" :loading="loading">
                    <el-table-column :label="$t('订单编号')" prop="orderSn" />
                    <el-table-column :label="$t('下单时间')" prop="orderTime" width="200" />
                    <el-table-column :label="$t('订单总金额')" width="150">
                        <template #default="{ row }">{{ money(Number(row.orderAmount || 0)) }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('账期支付金额')" width="150">
                        <template #default="{ row }">{{ money(Number(row.creditAmount || 0)) }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('退款金额')" width="150">
                        <template #default="{ row }">{{ money(Number(row.refundedAmount || 0)) }}</template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
        </div>

        <div v-if="total > 0" class="pagination-con">
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="onPageChange" />
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import { getCreditSettlementHistoryOrders } from "@/api/termPay/creditSettlement";
import type { CreditSettlementHistoryOrdersQuery, CreditSettlementHistoryOrderRecord } from "@/types/termPay/creditSettlement.d";

const props = defineProps<{
    /** 结算记录ID */
    settlementId: number;
}>();

type HistoryOrderRow = CreditSettlementHistoryOrderRecord & {
    /** 退款金额（接口字段可能不在类型里，页面计算用；缺省按 0 处理） */
    refundedAmount?: number;
};

const loading = ref(false);

const filterParams = reactive<CreditSettlementHistoryOrdersQuery>({
    settlementId: props.settlementId,
    page: 1,
    size: 10,
    orderSn: "",
    orderTimeStart: "",
    orderTimeEnd: ""
});

const total = ref(0);
const pageList = ref<HistoryOrderRow[]>([]);

const money = (n: number) => {
    const v = Number.isFinite(n) ? n : 0;
    return v.toFixed(2);
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadList();
};

const onPageChange = () => {
    loadList();
};

const resetParams = () => {
    filterParams.page = 1;
    filterParams.size = 10;
    filterParams.orderSn = "";
    filterParams.orderTimeStart = "";
    filterParams.orderTimeEnd = "";
    loadList();
};

const loadList = async () => {
    loading.value = true;
    try {
        if (!filterParams.settlementId) {
            total.value = 0;
            pageList.value = [];
            return;
        }
        const params: CreditSettlementHistoryOrdersQuery = {
            settlementId: filterParams.settlementId,
            page: filterParams.page,
            size: filterParams.size,
            orderSn: filterParams.orderSn || undefined,
            orderTimeStart: filterParams.orderTimeStart || undefined,
            orderTimeEnd: filterParams.orderTimeEnd || undefined
        };
        const res = await getCreditSettlementHistoryOrders(params);
        total.value = res?.total || 0;
        pageList.value = (res?.records || []) as HistoryOrderRow[];
    } finally {
        loading.value = false;
    }
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
    margin-top: 16px;
}
</style>

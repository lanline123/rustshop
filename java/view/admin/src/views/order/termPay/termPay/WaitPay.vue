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

        <div class="selected-bar">
            <div class="left"> {{ $t('已选') }} <b>{{ selectedRows.length }}</b> {{ $t('条') }} <span class="ml12">{{ $t('金额') }} <b>{{ money(selectedAmount) }}</b></span>
            </div>
        </div>

        <div class="table-container">
            <a-spin :spinning="loading">
                <el-table
                    ref="tableRef"
                    :data="pageList"
                    row-key="orderId"
                    :loading="loading"
                    @selection-change="onSelectionChange"
                    @row-click="onRowClick"
                >
                    <el-table-column type="selection" width="32" />
                    <el-table-column :label="$t('订单编号')" prop="orderSn" />
                    <el-table-column :label="$t('下单时间')" prop="orderTime" width="200" />
                    <el-table-column :label="$t('订单总金额')" width="150">
                        <template #default="{ row }">{{ money(Number(row.orderAmount || 0)) }}</template>
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

        <!-- 二级抽屉：本次结算（由上方 DialogForm 触发打开） -->
        <DialogForm
            ref="toPayFormDialogRef"
            :title="$t('确认结算')"
            isDrawer
            width="720px"
            path="order/termPay/termPay/src/toPayForm"
            :params="toPayFormParams"
            style="display: none"
            @okCallback="onToPayOk"
        >
            <span></span>
        </DialogForm>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { computed, nextTick, onMounted, reactive, ref, watchEffect } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { SelectTimeInterval } from "@/components/select";
import { getCreditSettlementPendingOrders } from "@/api/termPay/creditSettlement";
import type { CreditSettlementPendingOrderRecord } from "@/types/termPay/creditSettlement.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type WaitPayOrderRow = CreditSettlementPendingOrderRecord & {
    /** 退款金额（接口字段可能不在类型里，页面计算用；缺省按 0 处理） */
    refundedAmount?: number;
};

const props = defineProps<{
    /** 会员ID（接口需要） */
    userId?: number;
    /** 会员用户名 */
    username?: string;
    isDialog?: boolean;
}>();

const emit = defineEmits(["callback", "close", "okType"]);

const userId = computed(() => props.userId || 0);
const username = computed(() => props.username || "");

const toPayFormDialogRef = ref<any>();
const toPayFormParams = ref<{ userId?: number; orderIds?: number[] }>({});

const loading = ref(false);

const filterParams = reactive({
    page: 1,
    size: 10,
    orderSn: "",
    orderTimeStart: "",
    orderTimeEnd: ""
});

const selectedRows = ref<WaitPayOrderRow[]>([]);
const tableRef = ref<any>();
const total = ref(0);
const pageList = ref<WaitPayOrderRow[]>([]);

const canSubmit = computed(() => {
    return selectedRows.value.length > 0 && userId.value > 0;
});

watchEffect(() => {
    emit("okType", canSubmit.value);
});

// 金额 = 勾选行订单总金额之和 - 退款金额之和
const selectedAmount = computed(() =>
    selectedRows.value.reduce((sum, r) => sum + Number(r.orderAmount || 0) - Number(r.refundedAmount || 0), 0)
);

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

/**
 * el-table selection-change（仅当前表格数据范围内生效）
 * 不做跨页保留：翻页/筛选/刷新后，选中项以 el-table 内部状态为准
 */
const onSelectionChange = (selection: WaitPayOrderRow[]) => {
    selectedRows.value = selection || [];
};

const onRowClick = (row: WaitPayOrderRow) => {
    // 提升可用性：点击整行也能勾选/取消（不影响复选框点击）
    tableRef.value?.toggleRowSelection?.(row);
};

/**
 * 外层 DialogForm 点击“确认结算”时触发
 * 直接打开二级抽屉（跳过中间弹窗）
 */
const onFormSubmit = () => {
    if (!selectedRows.value.length) {
        message.warning(t('请先选择需要结算的订单'));
        return;
    }
    if (!userId.value) {
        message.warning(t('缺少会员ID'));
        return;
    }
    const orderIds = selectedRows.value.map((row) => row.orderId);
    toPayFormParams.value = {
        userId: userId.value,
        orderIds
    };
    nextTick(() => {
        toPayFormDialogRef.value?.show?.();
    });
};

const onToPayOk = (result: any) => {
    // 二级抽屉提交成功：关闭一级抽屉 + 通知上层刷新
    emit("close");
    emit("callback", { type: "settlementSuccess", payload: result });
    loadList();
};

const loadList = async () => {
    loading.value = true;
    try {
        if (!userId.value) {
            total.value = 0;
            pageList.value = [];
            return;
        }
        const res = await getCreditSettlementPendingOrders({
            userId: userId.value,
            page: filterParams.page,
            size: filterParams.size,
            orderSn: filterParams.orderSn || undefined,
            orderTimeStart: filterParams.orderTimeStart || undefined,
            orderTimeEnd: filterParams.orderTimeEnd || undefined
        });
        total.value = res?.total || 0;
        pageList.value = ((res?.records || []) as WaitPayOrderRow[]) || [];
        selectedRows.value = [];
        tableRef.value?.clearSelection?.();
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadList();
});

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.drawer-container {
    padding: 0 16px 16px 16px;
}

.selected-bar {
    display: flex;
    align-items: center;
    // justify-content: space-between;
    padding: 12px 0;

    .ml12 {
        margin-left: 12px;
    }
    .right {
        margin-left: 10px;
    }
}

.table-container {
    overflow-x: auto;

    .custom-table {
        width: 100%;
        border-collapse: collapse;
        background: #fff;
        border: 1px solid #f0f0f0;

        thead th {
            background-color: #f5f6fa;
            font-weight: 600;
            text-align: left;
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            white-space: nowrap;
        }

        tbody td {
            padding: 12px;
            border-bottom: 1px solid #f0f0f0;
            white-space: nowrap;
        }

        tbody tr:hover td {
            background: #fafafa;
        }
    }
}

.textR {
    text-align: right;
}
</style>

<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="advanced-search-warp list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field mr10">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('订单编号：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.orderSn"
                                            name="orderSn"
                                            :placeholder="$t('请输入订单编号')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        ></TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field" v-if="filterParams.timeType === 1">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('结算状态：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.status" :placeholder="$t('请选择')" @change="onSearchSubmit" clearable>
                                            <el-option :label="$t('未结算')" :value="0" />
                                            <el-option :label="$t('已结算')" :value="1" />
                                            <el-option :label="$t('已失效')" :value="2" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('筛选类型：') }}</span></label>
                                    <div class="control-container">
                                        <el-select v-model="filterParams.timeType" :placeholder="$t('请选择时间')">
                                            <el-option :label="$t('下单时间')" :value="1" />
                                            <el-option :label="$t('退款时间')" :value="2" />
                                        </el-select>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('筛选时间：') }}</span></label>
                                    <SelectTimeInterval
                                        type="date"
                                        v-model:start-date="filterParams.orderTimeStart"
                                        v-model:end-date="filterParams.orderTimeEnd"
                                        @callback="changeDateType(-1)"
                                    ></SelectTimeInterval>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType">
                                        <el-radio-button :value="0">{{ $t('今天') }}</el-radio-button>
                                        <el-radio-button :value="1">{{ $t('昨天') }}</el-radio-button>
                                        <el-radio-button :value="2">{{ $t('近7天') }}</el-radio-button>
                                        <el-radio-button :value="3">{{ $t('近30天') }}</el-radio-button>
                                        <el-radio-button :value="4">{{ $t('近半年') }}</el-radio-button>
                                    </el-radio-group>
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
                <el-dialog v-model="dialogVisible" :title="$t('导出数据')" width="500" top="30vh" append-to-body :style="{ zIndex: 2000 }">
                    <div style="padding-top: 30px">
                        <el-form :model="exportParams" label-width="100px">
                            <el-form-item :label="$t('导出维度：')">
                                <div>
                                    <el-radio-group v-model="exportParams.range">
                                        <el-radio :value="0">{{ $t('商品维度') }}</el-radio>
                                        <el-radio :value="1">{{ $t('订单维度') }}</el-radio>
                                    </el-radio-group>
                                    <div class="extra">
                                        <div v-if="exportParams.range == 0">{{ $t('以商品维度导出，每个商品展示一行') }}</div>
                                        <div v-if="exportParams.range == 1">{{ $t('以订单维度导出，每个订单展示一行') }}</div>
                                    </div>
                                </div>
                            </el-form-item>
                        </el-form>
                    </div>
                    <template #footer>
                        <div class="dialog-footer">
                            <el-button @click="dialogVisible = false">{{ $t('取消') }}</el-button>
                            <el-button type="primary" @click="handleExportData"> {{ $t('导出') }} </el-button>
                        </div>
                    </template>
                </el-dialog>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table
                            :data="filterState"
                            :total="total"
                            row-key="salesmanOrderId"
                            @selection-change="handleSelectionChange"
                            @sort-change="onSortChange"
                        >
                            <el-table-column type="selection" width="40" fixed="left" :selectable="canSettle" />
                            <el-table-column :label="$t('订单号')" prop="orderId" :minWidth="190" fixed="left">
                                <template #default="{ row }">
                                    <DialogForm
                                        :params="{ act: 'detail', id: row.orderUserInfo.orderId }"
                                        :showClose="false"
                                        :showOnOk="false"
                                        :title="$t('订单详情 ') + row.orderUserInfo.orderSn"
                                        isDrawer
                                        path="order/order/Info"
                                        width="880px"
                                        @callback="loadFilter"
                                    >
                                        <el-button link type="primary"> {{ row.orderUserInfo.orderSn }}</el-button>
                                    </DialogForm>
                                </template>
                            </el-table-column>
                            <el-table-column prop="orderAmount" :width="140">
                                <template #header>
                                    <div class="flex flex-align-center">
                                        <p>{{ $t('成交金额') }}</p>
                                        <el-tooltip placement="bottom-start" effect="light">
                                            <template #content> {{ $t('不计运费、税费和附加费') }} </template>
                                            <el-icon size="16" color="#c8c9cc" style="margin-left: 5px"><QuestionFilled /></el-icon>
                                        </el-tooltip>
                                    </div>
                                </template>
                                <template #default="{ row }">
                                    {{ priceFormat(row.userOrderItem?.totalProductMoney) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('分销员')" :width="140">
                                <template #default="{ row }">
                                    {{ row.salesman?.baseUserInfo?.nickname || row.salesman?.baseUserInfo?.username }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('客户')" :width="140">
                                <template #default="{ row }">
                                    {{ row.orderUserInfo?.user?.nickname || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('收益类型')" :width="140">
                                <template #default="{ row }"> {{ $t('商品佣金') }} </template>
                            </el-table-column>
                            <el-table-column :label="$t('收益组成')" :width="240">
                                <template #default="{ row }">
                                    {{ profitComposition(row.salesmanProductData, row.profitComposition) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('所属分组')" :width="240">
                                <template #default="{ row }">
                                    {{ row.salesman?.groupInfo?.groupName || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('收益')" :width="240">
                                <template #default="{ row }">
                                    {{ priceFormat(row.amount) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('结算方式')" :width="140">
                                <template #default="{ row }">
                                    <span v-if="row.salesmanSettlementData && row.salesmanSettlementData !== null">{{ row.salesmanSettlementData?.settlementType == 1 ? $t('系统结算') : $t('人工结算') }}</span>
                                    <span v-else>--</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('结算状态')" :width="140" fixed="right">
                                <template #default="{ row }">
                                    {{ row.statusText }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('退款时间')" :width="150" fixed="right">
                                <template #default="{ row }">
                                    {{ row.refundTime || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" :width="120" fixed="right">
                                <template #default="{ row }">
                                    <el-button v-if="canSettle(row)" link type="primary" @click="handleSettle(row)">{{ $t('人工结算') }}</el-button>
                                    <span v-else>--</span>
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
                <div v-if="selectedSettleRows.length > 0" class="selected-action-warp selected-warp-left">
                    <div class="selected-action">
                        <el-space>
                            <span
                                >{{ $t('已选择：') }}<b>{{ selectedSettleRows.length }}</b> {{ $t('项') }}</span
                            >
                            <el-button type="primary" @click="handleBatchSettle">{{ $t('批量人工结算') }}</el-button>
                        </el-space>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { SelectTimeInterval } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { batchSubmit } from "@/api/product/product";
import { priceFormat } from "@/utils/format";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import { QuestionFilled } from "@element-plus/icons-vue";
import type { SalesmanOrderFilterParams, SalesmanOrderFilterState } from "@/types/salesman/performanceSettlement.d";
import { getSalesmanOrderList, getSalesmanOrderExport, settlementSalesmanOrder } from "@/api/salesman/performanceSettlement";
import requestExport from "@/utils/export";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
import { useListRequest } from "@/hooks/useListRequest";
const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onBatchAction,
    resetParams
} = useListRequest<SalesmanOrderFilterState, SalesmanOrderFilterParams>({
    apiFunction: getSalesmanOrderList,
    idKey: "salesmanOrderId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        orderSn: "",
        orderTimeStart: "",
        orderTimeEnd: "",
        status: "",
        timeType: 1
    }
});
// 批量操作
const onBatchSubmit = async (action: string) => {
    await onBatchAction(action, batchSubmit);
};

// 仅「待结算」且结算方式为「人工结算」的记录可执行人工结算
const canSettle = (row: SalesmanOrderFilterState) => {
    return row.status === 0 && row.salesmanSettlementData?.settlementType === 2;
};

// 选中的可结算记录
const selectedSettleRows = ref<SalesmanOrderFilterState[]>([]);
const handleSelectionChange = (rows: SalesmanOrderFilterState[]) => {
    selectedSettleRows.value = rows.filter(canSettle);
};

// 执行结算
const doSettle = async (ids: number[]) => {
    try {
        await settlementSalesmanOrder({ ids });
        message.success(t('结算成功'));
        selectedSettleRows.value = [];
        loadFilter();
    } catch (error: any) {
        message.error(error.message || t('结算失败'));
    }
};

// 单条人工结算
const handleSettle = (row: SalesmanOrderFilterState) => {
    Modal.confirm({
        title: t('确认人工结算？'),
        content: t('结算后该记录将标记为「已结算」，仅统计业绩，不发放佣金金额（佣金按约定线下支付）。'),
        onOk: async () => {
            await doSettle([row.salesmanOrderId]);
        }
    });
};

// 批量人工结算
const handleBatchSettle = () => {
    const ids = selectedSettleRows.value.map((row) => row.salesmanOrderId);
    if (ids.length === 0) {
        message.warning(t('请选择待结算记录'));
        return;
    }
    Modal.confirm({
        title: t('确认批量人工结算？'),
        content: t('将对选中的 {length} 条待结算记录执行人工结算，仅统计业绩，不发放佣金金额。', { length: ids.length }),
        onOk: async () => {
            await doSettle(ids);
        }
    });
};

// 初始化加载
loadFilter();

const dialogVisible = ref<boolean>(false);
const exportParams = reactive<any>({
    range: 0,
    isExport: 1
});
const handleExportData = async () => {
    try {
        const result = await getSalesmanOrderExport({ ...filterParams, ...exportParams });
        requestExport(result, t('分销员业绩结算表'));
    } catch (error: any) {
        message.error(error.message);
    }
};
const dateType = ref(-1);
const changeDateType = (event: number) => {
    if (event === -1) {
        dateType.value = event;
        return;
    }
    filterParams.orderTimeStart = formattedDate(new Date(), "YYYY-MM-DD");
    if (event === 0) {
        //今天
        filterParams.orderTimeEnd = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 1) {
        //昨天
        filterParams.orderTimeStart = formattedDate(getDays(1, "sub"), "YYYY-MM-DD");
        filterParams.orderTimeEnd = formattedDate(getDays(1, "sub"), "YYYY-MM-DD");
    }
    if (event === 2) {
        //近七天
        filterParams.orderTimeStart = formattedDate(getDays(7, "sub"), "YYYY-MM-DD");
        filterParams.orderTimeEnd = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 3) {
        //近30天
        filterParams.orderTimeStart = formattedDate(getDays(30, "sub"), "YYYY-MM-DD");
        filterParams.orderTimeEnd = formattedDate(new Date(), "YYYY-MM-DD");
    }
    if (event === 4) {
        //近半年
        filterParams.orderTimeStart = formattedDate(getDays(180, "sub"), "YYYY-MM-DD");
        filterParams.orderTimeEnd = formattedDate(new Date(), "YYYY-MM-DD");
    }
    loadFilter();
};
const profitComposition = (salesmanProductData: any, profitComposition: string) => {
    let str = "--";
    let commissionType = 1;
    if (salesmanProductData.commissionData) {
        commissionType = salesmanProductData.commissionType;
        str = t('{commissionType3}{profitComposition}{get}（按实付金额计算）', { commissionType3: commissionType == 3 ? "" : t('比例'), profitComposition, get: commissionType == 3 ? config.get("amountUnit") : "%" });
    }
    return str;
};
</script>
<style lang="less" scoped>
.list-table-tool {
    margin-bottom: 0;
}
</style>

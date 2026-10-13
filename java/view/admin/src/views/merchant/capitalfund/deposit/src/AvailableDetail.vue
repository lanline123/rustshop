/** * @file 店铺端 可用明细/冻结明细抽屉 * @description 顶部三卡（公式）+ 冻结明细列表（明细与冻结明细同一弹窗） */
<template>
    <a-spin class="available-detail" :spinning="overviewLoading">
        <OverviewCards :overview="overview" />
        <div class="section-title">{{ $t('冻结明细') }}</div>
        <div class="lyecs-table-list-warp">
            <div class="list-table-tool lyecs-search-warp">
                <el-form :model="filterParams">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('时间：') }}</span></label>
                                    <SelectTimeInterval
                                        type="date"
                                        v-model:startDate="startDate"
                                        v-model:endDate="endDate"
                                        clearable
                                        @change="quickRange = ''"
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
                                    <el-button type="primary" plain @click="handleSearchSubmit">{{ $t('搜索') }}</el-button>
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
                        <!-- <el-table-column label="业务编号" min-width="120">
                            <template #default="{ row }">{{ row.orderId || row.relatedId || "--" }}</template>
                        </el-table-column> -->
                        <el-table-column :label="$t('原因')" min-width="120">
                            <template #default="{ row }">{{ row.reason || "--" }}</template>
                        </el-table-column>
                        <el-table-column :label="$t('金额/币种')" min-width="120">
                            <template #default="{ row }">{{ priceFormat(row.deductAmount) }}</template>
                        </el-table-column>
                        <el-table-column :label="$t('资金类型')" min-width="100">
                            <template #default="{ row }">{{ row.typeText || "--" }}</template>
                        </el-table-column>
                        <el-table-column :label="$t('时间')" prop="addTimeText" min-width="160" />
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
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import dayjs from "dayjs";
import { Pagination } from "@/components/list";
import { SelectTimeInterval } from "@/components/select";
import OverviewCards from "./OverviewCards.vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { getShopDepositFrozenList, getShopDepositOverview } from "@/api/merchant/capitalfund/deposit";
import type { ShopDepositFrozenFilterParams, ShopDepositFrozenState, ShopDepositOverview } from "@/types/merchant/capitalfund/deposit.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config = useConfigStore();
defineProps({ isDialog: Boolean });

const overviewLoading = ref<boolean>(true);
const overview = ref<ShopDepositOverview>({ required: 0, paid: 0, pending: 0, withdrawable: 0, frozen: 0 });

const quickRange = ref<string>("");
const startDate = ref<string>("");
const endDate = ref<string>("");
const quickOptions = [
    { value: "yesterday", label: t('昨天') },
    { value: "month", label: t('当月') },
    { value: "lastMonth", label: t('上个月') }
];

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<ShopDepositFrozenState, ShopDepositFrozenFilterParams>({
    apiFunction: getShopDepositFrozenList,
    idKey: "shopDepositLogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        startTime: undefined,
        endTime: undefined
    }
});

const syncTimeParams = () => {
    filterParams.startTime = startDate.value ? dayjs(startDate.value).startOf("day").unix() : undefined;
    filterParams.endTime = endDate.value ? dayjs(endDate.value).endOf("day").unix() : undefined;
};

const loadOverview = async () => {
    overviewLoading.value = true;
    try {
        const result = await getShopDepositOverview();
        overview.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        overviewLoading.value = false;
    }
};

const handleSearchSubmit = () => {
    syncTimeParams();
    onSearchSubmit();
};

const onQuickRange = (value: string) => {
    let start: dayjs.Dayjs;
    let end: dayjs.Dayjs;
    switch (value) {
        case "yesterday":
            start = dayjs().subtract(1, "day").startOf("day");
            end = dayjs().subtract(1, "day").endOf("day");
            break;
        case "month":
            start = dayjs().startOf("month");
            end = dayjs().endOf("month");
            break;
        case "lastMonth":
            start = dayjs().subtract(1, "month").startOf("month");
            end = dayjs().subtract(1, "month").endOf("month");
            break;
        default:
            return;
    }
    startDate.value = start.format("YYYY-MM-DD");
    endDate.value = end.format("YYYY-MM-DD");
    handleSearchSubmit();
};

const onReset = () => {
    quickRange.value = "";
    startDate.value = "";
    endDate.value = "";
    resetParams();
};

onMounted(() => {
    loadOverview();
    loadFilter();
});
</script>

<style lang="less" scoped>
.available-detail {
    padding: 0 6px;
}
.section-title {
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}
</style>

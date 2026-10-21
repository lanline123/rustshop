/** * @file 店铺端 店铺余额明细抽屉 */
<template>
    <a-spin class="available-detail" :spinning="overviewLoading">
        <div class="overview-cards">
            <div class="card" :class="{ highlight: selectedKey === 'all' }" @click="onSelectFilter('all')">
                <div class="card-label">{{ $t("可用店铺余额") }}</div>
                <div class="card-num">
                    {{ priceFormat(overview.available, false) }}<span class="unit">{{ $t("元") }}</span>
                </div>
            </div>
            <template v-for="(channel, index) in onlineChannels" :key="channel.code">
                <div class="operator">{{ index === 0 ? "=" : "+" }}</div>
                <div class="card" :class="{ highlight: selectedKey === channel.code }" @click="onSelectFilter(channel.code)">
                    <div class="card-label">{{ $t(channel.labelKey) }}</div>
                    <div class="card-num">
                        {{ priceFormat(channel.amount, false) }}<span class="unit">{{ $t("元") }}</span>
                    </div>
                </div>
            </template>
            <div class="operator">{{ onlineChannels.length ? "+" : "=" }}</div>
            <div class="card" :class="{ highlight: selectedKey === 'offline' }" @click="onSelectFilter('offline')">
                <div class="card-label">{{ $t("线下分账余额") }}</div>
                <div class="card-num">
                    {{ priceFormat(overview.offline, false) }}<span class="unit">{{ $t("元") }}</span>
                </div>
            </div>
        </div>
        <div class="section-title">{{ $t("余额明细") }}</div>
        <div class="lyecs-table-list-warp">
            <div class="list-table-tool lyecs-search-warp">
                <el-form :model="filterParams">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"
                                        ><span>{{ $t("时间：") }}</span></label
                                    >
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
                                    <el-button type="primary" plain @click="handleSearchSubmit">{{ $t("搜索") }}</el-button>
                                    <el-button plain @click="onReset">{{ $t("重置") }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </el-form>
            </div>
            <div class="table-container">
                <a-spin :spinning="loading">
                    <el-table :data="filterState" row-key="shopAccountLogId">
                        <el-table-column :label="$t('账户变动时间')" width="176">
                            <template #default="{ row }">
                                {{ row.addTime || "--" }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('{shopTypeName}名称', { shopTypeName })" min-width="180" show-overflow-tooltip>
                            <template #default="{ row }">
                                {{ row.shopTitle || "--" }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('分账类型')" width="130">
                            <template #default="{ row }">
                                {{ $t(formatAccountSplitType(row)) }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('变动资金账户')" min-width="280">
                            <template #default="{ row }">
                                <span :style="{ color: Number(row.changeAmount) >= 0 ? '#0000ff' : '#ff0000' }">{{ Number(row.changeAmount) >= 0 ? '+' : '' }}{{ Number(row.changeAmount).toFixed(2) }}</span>
                                <span v-if="row.newShopMoney !== null && row.newShopMoney !== undefined" style="color: #999; font-size: 12px">{{
                                    $t("（变更后金额:{newShopMoney}）", { newShopMoney: row.newShopMoney })
                                }}</span>
                                <span v-else style="color: #999; font-size: 12px">（—）</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('冻结资金账户')" min-width="220">
                            <template #default="{ row }">
                                <span v-if="row.newFrozenMoney - row.frozenMoney > 0" style="color: #0000ff"
                                    >+{{ (row.newFrozenMoney - row.frozenMoney).toFixed(2) }}</span
                                >
                                <span v-else-if="row.newFrozenMoney - row.frozenMoney == 0">0</span>
                                <span v-else style="color: #ff0000">-{{ (row.frozenMoney - row.newFrozenMoney).toFixed(2) }}</span>
                                <span v-if="row.newFrozenMoney" style="color: #999; font-size: 12px">{{
                                    $t("（变更后金额:{newFrozenMoney}）", { newFrozenMoney: row.newFrozenMoney })
                                }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('变动原因')" min-width="140" show-overflow-tooltip>
                            <template #default="{ row }">
                                <span class="ellipsis">{{ row.changeReasons }}</span>
                            </template>
                        </el-table-column>
                        <template #empty>
                            <div class="empty-warp">
                                <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                            </div>
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
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { priceFormat } from "@/utils/format";
import { getShopAccount } from "@/api/merchant/capitalfund/dashboard";
import { getDualBalance } from "@/api/merchant/capitalfund/withdraw";
import { getShopAccountLog } from "@/api/shop/shop";
import {
    formatAccountSplitType,
    resolveBalanceLogFilter,
    resolveShopBalances,
    type BalanceFilterKey,
    type OnlineChannelBalance
} from "./resolveShopBalances";
import { getShopType } from "@/utils/storage";
import { isStore } from "@/utils/version";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const shopTypeName = isStore() ? t("门店") : t("店铺");
const config = useConfigStore();
defineProps({ isDialog: Boolean });

const overviewLoading = ref<boolean>(true);
const overview = ref({ available: 0, offline: 0 });
const onlineChannels = ref<OnlineChannelBalance[]>([]);
const selectedKey = ref<BalanceFilterKey>("all");

const quickRange = ref<string>("");
const startDate = ref<string>("");
const endDate = ref<string>("");
const quickOptions = [
    { value: "yesterday", label: t("昨天") },
    { value: "month", label: t("当月") },
    { value: "lastMonth", label: t("上个月") }
];

const loadOverview = async () => {
    overviewLoading.value = true;
    try {
        const account = await getShopAccount();
        let dual: Awaited<ReturnType<typeof getDualBalance>> | undefined;
        try {
            dual = await getDualBalance();
        } catch {
            dual = undefined;
        }
        const parts = resolveShopBalances(account, dual);
        overview.value = {
            offline: parts.offline,
            available: parts.available
        };
        onlineChannels.value = parts.onlineChannels;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        overviewLoading.value = false;
    }
};

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    resetParams
} = useListRequest<any, any>({
    apiFunction: getShopAccountLog,
    idKey: "shopAccountLogId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        keyword: "",
        startTime: "",
        endTime: "",
        shopType: getShopType() === 1 ? 1 : 2,
        balanceType: "all",
        payChannel: ""
    }
});

const syncTimeParams = () => {
    filterParams.startTime = startDate.value || "";
    filterParams.endTime = endDate.value || "";
};

const onSelectFilter = (key: BalanceFilterKey) => {
    selectedKey.value = key;
    const nextFilter = resolveBalanceLogFilter(key);
    filterParams.balanceType = nextFilter.balanceType;
    filterParams.payChannel = nextFilter.payChannel;
    filterParams.page = 1;
    syncTimeParams();
    loadFilter();
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
    selectedKey.value = "all";
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
.overview-cards {
    display: flex;
    align-items: stretch;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 24px;
    .card {
        flex: 1 1 160px;
        min-width: 160px;
        background-color: #f7f8fb;
        border: 1px solid transparent;
        border-radius: 8px;
        padding: 20px;
        text-align: left;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: center;
        cursor: pointer;
        &.highlight {
            background-color: #3d80ff;
            .card-label {
                color: rgba(255, 255, 255, 0.85);
            }
            .card-num {
                color: #fff;
            }
        }
        .card-label {
            margin-bottom: 8px;
        }
        .card-num {
            font-size: 20px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
            .unit {
                margin-left: 4px;
                font-size: 12px;
                font-weight: normal;
                color: inherit;
            }
        }
    }
    .operator {
        font-size: 18px;
        font-weight: 500;
        user-select: none;
        align-self: center;
    }
}
.ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 90%;
}
</style>

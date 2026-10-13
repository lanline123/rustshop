<template>
    <div class="container">
        <div class="content_wrapper">
            <div>
                <el-tabs v-model="filterParams.statisticType" @tab-change="handleClick">
                    <el-tab-pane :label="$t('订单金额统计')" name="1"></el-tab-pane>
                    <el-tab-pane :label="$t('订单数统计')" name="0"></el-tab-pane>
                </el-tabs>

                <el-form :model="filterParams">
                    <div class="filtrate-menu">
                        <el-form-item class="mr-10">
                            <el-select v-model="filterParams.dateType" placeholder="Select" style="width: 94.5px" @change="handleSearch">
                                <el-option v-for="(item, index) in timeSorterList" :key="index" :label="item.label" :value="item.value" />
                            </el-select>
                        </el-form-item>
                        <el-form-item class="mr-10">
                            <el-date-picker
                                v-if="filterParams.dateType === '2'"
                                style="width: 200px"
                                :shortcuts="shortcuts"
                                v-model="selectDate['2']"
                                value-format="YYYY-MM"
                                type="month"
                                :editable="false"
                            />
                            <el-date-picker
                                v-if="filterParams.dateType === '1'"
                                v-model="selectDate['1']"
                                type="year"
                                value-format="YYYY"
                                style="width: 200px"
                                :editable="false"
                                :shortcuts="yearShortcuts"
                            />
                        </el-form-item>
                        <el-form-item class="mr-10">
                            <el-button plain @click="handleSearch">{{ $t('搜索') }}</el-button>
                        </el-form-item>
                        <el-form-item>
                            <el-button plain @click="handleExport" :loading="Exportloading">{{ $t('导出EXCEL') }}</el-button>
                        </el-form-item>
                    </div>
                </el-form>

                <div class="main-panel">
                    <ul :class="{ three: adminType !== 'admin' }">
                        <template v-if="filterParams.statisticType == '1'">
                            <li class="main-panel-item">
                                <div class="main-panel-item-title">
                                    <div class="flex flex-align-center"><div>{{ $t('商品支付金额') }}</div>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，订单合计金额减免掉优惠券、满减/满折、会员折扣后成功支付的金额，再扣除掉积分余额，后实际支付的金额（自提以支付成功后计入统计；不含充值订单）') }} </div>
                                        </template>
                                        <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    </div>
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.productPayment ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.productPaymentGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.productPaymentGrowthRate != null &&
                                            filterState?.salesData?.productPaymentGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.productPaymentGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item">
                                <div class="main-panel-item-title">
                                    <div class="flex flex-align-center"><div>{{ $t('商品退款金额') }}</div>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，平台/店铺售后金额（不含充值订单）') }} </div>
                                        </template>
                                        <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    </div>
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.productRefund ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.productRefundGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.productRefundGrowthRate != null &&
                                            filterState?.salesData?.productRefundGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.productRefundGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item">
                                <div class="main-panel-item-title">
                                    <div class="flex flex-align-center"><div>{{ $t('营业额') }}</div>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，平台/店铺销售的营业额，等于实收金额减去售后金额（不含充值订单）') }} </div>
                                        </template>
                                        <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    </div>
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.turnover ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.turnoverGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.turnoverGrowthRate != null &&
                                            filterState?.salesData?.turnoverGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.turnoverGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>

                            <li class="main-panel-item" v-if="adminType === 'admin'">
                                <div class="main-panel-item-title">
                                    <div class="flex flex-align-center"><div>{{ $t('充值金额') }}</div>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，平台/店铺充值订单的金额') }} </div>
                                        </template>
                                        <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    </div>
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.rechargeAmount ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.rechargeAmountGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.rechargeAmountGrowthRate != null &&
                                            filterState?.salesData?.rechargeAmountGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.rechargeAmountGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item" v-if="adminType === 'admin'">
                                <div class="main-panel-item-title">
                                    <div class="flex flex-align-center"><div>{{ $t('余额支付金额') }}</div>
                                    <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                        <template #content>
                                            <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px"> {{ $t('根据筛选时间，平台/店铺使用余额支付的金额（不含充值订单）') }} </div>
                                        </template>
                                        <el-icon class="tooltip-icon" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </el-tooltip>
                                    </div>
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.balancePayment ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.balancePaymentGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.balancePaymentGrowthRate != null &&
                                            filterState?.salesData?.balancePaymentGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.balancePaymentGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                        </template>
                        <template v-if="filterParams.statisticType == '0'">
                            <li class="main-panel-item">
                                <div class="main-panel-item-title"> {{ $t('支付订单数') }} <!-- <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                    <template #content>
                                        <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">
                                            根据筛选时间，订单合计金额减免掉优惠券、满减/满折、会员折扣后成功支付的金额，再扣除掉积分余额，后实际支付的金额（自提以支付成功后计入统计；不含充值订单）
                                        </div>
                                    </template>
                                    <div class="flex flex-align-center">
                                        <div>支付订单数</div>
                                        <el-icon style="margin-left: 5px" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </div>
                                </el-tooltip> -->
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.productPaymentNum ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.productPaymentNumGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.productPaymentNumGrowthRate != null &&
                                            filterState?.salesData?.productPaymentNumGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.productPaymentNumGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item">
                                <div class="main-panel-item-title"> {{ $t('退款订单数量') }} <!-- <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                    <template #content>
                                        <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">
                                            根据筛选时间，订单合计金额减免掉优惠券、满减/满折、会员折扣后成功支付的金额，再扣除掉积分余额，后实际支付的金额（自提以支付成功后计入统计；不含充值订单）
                                        </div>
                                    </template>
                                    <div class="flex flex-align-center">
                                        <div>退款订单数量</div>
                                        <el-icon style="margin-left: 5px" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </div>
                                </el-tooltip> -->
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.productRefundNum ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.rechargeNumGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.rechargeNumGrowthRate != null &&
                                            filterState?.salesData?.rechargeNumGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.rechargeNumGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item">
                                <div class="main-panel-item-title"> {{ $t('充值订单数') }} <!-- <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                    <template #content>
                                        <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">
                                            根据筛选时间，订单合计金额减免掉优惠券、满减/满折、会员折扣后成功支付的金额，再扣除掉积分余额，后实际支付的金额（自提以支付成功后计入统计；不含充值订单）
                                        </div>
                                    </template>
                                    <div class="flex flex-align-center">
                                        <div>充值订单数</div>
                                        <el-icon style="margin-left: 5px" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </div>
                                </el-tooltip> -->
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.rechargeNum ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.productRefundNumGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.productRefundNumGrowthRate != null &&
                                            filterState?.salesData?.productRefundNumGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.productRefundNumGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                            <li class="main-panel-item">
                                <div class="main-panel-item-title"> {{ $t('余额支付订单数') }} <!-- <el-tooltip class="box-item" effect="light" placement="top" show-after="300" trigger="hover">
                                    <template #content>
                                        <div style="width: 300px; padding: 10px; line-height: 24px; font-size: 14px">
                                            根据筛选时间，订单合计金额减免掉优惠券、满减/满折、会员折扣后成功支付的金额，再扣除掉积分余额，后实际支付的金额（自提以支付成功后计入统计；不含充值订单）
                                        </div>
                                    </template>
                                    <div class="flex flex-align-center">
                                        <div>余额支付订单数</div>
                                        <el-icon style="margin-left: 5px" size="14" color="#969799"><QuestionFilled /></el-icon>
                                    </div>
                                </el-tooltip> -->
                                </div>
                                <div class="main-panel-item-value">{{ filterState?.salesData?.balancePaymentNum ?? 0 }}</div>
                                <div class="main-panel-item-increase"> {{ $t('环比：') }} <span>{{ filterState?.salesData?.balancePaymentNumGrowthRate }}%</span>
                                    <i
                                        v-if="
                                            filterState?.salesData?.balancePaymentNumGrowthRate != null &&
                                            filterState?.salesData?.balancePaymentNumGrowthRate.toString() !== '--' &&
                                            Number(filterState?.salesData?.balancePaymentNumGrowthRate) > 0
                                        "
                                        class="admin-iconfont red f12 up"
                                        >&#xe61a;</i
                                    >
                                    <i v-else class="admin-iconfont green f12">&#xe61a;</i>
                                </div>
                            </li>
                        </template>
                    </ul>
                </div>
            </div>

            <div class="chart">
                <OrderamountStatistics v-if="filterParams.statisticType === '1'"></OrderamountStatistics>
                <OrderNumberStatistics v-if="filterParams.statisticType === '0'"></OrderNumberStatistics>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { QuestionFilled } from "@element-plus/icons-vue";
import { ref, onMounted, reactive, provide } from "vue";
import type { TabsPaneContext } from "element-plus";
import { formatDate } from "@/utils/util";
import OrderamountStatistics from "./src/OrderamountStatistics.vue";
import OrderNumberStatistics from "./src/OrderNumberStatistics.vue";
import type { statisticsOrdeFilterState, statisticsOrdeFilterParams } from "@/types/panel/statisticsOrde";
import { getSalesstatisticsIndexs, getStatisticsOrdexport } from "@/api/panel/statisticsOrde";
import { message } from "ant-design-vue";
import requestExport from "@/utils/export";
import { getAdminType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = getAdminType();

const handleClick = (tab: TabsPaneContext, event: Event) => {
    getData();
};

const filterParams = reactive<statisticsOrdeFilterParams>({
    statisticType: "1",
    startEndTime: "",
    dateType: "1"
});

const selectDate = reactive<any>({
    "1": "",
    "2": ""
});

const timeSorterList = [
    {
        label: t('按年筛选'),
        value: "1"
    },
    {
        label: t('按月筛选'),
        value: "2"
    }
];

const shortcuts = [
    {
        text: t('本月'),
        value: new Date()
    },
    {
        text: t('上月'),
        value: () => {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 30);
            return date;
        }
    },
    {
        text: t('本年一月'),
        value: () => {
            const date = new Date();
            const firstMonthOfYear = new Date(date.getFullYear(), 0, 1);
            return firstMonthOfYear;
        }
    }
];

const yearShortcuts = [
    {
        text: t('本年'),
        value: () => {
            return getDateYearsAgo(0);
        }
    },
    {
        text: t('去年'),
        value: () => {
            return getDateYearsAgo(1);
        }
    },
    {
        text: t('三年前'),
        value: () => {
            return getDateYearsAgo(3);
        }
    },
    {
        text: t('五年前'),
        value: () => {
            return getDateYearsAgo(5);
        }
    }
];

const getDateYearsAgo = (years: number) => {
    const date = new Date();
    const dateYearsAgo = new Date(date.getFullYear() - years, date.getMonth(), date.getDate());
    return dateYearsAgo;
};

const filterState = ref<statisticsOrdeFilterState>();
provide("filterState", filterState);
const getData = async () => {
    try {
        filterParams.startEndTime = selectDate[filterParams.dateType];
        const result = await getSalesstatisticsIndexs(filterParams);
        filterState.value = result;
        const { salesStatisticsData } = result;
        const formatUnit = filterParams.dateType === "1" ? t('月') : t('日');
        filterState.value.salesStatisticsData.horizontalAxis = salesStatisticsData.horizontalAxis.map((item) => item + formatUnit);
    } catch (error: any) {
        message.error(error.message);
        console.error(error);
    }
};

const handleSearch = () => {
    getData();
};

const Exportloading = ref<boolean>(false);
const handleExport = async () => {
    Exportloading.value = true;
    try {
        const result = await getStatisticsOrdexport({ ...filterParams, isExport: "1" });
        Exportloading.value = false;
        requestExport(result, t('销售统计导出'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        Exportloading.value = false;
    }
};
onMounted(() => {
    const nowDate = formatDate(new Date());
    if (nowDate) {
        const dates = nowDate.split("-");
        for (const key in selectDate) {
            if (key === "1") selectDate[key] = dates[0];
            if (key === "2") selectDate[key] = dates[0] + "-" + dates[1];
        }
    }
    getData();
});
</script>

<style lang="less" scoped>
.filtrate-menu {
    display: flex;
    // margin-bottom: 20px;

    .mr-10 {
        margin-right: 5px;
    }
}

.main-panel {
    ul {
        display: grid;
        grid-template-columns: 1fr 1fr 1fr 1fr;
        grid-gap: 17px 28px;
        &.three {
            grid-template-columns: 1fr 1fr 1fr;
        }
    }

    .main-panel-item {
        padding: 15px;
        border-radius: 10px;
        background-color: #f7f8fa;

        .main-panel-item-title {
            font-size: 14px;
            color: #777;

            .box-item {
                display: inline-flex;
                width: fit-content;
            }

            .tooltip-icon {
                margin-left: 5px;
            }
        }

        .main-panel-item-value {
            font-size: 24px;
            color: #333;
            display: block;
            font-weight: bold;
            padding: 10px 0;
        }

        .main-panel-item-increase {
            font-size: 14px;
            color: #777;

            i {
                font-size: 16px;
            }

            .up {
                display: inline-block;
                transform: rotate(180deg);
            }
        }
    }
}

.chart {
    margin-top: 25px;
    height: 400px;
    width: 100%;
}
</style>

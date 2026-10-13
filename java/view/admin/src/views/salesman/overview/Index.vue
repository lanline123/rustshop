<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="tit-row flex flex-align-center">
                <h2>{{ $t('核心数据汇总') }}</h2>
            </div>
            <div class="summary-row">
                <div class="summary-radio flex flex-align-center">
                    <el-radio-group v-model="filterParams.summaryType" size="default" @change="radioChange">
                        <el-radio-button :label="$t('今日')" :value="0" />
                        <el-radio-button :label="$t('累计')" :value="1" />
                    </el-radio-group>
                    <!-- <div class="time" v-if="filterParams.summaryType === 0">更新时间：{{ newTime }}</div> -->
                    <div class="time" @click="refresh">
                        <div>{{ $t('更新时间：{newTime}', { newTime }) }}</div>
                        <i class="iconfont-admin icon-shuaxin1" :class="{ 'rotate-animation': isRotating }"></i>
                    </div>
                </div>
                <div class="summary-list" v-if="filterParams.summaryType === 0">
                    <div class="summary-item" v-if="adminType === 'admin'">
                        <div class="tit">{{ $t('今日新增分销员数') }}</div>
                        <div class="num">{{ filterState.newSalesmanCount }}</div>
                    </div>
                    <div class="summary-item" v-if="adminType === 'admin'">
                        <div class="tit">{{ $t('今日分销员销售额') }}（{{ $t(config.config.dollarSignCn) }}）</div>
                        <div class="num">{{ filterState.salesmanAmount > 0 ? priceFormat(filterState.salesmanAmount) : "-" }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="tit">{{ $t('今日成交客户数') }}</div>
                        <div class="num">{{ filterState.customNum }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="tit">{{ $t('今日支出佣金') }}（{{ $t(config.config.dollarSignCn) }}）</div>
                        <div class="num">{{ filterState.salesmanCommission > 0 ? priceFormat(filterState.salesmanCommission) : "-" }}</div>
                    </div>
                </div>
                <div class="summary-list" v-if="filterParams.summaryType === 1">
                    <div class="summary-item" v-if="adminType === 'admin'">
                        <div class="tit">{{ $t('分销员数') }}</div>
                        <div class="num">{{ filterState.newSalesmanCount }}</div>
                    </div>
                    <div class="summary-item" v-if="adminType === 'admin'">
                        <div class="tit">{{ $t('分销员销售额') }}（{{ $t(config.config.dollarSignCn) }}）</div>
                        <div class="num">{{ filterState.salesmanAmount > 0 ? priceFormat(filterState.salesmanAmount) : "-" }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="tit">{{ $t('成交客户数') }}</div>
                        <div class="num">{{ filterState.customNum }}</div>
                    </div>
                    <div class="summary-item">
                        <div class="tit">{{ $t('支出佣金') }}（{{ $t(config.config.dollarSignCn) }}）</div>
                        <div class="num">{{ filterState.salesmanCommission > 0 ? priceFormat(filterState.salesmanCommission) : "-" }}</div>
                    </div>
                </div>
            </div>
            <div class="tit-row flex flex-align-center">
                <h2>{{ $t('核心指标趋势') }}</h2>
            </div>
            <TrendChart></TrendChart>
            <div class="tit-row flex flex-align-center" v-if="adminType === 'admin'">
                <h2>{{ $t('分销员排行') }}</h2>
            </div>
            <RankingTable class="padding-no"></RankingTable>
            <div class="tit-row flex flex-align-center">
                <h2>{{ $t('商品成交分析') }}</h2>
            </div>
            <AnalysisTable class="padding-no"></AnalysisTable>
        </div>
    </div>
</template>
<script setup lang="ts">
import "@/style/css/list.less";
import dayjs from "dayjs";
import { ref, reactive, onMounted } from "vue";
import { message } from "ant-design-vue";
import RankingTable from "./src/RankingTable.vue";
import AnalysisTable from "./src/AnalysisTable.vue";
import TrendChart from "./src/TrendChart.vue";
import { priceFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
import { CoreSummaryFilterParams, CoreSummaryFormState } from "@/types/salesman/overview.d";
import { getOverviewCoreSummary } from "@/api/salesman/overview";
import { getAdminType } from "@/utils/storage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const config: any = useConfigStore();
const adminType = getAdminType();
const filterParams = reactive<CoreSummaryFilterParams>({
    summaryType: 0
});
const filterState = ref<CoreSummaryFormState>({
    newSalesmanCount: 0,
    salesmanAmount: 0,
    customNum: 0,
    salesmanCommission: 0
});
const loading = ref<boolean>(true);
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getOverviewCoreSummary({ ...filterParams });
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const newTime = ref<string>(dayjs().format("YYYY-MM-DD HH:mm:ss"));
const isRotating = ref<boolean>(false);
const refresh = () => {
    isRotating.value = true;
    newTime.value = dayjs().format("YYYY-MM-DD HH:mm:ss");
    loadFilter();
    setTimeout(() => {
        isRotating.value = false;
        message.success(t('刷新成功'));
    }, 500); // 500ms 是动画的持续时间，需要与 CSS 中的动画时间一致
};
const radioChange = (value: number) => {
    filterParams.summaryType = value;
    if (value === 0) {
        newTime.value = dayjs().format("YYYY-MM-DD HH:mm:ss");
    }
    loadFilter();
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.container {
    .content_wrapper {
        .tit-row {
            margin-bottom: 20px;
            h2 {
                margin-right: 10px;
            }
        }
        .summary-row {
            .summary-radio {
                margin-bottom: 20px;
                :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
                    color: var(--tig-primary) !important;
                    background-color: var(--tig-item-active-bg) !important;
                }
                :deep(.el-radio-button__inner) {
                    min-width: 80px;
                }
                .time {
                    display: flex;
                    align-items: center;
                    font-size: 14px;
                    font-weight: 400;
                    color: #969799 !important;
                    margin-left: 10px !important;
                    cursor: pointer;
                    i {
                        font-size: 14px;
                        margin-left: 10px;
                        margin-top: 2px;
                    }
                }
            }
            .summary-list {
                margin-top: 16px;
                display: grid;
                grid-template-columns: 24% 24% 24% 24%;
                grid-template-rows: 1fr;
                margin-bottom: 20px;
                .summary-item {
                    margin-bottom: 32px;
                    .tit {
                        color: #646566;
                        font-size: 14px;
                        margin-bottom: 12px;
                    }
                    .num {
                        font-size: 24px;
                        color: #323233;
                        font-weight: 700;
                    }
                }
            }
        }
    }
}
.padding-no {
    :deep(.content_wrapper) {
        padding: 0 !important;
    }
}
.rotate-animation {
    animation: rotate 0.5s linear;
}
@media only screen and (max-width: 767px) {
    .summary-item {
        .tit {
            line-height: 20px;
            height: 35px;
        }
    }
}
</style>

<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <a-spin :spinning="loading">
                    <div class="card-warp flex">
                        <div class="card-box">
                            <div class="overview">
                                <div class="tit-box" @click="displayAmount = !displayAmount"> {{ $t('账户概览') }} <el-icon v-if="displayAmount">
                                        <View />
                                    </el-icon>
                                    <el-icon v-else>
                                        <Hide />
                                    </el-icon>
                                </div>
                                <div class="money-tit">
                                    {{ $t('可用') }}{{ getShopType() === 2 ? "门店" : "店铺" }}{{ $t('余额') }}({{ config.config.dollarSignCn }})
                                    <el-tooltip placement="right" effect="light">
                                        <template #content>
                                            <div class="tooltip-width">{{ $t('订单结算完成的入账金额，可用于商家日常经营收支、核心经营场景') }}</div>
                                        </template>
                                        <el-icon>
                                            <QuestionFilled />
                                        </el-icon>
                                    </el-tooltip>
                                </div>
                                <div class="money-box-a">
                                    <span class="money-num" v-if="displayAmount">{{ priceFormat(overviewAvailableBalance) || 0.0 }}</span>
                                    <span class="money-num" v-else>******</span>
                                    <DialogForm
                                        isDrawer
                                        :title="$t('可用店铺余额')"
                                        width="1200px"
                                        path="merchant/capitalfund/dashboard/src/BalanceDetail"
                                        :showOnOk="false"
                                    >
                                        <el-button type="primary" link class="detail-link">{{ $t("明细") }}</el-button>
                                    </DialogForm>
                                </div>
                                <div class="btn">
                                    <el-button type="primary" @click="handleWithdraw">{{ $t('提现') }}</el-button>
                                </div>
                            </div>
                            <el-divider />
                            <div class="flex flex-wrap">
                                <div class="overview h150">
                                    <div class="money-tit">
                                        {{ $t('待结算金额') }}({{ config.config.dollarSignCn }})
                                        <el-tooltip placement="right" effect="light">
                                            <template #content>
                                                <div class="tooltip-width">{{ $t('交易未完成的订单总额，消费者确认收货后将自动转入店铺余额') }}</div>
                                            </template>
                                            <el-icon>
                                                <QuestionFilled />
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                    <div class="money-box-b">
                                        <span v-if="displayAmount">{{ priceFormat(accountData.unSettlementMoney) || 0.0 }}</span>
                                        <span v-else>******</span>
                                        <!-- <el-icon><ArrowRightBold /></el-icon> -->
                                    </div>
                                </div>
                                <div class="overview h150">
                                    <div class="money-tit">
                                        {{ $t('冻结金额') }}({{ config.config.dollarSignCn }})
                                        <el-tooltip placement="right" effect="light">
                                            <template #content>
                                                <div class="tooltip-width">{{ $t('提现中或者退款中的冻结金额') }}</div>
                                            </template>
                                            <el-icon>
                                                <QuestionFilled />
                                            </el-icon>
                                        </el-tooltip>
                                    </div>
                                    <div class="money-box-b">
                                        <span v-if="displayAmount">{{ priceFormat(accountData.frozenMoney) || 0.0 }}</span>
                                        <span v-else>******</span>
                                        <!-- <el-icon><ArrowRightBold /></el-icon> -->
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="card-box">
                            <div class="overview">
                                <div class="tit-box">{{ $t('账户信息') }}</div>
                                <div class="money-tit">{{ $t('用户名：{companyName}', { companyName: accountData.merchant?.companyName || accountData.merchant?.corporateName }) }}</div>
                                <div class="money-tit">{{ $t('银行卡：{cardCount}张', { cardCount: accountData.cardCount }) }}<router-link :to="{ path: '/account/list' }">
                                        <el-button type="primary" link>{{ $t('去添加') }}</el-button>
                                    </router-link>
                                </div>
                            </div>
                            <el-divider />
                            <!-- <div class="overview h150">
                                <div class="labels">
                                    <DialogForm
                                        :params="{ act: 'detail' }"
                                        :showClose="false"
                                        :showOnOk="false"
                                        title="店铺协议"
                                        isDrawer
                                        path="merchant/capitalfund/dashboard/Agreement"
                                        width="880px"
                                        @callback="loadFilter"
                                    >
                                        <div class="label">
                                            <div class="ico-box">
                                                <el-icon size="25"><Tickets /></el-icon>
                                            </div>
                                            <div>协议信息</div>
                                        </div>
                                    </DialogForm>
                                </div>
                            </div> -->
                        </div>
                    </div>
                </a-spin>
                <div class="table-container">
                    <div class="table-head flex flex-align-center flex-justify-between">
                        <div class="name flex flex-align-center">
                            <span>{{ $t('收支概况') }}</span>
                            <!-- <el-radio-group v-model="filterParams.time" size="small">
                                <el-radio-button label="今日" :value="1" />
                                <el-radio-button label="昨日" :value="2" />
                            </el-radio-group> -->
                        </div>
                        <div class="more">
                            <!-- <el-button type="primary" link>
                                查看更多
                                <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                            </el-button> -->
                        </div>
                    </div>
                    <a-spin :spinning="tableLoading">
                        <el-table :data="filterState" :total="total" row-key="logId">
                            <el-table-column :label="$t('变动时间')" prop="addTime" width="160"></el-table-column>
                            <el-table-column :label="$t('变动余额资金')">
                                <template #default="{ row }">
                                    <!-- <span v-if="row.type == 1" style="color: #0000ff">+{{ row.changeAmount }}</span>
                                    <span v-else-if="row.type == 2" style="color: #ff0000">-{{ row.changeAmount }}</span>
                                    <span v-else>{{ row.changeAmount }}</span>
                                    <span v-if="row.newShopMoney" style="color: #999; font-size: 12px">（变更后金额:{{ row.newShopMoney }}）</span> -->

                                    <span v-if="row.newShopMoney - row.changeAmount > 0" style="color: #0000ff"
                                    >+{{ (row.newShopMoney - row.changeAmount).toFixed(2) }}</span
                                    >
                                    <span v-else-if="row.newShopMoney - row.changeAmount == 0">0</span>
                                    <span v-else style="color: #ff0000">-{{ (row.changeAmount - row.newShopMoney).toFixed(2) }}</span>
                                    <span v-if="row.newShopMoney" style="color: #999; font-size: 12px">{{ $t('（变更后金额:{newShopMoney}）', { newShopMoney: row.newShopMoney }) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('变动冻结资金')">
                                <template #default="{ row }">
                                    <span v-if="row.newFrozenMoney - row.frozenMoney > 0" style="color: #0000ff"
                                        >+{{ (row.newFrozenMoney - row.frozenMoney).toFixed(2) }}</span
                                    >
                                    <span v-else-if="row.newFrozenMoney - row.frozenMoney == 0">0</span>
                                    <span v-else style="color: #ff0000">-{{ (row.frozenMoney - row.newFrozenMoney).toFixed(2) }}</span>
                                    <span v-if="row.newFrozenMoney" style="color: #999; font-size: 12px">{{ $t('（变更后金额:{newFrozenMoney}）', { newFrozenMoney: row.newFrozenMoney }) }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('变动原因')">
                                <template #default="{ row }">
                                    <span class="ellipsis">{{ row.changeReasons }}</span>
                                </template>
                            </el-table-column>
                            <!-- <el-table-column label="变动余额资金">
                                <template #default="{ row }">
                                    <span v-if="row.newShopMoney - row.shopMoney > 0" style="color: #0000ff"
                                        >+{{ row.newShopMoney - row.shopMoney }}</span
                                    >
                                    <span v-else style="color: #ff0000">-{{ row.shopMoney - row.newShopMoney }}</span>
                                    <span v-if="row.newShopMoney" style="color: #999; font-size: 12px">（变更后金额:{{ row.newShopMoney }}）</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="变动冻结资金">
                                <template #default="{ row }">
                                    <span v-if="row.newFrozenMoney - row.frozenMoney > 0" style="color: #0000ff"
                                        >+{{ row.newFrozenMoney - row.frozenMoney }}</span
                                    >
                                    <span v-else-if="row.newFrozenMoney - row.frozenMoney == 0">{{ row.frozenMoney }}</span>
                                    <span v-else style="color: #ff0000">-{{ row.frozenMoney - row.newFrozenMoney }}</span>
                                    <span v-if="row.newFrozenMoney" style="color: #999; font-size: 12px">（变更后金额:{{ row.newFrozenMoney }}）</span>
                                </template>
                            </el-table-column> -->
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
import { onMounted, reactive, ref, computed } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { getShopAccount, getShopAccountList } from "@/api/merchant/capitalfund/dashboard";
import { AccountFormState, AccountFilterState } from "@/types/merchant/capitalfund/dashboard.d";
import { priceFormat } from "@/utils/format";
import { DialogForm } from "@/components/dialog";
import { useRouter } from "vue-router";
import { View, Hide, QuestionFilled, ArrowRightBold, Tickets, ArrowRight } from "@element-plus/icons-vue";
import { getShopType } from "@/utils/storage";

// 路由
const router = useRouter();
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref<AccountFilterState[]>([]);
const loading = ref<boolean>(true);
const tableLoading = ref<boolean>(true);
const total = ref<number>(0);
const displayAmount = ref<boolean>(true);
const filterParams = reactive<any>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    time: 1
});
const accountData = ref<AccountFormState>({
    shopMoney: 0
});
const overviewAvailableBalance = computed(() =>
    accountData.value.availableBalance !== undefined && accountData.value.availableBalance !== null
        ? Number(accountData.value.availableBalance)
        : Number(accountData.value.shopMoney || 0)
);
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopAccount();
        accountData.value = result;
        const accountList = await getShopAccountList({ ...filterParams });
        filterState.value = accountList.records;
        total.value = accountList.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        tableLoading.value = false;
    }
};
const handleWithdraw = () => {
    router.push("/withdraw/index/");
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.tooltip-width {
    width: 250px;
}

.card-warp {
    justify-content: space-between;
    margin-bottom: 10px;

    .card-box {
        background-color: #fff;

        &:first-child {
            width: 70%;
            margin-right: 10px;
        }

        &:last-child {
            width: 30%;
        }

        :deep(.el-divider--horizontal) {
            margin: 20px 0 !important;
        }

        .overview {
            padding: 0 20px;
            height: 180px;
            position: relative;

            .tit-box {
                margin-top: 20px;
                font-size: 16px;
                line-height: 24px;
                font-weight: 500;
                margin-bottom: 20px;
                display: flex;
                align-items: center;

                :deep(.el-icon) {
                    margin-left: 8px;
                    color: #c8c9cc;
                }
            }

            .money-tit {
                font-size: 14px;
                color: #646566;
                line-height: 20px;
                margin-bottom: 10px;
                display: flex;
                align-items: center;

                :deep(.el-icon) {
                    margin-left: 8px;
                    margin-top: 1px;
                    color: #c8c9cc;
                }

                :deep(.el-button) {
                    margin-left: 8px;
                    margin-top: 1px;
                }
            }

            .money-box-a {
                font-size: 26px;
                line-height: 1;
                min-height: 36px;
                font-weight: 700;
                display: flex;
                align-items: flex-end;
                flex-wrap: wrap;

                .money-num {
                    line-height: 1;
                }

                :deep(.dialog-link) {
                    display: inline-flex;
                    align-items: flex-end;
                    margin-left: 8px;
                    line-height: 1;
                }

                .detail-link {
                    margin-left: 0;
                    font-size: 14px;
                    font-weight: normal;
                    height: auto;
                    padding: 0;
                    line-height: 1;
                }
            }

            .btn {
                position: absolute;
                bottom: 0px;
            }

            .money-box-b {
                font-size: 20px;
                line-height: 28px;
                height: 28px;
                font-weight: 700;
                display: flex;
                align-items: center;

                :deep(.el-icon) {
                    font-size: 14px;
                    margin-left: 8px;
                    margin-top: 1px;
                    color: #c8c9cc;
                }
            }

            .labels {
                .label {
                    width: 60px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;

                    .ico-box {
                        display: inline-block;
                        background-color: #6a92ff;
                        padding: 5px;
                        color: #fff;
                        border-radius: 5px;
                        margin-bottom: 5px;
                    }
                }
            }
        }

        .h150 {
            height: 150px;
            margin-right: 80px;
        }
    }
}

.table-container {
    background-color: #fff;
    padding: 20px;

    .table-head {
        padding-bottom: 20px;

        span {
            display: inline-block;
            margin-right: 10px;
            font-size: 16px;
        }
    }
}

.ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    width: 90%;
}
</style>

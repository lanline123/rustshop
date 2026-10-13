<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="tit-box">
                <div class="tit">
                    <span>{{ $t('{getShopType2}余额', { getShopType2: getShopType() === 2 ? "门店" : "店铺" }) }}</span>
                </div>
            </div>
            <div class="balance-box flex-wrap">
                <div class="label">
                    <div class="title">
                        {{ $t('可用') }}{{ getShopType() === 2 ? "门店" : "店铺" }}{{ $t('余额') }}({{ config.config.dollarSignCn }})
                        <el-tooltip placement="bottom" effect="light">
                            <template #content>
                                <div class="tooltip-width">{{ $t('可用{getShopType2}余额', { getShopType2: getShopType() === 2 ? "门店" : "店铺" }) }}</div>
                            </template>
                            <el-icon>
                                <QuestionFilled />
                            </el-icon>
                        </el-tooltip>
                        <!-- <router-link :to="{ path: '/capitalfund/account/list' }">
                            <el-button type="primary" link>充值记录</el-button>
                        </router-link>
                        <el-divider direction="vertical"/> -->
                        <router-link :to="{ path: '/withdraw/index', query: { type: 2 } }">
                            <el-button type="primary" link>{{ $t('提现记录') }}</el-button>
                        </router-link>
                    </div>
                    <div class="num">
                        {{ priceFormat(accountData.shopMoney) || 0.0 }}
                    </div>
                </div>
                <div class="label">
                    <div class="title">
                        {{ $t('待结算') }}({{ config.config.dollarSignCn }})
                        <el-tooltip placement="bottom" effect="light">
                            <template #content>
                                <div class="tooltip-width">{{ $t('交易未完成的订单总额，消费者确认收货后将自动转入店铺余额') }}</div>
                            </template>
                            <el-icon>
                                <QuestionFilled />
                            </el-icon>
                        </el-tooltip>
                        <el-button type="primary" link @click="onTabChange(99)">{{ $t('明细') }}</el-button>
                    </div>
                    <div class="num">
                        {{ priceFormat(accountData.unSettlementMoney) || 0.0 }}
                    </div>
                </div>
                <div class="label">
                    <div class="title">
                        {{ $t('不可用余额') }}({{ config.config.dollarSignCn }})
                        <el-tooltip placement="bottom" effect="light">
                            <template #content>
                                <div class="tooltip-width">{{ $t('提现中或者退款中的冻结店铺余额') }}</div>
                            </template>
                            <el-icon>
                                <QuestionFilled />
                            </el-icon>
                        </el-tooltip>
                    </div>
                    <div class="num">
                        {{ priceFormat(accountData.frozenMoney) || 0.0 }}
                    </div>
                </div>
            </div>
            <div class="tit-box">
                <div class="tit">
                    <span>{{ $t('最近交易记录') }}</span>
                </div>
                <!-- <div class="more">
                    <router-link :to="{ path: '/capitalfund/account/list' }">
                        <el-button type="primary" link>全部交易记录</el-button>
                    </router-link>
                </div> -->
            </div>
            <div class="lyecs-table-list-warp">
                <el-tabs type="card" @tab-change="onTabChange" v-model="filterParams.orderStatus">
                    <el-tab-pane v-for="item in orderStatusList" :label="item.label" :name="item.value"></el-tab-pane>
                </el-tabs>
                <div class="table-container">
                    <el-table :data="filterState" :loading="loading" :total="total" row-key="logId"
                        v-loading="tableLoading">
                        <el-table-column :label="$t('时间')" prop="addTime" width="160"></el-table-column>
                        <!--                        <el-table-column label="名称" prop="productName" width="160"></el-table-column>-->
                        <el-table-column :label="$t('订单编号')" prop="orderSn" width="160"></el-table-column>
                        <el-table-column :label="`${$t('金额')}(${config.config.dollarSignCn})|${$t('明细')}`">
                            <template #default="{ row }">
                                <p>{{ $t('订单运费：{priceFormat}', { priceFormat: priceFormat(row.shippingFee) }) }}</p>
                                <p>{{ $t('附加费用：{priceFormat}', { priceFormat: priceFormat(row.serviceFee) }) }}</p>
                                <p v-if="row.couponAmount > 0">{{ $t('优惠券抵扣：{priceFormat}', { priceFormat: priceFormat(row.couponAmount) }) }}</p>
                                <p v-if="row.discountAmount > 0">{{ $t('优惠券折扣：{priceFormat}', { priceFormat: priceFormat(row.discountAmount) }) }}</p>
                                <p v-if="row.pointsAmount > 0">{{ $t('积分抵扣：{priceFormat}', { priceFormat: priceFormat(row.pointsAmount) }) }}</p>
                                <p>{{ $t('实收金额：{priceFormat}', { priceFormat: priceFormat(row.paidAmount) }) }}<span v-if="row.balance > 0">{{ $t('(使用余额:{priceFormat})', { priceFormat: priceFormat(row.balance) }) }}</span>
                                </p>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('状态')" prop="orderStatusName" width="120"></el-table-column>
                        <el-table-column :label="$t('操作')" width="80">
                            <template #default="{ row }">
                                <DialogForm :params="{ act: 'detail', id: row.orderId }" :showClose="false"
                                    :showOnOk="false" :title="$t('订单详情 ') + row.orderSn" isDrawer path="order/order/Info"
                                    width="880px" @callback="loadFilter">
                                    <el-button size="small" link type="primary"> {{ $t('查看详情') }} </el-button>
                                </DialogForm>
                            </template>
                        </el-table-column>
                        <template #empty>
                            <div class="empty-warp">
                                <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                            </div>
                        </template>
                    </el-table>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { UserBalanceLogFilterState } from "@/types/finance/userBalanceLog";
import { DialogForm } from "@/components/dialog";
import { QuestionFilled } from "@element-plus/icons-vue";
import { getShopAccount } from "@/api/merchant/capitalfund/dashboard";
import { AccountFormState } from "@/types/merchant/capitalfund/dashboard.d";
import { priceFormat } from "@/utils/format";
import { getOrderList } from "@/api/order/order";
import { OrderFilterParams } from "@/types/order/order.d";
import { getShopType } from "@/utils/storage";


// 基本参数定义
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const orderStatusList = reactive([
    { value: -1, label: t('全部') },
    { value: 0, label: t('待支付') },
    { value: 1, label: t('待发货') },
    { value: 2, label: t('已发货') },
    { value: 3, label: t('已取消') },
    { value: 4, label: t('无效') },
    { value: 5, label: t('已完成') },
    { value: -2, label: t('已删除') },
    { value: 99, label: t('待结算') }
]);
const config: any = useConfigStore();
// 基本参数定义
const filterState = ref(<UserBalanceLogFilterState[]>[]);
const loading = ref<boolean>(true);
const tableLoading = ref<boolean>(true);
const total = ref<number>(0);
const displayAmount = ref<boolean>(true);
const filterParams = reactive<OrderFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    orderStatus: -1
});
const accountData = ref<AccountFormState>({
    shopMoney: 0
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopAccount();
        accountData.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const fetchOrderList = async () => {
    tableLoading.value = true;
    try {
        const result = await getOrderList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        tableLoading.value = false;
    }
};
onMounted(() => {
    loadFilter();
    fetchOrderList();
});
const onTabChange = (e: number) => {
    filterParams.orderStatus = e != 99 ? e : "1,2,5";
    if (e == 99) {
        filterParams.isSettlement = 0;
    }
    fetchOrderList();
};
</script>
<style lang="less" scoped>
.content_wrapper {
    background-color: #fff;
    padding: 20px;

    .tit-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background-color: #f5f6fa;
        padding: 10px;
        margin-bottom: 20px;

        .tit {
            border-left: 3px solid #155bd4;
            padding-left: 10px;
            font-size: 14px;
        }
    }

    .balance-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background-color: #f5f6fa;
        padding: 10px;
        margin-bottom: 20px;
        font-size: 14px;

        .label {
            padding: 28px;

            .title {
                margin-bottom: 26px;
                display: flex;
                align-items: center;

                :deep(.el-icon) {
                    margin-left: 5px;
                    color: #333;
                }
            }

            .num {
                font-size: 24px;
                font-weight: 500;
            }
        }
    }

    :deep(.el-tabs--card) {
        .el-tabs__header .el-tabs__item {
            background-color: #f5f6fa;
        }

        .el-tabs__header .el-tabs__item.is-active {
            background-color: #fff;
            color: #333;
        }

        .el-tabs__header .el-tabs__nav {
            overflow: hidden;
        }
    }
}
</style>

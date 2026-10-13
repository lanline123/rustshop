<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="tit-box">
                <div class="tit">
                    <span>{{ $t('所有{shopTypeName}总余额: 截止时间: [{format}]', { shopTypeName, format: dayjs(new Date()).format("YYYY-MM-DD HH:mm:ss") }) }}</span>
                </div>
            </div>
            <div class="balance-box flex-wrap">
                <div class="label">
                    <div class="title">
                        {{ $t('余额总结余') }}({{ config.config.dollarSignCn }})
                        <el-tooltip placement="bottom" effect="light">
                            <template #content>
                                <div class="tooltip-width">{{ $t('可用{shopTypeName}余额={shopTypeName1}余额-不可用{shopTypeName2}余额', { shopTypeName, shopTypeName1: shopTypeName, shopTypeName2: shopTypeName }) }}</div>
                            </template>
                            <el-icon><QuestionFilled /></el-icon>
                        </el-tooltip>
                        <router-link v-if="getAdminType() !== 'admin'" :to="{ path: '/shop/shop-account-raply/list' }">
                            <el-button type="primary" link>{{ $t('提现申请') }}</el-button>
                        </router-link>
                    </div>
                    <div class="num">
                        {{ priceFormat(accountData.shopMoney) || 0.0 }}
                    </div>
                </div>
                <div class="label">
                    <div class="title">
                        {{ $t('待结算总额') }}({{ config.config.dollarSignCn }})
                        <el-tooltip placement="bottom" effect="light">
                            <template #content>
                                <div class="tooltip-width">{{ $t('交易未完成的订单总额，消费者确认收货后将自动转入{shopTypeName}余额', { shopTypeName }) }}</div>
                            </template>
                            <el-icon><QuestionFilled /></el-icon>
                        </el-tooltip>
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
                                <div class="tooltip-width">{{ $t('提现中或者退款中的冻结{shopTypeName}余额', { shopTypeName }) }}</div>
                            </template>
                            <el-icon><QuestionFilled /></el-icon>
                        </el-tooltip>
                    </div>
                    <div class="num">
                        {{ priceFormat(accountData.frozenMoney) || 0.0 }}
                    </div>
                </div>
            </div>
            <Ranking></Ranking>
            <TransactionRecord></TransactionRecord>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { Pagination } from "@/components/list";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { Image } from "@/components/image";
import { UserBalanceLogFilterState } from "@/types/finance/userBalanceLog";
import { DialogForm } from "@/components/dialog";
import { QuestionFilled } from "@element-plus/icons-vue";
import { getShopAccount } from "@/api/merchant/capitalfund/dashboard";
import { AccountFormState } from "@/types/merchant/capitalfund/dashboard.d";
import { priceFormat } from "@/utils/format";
import { getOrderList } from "@/api/order/order";
import { OrderFilterParams } from "@/types/order/order.d";
import { getShopList } from "@/api/shop/shop";
import Ranking from "./src/Ranking.vue";
import TransactionRecord from "./src/TransactionRecord.vue";
import dayjs from "dayjs";
import { getAdminType, getShopType } from "@/utils/storage";
import { isMerchant, isStore } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopTypeName = isStore() ? t('门店') : isMerchant() ? t('店铺') : t('店铺')
const config: any = useConfigStore();
const loading = ref<boolean>(true);
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
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.content_wrapper {
    background-color: #fff;
    padding: 20px;
    .tit-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 0;
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

/**
 * @file 分销佣金（D1107）
 * @description 切换：分销佣金（佣金订单/业绩明细）+ 提现记录（分销佣金提现申请，wallet_type=1）
 */
<template>
    <div class="container">
        <el-tabs v-model="activeName" class="lyecs-tabs">
            <el-tab-pane :label="$t('分销佣金')" name="commission" />
            <el-tab-pane :label="$t('提现记录')" name="withdraw" />
        </el-tabs>
        <PerformanceList v-if="activeName === 'commission'" />
        <WithdrawList v-if="activeName === 'withdraw'" :wallet-type="1" />
    </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import PerformanceList from "./performanceSettlement/List.vue";
import WithdrawList from "@/views/finance/userWithdrawApply/List.vue";

const route = useRoute();
const activeName = ref<string>(route.query.tab === "withdraw" ? "withdraw" : "commission");
</script>

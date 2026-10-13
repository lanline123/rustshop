<!--
 * @file 支付设置页面
 * @description 支付方式配置页面，支持多种支付渠道的配置：
 *   - 基础设置：支付相关的基础配置
 *   - 微信支付：微信公众号/小程序/APP支付配置
 *   - 支付宝：支付宝支付配置
 *   - 其他支付：PayPal / Stripe / YaBandPay / Antom / Xendit / 汇付天下 等第三方聚合支付配置
 *     （Stripe / YaBandPay / Antom / Xendit 仅跨境版）
 * @module views/setting/config/payment/Payment
-->
<template>
    <div class="container">
        <div class="content_wrapper">
            <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                <el-tab-pane :key="1" :label="$t('基础设置')" name="Base"></el-tab-pane>
                <el-tab-pane :key="2" :label="$t('微信支付')" name="Wechat"></el-tab-pane>
                <el-tab-pane :key="3" :label="$t('支付宝')" name="Alipay"></el-tab-pane>
                <el-tab-pane :key="5" :label="$t('线下支付')" name="Offline"></el-tab-pane>
                <el-tab-pane :key="7" label="Apple Pay" name="ApplePay"></el-tab-pane>
                <el-tab-pane :key="6" label="其他支付" name="Other"></el-tab-pane>
            </el-tabs>
            <Base v-if="activeKey === 'Base'"></Base>
            <Wechat v-if="activeKey === 'Wechat'"></Wechat>
            <Alipay v-if="activeKey === 'Alipay'"></Alipay>
            <Offline v-if="activeKey === 'Offline'"></Offline>
            <ApplePay v-if="activeKey === 'ApplePay'"></ApplePay>
            <template v-if="activeKey === 'Other'">
                <el-radio-group v-model="otherActiveKey" class="other-pay-group">
                    <el-radio-button value="PayPal">PayPal</el-radio-button>
                    <el-radio-button value="Huifu">汇付天下</el-radio-button>
                    <el-radio-button value="Lakala">拉卡拉支付</el-radio-button>
                    <el-radio-button value="Yeepay">易宝支付</el-radio-button>
                </el-radio-group>
                <PayPal v-if="otherActiveKey === 'PayPal'"></PayPal>
                <Huifu v-if="otherActiveKey === 'Huifu'"></Huifu>
                <Lakala v-if="otherActiveKey === 'Lakala'"></Lakala>
                <Yeepay v-if="otherActiveKey === 'Yeepay'"></Yeepay>
            </template>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { ref, watch } from "vue";
import { useRoute } from "vue-router";
import { isOverseas } from "@/utils/version";
import { Base, Wechat, Alipay, Offline, PayPal } from "./src/index";
import { Huifu } from "./src/index";
import { Lakala } from "./src/index";
import { Yeepay } from "./src/index";
import { ApplePay } from "./src/index";

const route = useRoute();
const mainTabs = [
    "Base",
    "Wechat",
    "Alipay",
    "Offline",
    "ApplePay",
    "Other"
];
const otherTabs = [
    "PayPal",
    "Huifu",
    "Lakala",
    "Yeepay"
];

/** 当前激活的支付配置Tab */
const activeKey = ref("Base");
/** 其他支付下当前激活的子Tab */
const otherActiveKey = ref("PayPal");

const getActiveTab = () => {
    const tab = typeof route.query.tab === "string" ? route.query.tab : "Base";
    if (otherTabs.includes(tab)) {
        const overseasOnly = ["Stripe", "Yabanpay", "Antom", "Xendit"];
        if (overseasOnly.includes(tab) && !isOverseas()) {
            otherActiveKey.value = "PayPal";
            return "Other";
        }
        otherActiveKey.value = tab;
        return "Other";
    }
    return mainTabs.includes(tab) ? tab : "Base";
};

watch(
    () => route.query.tab,
    () => {
        activeKey.value = getActiveTab();
    },
    {
        immediate: true
    }
);

/**
 * Tab切换处理
 * @param {string} val - 新的Tab值
 */
const onTabChange = (val: string) => {
    activeKey.value = val;
};
</script>
<style lang="less" scoped>
.content_wrapper {
    min-height: calc(100vh - 260px);
}

.other-pay-group {
    margin-bottom: 20px;
}
</style>

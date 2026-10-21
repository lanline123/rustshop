/**
 * @file 店铺端 保证金退还申请弹窗
 * @description 顶部三卡总览（公式形态，数据取自保证金总览）+ 全部/部分退还 + 全额退还条件提示 + 可退额填入
 *              对齐退还申请接口 DepositRefundApplyDTO {type, amount, reason}
 */
<template>
    <a-spin class="refund-form" :spinning="loading">
        <OverviewCards :overview="overview" />
        <el-form ref="formRef" :model="formState" label-width="100px" label-suffix="：">
            <el-form-item :label="$t('退还类型')" prop="type" :rules="[{ required: true, message: '请选择退还类型' }]">
                <div class="type-content">
                    <el-radio-group v-model="formState.type">
                        <el-radio :value="RefundType.Full" :disabled="!canFullRefund">{{ $t('全部退还') }}</el-radio>
                        <el-radio :value="RefundType.Partial">{{ $t('部分退还') }}</el-radio>
                    </el-radio-group>
                    <div class="full-refund-tip">
                        <div>{{ $t('申请全额退还{shopLabel}保证金，需同时满足以下两个条件：', { shopLabel }) }}</div>
                        <div>{{ $t('1、{shopLabel}已停业，无正在进行中的订单；', { shopLabel }) }}</div>
                        <div>{{ $t('2、无未完结售后，且{shopLabel}最后一笔订单买家确认收货满{afterSalesLimitDays}天，方可申请。', { shopLabel, afterSalesLimitDays }) }}</div>
                    </div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('退还金额')" prop="amount" :rules="amountRules">
                <div class="amount-block">
                    <TigInput
                        v-model="formState.amount"
                        type="decimal"
                        width="100%"
                        :min="0.01"
                        :decimal-places="2"
                        :disabled="isFullRefund"
                        :placeholder="$t('请输入退还金额')"
                    />
                    <div v-if="!isFullRefund" class="extra">{{ $t('当前保证金可退还金额：{priceFormat}', { priceFormat: priceFormat(refundableAmount) }) }}<el-button type="primary" link class="fill-btn" @click="fillRefundable">{{ $t('填入') }}</el-button>
                    </div>
                </div>
            </el-form-item>

            <el-form-item :label="$t('备注')" prop="reason">
                <el-input v-model="formState.reason" type="textarea" :rows="4" :maxlength="200" :placeholder="$t('请输入')" />
            </el-form-item>
        </el-form>
    </a-spin>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef, watch } from "vue";
import { message } from "ant-design-vue";
import { accSub, priceFormat } from "@/utils/format";
import { getShopDepositFullRefundCheck, getShopDepositOverview, submitShopDepositRefund } from "@/api/merchant/capitalfund/deposit";
import { getShopTypeName } from "@/utils/storage";
import { RefundType } from "@/types/finance/deposit.d";
import type { DepositFullRefundCheck, ShopDepositOverview } from "@/types/merchant/capitalfund/deposit.d";
import OverviewCards from "./OverviewCards.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const shopLabel = getShopTypeName();

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
defineProps({ isDialog: Boolean });

const loading = ref<boolean>(true);
const formRef = shallowRef();
const overview = ref<ShopDepositOverview>({ required: 0, paid: 0, pending: 0, withdrawable: 0, pendingWithdraw: 0, frozen: 0 });
const fullRefundCheck = ref<DepositFullRefundCheck>({
    canRefund: false,
    refundableAmount: 0,
    afterSalesLimitDays: 0,
    unmetReasons: []
});

const formState = ref({
    type: RefundType.Partial as number,
    amount: undefined as number | string | undefined,
    reason: ""
});

const isFullRefund = computed(() => formState.value.type === RefundType.Full);

// 部分退还可退额 = max(0, 可提现 - 待审核提现金额)；全额退还取预检接口 refundableAmount
const partialRefundableAmount = computed(() => {
    const withdrawable = Number(overview.value.withdrawable || 0);
    const pendingWithdraw = Number(overview.value.pendingWithdraw || 0);
    return Math.max(0, Number(accSub(withdrawable, pendingWithdraw)));
});
const fullRefundableAmount = computed(() => Number(fullRefundCheck.value.refundableAmount || 0));
const refundableAmount = computed(() => (isFullRefund.value ? fullRefundableAmount.value : partialRefundableAmount.value));
const canFullRefund = computed(() => fullRefundCheck.value.canRefund);
const afterSalesLimitDays = computed(() => fullRefundCheck.value.afterSalesLimitDays ?? 0);

// 保留两位小数（用于填入/默认值）
const refundableAmountText = computed(() => refundableAmount.value.toFixed(2));

// 全部退还：金额默认填满可退额且不可修改；部分退还：清空交由手动输入
watch(
    () => formState.value.type,
    type => {
        formState.value.amount = type === RefundType.Full ? refundableAmountText.value : undefined;
    }
);

const amountRules = [
    { required: true, message: t('请输入退还金额') },
    {
        validator: (rule: any, value: any, callback: any) => {
            const amount = Number(value);
            if (!value || amount <= 0) return callback(new Error(t('退还金额必须大于 0')));
            if (amount > refundableAmount.value) return callback(new Error(t('退还金额不能超过可退额')));
            callback();
        }
    }
];

const fillRefundable = () => {
    formState.value.amount = refundableAmountText.value;
};

const loadInfo = async () => {
    try {
        const [overviewResult, checkResult] = await Promise.all([getShopDepositOverview(), getShopDepositFullRefundCheck()]);
        overview.value = overviewResult;
        fullRefundCheck.value = checkResult;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const onSubmit = async () => {
    try {
        await formRef.value.validate();
    } catch {
        return;
    }
    if (isFullRefund.value && !canFullRefund.value) {
        message.error(t('当前不满足全额退还条件'));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await submitShopDepositRefund({
            type: formState.value.type,
            amount: Number(formState.value.amount),
            reason: formState.value.reason || undefined
        });
        message.success(t('提交成功，等待平台审核'));
        emit("submitCallback", result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });

onMounted(() => {
    loadInfo();
});
</script>

<style lang="less" scoped>
.refund-form {
    padding: 0 6px;
    :deep(.el-form-item__label) {
        white-space: nowrap;
    }
}
.type-content {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
}
.full-refund-tip {
    color: #999;
    font-size: 12px;
    line-height: 1.8;
    margin-top: 8px;
}
.amount-block {
    width: 100%;
}
.extra {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
    line-height: 1.5;
    .fill-btn {
        font-size: inherit;
        height: auto;
        padding: 0;
        vertical-align: baseline;
    }
}
</style>

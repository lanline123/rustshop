/**
 * @file 确认收货表单
 * @description 售后退货场景下，商家确认已收到用户退回的商品
 * @features
 * - 顶部风险提示：确认收货后自动进入退款流程
 * - 展示本次退款明细金额
 * - 确认后调用确认收货接口并回调刷新详情
 */
<template>
    <div class="confirm-receive-form">
        <el-alert
            class="mb-4"
            :closable="false"
            show-icon
            type="info"
            :title="$t('操作前请确保已经收到用户退货，确认收货后将自动进入退款流程，并退返至买家账户。')"
        />
        <div class="refund-detail">
            <span class="refund-detail__label">{{ adminType === "vendor" ? $t('退货供货金额') : $t('退款明细') }}</span>
            <span class="refund-detail__value">{{ refundAmountDisplay }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, PropType } from "vue";
import { message } from "ant-design-vue";
import { FormState } from "@/types/order/aftersales";
import { updataConfirmReceipt } from "@/api/order/aftersales";
import { priceFormat } from "@/utils/format";
import { getSupplierAftersalesAmount } from "@/utils/orderSupplierPrice";
import { useI18n } from "vue-i18n";

const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const { t } = useI18n();
const adminType = localStorage.getItem("adminType");

const props = defineProps({
    formData: {
        type: Object as PropType<FormState>,
        default: () => ({})
    }
});

const refundAmountDisplay = computed(() => {
    if (adminType === "vendor") {
        return priceFormat(getSupplierAftersalesAmount(props.formData?.aftersalesItems));
    }
    const amount = props.formData?.actualRefundAmount ?? props.formData?.refundAmount ?? 0;
    return t("现金 {amount}", { amount: priceFormat(Number(amount)) });
});

const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        await updataConfirmReceipt({ id: props.formData?.aftersaleId });
        message.success(t("操作成功"));
        emit("submitCallback");
    } catch (error: any) {
        if (error?.message) {
            message.error(error.message);
        }
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.confirm-receive-form {
    padding: 4px 0 8px;
}
.mb-4 {
    margin-bottom: 20px;
}
.refund-detail {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 4px 0;
    font-size: 14px;

    &__label {
        color: #86909c;
    }
    &__value {
        color: #333;
        font-weight: 500;
    }
}
</style>

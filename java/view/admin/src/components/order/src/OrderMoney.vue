/**
* @file 订单金额明细组件
* @description 显示订单的各项金额明细，包括商品金额、运费、优惠、实收等
* @features
* - 商品总金额、运费、附加费用
* - 各类优惠抵扣（优惠券、折扣、积分、抹零）
* - 支付明细（线上/线下/余额）
* - 售后退款金额
*/
<template>
    <div class="div-main">
        <div class="text-right flex" style="justify-content: flex-end">
            <div> {{ $t('商品总金额：') }}<span class="money-style">{{ priceFormat(modelValue.productAmount) }}</span>
            </div>
            <div>{{ $t('+ 订单{getOrderShippingFeeLabel}：', { getOrderShippingFeeLabel: getOrderShippingFeeLabel(modelValue) }) }}<span class="money-style">{{
                    priceFormat(modelValue.shippingFee) }}</span>
            </div>
            <div> {{ $t('+ 附加费用：') }}<span class="money-style">{{ priceFormat(modelValue.serviceFee) }}</span>
            </div>
            <div v-if="isOverseas()"> {{ $t('+ 订单税费：') }}<span class="money-style">{{ priceFormat(getOrderTaxFee(modelValue)) }}</span>
            </div>
            <div v-if="isStore()"> {{ $t('- 抹零优惠：') }}<span class="money-style">{{ priceFormat(modelValue.roundOffCentsAmount +
                    modelValue.roundOffDimeAmount) }}</span>
            </div>
            <div v-if="modelValue.couponAmount > 0"> {{ $t('- 优惠券抵扣：') }}<span class="money-style">{{ priceFormat(modelValue.couponAmount) }}</span>
            </div>
            <div v-if="modelValue.discountAmount > 0"> {{ $t('- 优惠折扣：') }}<span class="money-style">{{ priceFormat(modelValue.discountAmount) }}</span>
            </div>
            <div v-if="modelValue.pointsAmount > 0"> {{ $t('- 积分抵扣：') }}<span class="money-style">{{ priceFormat(modelValue.pointsAmount) }}</span>
            </div>
            <div> {{ $t('= 订单总金额：') }}<span class="money-style">{{ priceFormat(modelValue.totalAmount) }}</span>
            </div>

        </div>
        <div class="text-right">
            <div v-if="showOnlinePaidAmount"> {{ $t('线上支付：') }}<span class=" money-style">{{ priceFormat(modelValue.onlinePaidAmount) }}</span>
            </div>
            <div v-if="showOfflinePaidAmount"> {{ $t('线下支付：') }}<span class="money-style">{{ priceFormat(modelValue.offlinePaidAmount) }}</span>
            </div>
        </div>
        <div class="text-right"> {{ $t('实收金额：') }}<span class="money-style">{{ priceFormat(Number(modelValue.paidAmount)) }}</span>
            <span class="money-style2" v-if="modelValue.balance > 0">{{ $t('(使用余额:{priceFormat})', { priceFormat: priceFormat(modelValue.balance) }) }}</span>
        </div>
        <div class="text-right" v-if="modelValue.refundAmount > 0"> {{ $t('售后协商金额：') }}<span class="money-style">{{ priceFormat(modelValue.refundAmount) }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import type { OrderFormState } from "@/types/order/order";
import { priceFormat } from "@/utils/format";
import { isStore, isOverseas } from "@/utils/version";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";

const props = defineProps({
    /** 订单金额数据对象 */
    modelValue: {
        type: Object,
        default: {
            productAmount: 0,      // 商品金额
            couponAmount: 0,       // 优惠券抵扣
            totalAmount: 0,        // 订单总金额
            paidAmount: 0,         // 已支付金额
            unpaidAmount: 0,       // 未支付金额
            discountAmount: 0,     // 折扣金额
            pointsAmount: 0,       // 积分抵扣
            balance: 0,            // 余额支付
            onlinePaidAmount: 0,   // 线上支付
            offlinePaidAmount: 0   // 线下支付
        }
    }
});

// 周期购子订单（cycleDeliveryDate 有值）不展示支付明细
const isCycleChildOrder = computed(() => {
    const order = props.modelValue as {
        cycleDeliveryDate?: string;
    };
    return order?.cycleDeliveryDate && order.cycleDeliveryDate.trim().length > 0;
});

// 线上支付：金额大于 0 且非周期购子订单时展示
const showOnlinePaidAmount = computed(() => {
    const order = props.modelValue as {
        onlinePaidAmount?: number | string;
    };
    return Number(order?.onlinePaidAmount) > 0 && !isCycleChildOrder.value;
});

// 线下支付：金额大于 0 且非周期购子订单时展示
const showOfflinePaidAmount = computed(() => {
    const order = props.modelValue as {
        offlinePaidAmount?: number | string;
    };
    return Number(order?.offlinePaidAmount) > 0 && !isCycleChildOrder.value;
});

// TODO: 后端税费字段名待统一，统一后应只保留标准字段
const getOrderTaxFee = (data: Partial<OrderFormState> | Record<string, any>) => {
    return Number(data?.taxRateAmount ?? data?.taxFee ?? data?.taxAmount ?? data?.orderTaxFee ?? 0);
};
</script>
<style lang="less" scoped>
.div-main {
    padding: 20px;
    background-color: #f5f8ff;
    text-align: right;

    .text-right {
        width: 100%;
        line-height: 26px;
        text-align: right;
        font-size: 12px;
    }

    .text-right>div {
        padding-left: 5px;
    }

    .money-style {
        font-size: 14px;
        color: #f64747;
        font-weight: bold;
    }

    .money-style2 {
        margin-left: 10px;
    }
}
</style>

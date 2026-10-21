<template>
    <view class="payment-channel-list">
        <view v-if="title" class="payment-channel-list-title">{{ $t(title) }}</view>
        <view class="channel-list">
            <view v-for="channel in displayPaymentList" :key="channel" class="channel-item" @click="selectChannel(channel)">
                <view class="channel-info">
                    <image lazy-load class="channel-icon" :src="staticResource('payment/pay_' + channel + '.png')" />
                    <view class="channel-name">{{ $t(PAYMENT_TEXT_MAP[channel] || channel) }}</view>
                </view>
                <view class="radio-circle" :class="{ checked: modelValue === channel }" />
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * @description 通用支付渠道单选组件
 * - 展示在线支付渠道（微信/支付宝/applepay 等）图标、名称与单选态
 * - 结算页与提交订单二次确认收银台共用
 */
import { PAYMENT_TEXT_MAP } from "@/hooks/usePayment";
import { isOverseas, staticResource } from "@/utils";
import { computed, watch } from "vue";

interface Props {
    /** 当前选中的渠道 code */
    modelValue: string;
    /** 渠道 code 列表 */
    paymentList: string[];
    /** 标题，传空字符串则不展示 */
    title?: string;
}
const props = withDefaults(defineProps<Props>(), {
    title: "支付方式"
});
const emit = defineEmits(["update:modelValue"]);

// 跨境渠道仅在跨境运行环境展示；构建标记用于普通版移除相关代码。
const displayPaymentList = computed(() =>
    props.paymentList.filter((channel) => {
        return true;
    })
);

watch(
    displayPaymentList,
    (list) => {
        if (list.length && !list.includes(props.modelValue)) {
            emit("update:modelValue", list[0]);
        }
    },
    { immediate: true }
);

const selectChannel = (channel: string) => {
    if (channel === props.modelValue) return;
    emit("update:modelValue", channel);
};
</script>

<style lang="scss" scoped>
.payment-channel-list {
    border-radius: 18rpx;
    background: #fff;
    margin-bottom: 20rpx;
    margin-top: 20rpx;
    padding: 10rpx 32rpx 10rpx;

    .payment-channel-list-title {
        font-weight: bold;
    }
}

.channel-list {
    .channel-item {
        height: 100rpx;
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 1rpx solid #eee;

        &:last-child {
            border-bottom: 0;
        }
    }

    .channel-info {
        display: flex;
        align-items: center;

        .channel-icon {
            width: 56rpx;
            height: 56rpx;
        }

        .channel-name {
            padding-left: 15rpx;
            font-size: 28rpx;
        }
    }
}

.radio-circle {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 2rpx solid #ccc;
    box-sizing: border-box;
    position: relative;
    flex-shrink: 0;

    &.checked {
        border-color: var(--general);
        background-color: var(--general);

        &::after {
            content: "";
            position: absolute;
            left: 50%;
            top: 50%;
            width: 16rpx;
            height: 9rpx;
            border-left: 3rpx solid #fff;
            border-bottom: 3rpx solid #fff;
            transform: translate(-50%, -65%) rotate(-45deg);
        }
    }
}
</style>
